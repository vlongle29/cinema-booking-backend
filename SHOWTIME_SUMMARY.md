# 🎬 Module Showtimes - Tóm tắt

## 📦 Các file đã tạo

### 1. DTOs (4 files)
```
dto/showtime/
├── CreateShowtimeRequest.java      # Request tạo mới
├── UpdateShowtimeRequest.java      # Request cập nhật
├── ShowtimeSearchDTO.java          # DTO filter/search
└── ShowtimeResponse.java           # Response với nested objects
```

### 2. Repository (3 files)
```
repository/
├── irepository/ShowtimeRepository.java           # Interface với overlap queries
├── custom/ShowtimeRepositoryCustom.java          # Custom interface
└── impl/ShowtimeRepositoryImpl.java              # Implementation search
```

### 3. Mapper (1 file)
```
mapper/ShowtimeMapper.java          # MapStruct mapper
```

### 4. Service (2 files)
```
service/
├── ShowtimeService.java            # Interface
└── impl/ShowtimeServiceImpl.java   # Implementation với full validation
```

### 5. Controller (1 file)
```
controller/ShowtimeController.java  # REST endpoints
```

### 6. Configuration & Messages (2 files)
```
common/exception/MessageCode.java   # Thêm 5 error codes
resources/messages.properties       # Thêm 5 messages tiếng Việt
```

### 7. Documentation (4 files)
```
SHOWTIME_MODULE.md                              # Hướng dẫn chi tiết
SHOWTIME_CHECKLIST.md                           # Checklist kiểm tra
SHOWTIME_SUMMARY.md                             # File này
test-showtime-data.sql                          # SQL test data
Showtime-API-Tests.postman_collection.json      # Postman collection
```

**Tổng cộng: 17 files mới + 2 files cập nhật**

---

## 🎯 Tính năng chính

### ✅ CRUD Operations
- **CREATE**: Tạo suất chiếu với validation đầy đủ
- **READ**: Xem chi tiết + Search/Filter
- **UPDATE**: Cập nhật với validation
- **DELETE**: Soft delete

### ✅ Business Logic
1. **Auto branchId**: Tự động lấy từ room
2. **Overlap check**: Không cho trùng giờ trong cùng phòng
3. **Time validation**: endTime phải > startTime
4. **Entity validation**: Movie và Room phải tồn tại
5. **Status management**: OPEN, CLOSED, CANCELLED

### ✅ Search & Filter
- Filter theo `movieId`
- Filter theo `branchId`
- Filter theo `roomId`
- Filter theo `date`
- Pagination support

### ✅ Security
- POST/PUT/DELETE: Chỉ ADMIN, SUPER_ADMIN
- GET: Public (không cần auth)

### ✅ Response Format
```json
{
  "success": true,
  "data": {
    "id": "uuid",
    "movieId": "uuid",
    "roomId": "uuid",
    "branchId": "uuid",
    "startTime": "2024-01-15T14:00:00",
    "endTime": "2024-01-15T16:30:00",
    "price": 100000,
    "status": "OPEN",
    "movie": { ... },      // Nested movie info
    "room": { ... },       // Nested room info
    "branch": { ... }      // Nested branch info
  },
  "message": "success"
}
```

---

## 🔧 API Endpoints

| Method | Endpoint | Role | Mô tả |
|--------|----------|------|-------|
| POST | `/api/showtimes` | ADMIN | Tạo suất chiếu mới |
| GET | `/api/showtimes` | Public | Search/Filter showtimes |
| GET | `/api/showtimes/{id}` | Public | Chi tiết suất chiếu |
| PUT | `/api/showtimes/{id}` | ADMIN | Cập nhật suất chiếu |
| DELETE | `/api/showtimes/{id}` | ADMIN | Xóa suất chiếu |

---

## ⚠️ Error Codes

| Code | Message | Khi nào xảy ra |
|------|---------|----------------|
| SHOWTIME_NOT_FOUND | Suất chiếu không tồn tại | ID không hợp lệ |
| SHOWTIME_TIME_OVERLAP | Suất chiếu bị trùng giờ trong phòng | Trùng thời gian |
| SHOWTIME_INVALID_TIME_RANGE | Thời gian kết thúc phải sau thời gian bắt đầu | endTime <= startTime |
| SHOWTIME_ALREADY_FINISHED | Suất chiếu đã kết thúc | Update showtime CLOSED |
| MOVIE_NOT_FOUND | Phim không tồn tại | movieId không hợp lệ |
| ROOM_NOT_FOUND | Phòng chiếu không tồn tại | roomId không hợp lệ |

