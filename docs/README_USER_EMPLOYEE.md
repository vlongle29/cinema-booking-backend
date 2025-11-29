# HỆ THỐNG USER & EMPLOYEE - HƯỚNG DẪN TRIỂN KHAI

## 📋 Mục lục

1. [Tổng quan](#1-tổng-quan)
2. [Cấu trúc file](#2-cấu-trúc-file)
3. [Hướng dẫn cài đặt](#3-hướng-dẫn-cài-đặt)
4. [API Testing](#4-api-testing)
5. [Troubleshooting](#5-troubleshooting)

---

## 1. Tổng quan

Hệ thống phân quyền nhiều lớp với:
- **4 Role hệ thống**: SUPER_ADMIN, ADMIN, STAFF, CUSTOMER
- **5 Position nghiệp vụ**: MANAGER, CASHIER, TECHNICIAN, CLEANER, SECURITY
- **Annotation @RequirePosition**: Kiểm tra chức vụ nghiệp vụ
- **Business flow**: SUPER_ADMIN tạo ADMIN → ADMIN tạo Employee (STAFF)

---

## 2. Cấu trúc file

### 2.1. Tài liệu
```
docs/
├── USER_EMPLOYEE_SYSTEM.md          # Tài liệu chi tiết hệ thống (ERD, API, Business Rules)
├── POSITION_ANNOTATION_EXAMPLE.md   # Hướng dẫn sử dụng @RequirePosition
├── INIT_DATA.sql                    # Script khởi tạo dữ liệu mẫu
└── README_USER_EMPLOYEE.md          # File này
```

### 2.2. Source code mới

```
src/main/java/com/example/CineBook/
├── common/
│   ├── constant/
│   │   └── PositionEnum.java                    # Enum định nghĩa các position
│   ├── security/
│   │   ├── RequirePosition.java                 # Annotation kiểm tra position
│   │   └── PositionCheckAspect.java             # Aspect xử lý @RequirePosition
│   └── exception/
│       └── MessageCode.java                     # (Đã cập nhật thêm error codes)
├── dto/
│   └── employee/
│       ├── EmployeeCreateRequest.java           # (Đã thêm position)
│       ├── EmployeeUpdateRequest.java           # (Đã thêm position)
│       └── EmployeeResponse.java                # (Đã thêm position)
├── service/impl/
│   └── EmployeeServiceImpl.java                 # (Đã thêm validation position)
└── controller/
    └── EmployeeController.java                  # (Đã thêm @PreAuthorize)
```

---

## 3. Hướng dẫn cài đặt

### 3.1. Bước 1: Compile project

```bash
cd d:\project-java\CineBook
mvn clean compile
```

**Lưu ý:** Đảm bảo không có lỗi compile. Nếu có lỗi về MapStruct, chạy:
```bash
mvn clean install -DskipTests
```

### 3.2. Bước 2: Khởi tạo database

```bash
# Kết nối PostgreSQL
psql -U postgres -d cinebook_db

# Chạy script init data
\i d:/project-java/CineBook/docs/INIT_DATA.sql
```

Hoặc sử dụng tool GUI (DBeaver, pgAdmin):
- Mở file `INIT_DATA.sql`
- Execute script

### 3.3. Bước 3: Cấu hình biến môi trường

Tạo/cập nhật file `.env`:

```env
# Database
DB_URL=jdbc:postgresql://localhost:5432/cinebook_db
DB_USERNAME=postgres
DB_PASSWORD=your_password

# JWT
JWT_SECRET=your_jwt_secret_key_at_least_256_bits
JWT_EXPIRATION_MS=86400000

# Email (optional)
EMAIL_USERNAME=your_email@gmail.com
EMAIL_PASSWORD=your_app_password
```

### 3.4. Bước 4: Chạy ứng dụng

```bash
mvn spring-boot:run
```

Hoặc trong IDE:
- Run `CineBookApplication.java`

### 3.5. Bước 5: Kiểm tra Swagger

Truy cập: http://localhost:8080/swagger-ui.html

Kiểm tra các endpoint:
- `POST /api/employee/create`
- `GET /api/employee/search`
- `GET /api/employee/{userId}`
- `PUT /api/employee/{userId}`

---

## 4. API Testing

### 4.1. Login để lấy JWT Token

#### 4.1.1. Login với ADMIN

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "admin01",
    "password": "Admin@123"
  }'
```

**Response:**
```json
{
  "code": 200,
  "message": "Success",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "type": "Bearer",
    "username": "admin01",
    "roles": ["ADMIN"]
  }
}
```

Copy token để sử dụng cho các request tiếp theo.

#### 4.1.2. Login với STAFF (Manager)

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "manager01",
    "password": "Manager@123"
  }'
```

### 4.2. Tạo Employee (ADMIN only)

```bash
curl -X POST http://localhost:8080/api/employee/create \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer {ADMIN_TOKEN}" \
  -d '{
    "username": "cleaner01",
    "password": "Cleaner@123",
    "name": "Nguyễn Văn Vệ Sinh",
    "email": "cleaner01@cinebook.com",
    "phone": "0906666666",
    "branchId": "10000000-0000-0000-0000-000000000001",
    "employeeCode": "NV005",
    "position": "CLEANER",
    "salary": 6000000,
    "hireDate": "2024-01-15"
  }'
```

**Expected Response:**
```json
{
  "code": 200,
  "message": "Success",
  "data": {
    "userId": "...",
    "username": "cleaner01",
    "name": "Nguyễn Văn Vệ Sinh",
    "email": "cleaner01@cinebook.com",
    "phone": "0906666666",
    "branchId": "10000000-0000-0000-0000-000000000001",
    "branchName": "CGV Vincom Center",
    "employeeCode": "NV005",
    "position": "CLEANER",
    "salary": 6000000,
    "hireDate": "2024-01-15",
    "role": "STAFF"
  }
}
```

### 4.3. Search Employee

```bash
curl -X GET "http://localhost:8080/api/employee/search?branchId=10000000-0000-0000-0000-000000000001&page=0&size=10" \
  -H "Authorization: Bearer {ADMIN_TOKEN}"
```

### 4.4. Get Employee Info

```bash
curl -X GET http://localhost:8080/api/employee/{userId} \
  -H "Authorization: Bearer {ADMIN_TOKEN}"
```

### 4.5. Update Employee

```bash
curl -X PUT http://localhost:8080/api/employee/{userId} \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer {ADMIN_TOKEN}" \
  -d '{
    "salary": 9000000,
    "position": "MANAGER"
  }'
```

### 4.6. Transfer Employee

```bash
curl -X PUT http://localhost:8080/api/employee/{userId}/transfer \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer {ADMIN_TOKEN}" \
  -d '{
    "branchId": "10000000-0000-0000-0000-000000000002"
  }'
```

### 4.7. Set Branch Manager

```bash
curl -X PUT http://localhost:8080/api/employee/branch/{branchId}/manager \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer {ADMIN_TOKEN}" \
  -d '{
    "managerId": "{employeeUserId}"
  }'
```

---

## 5. Troubleshooting

### 5.1. Lỗi compile MapStruct

**Triệu chứng:**
```
[ERROR] Failed to execute goal org.apache.maven.plugins:maven-compiler-plugin
```

**Giải pháp:**
```bash
mvn clean install -DskipTests
```

### 5.2. Lỗi "STAFF_ROLE_NOT_CONFIGURED"

**Nguyên nhân:** Chưa chạy script INIT_DATA.sql

**Giải pháp:**
```sql
INSERT INTO sys_role (id, code, name, description, system_flag, priority)
VALUES (gen_random_uuid(), 'STAFF', 'Staff', 'Nhân viên', 'NORMAL', 3);
```

### 5.3. Lỗi "INVALID_POSITION"

**Nguyên nhân:** Position không thuộc enum PositionEnum

**Giải pháp:** Sử dụng một trong các giá trị:
- MANAGER
- CASHIER
- TECHNICIAN
- CLEANER
- SECURITY

### 5.4. Lỗi 403 Forbidden khi tạo Employee

**Nguyên nhân:** User không có role ADMIN

**Giải pháp:** Login với tài khoản ADMIN hoặc SUPER_ADMIN

### 5.5. Lỗi "NOT_EMPLOYEE" khi dùng @RequirePosition

**Nguyên nhân:** User không có record trong bảng employees

**Giải pháp:** Chỉ STAFF mới có Employee record. ADMIN/SUPER_ADMIN không có.

### 5.6. Aspect không hoạt động

**Kiểm tra:**
1. Có `@EnableAspectJAutoProxy` trong config không?
2. Có dependency `spring-boot-starter-aop` không?

**Thêm dependency nếu thiếu:**
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-aop</artifactId>
</dependency>
```

---

## 6. Testing Checklist

### 6.1. Functional Testing

- [ ] SUPER_ADMIN có thể tạo ADMIN
- [ ] ADMIN có thể tạo Employee (STAFF)
- [ ] STAFF không thể tạo Employee
- [ ] Employee được tạo tự động có role STAFF
- [ ] Position được validate đúng
- [ ] MANAGER có thể xem Employee trong branch
- [ ] CASHIER không thể xem Employee khác branch
- [ ] Transfer Employee cập nhật branch_id
- [ ] Set manager cập nhật branch.manager_id

### 6.2. Security Testing

- [ ] Không có JWT token → 401 Unauthorized
- [ ] JWT token hết hạn → 401 Unauthorized
- [ ] CUSTOMER gọi API Employee → 403 Forbidden
- [ ] STAFF không có position MANAGER gọi API Manager-only → 403 Forbidden
- [ ] SQL Injection không thành công
- [ ] XSS không thành công

### 6.3. Performance Testing

- [ ] Search Employee với 1000 records < 1s
- [ ] Create Employee < 500ms
- [ ] Concurrent create 10 employees không conflict

---

## 7. Deployment

### 7.1. Build JAR

```bash
mvn clean package -DskipTests
```

Output: `target/CineBook-0.0.1-SNAPSHOT.jar`

### 7.2. Run JAR

```bash
java -jar target/CineBook-0.0.1-SNAPSHOT.jar \
  --DB_URL=jdbc:postgresql://prod-db:5432/cinebook \
  --DB_USERNAME=prod_user \
  --DB_PASSWORD=prod_pass \
  --JWT_SECRET=prod_secret_key
```

### 7.3. Docker

```bash
# Build image
docker build -t cinebook:latest .

# Run container
docker run -d \
  -p 8080:8080 \
  -e DB_URL=jdbc:postgresql://host.docker.internal:5432/cinebook_db \
  -e DB_USERNAME=postgres \
  -e DB_PASSWORD=password \
  -e JWT_SECRET=secret_key \
  --name cinebook-app \
  cinebook:latest
```

---

## 8. Tài liệu tham khảo

1. **USER_EMPLOYEE_SYSTEM.md**: Tài liệu chi tiết về ERD, Business Flow, API
2. **POSITION_ANNOTATION_EXAMPLE.md**: Hướng dẫn sử dụng @RequirePosition
3. **INIT_DATA.sql**: Script khởi tạo dữ liệu mẫu
4. **Swagger UI**: http://localhost:8080/swagger-ui.html

---

## 9. Contact & Support

- **Developer**: CineBook Team
- **Email**: support@cinebook.com
- **Documentation**: [GitHub Wiki](https://github.com/cinebook/docs)

---

**Version**: 1.0.0  
**Last Updated**: 2024-01-15
