package com.spring.demo.springdemo.common.exception;

/** A third-party API failed or timed out. Mapped to HTTP 502. */
public class ExternalServiceException extends RuntimeException {

    public ExternalServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}
