package com.spring.demo.springdemo.product;

import com.spring.demo.springdemo.common.currency.CurrencyCode;
import com.spring.demo.springdemo.product.dto.PriceQuoteResponse;
import com.spring.demo.springdemo.product.dto.ProductRequest;
import com.spring.demo.springdemo.product.dto.ProductResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

/** Thin HTTP layer: parse + validate input, call the service, shape the HTTP response. No business logic here. */
@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
@Tag(name = "Products", description = "Product catalogue and stock")
public class ProductController {

    private final ProductService productService;

    @PostMapping
    @Operation(summary = "Create a product")
    @ApiResponse(responseCode = "201", description = "Created")
    @ApiResponse(responseCode = "400", description = "Validation failed")
    public ResponseEntity<ProductResponse> create(@Valid @RequestBody ProductRequest request) {
        ProductResponse created = productService.createProduct(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(created.id()).toUri();
        return ResponseEntity.created(location).body(created);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a product by id")
    @ApiResponse(responseCode = "404", description = "Product not found")
    public ProductResponse get(@PathVariable Long id) {
        return productService.getProduct(id);
    }

    @GetMapping
    @Operation(summary = "List products (paged, optional name search)")
    public Page<ProductResponse> list(
            @Parameter(description = "Case-insensitive name filter") @RequestParam(required = false) String name,
            @ParameterObject @PageableDefault(size = 20, sort = "id", direction = Sort.Direction.DESC) Pageable pageable) {
        return productService.getProducts(name, pageable);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Replace a product's details")
    @ApiResponse(responseCode = "404", description = "Product not found")
    public ProductResponse update(@PathVariable Long id, @Valid @RequestBody ProductRequest request) {
        return productService.updateProduct(id, request);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a product")
    @ApiResponse(responseCode = "204", description = "Deleted")
    @ApiResponse(responseCode = "409", description = "Product is referenced by an order")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        productService.deleteProduct(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/price")
    @Operation(summary = "Quote a product's price in another currency (live rates from a third-party API)")
    @ApiResponse(responseCode = "502", description = "Exchange-rate provider unavailable")
    public PriceQuoteResponse priceQuote(@PathVariable Long id, @RequestParam CurrencyCode currency) {
        return productService.getPriceQuote(id, currency);
    }
}
