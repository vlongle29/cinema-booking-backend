# Module Showtimes - Hướng dẫn sử dụng

## Tổng quan
Module Showtimes quản lý các suất chiếu phim trong hệ thống CineBook. Mỗi suất chiếu liên kết với một phim (Movie), một phòng chiếu (Room), và tự động xác định chi nhánh (Branch) dựa trên phòng chiếu.

## Cấu trúc Database

### Bảng: showtimes
- `id` (UUID): Primary key
- `movie_id` (UUID): Foreign key đến bảng movies
- `room_id` (UUID): Foreign key đến bảng rooms
- `branch_id` (UUID): Foreign key đến bảng branches (tự động lấy từ room)
- `start_time` (TIMESTAMP): Thời gian bắt đầu
- `end_time` (TIMESTAMP): Thời gian kết thúc
- `base_price` (DECIMAL): Giá vé cơ bản
- `status` (VARCHAR): Trạng thái (OPEN, CLOSED, CANCELLED)
- Các trường audit: create_time, update_time, create_by, update_by, del_flag

## API Endpoints

### 1. Tạo suất chiếu mới
**POST** `/api/showtimes`

**Quyền:** ADMIN, SUPER_ADMIN

**Request Body:**
```json
{
  "movieId": "uuid",
  "roomId": "uuid",
  "startTime": "2024-01-15T14:00:00",
  "endTime": "2024-01-15T16:30:00",
  "price": 100000
}
```

**Validation:**
- movieId: Bắt buộc, phải tồn tại trong database
- roomId: Bắt buộc, phải tồn tại trong database
- startTime: Bắt buộc
- endTime: Bắt buộc, phải sau startTime
- price: Bắt buộc, phải > 0
- Không được trùng giờ với suất chiếu khác trong cùng phòng

