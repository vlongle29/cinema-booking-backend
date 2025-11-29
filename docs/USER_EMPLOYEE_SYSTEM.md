# HỆ THỐNG PHÂN QUYỀN NHIỀU LỚP - USER & EMPLOYEE

## 1. TỔNG QUAN KIẾN TRÚC

### 1.1. Phân cấp Role hệ thống
```
SUPER_ADMIN (Quản trị tối cao)
    ↓ tạo
ADMIN (Quản trị viên)
    ↓ tạo
STAFF (Nhân viên) + Position (Chức vụ nghiệp vụ)
    
CUSTOMER (Khách hàng - đăng ký tự do)
```

### 1.2. Phân biệt Role và Position
- **Role (sys_role)**: Quyền hệ thống - SUPER_ADMIN, ADMIN, STAFF, CUSTOMER
- **Position (employees.position)**: Chức vụ nghiệp vụ - MANAGER, CASHIER, TECHNICIAN, CLEANER

**Ví dụ:**
- User A có role = STAFF, position = MANAGER → Quản lý chi nhánh
- User B có role = STAFF, position = CASHIER → Thu ngân
- User C có role = ADMIN → Không có position (không phải employee)

---

## 2. DATABASE SCHEMA & ERD

### 2.1. ERD Diagram
```
┌─────────────────────┐
│     users           │
│─────────────────────│
│ id (PK, UUID)       │
│ username (UNIQUE)   │
│ password            │
│ name                │
│ email (UNIQUE)      │
│ phone               │
│ avatar              │
│ lock_flag           │
│ system_flag         │◄──────┐
│ created_at          │       │
│ updated_at          │       │
│ is_delete           │       │
└─────────────────────┘       │
         │                    │
         │ 1                  │ 1
         │                    │
         │ N                  │
         ▼                    │
┌─────────────────────┐       │
│   user_role         │       │
│─────────────────────│       │
│ id (PK, UUID)       │       │
│ user_id (FK)        │       │
│ role_id (FK)        │       │
└─────────────────────┘       │
         │                    │
         │ N                  │
         │                    │
         │ 1                  │
         ▼                    │
┌─────────────────────┐       │
│     sys_role        │       │
│─────────────────────│       │
│ id (PK, UUID)       │       │
│ name                │       │
│ code (UNIQUE)       │       │
│ description         │       │
│ system_flag         │       │
│ priority            │       │
└─────────────────────┘       │
                              │
┌─────────────────────┐       │
│    employees        │       │
│─────────────────────│       │
│ user_id (PK, FK)    │───────┘
│ branch_id (FK)      │───────┐
│ employee_code       │       │
│ position            │       │
│ salary              │       │
│ hire_date           │       │
│ created_at          │       │
│ updated_at          │       │
│ is_delete           │       │
└─────────────────────┘       │
                              │ N
                              │
                              │ 1
                              ▼
                    ┌─────────────────────┐
                    │     branches        │
                    │─────────────────────│
                    │ id (PK, UUID)       │
                    │ name                │
                    │ address             │
                    │ city                │
                    │ manager_id (FK)     │
                    │ created_at          │
                    │ updated_at          │
                    │ is_delete           │
                    └─────────────────────┘
```

### 2.2. Chi tiết các bảng

#### Table: users
```sql
CREATE TABLE users (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    username VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    phone VARCHAR(20) NOT NULL,
    avatar VARCHAR(100),
    lock_flag VARCHAR(10) DEFAULT '0',
    provider_id VARCHAR(100),
    failed_login_attempts INTEGER DEFAULT 0,
    system_flag VARCHAR(20) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by UUID,
    updated_by UUID,
    is_delete BOOLEAN DEFAULT FALSE
);
```

**Ý nghĩa các trường:**
- `system_flag`: Phân biệt user hệ thống (SYSTEM) và user thường (NORMAL)
- `lock_flag`: Khóa tài khoản (0=active, 1=locked)
- `provider_id`: ID từ OAuth2 (Google, Facebook)

#### Table: sys_role
```sql
CREATE TABLE sys_role (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(100) NOT NULL,
    code VARCHAR(100) NOT NULL UNIQUE,
    description VARCHAR(500) NOT NULL,
    system_flag VARCHAR(50),
    priority INTEGER,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by UUID,
    updated_by UUID,
    is_delete BOOLEAN DEFAULT FALSE
);

-- Data mẫu
INSERT INTO sys_role (code, name, description, system_flag, priority) VALUES
('SUPER_ADMIN', 'Super Admin', 'Quản trị tối cao hệ thống', 'SYSTEM', 1),
('ADMIN', 'Admin', 'Quản trị viên', 'SYSTEM', 2),
('STAFF', 'Staff', 'Nhân viên', 'NORMAL', 3),
('CUSTOMER', 'Customer', 'Khách hàng', 'NORMAL', 4);
```

