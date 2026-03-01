package com.ecommerce.productservice.service;

import com.ecommerce.productservice.dto.*;
import com.ecommerce.productservice.exception.InsufficientStockException;
import com.ecommerce.productservice.exception.ProductAlreadyExistsException;
import com.ecommerce.productservice.exception.ProductNotFoundException;
import com.ecommerce.productservice.model.Category;
import com.ecommerce.productservice.model.Product;
import com.ecommerce.productservice.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryService categoryService;

    @Transactional(readOnly = true)
    public Page<ProductDto> getAllProducts(Pageable pageable) {
        return productRepository.findAllByActiveTrue(pageable).map(this::toDto);
    }

    @Transactional(readOnly = true)
    public Page<ProductDto> getProductsByCategory(Long categoryId, Pageable pageable) {
        return productRepository.findAllByCategoryIdAndActiveTrue(categoryId, pageable).map(this::toDto);
    }

    @Transactional(readOnly = true)
    public Page<ProductDto> searchProducts(String keyword, Pageable pageable) {
        return productRepository.searchByKeyword(keyword, pageable).map(this::toDto);
    }

    @Transactional(readOnly = true)
    public ProductDto getProductById(Long id) {
        return toDto(findOrThrow(id));
    }

    @Transactional(readOnly = true)
    public ProductDto getProductBySku(String sku) {
        return toDto(productRepository.findBySku(sku)
                .orElseThrow(() -> new ProductNotFoundException("Product not found with SKU: " + sku)));
    }

    @Transactional
    public ProductDto createProduct(CreateProductRequest request) {
        if (productRepository.existsBySku(request.getSku())) {
            throw new ProductAlreadyExistsException("Product with SKU '" + request.getSku() + "' already exists");
        }
        Category category = categoryService.findOrThrow(request.getCategoryId());
        Product product = Product.builder()
                .name(request.getName())
                .description(request.getDescription())
                .sku(request.getSku())
                .price(request.getPrice())
                .stockQuantity(request.getStockQuantity())
                .imageUrl(request.getImageUrl())
                .category(category)
                .active(true)
                .build();
        Product saved = productRepository.save(product);
        log.info("Created product: {} (SKU: {})", saved.getName(), saved.getSku());
        return toDto(saved);
    }

    @Transactional
    public ProductDto updateProduct(Long id, UpdateProductRequest request) {
        Product product = findOrThrow(id);
        if (request.getName() != null) product.setName(request.getName());
        if (request.getDescription() != null) product.setDescription(request.getDescription());
        if (request.getPrice() != null) product.setPrice(request.getPrice());
        if (request.getStockQuantity() != null) product.setStockQuantity(request.getStockQuantity());
        if (request.getImageUrl() != null) product.setImageUrl(request.getImageUrl());
        if (request.getCategoryId() != null) {
            product.setCategory(categoryService.findOrThrow(request.getCategoryId()));
        }
        if (request.getActive() != null) product.setActive(request.getActive());
        return toDto(productRepository.save(product));
    }

    @Transactional
    public ProductDto updateInventory(Long id, InventoryUpdateRequest request) {
        Product product = findOrThrow(id);
        int newQty = switch (request.getMode()) {
            case ABSOLUTE -> request.getQuantity();
            case DELTA -> product.getStockQuantity() + request.getQuantity();
        };
        if (newQty < 0) {
            throw new InsufficientStockException(
                "Insufficient stock for product id " + id + ". Current: " + product.getStockQuantity());
        }
        product.setStockQuantity(newQty);
        log.info("Updated inventory for product {} to {}", id, newQty);
        return toDto(productRepository.save(product));
    }

    @Transactional
    public void deleteProduct(Long id) {
        Product product = findOrThrow(id);
        product.setActive(false);   // soft delete
        productRepository.save(product);
        log.info("Soft-deleted product id: {}", id);
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    public Product findOrThrow(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException("Product not found with id: " + id));
    }

    private ProductDto toDto(Product p) {
        CategoryDto categoryDto = CategoryDto.builder()
                .id(p.getCategory().getId())
                .name(p.getCategory().getName())
                .description(p.getCategory().getDescription())
                .imageUrl(p.getCategory().getImageUrl())
                .active(p.getCategory().isActive())
                .build();

        return ProductDto.builder()
                .id(p.getId()).name(p.getName()).description(p.getDescription())
                .sku(p.getSku()).price(p.getPrice()).stockQuantity(p.getStockQuantity())
                .imageUrl(p.getImageUrl()).category(categoryDto).active(p.isActive())
                .createdAt(p.getCreatedAt()).updatedAt(p.getUpdatedAt())
                .build();
    }
}
