package com.example.CineBook.repository.irepository;

import com.example.CineBook.dto.employee.EmployeeSearchDTO;
import com.example.CineBook.model.Employee;
import com.example.CineBook.repository.base.BaseRepositoryCustom;
import com.example.CineBook.repository.custom.EmployeeRepositoryCustom;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface EmployeeRepository extends BaseRepositoryCustom<Employee, EmployeeSearchDTO>, EmployeeRepositoryCustom, JpaRepository<Employee, UUID> {
//    Optional<Employee> findByUsername(@NotBlank(message = "{USER_USERNAME_REQUIRED}") String username);
//    Boolean existsByUsername(String username);
//    Boolean existsByEmail(String email);
    Boolean existsByEmployeeCode(String employeeCode);
    List<Employee> findByBranchId(UUID branchId);
}
