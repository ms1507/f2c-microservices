package com.rural.marketplace.order.controller;

import com.rural.marketplace.order.dto.OrderRequest;
import com.rural.marketplace.order.entity.Order;
import com.rural.marketplace.order.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
@Slf4j
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    public ResponseEntity<Order> placeOrder(@RequestBody OrderRequest request,
            @RequestHeader(value = "X-Logged-In-User", required = false) String loggedInUser) {
        // Fallback for testing if header is missing (though Gateway should provide it)
        if (loggedInUser == null) {
            log.warn("X-Logged-In-User header missing, defaulting to test user ID 1");
            loggedInUser = "1";
        }

        return new ResponseEntity<>(orderService.placeOrder(request, loggedInUser), HttpStatus.CREATED);
    }
}
