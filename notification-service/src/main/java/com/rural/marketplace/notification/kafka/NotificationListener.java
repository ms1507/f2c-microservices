package com.rural.marketplace.notification.kafka;

import com.rural.marketplace.common.event.OrderPlacedEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class NotificationListener {

    @KafkaListener(topics = "order-placed-topic", groupId = "notification-group")
    public void handleOrderPlacedEvent(OrderPlacedEvent event) {
        log.info("****************************************************************");
        log.info("RECEIVED ORDER PLACED EVENT VIA KAFKA");
        log.info("Order ID: {}", event.getOrderId());
        log.info("Customer: {}", event.getCustomerName());
        log.info("Email: {}", event.getCustomerEmail());
        log.info("Total Amount: {}", event.getTotalAmount());
        log.info("****************************************************************");

        // Simulation: Send Email
        sendEmailSimulation(event);
    }

    private void sendEmailSimulation(OrderPlacedEvent event) {
        log.info("SImulating Email to {}: 'Dear {}, your order #{} has been placed successfully!'",
                event.getCustomerEmail(), event.getCustomerName(), event.getOrderId());
    }
}
