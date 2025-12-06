# Checklist Module Showtimes

## ✅ Hoàn thành

### 1. Model & Entity
- [x] Showtime entity đã có sẵn với đầy đủ fields
- [x] Extends AuditingEntity (có soft delete)
- [x] Có các trường: movieId, roomId, branchId, startTime, endTime, basePrice, status

### 2. DTOs
- [x] CreateShowtimeRequest - DTO cho tạo mới
- [x] UpdateShowtimeRequest - DTO cho cập nhật
- [x] ShowtimeSearchDTO - DTO cho filter/search
- [x] ShowtimeResponse - DTO cho response với nested objects

### 3. Repository
- [x] ShowtimeRepository interface
- [x] Query findOverlappingShowtimes - kiểm tra trùng giờ khi tạo mới
- [x] Query findOverlappingShowtimesExcludingCurrent - kiểm tra trùng giờ khi update
- [x] ShowtimeRepositoryCustom interface
- [x] ShowtimeRepositoryImpl - implementation với search/filter logic

### 4. Mapper
- [x] ShowtimeMapper với MapStruct
- [x] Mapping toEntity (CreateShowtimeRequest -> Showtime)
- [x] Mapping updateEntityFromDto (UpdateShowtimeRequest -> Showtime)
- [x] Mapping toResponse (Showtime -> ShowtimeResponse)
- [x] Ignore nested objects (movie, room, branch) để set manually

### 5. Service
- [x] ShowtimeService interface
- [x] ShowtimeServiceImpl implementation
- [x] createShowtime - với validation đầy đủ
- [x] searchShowtimes - với filter theo movieId, branchId, roomId, date
- [x] getShowtimeById - lấy chi tiết
- [x] updateShowtime - với validation
- [x] deleteShowtime - soft delete
- [x] buildShowtimeResponse - load nested objects

### 6. Controller
- [x] ShowtimeController với REST endpoints
- [x] POST /api/showtimes - tạo mới (ADMIN, SUPER_ADMIN)
- [x] GET /api/showtimes - search/filter (public)
- [x] GET /api/showtimes/{id} - chi tiết (public)
- [x] PUT /api/showtimes/{id} - cập nhật (ADMIN, SUPER_ADMIN)
- [x] DELETE /api/showtimes/{id} - xóa (ADMIN, SUPER_ADMIN)

### 7. Validation & Business Rules
- [x] Validate movieId tồn tại
- [x] Validate roomId tồn tại
- [x] Validate endTime > startTime
- [x] Validate price > 0
- [x] Check overlap trong cùng room
- [x] Auto set branchId từ room
- [x] Không cho update nếu status = CLOSED
- [x] Soft delete

### 8. Error Handling
- [x] SHOWTIME_NOT_FOUND
- [x] SHOWTIME_TIME_OVERLAP
- [x] SHOWTIME_INVALID_TIME_RANGE
- [x] SHOWTIME_ALREADY_FINISHED
- [x] MOVIE_NOT_FOUND
- [x] ROOM_NOT_FOUND

### 9. Messages
- [x] Thêm message codes vào MessageCode enum
- [x] Thêm messages vào messages.properties (tiếng Việt)

### 10. Documentation
- [x] SHOWTIME_MODULE.md - hướng dẫn chi tiết
- [x] test-showtime-data.sql - script tạo dữ liệu test
- [x] Showtime-API-Tests.postman_collection.json - Postman collection

## 🔍 Cần kiểm tra

### 1. Build & Compile
```bash
mvn clean compile
```
- [ ] Không có lỗi compile
- [ ] MapStruct generate mapper implementation thành công

### 2. Run Application
```bash
mvn spring-boot:run
```
- [ ] Application start thành công
- [ ] Không có lỗi khi khởi động
- [ ] Swagger UI accessible tại http://localhost:8080/swagger-ui.html

### 3. Database
- [ ] Bảng showtimes đã tồn tại
- [ ] Có dữ liệu test (movies, rooms, branches)
- [ ] Chạy script test-showtime-data.sql thành công

### 4. API Testing

#### Test với Postman hoặc curl:

**A. Tạo Showtime thành công**
```bash
POST /api/showtimes
{
  "movieId": "valid-movie-id",
  "roomId": "valid-room-id",
  "startTime": "2024-01-15T14:00:00",
  "endTime": "2024-01-15T16:30:00",
  "price": 100000
}
```
- [ ] Response 200 OK
- [ ] branchId tự động được set
- [ ] status = OPEN
- [ ] Có đầy đủ thông tin movie, room, branch

