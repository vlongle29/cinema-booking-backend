# Hướng dẫn sử dụng @RequirePosition Annotation

## 1. Giới thiệu

Annotation `@RequirePosition` được sử dụng để kiểm tra chức vụ (position) của Employee trước khi cho phép truy cập vào method.

**Lưu ý quan trọng:**
- `@RequirePosition` chỉ áp dụng cho Employee (user có role STAFF)
- Phải kết hợp với `@PreAuthorize("hasRole('STAFF')")` để đảm bảo user là STAFF
- Nếu user không phải Employee hoặc position không đủ → throw BusinessException

---

## 2. Cách sử dụng

### 2.1. Import

```java
import com.example.CineBook.common.security.RequirePosition;
import com.example.CineBook.common.constant.PositionEnum;
import org.springframework.security.access.prepost.PreAuthorize;
```

### 2.2. Ví dụ cơ bản

#### Chỉ MANAGER được truy cập

```java
@RestController
@RequestMapping("/api/branch")
public class BranchController {

    @GetMapping("/{branchId}/report")
    @PreAuthorize("hasRole('STAFF')")
    @RequirePosition({PositionEnum.MANAGER})
    @Operation(summary = "Xem báo cáo chi nhánh - Chỉ Manager")
    public ResponseEntity<ApiResponse<BranchReport>> getBranchReport(
            @PathVariable UUID branchId) {
        // Chỉ STAFF có position = MANAGER mới truy cập được
        return ResponseEntity.ok(ApiResponse.success(branchService.getReport(branchId)));
    }
}
```

#### Nhiều position được phép

```java
@PutMapping("/{branchId}/schedule")
@PreAuthorize("hasRole('STAFF')")
@RequirePosition({PositionEnum.MANAGER, PositionEnum.TECHNICIAN})
@Operation(summary = "Cập nhật lịch chiếu - Manager hoặc Technician")
public ResponseEntity<ApiResponse<Void>> updateSchedule(
        @PathVariable UUID branchId,
        @RequestBody ScheduleRequest request) {
    // MANAGER hoặc TECHNICIAN đều truy cập được
    scheduleService.update(branchId, request);
    return ResponseEntity.ok(ApiResponse.success());
}
```

---

## 3. Các trường hợp sử dụng thực tế

### 3.1. Quản lý nhân viên trong chi nhánh

```java
@RestController
@RequestMapping("/api/employee")
public class EmployeeController {

    /**
     * Manager xem danh sách nhân viên trong chi nhánh của mình
     */
    @GetMapping("/my-branch")
    @PreAuthorize("hasRole('STAFF')")
    @RequirePosition({PositionEnum.MANAGER})
    public ResponseEntity<ApiResponse<PageResponse<EmployeeResponse>>> getMyBranchEmployees(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        UUID currentUserId = SecurityUtils.getCurrentUserId();
        Employee manager = employeeRepository.findById(currentUserId)
                .orElseThrow(() -> new BusinessException(MessageCode.EMPLOYEE_NOT_FOUND));
        
        return ResponseEntity.ok(ApiResponse.success(
            employeeService.getEmployeesByBranch(manager.getBranchId(), page, size)
        ));
    }

    /**
     * Manager phê duyệt nghỉ phép cho nhân viên
     */
    @PostMapping("/leave-request/{requestId}/approve")
    @PreAuthorize("hasRole('STAFF')")
    @RequirePosition({PositionEnum.MANAGER})
    public ResponseEntity<ApiResponse<Void>> approveLeaveRequest(
            @PathVariable UUID requestId) {
        leaveRequestService.approve(requestId);
        return ResponseEntity.ok(ApiResponse.success());
    }
}
```

### 3.2. Quản lý kỹ thuật

