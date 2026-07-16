package com.example.CineBook.mapper;

import com.example.CineBook.dto.employee.EmployeeCreateRequest;
import com.example.CineBook.dto.employee.EmployeeResponse;
import com.example.CineBook.model.Employee;
import com.example.CineBook.model.SysUser;
import org.mapstruct.*;

import java.util.UUID;

@Mapper(
        componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS,
        unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface EmployeeMapper {

    @Mapping(target = "userId", source = "userId")
    Employee toEntity(EmployeeCreateRequest request, UUID userId);

    @Mapping(target = "positionName", ignore = true)
    @Mapping(target = "branchName", ignore = true)
    @Mapping(target = "username", ignore = true)
    @Mapping(target = "name", ignore = true)
    @Mapping(target = "email", ignore = true)
    @Mapping(target = "phone", ignore = true)
    @Mapping(target = "hireDate", ignore = true)
    EmployeeResponse toResponse(Employee employee);
}
