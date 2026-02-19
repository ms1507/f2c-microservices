package com.rural.marketplace.catalog.controller;

import com.rural.marketplace.common.dto.ProductDTO;
import com.rural.marketplace.catalog.repository.ProductSpecifications;
import com.rural.marketplace.catalog.service.FileStorageService;
import com.rural.marketplace.catalog.service.ProductService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rural.marketplace.common.context.UserContextHolder;

import java.io.IOException;
import java.math.BigDecimal;

@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;
    private final FileStorageService fileStorageService;
    private final ObjectMapper objectMapper;

    /**
     * Create product with JSON payload (no image upload)
     * Automatically sets farmerId from logged-in user if not provided
     */
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ProductDTO> createProduct(
            @Valid @RequestBody ProductDTO productDTO,
            HttpServletRequest request) {

        // Auto-populate farmerId from logged-in user if not explicitly set
        if (productDTO.getFarmerId() == null) {
            Long userId = UserContextHolder.getCurrentUserId(request);
            productDTO.setFarmerId(userId);
        }

        return new ResponseEntity<>(productService.createProduct(productDTO, null), HttpStatus.CREATED);
    }

    /**
     * Create product with multipart form data (supports image upload)
     * Automatically sets farmerId from logged-in user if not provided
     */
    @PostMapping(value = "/with-image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ProductDTO> createProductWithImage(
            @RequestPart("product") String productDTOStr,
            @RequestPart(value = "image", required = false) MultipartFile image,
            HttpServletRequest request) throws IOException {

        ProductDTO productDTO = objectMapper.readValue(productDTOStr, ProductDTO.class);

        // Auto-populate farmerId from logged-in user if not explicitly set
        if (productDTO.getFarmerId() == null) {
            Long userId = UserContextHolder.getCurrentUserId(request);
            productDTO.setFarmerId(userId);
        }

        return new ResponseEntity<>(productService.createProduct(productDTO, image), HttpStatus.CREATED);
    }

    /**
     * Update product with JSON payload (no image upload)
     */
    @PutMapping(value = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ProductDTO> updateProduct(
            @PathVariable Long id,
            @Valid @RequestBody ProductDTO productDTO) {
        return ResponseEntity.ok(productService.updateProduct(id, productDTO, null));
    }

    /**
     * Update product with multipart form data (supports image upload)
     */
    @PutMapping(value = "/{id}/with-image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ProductDTO> updateProductWithImage(
            @PathVariable Long id,
            @RequestPart("product") String productDTOStr,
            @RequestPart(value = "image", required = false) MultipartFile image) throws IOException {

        ProductDTO productDTO = objectMapper.readValue(productDTOStr, ProductDTO.class);
        return ResponseEntity.ok(productService.updateProduct(id, productDTO, image));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductDTO> getProduct(@PathVariable Long id) {
        return ResponseEntity.ok(productService.getProduct(id));
    }

    @GetMapping
    public ResponseEntity<Page<ProductDTO>> getAllProducts(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(required = false) String location,
            Pageable pageable) {

        Specification<com.rural.marketplace.catalog.entity.Product> spec = Specification
                .where(ProductSpecifications.withName(name))
                .and(ProductSpecifications.withCategory(categoryId))
                .and(ProductSpecifications.withPriceRange(minPrice, maxPrice))
                .and(ProductSpecifications.withLocation(location));

        return ResponseEntity.ok(productService.getAllProducts(spec, pageable));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
        productService.deleteProduct(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/images/{fileName:.+}")
    public ResponseEntity<Resource> downloadFile(@PathVariable String fileName, HttpServletRequest request) {
        Resource resource = fileStorageService.loadFileAsResource(fileName);

        String contentType = null;
        try {
            contentType = request.getServletContext().getMimeType(resource.getFile().getAbsolutePath());
        } catch (IOException ex) {
            // logger.info("Could not determine file type.");
        }

        if (contentType == null) {
            contentType = "application/octet-stream";
        }

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + resource.getFilename() + "\"")
                .body(resource);
    }
}
