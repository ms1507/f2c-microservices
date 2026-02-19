package com.rural.marketplace.order.client;

import com.rural.marketplace.common.dto.ProductDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "catalog-service")
public interface ProductClient {

    @GetMapping("/api/v1/products/{id}")
    ResponseEntity<ProductDTO> getProductById(@PathVariable("id") Long id);
}
