package com.spring.demo.springdemo.product;

import com.spring.demo.springdemo.common.currency.CurrencyCode;
import com.spring.demo.springdemo.product.dto.PriceQuoteResponse;
import com.spring.demo.springdemo.product.dto.ProductRequest;
import com.spring.demo.springdemo.product.dto.ProductResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/** The contract controllers depend on. Implementation details can change without touching callers. */
public interface ProductService {

    ProductResponse createProduct(ProductRequest request);

    ProductResponse getProduct(Long id);

    /** @param name optional case-insensitive "contains" filter */
    Page<ProductResponse> getProducts(String name, Pageable pageable);

    ProductResponse updateProduct(Long id, ProductRequest request);

    void deleteProduct(Long id);

    PriceQuoteResponse getPriceQuote(Long id, CurrencyCode currency);
}
