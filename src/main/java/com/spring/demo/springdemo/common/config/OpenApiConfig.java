package com.spring.demo.springdemo.common.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.context.annotation.Configuration;

/** Swagger UI: http://localhost:8080/swagger-ui.html  |  raw spec: /v3/api-docs */
@Configuration
@OpenAPIDefinition(info = @Info(
        title = "Spring Demo Store API",
        version = "v1",
        description = "Products, orders and payments. Prices are stored in INR."))
public class OpenApiConfig {
}