#### Table: user_role
```sql
CREATE TABLE user_role (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    role_id UUID NOT NULL REFERENCES sys_role(id) ON DELETE CASCADE,
    UNIQUE(user_id, role_id)
);
```

#### Table: employees
```sql
CREATE TABLE employees (
    user_id UUID PRIMARY KEY REFERENCES users(id) ON DELETE CASCADE,
    branch_id UUID NOT NULL REFERENCES branches(id),
    employee_code VARCHAR(50) UNIQUE,
    position VARCHAR(50),
    salary DECIMAL(15,2),
    hire_date DATE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by UUID,
    updated_by UUID,
    is_delete BOOLEAN DEFAULT FALSE
);
```

**Position values:**
- `MANAGER`: Quản lý chi nhánh
- `CASHIER`: Thu ngân
- `TECHNICIAN`: Kỹ thuật viên (chiếu phim, bảo trì)
- `CLEANER`: Nhân viên vệ sinh
- `SECURITY`: Bảo vệ

#### Table: branches
```sql
CREATE TABLE branches (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(255) NOT NULL,
    address VARCHAR(500),
    city VARCHAR(100),
    manager_id UUID REFERENCES employees(user_id),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by UUID,
    updated_by UUID,
    is_delete BOOLEAN DEFAULT FALSE
);
```

---

## 3. BUSINESS FLOW

### 3.1. Flow tạo Employee

```
┌─────────────┐
│   ADMIN     │
└──────┬──────┘
       │
       │ POST /api/employee/create
       │ {username, password, name, email, phone, branchId, employeeCode, position, salary}
       ▼
┌──────────────────────────────────────┐
│  EmployeeController.createEmployee() │
└──────────────┬───────────────────────┘
               │
               ▼
┌──────────────────────────────────────┐
│  EmployeeService.createEmployee()    │
│  ┌────────────────────────────────┐  │
│  │ 1. Validate employeeCode       │  │
│  │ 2. Find STAFF role             │  │
│  │ 3. Validate branch exists      │  │
│  │ 4. Create User (SysUser)       │  │
│  │ 5. Assign STAFF role           │  │
│  │ 6. Create Employee record      │  │
│  │ 7. Return EmployeeResponse     │  │
│  └────────────────────────────────┘  │
└──────────────┬───────────────────────┘
               │
               ▼
┌──────────────────────────────────────┐
│         Database                     │
│  ┌────────────────────────────────┐  │
│  │ INSERT INTO users              │  │
│  │ INSERT INTO user_role          │  │
│  │ INSERT INTO employees          │  │
│  └────────────────────────────────┘  │
└──────────────────────────────────────┘
```

### 3.2. Quy tắc phân quyền

#### 3.2.1. Quyền tạo User/Employee
```
SUPER_ADMIN → Tạo ADMIN
ADMIN       → Tạo STAFF (Employee)
STAFF       → Không có quyền tạo user
CUSTOMER    → Không có quyền tạo user
```

#### 3.2.2. Quyền quản lý Employee
```
SUPER_ADMIN → Quản lý tất cả
ADMIN       → Quản lý tất cả Employee
STAFF       → Chỉ xem thông tin của mình
  + MANAGER → Xem/Quản lý Employee trong chi nhánh
```

#### 3.2.3. Matrix phân quyền

| Action                    | SUPER_ADMIN | ADMIN | STAFF+MANAGER | STAFF | CUSTOMER |
|---------------------------|-------------|-------|---------------|-------|----------|
| Tạo ADMIN                 | ✓           | ✗     | ✗             | ✗     | ✗        |
| Tạo Employee              | ✓           | ✓     | ✗             | ✗     | ✗        |
| Xem tất cả Employee       | ✓           | ✓     | ✗             | ✗     | ✗        |
| Xem Employee trong branch | ✓           | ✓     | ✓             | ✗     | ✗        |
| Cập nhật Employee         | ✓           | ✓     | ✓(own branch) | ✗     | ✗        |
| Chuyển Employee           | ✓           | ✓     | ✗             | ✗     | ✗        |
| Set Branch Manager        | ✓           | ✓     | ✗             | ✗     | ✗        |
| Xem thông tin bản thân    | ✓           | ✓     | ✓             | ✓     | ✗        |

