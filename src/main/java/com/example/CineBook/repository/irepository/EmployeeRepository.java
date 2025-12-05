package com.example.CineBook.repository.irepository;

import com.example.CineBook.model.Employee;
import com.example.CineBook.repository.custom.EmployeeRepositoryCustom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface EmployeeRepository extends JpaRepository<Employee, UUID>, EmployeeRepositoryCustom {
    Boolean existsByEmployeeCode(String employeeCode);
    List<Employee> findByBranchId(UUID branchId);
    Optional<Employee> findByUserId(UUID userId);
    
    @Modifying
    @Query("UPDATE Employee e SET e.isDelete = true, e.deleteTime = CURRENT_TIMESTAMP WHERE e.id = :id")
    void softDeleteById(@Param("id") UUID id);
}
