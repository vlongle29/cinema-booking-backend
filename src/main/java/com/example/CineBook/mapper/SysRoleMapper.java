package com.example.CineBook.mapper;

import com.example.CineBook.dto.sysRole.SysRoleRequest;
import com.example.CineBook.dto.sysRole.SysRoleResponse;
import com.example.CineBook.model.SysRole;
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
public interface SysRoleMapper extends BaseMapper<SysRoleRequest, SysRole> {
    SysRole map(SysRoleRequest request, @Context Map<Object, Object> context);
    SysRoleResponse toResponse(SysRole entity, @Context Map<Object, Object> context);
    List<SysRoleResponse> toResponseList(List<SysRole> entities, @Context Map<Object, Object> context);
    void update(SysRoleRequest request, @MappingTarget SysRole entity, @Context Map<Object, Object> context);

    default Page<SysRoleResponse> mapPage(Page<SysRole> entityPage, Map<Object, Object> context) {
        return entityPage.map(entity -> toResponse(entity, context));
    }
}
