package com.rural.marketplace.catalog.service;

import com.rural.marketplace.common.dto.ProductDTO;
import com.rural.marketplace.catalog.entity.Product;
import com.rural.marketplace.catalog.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import com.rural.marketplace.common.exception.ResourceNotFoundException;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;

/**
 * Service class for managing products in the catalog.
 * Implements caching using Spring Cache to optimize search and retrieval
 * performance.
 */
@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final FileStorageService fileStorageService;

    /**
     * Creates a new product and invalidates the 'products' list cache.
     */
    @Transactional
    @CacheEvict(value = "products", allEntries = true)
    public ProductDTO createProduct(ProductDTO productDTO, MultipartFile image) {
        Product product = new Product();
        mapToEntity(productDTO, product);

        if (image != null && !image.isEmpty()) {
            String fileName = fileStorageService.storeFile(image);
            String fileDownloadUri = ServletUriComponentsBuilder.fromCurrentContextPath()
                    .path("/api/products/images/")
                    .path(fileName)
                    .toUriString();
            product.setImageUrl(fileDownloadUri);
        }

        Product savedProduct = productRepository.save(product);
        return mapToDTO(savedProduct);
    }

    /**
     * Updates an existing product.
     * Evicts the specific product cache and invalidates the general 'products' list
     * cache.
     */
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "product", key = "#id"),
            @CacheEvict(value = "products", allEntries = true)
    })
    public ProductDTO updateProduct(Long id, ProductDTO productDTO, MultipartFile image) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Product not found with id: " + id));

        mapToEntity(productDTO, product);

        if (image != null && !image.isEmpty()) {
            String fileName = fileStorageService.storeFile(image);
            String fileDownloadUri = ServletUriComponentsBuilder.fromCurrentContextPath()
                    .path("/api/products/images/")
                    .path(fileName)
                    .toUriString();
            product.setImageUrl(fileDownloadUri);
        }

        Product updatedProduct = productRepository.save(product);
        return mapToDTO(updatedProduct);
    }

    /**
     * Retrieves a single product by ID.
     * Caches the result to improve performance on subsequent lookups.
     */
    @Transactional(readOnly = true)
    @Cacheable(value = "product", key = "#id")
    public ProductDTO getProduct(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Product not found with id: " + id));
        return mapToDTO(product);
    }

    @Transactional(readOnly = true)
    public Page<ProductDTO> getAllProducts(Specification<Product> spec, Pageable pageable) {
        return productRepository.findAll(spec, pageable).map(this::mapToDTO);
    }

    /**
     * Deletes a product and removes it from both the specific and general list
     * caches.
     */
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "product", key = "#id"),
            @CacheEvict(value = "products", allEntries = true)
    })
    public void deleteProduct(Long id) {
        if (!productRepository.existsById(id)) {
            throw new ResourceNotFoundException(
                    "Product not found with id: " + id);
        }
        productRepository.deleteById(id);
    }

    private void mapToEntity(ProductDTO dto, Product product) {
        product.setName(dto.getName());
        product.setDescription(dto.getDescription());
        product.setPrice(dto.getPrice());
        product.setStockQuantity(dto.getStockQuantity());
        product.setCategoryId(dto.getCategoryId());
        product.setFarmerId(dto.getFarmerId());
        product.setLocation(dto.getLocation());
        // Image handled separately
    }

    private ProductDTO mapToDTO(Product product) {
        ProductDTO dto = new ProductDTO();
        dto.setId(product.getId());
        dto.setName(product.getName());
        dto.setDescription(product.getDescription());
        dto.setPrice(product.getPrice());
        dto.setStockQuantity(product.getStockQuantity());
        dto.setCategoryId(product.getCategoryId());
        dto.setFarmerId(product.getFarmerId());
        dto.setImageUrl(product.getImageUrl());
        dto.setLocation(product.getLocation());
        return dto;
    }
}
