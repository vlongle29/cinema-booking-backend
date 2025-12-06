# 🎬 Module Showtimes - Quick Guide

## 📁 Files Created

### Core Files (11 files)
- ✅ 4 DTOs: `CreateShowtimeRequest`, `UpdateShowtimeRequest`, `ShowtimeSearchDTO`, `ShowtimeResponse`
- ✅ 3 Repositories: `ShowtimeRepository`, `ShowtimeRepositoryCustom`, `ShowtimeRepositoryImpl`
- ✅ 1 Mapper: `ShowtimeMapper`
- ✅ 2 Services: `ShowtimeService`, `ShowtimeServiceImpl`
- ✅ 1 Controller: `ShowtimeController`

### Documentation (4 files)
- 📖 `SHOWTIME_MODULE.md` - Chi tiết đầy đủ
- 📋 `SHOWTIME_CHECKLIST.md` - Checklist kiểm tra
- 📊 `SHOWTIME_SUMMARY.md` - Tóm tắt module
- 🚀 `README_SHOWTIME.md` - File này

### Testing (2 files)
- 🗃️ `test-showtime-data.sql` - SQL test data
- 📮 `Showtime-API-Tests.postman_collection.json` - Postman collection

---

## 🚀 Quick Start

### 1. Build Project
```bash
mvn clean install
```

### 2. Run Application
```bash
mvn spring-boot:run
```

### 3. Access Swagger
```
http://localhost:8080/swagger-ui.html
```

### 4. Test API
Import Postman collection hoặc dùng Swagger UI

---

## 📌 API Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/showtimes` | Tạo suất chiếu (ADMIN) |
| GET | `/api/showtimes` | Search/Filter (Public) |
| GET | `/api/showtimes/{id}` | Chi tiết (Public) |
| PUT | `/api/showtimes/{id}` | Cập nhật (ADMIN) |
| DELETE | `/api/showtimes/{id}` | Xóa (ADMIN) |

---

## ✨ Key Features

1. **Auto branchId** - Tự động lấy từ room
2. **Overlap validation** - Không trùng giờ trong phòng
3. **Time validation** - endTime > startTime
4. **Nested response** - Trả về movie, room, branch info
5. **Soft delete** - Không xóa vật lý
6. **Search & Filter** - Theo movie, branch, room, date
7. **Pagination** - Support phân trang
8. **Security** - Role-based access

---

## 📖 Documentation

- **Full Guide**: `SHOWTIME_MODULE.md`
- **Summary**: `SHOWTIME_SUMMARY.md`
- **Checklist**: `SHOWTIME_CHECKLIST.md`

---

## ✅ Status

**Module Showtimes: HOÀN THÀNH** 🎉

Sẵn sàng để:
- ✅ Build & Run
- ✅ Test API
- ✅ Tích hợp với module khác
