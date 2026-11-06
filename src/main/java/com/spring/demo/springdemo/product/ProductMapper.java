package com.spring.demo.springdemo.product;

import com.spring.demo.springdemo.common.mapper.ResponseMapper;
import com.spring.demo.springdemo.product.dto.ProductRequest;
import com.spring.demo.springdemo.product.dto.ProductResponse;
import org.springframework.stereotype.Component;

/** Converting between API shapes and entities is its own job (Single Responsibility), kept out of services. */
@Component
public class ProductMapper implements ResponseMapper<Product, ProductResponse> {

    public Product toEntity(ProductRequest request) {
        return new Product(request.name(), request.description(), request.price(), request.stockQuantity());
    }

    public void updateEntity(Product product, ProductRequest request) {
        product.updateDetails(request.name(), request.description(), request.price(), request.stockQuantity());
    }

    @Override
    public ProductResponse toResponse(Product product) {
        return new ProductResponse(
                product.getId(),
                product.getName(),
                product.getDescription(),
                product.getPrice(),
                product.getStockQuantity(),
                product.getCreatedAt(),
                product.getUpdatedAt());
    }
}
