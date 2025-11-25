package com.example.CineBook.mapper;

import org.mapstruct.MappingTarget;

import java.util.List;
import java.util.stream.Collectors;

public interface BaseMapper<S, T> {
    T map(S source);
    void update(S source, @MappingTarget T target);
    default List<T> mapList(List<S> sourceList) {
        if (sourceList == null) return null;
        return sourceList.stream().map(this::map).collect(Collectors.toList());
    }
}
