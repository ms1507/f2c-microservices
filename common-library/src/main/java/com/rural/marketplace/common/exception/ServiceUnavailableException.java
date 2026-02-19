package com.rural.marketplace.common.exception;

import com.rural.marketplace.common.dto.ProductDTO;
import lombok.Getter;

import java.util.List;

@Getter
public class ServiceUnavailableException extends RuntimeException {
    private final List<ProductDTO> recommendations;

    public ServiceUnavailableException(String message) {
        super(message);
        this.recommendations = null;
    }

    public ServiceUnavailableException(String message, List<ProductDTO> recommendations) {
        super(message);
        this.recommendations = recommendations;
    }
}
