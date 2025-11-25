package com.example.CineBook.mapper;

import com.example.CineBook.dto.sysPermission.SysPermissionRequest;
import com.example.CineBook.dto.sysPermission.SysPermissionResponse;
import com.example.CineBook.model.SysPermission;
import org.mapstruct.*;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.Map;

@Mapper(
        componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS,
        unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface SysPermissionMapper {
    SysPermission map(SysPermissionRequest request, @Context Map<String, Object> context);
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void update(SysPermissionRequest request, @MappingTarget SysPermission entity, @Context Map<String, Object> context);
    SysPermissionResponse toResponse(SysPermission entity, @Context Map<String, Object> context);
    List<SysPermissionResponse> toResponseList(List<SysPermission> entityList, @Context Map<String, Object> context);

    default Page<SysPermissionResponse> mapPage(Page<SysPermission> entityPage, Map<String, Object> context) {
        return entityPage.map(entity -> toResponse(entity, context));
    }
}
