package com.example.CineBook.service;

import com.example.CineBook.common.dto.response.PageResponse;
import com.example.CineBook.dto.employee.*;
import io.swagger.v3.oas.annotations.Operation;

import java.util.UUID;

public interface EmployeeService {
    EmployeeResponse createEmployee(EmployeeCreateRequest request);

    EmployeeResponse getEmployeeInfo(UUID userId);
    EmployeeResponse updateEmployee(UUID userId, EmployeeUpdateRequest request);
    EmployeeResponse transferEmployee(UUID userId, UUID branchId);
    void setBranchManager(UUID branchId, UUID managerId);

    /**
     * Get employee belong specific branch and pagination
     *
     * @param branchId
     * @param page
     * @param size
     * @return A Page of employee belong specific branch response DTOs.
     */
    PageResponse<EmployeeResponse> getEmployeesByBranch(UUID branchId, int page, int size);

    /**
     * Searches for employee with pagination and filtering.
     *
     * @param searchDTO The search criteria and pagination information.
     * @return A Page of employee response DTOs.
     */
    PageResponse<EmployeeResponse> searchEmployees(EmployeeSearchDTO searchDTO);

//    void deactivateEmployee(UUID employeeId);
//    void activateEmployee(UUID employeeId);

}