---

## 4. ANNOTATION KIỂM TRA POSITION

### 4.1. Custom Annotation: @RequirePosition

```java
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RequirePosition {
    PositionEnum[] value();
    String message() default "Access denied: insufficient position";
}
```

### 4.2. Aspect xử lý

```java
@Aspect
@Component
public class PositionCheckAspect {
    
    @Before("@annotation(requirePosition)")
    public void checkPosition(JoinPoint joinPoint, RequirePosition requirePosition) {
        UUID userId = SecurityUtils.getCurrentUserId();
        Employee employee = employeeRepository.findById(userId)
            .orElseThrow(() -> new BusinessException("NOT_EMPLOYEE"));
        
        PositionEnum[] allowedPositions = requirePosition.value();
        PositionEnum currentPosition = PositionEnum.valueOf(employee.getPosition());
        
        if (!Arrays.asList(allowedPositions).contains(currentPosition)) {
            throw new BusinessException("INSUFFICIENT_POSITION");
        }
    }
}
```

### 4.3. Sử dụng

```java
@PreAuthorize("hasRole('STAFF')")
@RequirePosition({PositionEnum.MANAGER})
@GetMapping("/branch/{branchId}/employees")
public ResponseEntity<PageResponse<EmployeeResponse>> getEmployeesByBranch(
    @PathVariable UUID branchId) {
    // Chỉ STAFF có position = MANAGER mới truy cập được
}
```

---

## 5. API ENDPOINTS

### 5.1. Employee Management

#### POST /api/employee/create
**Quyền:** ADMIN
**Mô tả:** Tạo nhân viên mới (tự động tạo User + gán role STAFF)

**Request:**
```json
{
  "username": "nv001",
  "password": "Password123!",
  "name": "Nguyễn Văn A",
  "email": "nva@cinebook.com",
  "phone": "0901234567",
  "branchId": "uuid-branch-1",
  "employeeCode": "NV001",
  "position": "CASHIER",
  "salary": 8000000,
  "hireDate": "2024-01-15"
}
```

**Response:**
```json
{
  "code": 200,
  "message": "Success",
  "data": {
    "userId": "uuid-user-1",
    "username": "nv001",
    "name": "Nguyễn Văn A",
    "email": "nva@cinebook.com",
    "phone": "0901234567",
    "branchId": "uuid-branch-1",
    "branchName": "CGV Vincom",
    "employeeCode": "NV001",
    "position": "CASHIER",
    "salary": 8000000,
    "hireDate": "2024-01-15",
    "roles": ["STAFF"]
  }
}
```

#### GET /api/employee/search
**Quyền:** ADMIN, STAFF+MANAGER
**Mô tả:** Tìm kiếm nhân viên với filter

**Query Params:**
- `branchId`: UUID (optional)
- `position`: String (optional)
- `keyword`: String (optional) - tìm theo tên, mã NV
- `page`: int (default: 0)
- `size`: int (default: 10)

#### GET /api/employee/{userId}
**Quyền:** ADMIN, STAFF (chỉ xem của mình)
**Mô tả:** Xem chi tiết nhân viên

#### PUT /api/employee/{userId}
**Quyền:** ADMIN, STAFF+MANAGER (trong cùng branch)
**Mô tả:** Cập nhật thông tin nhân viên

#### PUT /api/employee/{userId}/transfer
**Quyền:** ADMIN
**Mô tả:** Chuyển nhân viên sang chi nhánh khác

#### PUT /api/employee/branch/{branchId}/manager
**Quyền:** ADMIN
**Mô tả:** Đặt manager cho chi nhánh

---

## 6. VALIDATION & BUSINESS RULES

### 6.1. Validation Rules

#### Employee Creation
- `username`: 4-50 ký tự, chỉ chữ, số, underscore
- `password`: Tối thiểu 8 ký tự, có chữ hoa, chữ thường, số
- `email`: Format email hợp lệ, unique
- `phone`: 10-11 số
- `employeeCode`: Unique, format tùy chỉnh (VD: NV001)
- `position`: Phải thuộc enum PositionEnum
- `branchId`: Phải tồn tại trong DB

### 6.2. Business Rules

1. **Employee luôn có role = STAFF**
   - Khi tạo Employee, tự động gán role STAFF
   - Không được gán role khác cho Employee

2. **Position độc lập với Role**
   - Position chỉ áp dụng cho Employee (role STAFF)
   - ADMIN, SUPER_ADMIN không có position