---

## 🧪 Testing

### 1. Compile & Build
```bash
mvn clean compile
mvn clean install
```

### 2. Run Application
```bash
mvn spring-boot:run
```

### 3. Tạo Test Data
```bash
# Chạy script SQL
psql -U postgres -d cinebook_db -f test-showtime-data.sql
```

### 4. Test API
- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **Postman**: Import file `Showtime-API-Tests.postman_collection.json`

### 5. Test Cases
- ✅ Tạo showtime thành công
- ✅ Validate time range
- ✅ Validate overlap
- ✅ Auto set branchId
- ✅ Search by filters
- ✅ Update và validate
- ✅ Soft delete

---

## 📊 Database Schema

```sql
CREATE TABLE showtimes (
    id UUID PRIMARY KEY,
    movie_id UUID NOT NULL,
    room_id UUID NOT NULL,
    branch_id UUID NOT NULL,
    start_time TIMESTAMP NOT NULL,
    end_time TIMESTAMP NOT NULL,
    base_price DECIMAL(10,2) NOT NULL,
    status VARCHAR(20),
    create_time TIMESTAMP,
    update_time TIMESTAMP,
    create_by UUID,
    update_by UUID,
    del_flag VARCHAR(20),
    delete_time TIMESTAMP,
    delete_by UUID
);
```

---

## 🔗 Tích hợp với các module khác

### Đã tích hợp:
- ✅ **Movies**: Validate movieId, load movie info
- ✅ **Rooms**: Validate roomId, load room info, lấy branchId
- ✅ **Branches**: Load branch info từ branchId

### Sẽ tích hợp (tương lai):
- 🔜 **Bookings**: Check bookings trước khi xóa
- 🔜 **Tickets**: Liên kết với vé đã bán
- 🔜 **Seats**: Quản lý ghế đã đặt cho showtime

---

## 💡 Best Practices đã áp dụng

1. **Separation of Concerns**: DTOs, Service, Repository tách biệt
2. **Validation**: Validate ở nhiều layer (DTO, Service)
3. **Soft Delete**: Không xóa vật lý, dùng del_flag
4. **Nested Response**: Trả về đầy đủ thông tin liên quan
5. **Security**: Role-based access control
6. **Error Handling**: Message codes rõ ràng
7. **Documentation**: Đầy đủ docs, examples, test data
8. **MapStruct**: Auto mapping giảm boilerplate code
9. **Pagination**: Support phân trang cho search
10. **Transaction**: @Transactional cho data consistency

---

## 🚀 Quick Start

### Bước 1: Build
```bash
cd CineBook
mvn clean install
```

### Bước 2: Run
```bash
mvn spring-boot:run
```

### Bước 3: Login để lấy token
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "superadmin",
    "password": "Admin@123"
  }'
```

### Bước 4: Tạo Showtime
```bash
curl -X POST http://localhost:8080/api/showtimes \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -d '{
    "movieId": "movie-uuid",
    "roomId": "room-uuid",
    "startTime": "2024-01-15T14:00:00",
    "endTime": "2024-01-15T16:30:00",
    "price": 100000
  }'
```

### Bước 5: Search Showtimes
```bash
curl -X GET "http://localhost:8080/api/showtimes?date=2024-01-15"
```

---

## 📚 Tài liệu tham khảo

- **Chi tiết API**: Xem file `SHOWTIME_MODULE.md`
- **Checklist**: Xem file `SHOWTIME_CHECKLIST.md`
- **Test Data**: Xem file `test-showtime-data.sql`
- **Postman**: Import file `Showtime-API-Tests.postman_collection.json`
- **Swagger**: http://localhost:8080/swagger-ui.html

---

## ✨ Kết luận

Module Showtimes đã được xây dựng hoàn chỉnh với:
- ✅ 17 files mới
- ✅ Đầy đủ CRUD + validation
- ✅ Security & authorization
- ✅ Search & filter linh hoạt
- ✅ Documentation đầy đủ
- ✅ Test data & Postman collection
- ✅ Best practices

**Sẵn sàng để sử dụng và tích hợp với các module khác!** 🎉
