# FLOW ĐĂNG KÝ TÀI KHOẢN VÀ GÁN QUYỀN

## 1. Flow đăng ký

```
User gọi POST /api/auth/register
  ↓
Validate password == confirmPassword
  ↓
Check username/email/phone đã tồn tại
  ↓
Tạo SysUser (hash password)
  ↓
Gán role CUSTOMER tự động
  ↓
Return RegisterResponse
```

## 2. Code Implementation

### RegisterRequest
```java
{
  "username": "customer01",
  "password": "Customer@123",
  "confirmPassword": "Customer@123",
  "name": "Nguyễn Văn A",
  "email": "customer01@gmail.com",
  "phone": "0905555555"
}
```

### AuthServiceImpl.register()
```java
@Transactional
public RegisterResponse register(RegisterRequest request) {
    // 1. Validate
    // 2. Check duplicate
    // 3. Create user
    SysUser user = userMapper.toEntity(request);
    userRepository.save(user);
    
    // 4. Gán role CUSTOMER tự động
    assignDefaultRole(user.getId());
    
    return authMapper.map(request);
}

private void assignDefaultRole(UUID userId) {
    // Check nếu đã có role thì skip
    if (sysUserRoleRepository.existsByUserId(userId)) {
        return;
    }
    
    // Tìm role CUSTOMER
    SysRole customerRole = sysRoleRepository.findByCode("CUSTOMER")
        .orElseThrow(() -> new IllegalArgumentException("CUSTOMER role not found"));
    
    // Tạo user_role
    SysUserRole userRole = SysUserRole.builder()
        .userId(userId)
        .roleId(customerRole.getId())
        .build();
    
    sysUserRoleRepository.save(userRole);
}
```

## 3. Test API

### Đăng ký tài khoản mới
```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "newcustomer",
    "password": "Customer@123",
    "confirmPassword": "Customer@123",
    "name": "Khách Hàng Mới",
    "email": "newcustomer@gmail.com",
    "phone": "0906666666"
  }'
```

### Login để kiểm tra role
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "newcustomer",
    "password": "Customer@123"
  }'
```

**Response sẽ có:**
```json
{
  "code": 200,
  "data": {
    "token": "...",
    "userInfo": {
      "username": "newcustomer",
      "roles": ["CUSTOMER"]
    }
  }
}
```

## 4. Quy tắc gán role

| Cách tạo user | Role được gán | Ai có quyền tạo |
|---------------|---------------|-----------------|
| Đăng ký (register) | CUSTOMER | Public |
| SUPER_ADMIN tạo | ADMIN | SUPER_ADMIN |
| ADMIN tạo Employee | STAFF | ADMIN |

## 5. Lưu ý

- ✅ User đăng ký tự động có role CUSTOMER
- ✅ Không thể đăng ký với role khác (ADMIN, STAFF)
- ✅ Check duplicate role trước khi insert
- ✅ Role CUSTOMER phải tồn tại trong DB (chạy INIT_DATA.sql)
