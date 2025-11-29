package com.example.CineBook.controller;

import com.example.CineBook.common.dto.response.PageResponse;
import com.example.CineBook.common.response.ApiResponse;
import com.example.CineBook.dto.employee.*;
import com.example.CineBook.service.EmployeeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.UUID;

@Tag(name = "Employee Management", description = "APIs quản lý nhân viên")
@RestController
@RequestMapping("/api/employee")
@RequiredArgsConstructor
public class EmployeeController {

    private final EmployeeService employeeService;

    @PostMapping("/create")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
    @Operation(summary = "Tạo nhân viên mới", description = "ADMIN tạo Employee, tự động tạo User và gán role STAFF")
    public ResponseEntity<ApiResponse<EmployeeResponse>> createEmployee(@Valid @RequestBody EmployeeCreateRequest request) {
        return ResponseEntity.ok(ApiResponse.success(employeeService.createEmployee(request)));
    }

    @GetMapping("/branch/{branchId}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'STAFF')")
    @Operation(summary = "Liệt kê nhân viên theo chi nhánh")
    public ResponseEntity<ApiResponse<PageResponse<EmployeeResponse>>>  getEmployeesByBranch(
            @PathVariable UUID branchId, 
            @RequestParam(defaultValue = "0") int page, 
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(ApiResponse.success(employeeService.getEmployeesByBranch(branchId, page, size)));
    }

    @GetMapping("/{userId}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'STAFF')")
    @Operation(summary = "Xem thông tin nhân viên", description = "Xem lương, hire_date, employee_code, position")
    public ResponseEntity<ApiResponse<EmployeeResponse>>  getEmployeeInfo(@PathVariable UUID userId) {
        return ResponseEntity.ok(ApiResponse.success(employeeService.getEmployeeInfo(userId)));
    }

    @PutMapping("/{userId}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
    @Operation(summary = "Cập nhật thông tin nhân viên", description = "Cập nhật lương, hire_date, branch, position")
    public ResponseEntity<ApiResponse<EmployeeResponse>> updateEmployee(
            @PathVariable UUID userId,
            @Valid @RequestBody EmployeeUpdateRequest request) {
        return ResponseEntity.ok(ApiResponse.success(employeeService.updateEmployee(userId, request)));
    }

    @PutMapping("/{userId}/transfer")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
    @Operation(summary = "Chuyển nhân viên sang chi nhánh khác")
    public ResponseEntity<ApiResponse<EmployeeResponse>> transferEmployee(
            @PathVariable UUID userId,
            @Valid @RequestBody TransferEmployeeRequest request) {
        return ResponseEntity.ok(ApiResponse.success(employeeService.transferEmployee(userId, request.getBranchId())));
    }

    @PutMapping("/branch/{branchId}/manager")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
    @Operation(summary = "Set manager cho chi nhánh")
    public ResponseEntity<ApiResponse<Void>> setBranchManager(
            @PathVariable UUID branchId,
            @Valid @RequestBody SetManagerRequest request) {
        employeeService.setBranchManager(branchId, request.getManagerId());
        return ResponseEntity.ok(ApiResponse.success());
    }

    @GetMapping("/search")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'STAFF')")
    @Operation(summary = "Tìm kiếm và lọc nhân viên", description = "Tìm theo tên, mã NV, chi nhánh, position")
    public ResponseEntity<ApiResponse<PageResponse<EmployeeResponse>>> searchEmployees(
            @ModelAttribute EmployeeSearchDTO searchDTO) {
        return ResponseEntity.ok(ApiResponse.success(employeeService.searchEmployees(searchDTO)));
    }

    @DeleteMapping("/{employeeId}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
    @Operation(summary = "Xóa mềm nhân viên", description = "Xóa mềm nhân viên khi xin nghỉ việc, thôi việc...")
    public ResponseEntity<ApiResponse<Void>> deactivateEmployee(@PathVariable UUID employeeId) {
        // TODO: Implement soft delete logic
        return ResponseEntity.ok(ApiResponse.success());
    }


}
