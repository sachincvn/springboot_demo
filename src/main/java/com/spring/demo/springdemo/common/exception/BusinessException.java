package com.spring.demo.springdemo.common.exception;

/** A business rule was broken (e.g. not enough stock, illegal status change). Mapped to HTTP 409. */
public class BusinessException extends RuntimeException {

    public BusinessException(String message) {
        super(message);
    }
}