```java
@RestController
@RequestMapping("/api/technical")
public class TechnicalController {

    /**
     * Kỹ thuật viên cập nhật trạng thái thiết bị
     */
    @PutMapping("/equipment/{equipmentId}/status")
    @PreAuthorize("hasRole('STAFF')")
    @RequirePosition({PositionEnum.TECHNICIAN, PositionEnum.MANAGER})
    public ResponseEntity<ApiResponse<Void>> updateEquipmentStatus(
            @PathVariable UUID equipmentId,
            @RequestBody EquipmentStatusRequest request) {
        equipmentService.updateStatus(equipmentId, request);
        return ResponseEntity.ok(ApiResponse.success());
    }

    /**
     * Báo cáo sự cố kỹ thuật
     */
    @PostMapping("/incident/report")
    @PreAuthorize("hasRole('STAFF')")
    @RequirePosition({PositionEnum.TECHNICIAN})
    public ResponseEntity<ApiResponse<IncidentResponse>> reportIncident(
            @RequestBody IncidentRequest request) {
        return ResponseEntity.ok(ApiResponse.success(
            incidentService.report(request)
        ));
    }
}
```

### 3.3. Quản lý thu ngân

```java
@RestController
@RequestMapping("/api/cashier")
public class CashierController {

    /**
     * Thu ngân xử lý thanh toán
     */
    @PostMapping("/payment/process")
    @PreAuthorize("hasRole('STAFF')")
    @RequirePosition({PositionEnum.CASHIER, PositionEnum.MANAGER})
    public ResponseEntity<ApiResponse<PaymentResponse>> processPayment(
            @RequestBody PaymentRequest request) {
        return ResponseEntity.ok(ApiResponse.success(
            paymentService.process(request)
        ));
    }

    /**
     * Xem báo cáo doanh thu ca làm việc
     */
    @GetMapping("/shift-report")
    @PreAuthorize("hasRole('STAFF')")
    @RequirePosition({PositionEnum.CASHIER})
    public ResponseEntity<ApiResponse<ShiftReport>> getShiftReport() {
        UUID currentUserId = SecurityUtils.getCurrentUserId();
        return ResponseEntity.ok(ApiResponse.success(
            reportService.getShiftReport(currentUserId)
        ));
    }
}
```

---

## 4. Kết hợp với logic nghiệp vụ

### 4.1. Kiểm tra Manager của cùng chi nhánh

```java
@Service
@RequiredArgsConstructor
public class EmployeeServiceImpl implements EmployeeService {

    private final EmployeeRepository employeeRepository;

    /**
     * Manager chỉ được cập nhật nhân viên trong chi nhánh của mình
     */
    @Transactional
    public EmployeeResponse updateEmployeeByManager(UUID employeeId, EmployeeUpdateRequest request) {
        UUID currentUserId = SecurityUtils.getCurrentUserId();
        
        // Lấy thông tin manager
        Employee manager = employeeRepository.findById(currentUserId)
                .orElseThrow(() -> new BusinessException(MessageCode.EMPLOYEE_NOT_FOUND));
        
        // Kiểm tra position (có thể dùng annotation ở controller)
        if (!PositionEnum.MANAGER.getValue().equals(manager.getPosition())) {
            throw new BusinessException(MessageCode.INSUFFICIENT_POSITION);
        }
        
        // Lấy thông tin employee cần update
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new BusinessException(MessageCode.EMPLOYEE_NOT_FOUND));
        
        // Kiểm tra cùng chi nhánh
        if (!manager.getBranchId().equals(employee.getBranchId())) {
            throw new BusinessException(MessageCode.PERMISSION_DENIED);
        }
        
        // Thực hiện update
        // ...
        
        return employeeMapper.toResponse(employee);
    }
}
```

### 4.2. Controller tương ứng

```java
@PutMapping("/my-branch/employee/{employeeId}")
@PreAuthorize("hasRole('STAFF')")
@RequirePosition({PositionEnum.MANAGER})
@Operation(summary = "Manager cập nhật nhân viên trong chi nhánh")
public ResponseEntity<ApiResponse<EmployeeResponse>> updateEmployeeByManager(
        @PathVariable UUID employeeId,
        @RequestBody EmployeeUpdateRequest request) {
    return ResponseEntity.ok(ApiResponse.success(
        employeeService.updateEmployeeByManager(employeeId, request)
    ));
}
```

