package com.example.CineBook.repository.base;

import org.springframework.data.domain.Page;
import org.springframework.data.repository.NoRepositoryBean;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@NoRepositoryBean
public interface BaseRepositoryCustom<T, S> {
    // --- Overloaded Methods for Searching with Optional Projection ---
    Page<T> findAllWithFilters(S searchDTO);
    <P> Page<P> findAllWithFilters(S searchDTO, Class<P> projectionClass);

    List<T> findAllWithFiltersAndSort(S searchDTO);
    <P> List<P> findAllWithFiltersAndSort(S searchDTO, Class<P> projectionClass);

    // --- Soft Delete & Restore ---
    int softDeleteById(UUID id);
    int softDeleteByIds(List<UUID> ids);
    int restoreById(UUID id);
    int restoreByIds(List<UUID> ids);

    // --- Batch Update & Delete ---
    int updateFieldByFilter(S searchDTO, String fieldName, Object value);
//    int hardDeleteByFilter(S searchDTO);
//    <P> Optional<P> findById(Object id, Class<P> projectionClass);
}
