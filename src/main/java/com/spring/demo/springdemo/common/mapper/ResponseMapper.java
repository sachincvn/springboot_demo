package com.spring.demo.springdemo.common.mapper;

import java.util.List;

/**
 * Generic contract for entity -> response DTO mapping, shared by every feature.
 * Entities never leave the service layer; the API only exposes DTOs.
 *
 * @param <E> entity type
 * @param <R> response DTO type
 */
public interface ResponseMapper<E, R> {

    R toResponse(E entity);

    default List<R> toResponses(List<E> entities) {
        return entities.stream().map(this::toResponse).toList();
    }
}
