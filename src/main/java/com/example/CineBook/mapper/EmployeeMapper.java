package com.example.CineBook.mapper;

import com.example.CineBook.dto.employee.EmployeeCreateRequest;
import com.example.CineBook.dto.employee.EmployeeResponse;
import com.example.CineBook.dto.employee.EmployeeSearchDTO;
import com.example.CineBook.dto.sysRole.SysRoleResponse;
import com.example.CineBook.model.Employee;
import com.example.CineBook.model.SysRole;
import org.mapstruct.*;
import org.springframework.data.domain.Page;

import java.util.Map;
import java.util.UUID;

@Mapper(
        componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS,
        unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface EmployeeMapper {
//    @Mapping(target = "userId", source = "userId")
    Employee toEntity(EmployeeCreateRequest request, @Context UUID userId);

    EmployeeResponse toResponse(Employee employee);
    EmployeeResponse toResponse(Employee employee, @Context Map<Object, Object> context);

    default Page<EmployeeResponse> mapPage(Page<Employee> entityPage, Map<Object, Object> context) {
        return entityPage.map(entity -> toResponse(entity, context));
    }
}