---

## 5. Error Handling

### 5.1. Các exception có thể xảy ra

```java
// User không phải Employee
MessageCode.NOT_EMPLOYEE → "User không phải là nhân viên"

// Position không hợp lệ trong DB
MessageCode.INVALID_POSITION → "Position không hợp lệ"

// Position không đủ quyền
MessageCode.INSUFFICIENT_POSITION → "Không đủ quyền hạn position"

// Employee đã bị soft delete
MessageCode.EMPLOYEE_NOT_FOUND → "Nhân viên không tồn tại"
```

### 5.2. Response khi lỗi

```json
{
  "code": 403,
  "message": "Không đủ quyền hạn position",
  "data": null
}
```

---

## 6. Testing

### 6.1. Unit Test

```java
@SpringBootTest
@AutoConfigureMockMvc
class PositionCheckAspectTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @WithMockUser(username = "manager01", roles = {"STAFF"})
    void testManagerCanAccessManagerOnlyEndpoint() throws Exception {
        // Setup: Tạo employee với position = MANAGER
        
        mockMvc.perform(get("/api/branch/{branchId}/report", branchId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "cashier01", roles = {"STAFF"})
    void testCashierCannotAccessManagerOnlyEndpoint() throws Exception {
        // Setup: Tạo employee với position = CASHIER
        
        mockMvc.perform(get("/api/branch/{branchId}/report", branchId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("Không đủ quyền hạn position"));
    }
}
```

---

## 7. Best Practices

### 7.1. Luôn kết hợp với @PreAuthorize

❌ **SAI:**
```java
@RequirePosition({PositionEnum.MANAGER})
public void managerMethod() {
    // Thiếu @PreAuthorize, ADMIN có thể bypass
}
```

✅ **ĐÚNG:**
```java
@PreAuthorize("hasRole('STAFF')")
@RequirePosition({PositionEnum.MANAGER})
public void managerMethod() {
    // Đảm bảo chỉ STAFF + MANAGER mới truy cập được
}
```

### 7.2. Đặt annotation ở Controller, không phải Service

✅ **ĐÚNG:**
```java
// Controller
@PreAuthorize("hasRole('STAFF')")
@RequirePosition({PositionEnum.MANAGER})
@GetMapping("/report")
public ResponseEntity<Report> getReport() {
    return ResponseEntity.ok(service.getReport());
}

// Service
public Report getReport() {
    // Logic nghiệp vụ thuần túy
}
```

### 7.3. Sử dụng SpEL cho logic phức tạp

```java
@PreAuthorize("hasRole('STAFF') and @securityService.isManagerOfBranch(#branchId)")
@RequirePosition({PositionEnum.MANAGER})
@GetMapping("/branch/{branchId}/sensitive-data")
public ResponseEntity<Data> getSensitiveData(@PathVariable UUID branchId) {
    // Kiểm tra cả position và branch ownership
}
```

---

## 8. Tóm tắt

| Annotation | Mục đích | Áp dụng cho |
|------------|----------|-------------|
| `@PreAuthorize("hasRole('STAFF')")` | Kiểm tra role hệ thống | Tất cả user |
| `@RequirePosition({...})` | Kiểm tra chức vụ nghiệp vụ | Chỉ Employee (STAFF) |
| Kết hợp cả 2 | Kiểm tra đầy đủ quyền hạn | Employee với position cụ thể |

**Quy tắc vàng:**
1. ADMIN/SUPER_ADMIN → Không cần `@RequirePosition`
2. STAFF → Cần `@RequirePosition` nếu phân quyền theo chức vụ
3. CUSTOMER → Không có Employee record, không áp dụng position