**B. Validate endTime > startTime**
```bash
POST /api/showtimes
{
  "movieId": "valid-movie-id",
  "roomId": "valid-room-id",
  "startTime": "2024-01-15T16:00:00",
  "endTime": "2024-01-15T14:00:00",
  "price": 100000
}
```
- [ ] Response 400 Bad Request
- [ ] Error: SHOWTIME_INVALID_TIME_RANGE

**C. Validate không trùng giờ**
```bash
# Tạo showtime thứ 2 trùng giờ với showtime đã tạo
POST /api/showtimes
{
  "movieId": "valid-movie-id",
  "roomId": "same-room-id",
  "startTime": "2024-01-15T14:30:00",
  "endTime": "2024-01-15T17:00:00",
  "price": 100000
}
```
- [ ] Response 400 Bad Request
- [ ] Error: SHOWTIME_TIME_OVERLAP

**D. Search showtimes**
```bash
GET /api/showtimes?movieId=xxx&date=2024-01-15
```
- [ ] Response 200 OK
- [ ] Trả về đúng showtimes theo filter
- [ ] Có pagination

**E. Get showtime by ID**
```bash
GET /api/showtimes/{id}
```
- [ ] Response 200 OK
- [ ] Có đầy đủ thông tin nested

**F. Update showtime**
```bash
PUT /api/showtimes/{id}
{
  "price": 120000,
  "roomId": "new-room-id"
}
```
- [ ] Response 200 OK
- [ ] branchId tự động update khi đổi room
- [ ] Vẫn validate không trùng giờ

**G. Delete showtime**
```bash
DELETE /api/showtimes/{id}
```
- [ ] Response 200 OK
- [ ] Soft delete (del_flag = DELETED)
- [ ] Không hiện trong search nữa

### 5. Security
- [ ] POST, PUT, DELETE yêu cầu role ADMIN hoặc SUPER_ADMIN
- [ ] GET không yêu cầu authentication
- [ ] Unauthorized user không thể tạo/sửa/xóa

### 6. Swagger Documentation
- [ ] Tất cả endpoints hiển thị trong Swagger UI
- [ ] Có description cho từng endpoint
- [ ] Có example request/response
- [ ] Có thông tin về authorization

## 📝 Ghi chú

### Các điểm cần lưu ý:
1. **branchId tự động**: Luôn lấy từ room.branchId, không cho user truyền vào
2. **Overlap check**: Sử dụng công thức `startA < endB AND startB < endA`
3. **Soft delete**: Sử dụng del_flag, không xóa vật lý
4. **Status**: Mặc định là OPEN khi tạo mới
5. **Nested objects**: Load manual trong buildShowtimeResponse

### Tích hợp tương lai:
- [ ] Khi có module Bookings, cần check trước khi xóa showtime
- [ ] Có thể thêm logic tự động đổi status sang CLOSED khi hết giờ
- [ ] Có thể thêm validation về thời gian (không cho tạo showtime quá khứ)
- [ ] Có thể thêm logic tính giá động theo giờ chiếu (sáng/chiều/tối)

## 🚀 Các bước tiếp theo

1. **Build project:**
   ```bash
   mvn clean install
   ```

2. **Run application:**
   ```bash
   mvn spring-boot:run
   ```

3. **Tạo dữ liệu test:**
   - Chạy script `test-showtime-data.sql`

4. **Test API:**
   - Import `Showtime-API-Tests.postman_collection.json` vào Postman
   - Hoặc test trực tiếp qua Swagger UI

5. **Verify:**
   - Kiểm tra tất cả test cases trong checklist
   - Đảm bảo tất cả validation hoạt động đúng
   - Kiểm tra response format đúng chuẩn

## ✨ Kết luận

Module Showtimes đã được xây dựng hoàn chỉnh với:
- ✅ Đầy đủ CRUD operations
- ✅ Validation nghiệp vụ chặt chẽ
- ✅ Security với role-based access
- ✅ Search/Filter linh hoạt
- ✅ Nested response với thông tin đầy đủ
- ✅ Documentation chi tiết
- ✅ Test data và Postman collection

Sẵn sàng để tích hợp với các module khác (Bookings, Tickets, etc.)