**Response:**
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
    "movie": {
      "id": "uuid",
      "title": "Tên phim",
      "durationMinutes": 150,
      ...
    },
    "room": {
      "id": "uuid",
      "name": "Phòng 1",
      "totalSeats": 100,
      ...
    },
    "branch": {
      "id": "uuid",
      "name": "CGV Vincom",
      "address": "123 Nguyễn Huệ",
      ...
    }
  },
  "message": "success"
}
```

### 2. Tìm kiếm suất chiếu
**GET** `/api/showtimes`

**Quyền:** Tất cả (không cần đăng nhập)

**Query Parameters:**
- `movieId` (UUID, optional): Lọc theo phim
- `branchId` (UUID, optional): Lọc theo chi nhánh
- `roomId` (UUID, optional): Lọc theo phòng
- `date` (LocalDate, optional): Lọc theo ngày (format: yyyy-MM-dd)
- `page` (Integer, default: 0): Số trang
- `size` (Integer, default: 10): Kích thước trang

**Ví dụ:**
```
GET /api/showtimes?movieId=xxx&date=2024-01-15&page=0&size=10
```

**Response:**
```json
{
  "success": true,
  "data": {
    "content": [...],
    "page": 0,
    "size": 10,
    "totalElements": 50,
    "totalPages": 5
  },
  "message": "success"
}
```

### 3. Xem chi tiết suất chiếu
**GET** `/api/showtimes/{id}`

**Quyền:** Tất cả

**Response:** Giống như response của POST

### 4. Cập nhật suất chiếu
**PUT** `/api/showtimes/{id}`

**Quyền:** ADMIN, SUPER_ADMIN

**Request Body:**
```json
{
  "movieId": "uuid",
  "roomId": "uuid",
  "startTime": "2024-01-15T14:00:00",
  "endTime": "2024-01-15T16:30:00",
  "price": 120000,
  "status": "OPEN"
}
```

**Lưu ý:**
- Tất cả các field đều optional
- Không thể update nếu status = CLOSED
- Nếu đổi roomId, branchId sẽ tự động cập nhật
- Vẫn phải validate không trùng giờ

### 5. Xóa suất chiếu
**DELETE** `/api/showtimes/{id}`

**Quyền:** ADMIN, SUPER_ADMIN

**Lưu ý:**
- Thực hiện soft delete (đánh dấu del_flag = DELETED)

## Business Rules

### 1. Kiểm tra trùng giờ
Hai suất chiếu được coi là trùng giờ nếu:
```
startA < endB AND startB < endA
```

### 2. Tự động lấy branchId
Khi tạo hoặc update showtime với roomId mới:
```java
Room room = roomRepository.findById(roomId);
showtime.setBranchId(room.getBranchId());
```

### 3. Trạng thái Showtime
- **OPEN**: Suất chiếu đang mở bán vé
- **CLOSED**: Suất chiếu đã kết thúc
- **CANCELLED**: Suất chiếu bị hủy

### 4. Validation Rules
- endTime > startTime
- price > 0
- movieId phải tồn tại và chưa bị xóa
- roomId phải tồn tại và chưa bị xóa
- Không trùng giờ trong cùng phòng

## Error Codes

| Code | Message | Mô tả |
|------|---------|-------|
| SHOWTIME_NOT_FOUND | Suất chiếu không tồn tại | Không tìm thấy showtime với ID |
| SHOWTIME_TIME_OVERLAP | Suất chiếu bị trùng giờ trong phòng | Có suất chiếu khác trùng thời gian |
| SHOWTIME_INVALID_TIME_RANGE | Thời gian kết thúc phải sau thời gian bắt đầu | endTime <= startTime |
| SHOWTIME_ALREADY_FINISHED | Suất chiếu đã kết thúc | Không thể update showtime có status = CLOSED |
| MOVIE_NOT_FOUND | Phim không tồn tại | movieId không hợp lệ |
| ROOM_NOT_FOUND | Phòng chiếu không tồn tại | roomId không hợp lệ |

## Ví dụ sử dụng

### Tạo suất chiếu cho phim Avatar vào ngày 15/01/2024
```bash
curl -X POST http://localhost:8080/api/showtimes \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer {token}" \
  -d '{
    "movieId": "movie-uuid",
    "roomId": "room-uuid",
    "startTime": "2024-01-15T14:00:00",
    "endTime": "2024-01-15T16:30:00",
    "price": 100000
  }'
```

### Tìm tất cả suất chiếu của một phim trong ngày
```bash
curl -X GET "http://localhost:8080/api/showtimes?movieId=movie-uuid&date=2024-01-15"
```

### Tìm tất cả suất chiếu tại một chi nhánh
```bash
curl -X GET "http://localhost:8080/api/showtimes?branchId=branch-uuid"
```

## Testing

Để test module này, bạn cần:

1. Tạo dữ liệu test:
   - Ít nhất 1 Movie
   - Ít nhất 1 Branch
   - Ít nhất 1 Room thuộc Branch đó

2. Test cases cần kiểm tra:
   - ✅ Tạo showtime thành công
   - ✅ Không cho tạo showtime với endTime < startTime
   - ✅ Không cho tạo showtime trùng giờ trong cùng phòng
   - ✅ branchId tự động lấy từ room
   - ✅ Tìm kiếm theo movieId, branchId, roomId, date
   - ✅ Update showtime và branchId tự động cập nhật khi đổi room
   - ✅ Không cho update showtime đã CLOSED
   - ✅ Xóa showtime (soft delete)

## Tích hợp với các module khác

### Movies Module
- Showtime cần movieId hợp lệ
- Khi xóa Movie, cần xử lý các Showtime liên quan

### Rooms Module
- Showtime cần roomId hợp lệ
- branchId tự động lấy từ Room

### Branches Module
- branchId được lấy gián tiếp qua Room
- Dùng để filter showtimes theo chi nhánh

### Bookings Module (tương lai)
- Khi có Booking, cần kiểm tra trước khi xóa Showtime
- Có thể chuyển status sang CANCELLED thay vì xóa
