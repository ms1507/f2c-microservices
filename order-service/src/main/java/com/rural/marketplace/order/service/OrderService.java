package com.rural.marketplace.order.service;

import com.rural.marketplace.common.dto.ProductDTO;
import com.rural.marketplace.common.event.OrderPlacedEvent;
import com.rural.marketplace.common.exception.ServiceUnavailableException;
import com.rural.marketplace.order.client.ProductClient;
import com.rural.marketplace.order.config.KafkaTopicConfig;
import com.rural.marketplace.order.dto.OrderItemRequest;
import com.rural.marketplace.order.dto.OrderRequest;
import com.rural.marketplace.order.entity.Order;
import com.rural.marketplace.order.entity.OrderItem;
import com.rural.marketplace.order.repository.OrderRepository;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {

    private final OrderRepository orderRepository;
    private final ProductClient productClient;
    private final KafkaTemplate<String, OrderPlacedEvent> kafkaTemplate;

    @Transactional
    @CircuitBreaker(name = "catalogService", fallbackMethod = "catalogServiceFallback")
    @Retry(name = "catalogService")
    public Order placeOrder(OrderRequest request, String userIdStr) {
        log.info("Placing order for User: {}", userIdStr);
        Long userId = 1L; // Mocking numeric ID for now, should come from header

        Order order = new Order();
        order.setUserId(userId);
        order.setOrderDate(LocalDateTime.now());
        order.setStatus("CREATED");
        order.setShippingAddress(request.getShippingAddress());

        List<OrderItem> orderItems = new ArrayList<>();
        BigDecimal totalAmount = BigDecimal.ZERO;

        for (OrderItemRequest itemRequest : request.getItems()) {
            // 1. Fetch Product from Catalog Service via Feign
            log.info("Fetching product details for ID: {}", itemRequest.getProductId());
            ResponseEntity<ProductDTO> response = productClient.getProductById(itemRequest.getProductId());

            if (response.getBody() == null) {
                throw new RuntimeException("Product not found: " + itemRequest.getProductId());
            }
            ProductDTO product = response.getBody();

            // 2. Calculate Item Price
            BigDecimal itemTotal = product.getPrice().multiply(BigDecimal.valueOf(itemRequest.getQuantity()));
            totalAmount = totalAmount.add(itemTotal);

            // 3. Create Order Item
            OrderItem orderItem = OrderItem.builder()
                    .productId(product.getId())
                    .productName(product.getName())
                    .quantity(itemRequest.getQuantity())
                    .price(product.getPrice())
                    .order(order)
                    .build();

            orderItems.add(orderItem);
        }

        order.setItems(orderItems);
        order.setTotalAmount(totalAmount);

        log.info("Saving order with Total: {}", totalAmount);
        Order savedOrder = orderRepository.save(order);

        // 4. Publish Order Placed Event to Kafka
        publishOrderPlacedEvent(savedOrder, userIdStr);

        return savedOrder;
    }

    private void publishOrderPlacedEvent(Order order, String username) {
        try {
            OrderPlacedEvent event = OrderPlacedEvent.builder()
                    .orderId(order.getId())
                    .userId(order.getUserId())
                    .customerName(username)
                    .customerEmail(username + "@example.com") // Mock email
                    .totalAmount(order.getTotalAmount())
                    .orderDate(order.getOrderDate())
                    .build();

            log.info("Publishing OrderPlacedEvent for Order ID: {}", order.getId());
            kafkaTemplate.send(KafkaTopicConfig.ORDER_PLACED_TOPIC, event);
        } catch (Exception e) {
            log.error("Failed to publish OrderPlacedEvent: {}", e.getMessage());
        }
    }

    public Order catalogServiceFallback(OrderRequest request, String userIdStr, Exception e) {
        log.error("Fallback triggered for placeOrder. Reason: {}", e.getMessage());

        List<ProductDTO> recommendations = getRecommendations();

        throw new ServiceUnavailableException(
                "Catalog Service is currently unavailable. We couldn't fetch product details for your order. " +
                        "Please try again later or check out these recommended products.",
                recommendations);
    }

    private List<ProductDTO> getRecommendations() {
        // Mocking recommendations. In a real scenario, this could come from a cache or
        // a recommendation service.
        return List.of(
                ProductDTO.builder()
                        .id(101L)
                        .name("Organic Fertilizer")
                        .price(BigDecimal.valueOf(25.50))
                        .description("High-quality organic fertilizer for better yield.")
                        .build(),
                ProductDTO.builder()
                        .id(102L)
                        .name("Premium Seeds")
                        .price(BigDecimal.valueOf(15.00))
                        .description("Hybrid seeds for robust crops.")
                        .build());
    }
}
