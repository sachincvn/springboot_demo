package com.spring.demo.springdemo.product;

import com.spring.demo.springdemo.common.currency.CurrencyCode;
import com.spring.demo.springdemo.common.exception.ResourceNotFoundException;
import com.spring.demo.springdemo.integration.exchangerate.ExchangeRateClient;
import com.spring.demo.springdemo.product.dto.PriceQuoteResponse;
import com.spring.demo.springdemo.product.dto.ProductRequest;
import com.spring.demo.springdemo.product.dto.ProductResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Slf4j
@Service
@RequiredArgsConstructor // constructor injection: dependencies are final and obvious, easy to mock in tests
@Transactional(readOnly = true) // default for reads; write methods override it
public class ProductServiceImpl implements ProductService {

    static final CurrencyCode STORE_CURRENCY = CurrencyCode.INR;

    private final ProductRepository productRepository;
    private final ProductMapper productMapper;
    private final ExchangeRateClient exchangeRateClient;

    @Override
    @Transactional
    public ProductResponse createProduct(ProductRequest request) {
        Product product = productRepository.save(productMapper.toEntity(request));
        log.info("Created product id={}", product.getId());
        return productMapper.toResponse(product);
    }

    @Override
    public ProductResponse getProduct(Long id) {
        return productMapper.toResponse(findProduct(id));
    }

    @Override
    public Page<ProductResponse> getProducts(String name, Pageable pageable) {
        Page<Product> page = StringUtils.hasText(name)
                ? productRepository.findByNameContainingIgnoreCase(name, pageable)
                : productRepository.findAll(pageable);
        return page.map(productMapper::toResponse);
    }

    @Override
    @Transactional
    public ProductResponse updateProduct(Long id, ProductRequest request) {
        Product product = findProduct(id);
        productMapper.updateEntity(product, request); // managed entity: changes flush on commit, no save() needed
        log.info("Updated product id={}", id);
        return productMapper.toResponse(product);
    }

    @Override
    @Transactional
    public void deleteProduct(Long id) {
        productRepository.delete(findProduct(id));
        log.info("Deleted product id={}", id);
    }

    @Override
    public PriceQuoteResponse getPriceQuote(Long id, CurrencyCode currency) {
        Product product = findProduct(id);
        BigDecimal rate = exchangeRateClient.getRate(STORE_CURRENCY, currency);
        BigDecimal converted = product.getPrice().multiply(rate).setScale(2, RoundingMode.HALF_UP);
        return new PriceQuoteResponse(product.getId(), product.getPrice(), STORE_CURRENCY, converted, currency, rate);
    }

    private Product findProduct(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(Product.class, id));
    }
}
