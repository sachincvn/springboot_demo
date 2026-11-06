package com.spring.demo.springdemo.product;

import com.spring.demo.springdemo.common.exception.ResourceNotFoundException;
import com.spring.demo.springdemo.product.dto.ProductRequest;
import com.spring.demo.springdemo.product.dto.ProductResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/** Web-layer slice: real controller + validation + GlobalExceptionHandler, service mocked, no DB. */
@WebMvcTest(ProductController.class)
class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ProductService productService;

    @Test
    void createReturns201WithLocationHeader() throws Exception {
        when(productService.createProduct(any(ProductRequest.class))).thenReturn(
                new ProductResponse(7L, "Keyboard", null, new BigDecimal("1500.00"), 10, null, null));

        mockMvc.perform(post("/api/v1/products").contentType(APPLICATION_JSON).content("""
                        {"name": "Keyboard", "price": 1500.00, "stockQuantity": 10}
                        """))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "http://localhost/api/v1/products/7"))
                .andExpect(jsonPath("$.id").value(7));
    }

    @Test
    void createRejectsInvalidBodyWithFieldErrors() throws Exception {
        mockMvc.perform(post("/api/v1/products").contentType(APPLICATION_JSON).content("""
                        {"name": "", "price": -5, "stockQuantity": -1}
                        """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Validation failed"))
                .andExpect(jsonPath("$.errors.name").value("Name is required"))
                .andExpect(jsonPath("$.errors.price").value("Price must be greater than 0"))
                .andExpect(jsonPath("$.errors.stockQuantity").value("Stock quantity cannot be negative"));

        verifyNoInteractions(productService);
    }

    @Test
    void unknownProductReturns404ProblemDetail() throws Exception {
        when(productService.getProduct(99L)).thenThrow(new ResourceNotFoundException(Product.class, 99L));

        mockMvc.perform(get("/api/v1/products/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Resource not found"))
                .andExpect(jsonPath("$.detail").value("Product not found with id 99"));
    }

    @Test
    void invalidCurrencyReturns400() throws Exception {
        mockMvc.perform(get("/api/v1/products/1/price").param("currency", "XYZ"))
                .andExpect(status().isBadRequest());
    }
}
