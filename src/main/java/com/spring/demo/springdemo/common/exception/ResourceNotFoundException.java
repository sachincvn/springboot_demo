package com.spring.demo.springdemo.common.exception;

/** Requested entity does not exist. Mapped to HTTP 404. */
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(Class<?> resourceType, Object id) {
        super("%s not found with id %s".formatted(resourceType.getSimpleName(), id));
    }
}