3. **Branch Manager**
   - Mỗi branch chỉ có 1 manager
   - Manager phải là Employee có position = MANAGER
   - Manager phải thuộc branch đó

4. **Soft Delete**
   - Xóa Employee → set is_delete = true
   - Không xóa vật lý để giữ lịch sử

5. **Transfer Employee**
   - Chuyển branch → cập nhật branch_id
   - Nếu là manager → remove manager_id của branch cũ

---

## 7. SECURITY CONFIGURATION

### 7.1. Method Security

```java
@Configuration
@EnableMethodSecurity
public class SecurityConfig {
    // Đã có sẵn trong project
}
```

### 7.2. Sử dụng trong Controller

```java
// Kiểm tra Role
@PreAuthorize("hasRole('ADMIN')")

// Kiểm tra nhiều Role
@PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")

// Kiểm tra Role + Position (custom)
@PreAuthorize("hasRole('STAFF')")
@RequirePosition({PositionEnum.MANAGER})

// Kiểm tra owner
@PreAuthorize("hasRole('STAFF') and #userId == authentication.principal.id")
```

---

## 8. ERROR CODES

```java
// MessageCode.java
EMPLOYEE_CODE_ALREADY_EXISTS("E001", "Mã nhân viên đã tồn tại"),
STAFF_ROLE_NOT_CONFIGURED("E002", "Role STAFF chưa được cấu hình"),
BRANCH_NOT_FOUND("E003", "Chi nhánh không tồn tại"),
EMPLOYEE_NOT_FOUND("E004", "Nhân viên không tồn tại"),
USER_NOT_FOUND("E005", "User không tồn tại"),
INVALID_POSITION("E006", "Position không hợp lệ"),
NOT_EMPLOYEE("E007", "User không phải là nhân viên"),
INSUFFICIENT_POSITION("E008", "Không đủ quyền hạn position"),
MANAGER_ALREADY_EXISTS("E009", "Chi nhánh đã có manager"),
CANNOT_TRANSFER_MANAGER("E010", "Không thể chuyển manager sang branch khác");
```

---

## 9. TESTING SCENARIOS

### 9.1. Test Cases

1. **TC01: ADMIN tạo Employee thành công**
   - Input: Valid EmployeeCreateRequest
   - Expected: Employee được tạo, có role STAFF

2. **TC02: STAFF không thể tạo Employee**
   - Input: STAFF gọi API create
   - Expected: 403 Forbidden

3. **TC03: Tạo Employee với employeeCode trùng**
   - Input: employeeCode đã tồn tại
   - Expected: 400 Bad Request, code E001

4. **TC04: MANAGER xem Employee trong branch**
   - Input: STAFF+MANAGER gọi API search với branchId
   - Expected: Danh sách Employee trong branch

5. **TC05: STAFF xem thông tin bản thân**
   - Input: STAFF gọi GET /api/employee/{ownUserId}
   - Expected: Thông tin của chính mình

6. **TC06: STAFF xem thông tin người khác**
   - Input: STAFF gọi GET /api/employee/{otherUserId}
   - Expected: 403 Forbidden

---

## 10. MIGRATION CHECKLIST

- [x] Tạo enum PositionEnum
- [x] Cập nhật Employee entity (thêm position)
- [x] Tạo annotation @RequirePosition
- [x] Tạo PositionCheckAspect
- [x] Cập nhật EmployeeCreateRequest (thêm position)
- [x] Cập nhật EmployeeService.createEmployee()
- [x] Cập nhật EmployeeController (thêm @PreAuthorize)
- [x] Thêm MessageCode cho Employee
- [x] Viết Unit Test
- [x] Viết Integration Test
- [x] Cập nhật Swagger documentation

---

## 11. SWAGGER EXAMPLES

### 11.1. Create Employee Example

```yaml
POST /api/employee/create
Authorization: Bearer {admin_token}
Content-Type: application/json

{
  "username": "cashier01",
  "password": "Cashier@123",
  "name": "Trần Thị B",
  "email": "cashier01@cinebook.com",
  "phone": "0912345678",
  "branchId": "123e4567-e89b-12d3-a456-426614174000",
  "employeeCode": "NV002",
  "position": "CASHIER",
  "salary": 7500000,
  "hireDate": "2024-02-01"
}
```

### 11.2. Search Employee Example

```yaml
GET /api/employee/search?branchId=123e4567-e89b-12d3-a456-426614174000&position=CASHIER&page=0&size=10
Authorization: Bearer {admin_or_manager_token}
```

---

**Tài liệu này mô tả đầy đủ hệ thống User + Employee với phân quyền nhiều lớp.**
