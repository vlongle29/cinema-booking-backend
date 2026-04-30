-- Migration: Add performance indexes for Movie Ticket Booking System
-- Version: V7
-- Description: Add indexes to optimize query performance

-- Users table indexes
CREATE INDEX IF NOT EXISTS idx_users_username ON sys_user(username);
CREATE INDEX IF NOT EXISTS idx_users_email ON sys_user(email);
CREATE INDEX IF NOT EXISTS idx_users_phone ON sys_user(phone);
CREATE INDEX IF NOT EXISTS idx_users_create_time ON sys_user(create_time);

-- Movies table indexes
CREATE INDEX IF NOT EXISTS idx_movies_title ON movies(title);
CREATE INDEX IF NOT EXISTS idx_movies_release_date ON movies(release_date);
CREATE INDEX IF NOT EXISTS idx_movies_status_release_date ON movies(status, release_date);

-- Cinemas table indexes
CREATE INDEX IF NOT EXISTS idx_cinemas_city_id ON branches(city_id);
CREATE INDEX IF NOT EXISTS idx_cinemas_name ON branches(name);

-- =================================================
-- BẢNG SHOWTIMES
-- =================================================
CREATE INDEX IF NOT EXISTS idx_showtimes_start_time ON showtimes(start_time);
-- 1. Tìm suất chiếu theo Phim + Thời gian (Luồng chính user mua vé)
-- Index này hỗ trợ cả: WHERE movie_id = ? VÀ WHERE movie_id = ? AND start_time = ?
CREATE INDEX IF NOT EXISTS idx_showtimes_movie_date_time ON showtimes(movie_id, start_time);
-- 2. Tìm suất chiếu theo Rạp + Thời gian (Luồng lọc theo rạp)
-- Index này hỗ trợ cả: WHERE branch_id = ?
CREATE INDEX IF NOT EXISTS idx_showtimes_branch_start ON showtimes(branch_id, start_time);
-- 3. Check trùng lịch phòng (Luồng Admin tạo suất chiếu)
-- Khi tạo suất chiếu mới, cần check xem phòng đó giờ đó có ai đặt chưa
CREATE INDEX IF NOT EXISTS idx_showtimes_room_start ON showtimes(room_id, start_time);


-- =================================================
-- BẢNG SEATS (Lưu ý: Giả sử đây là bảng trạng thái ghế theo suất chiếu)
-- =================================================
-- 1. Lấy sơ đồ ghế của một suất chiếu (Luồng user chọn ghế)
-- Cover luôn việc đếm số ghế trống: WHERE showtime_id = ? AND status = 'AVAILABLE'
CREATE INDEX IF NOT EXISTS idx_seats_room_id ON seats(room_id);
-- 2. Join với bảng Room (như câu hỏi trước của bạn)
CREATE INDEX IF NOT EXISTS idx_seats_room_id
ON seats(room_id);

-- Bookings table indexes
CREATE INDEX IF NOT EXISTS idx_bookings_showtime_id ON bookings(showtime_id);
CREATE INDEX IF NOT EXISTS idx_bookings_status ON bookings(status);
CREATE INDEX IF NOT EXISTS idx_bookings_create_by ON bookings(create_by, create_time DESC);
--CREATE INDEX IF NOT EXISTS idx_bookings_payment_status ON bookings(payment_status);

---- Payments table indexes (if exists)
--CREATE INDEX IF NOT EXISTS idx_payments_booking_id ON payments(booking_id);
--CREATE INDEX IF NOT EXISTS idx_payments_transaction_id ON payments(transaction_id);
--CREATE INDEX IF NOT EXISTS idx_payments_status ON payments(status);
--CREATE INDEX IF NOT EXISTS idx_payments_create_time ON payments(create_time);

-- Tickets table indexes (if exists)
CREATE INDEX IF NOT EXISTS idx_tickets_booking_id ON tickets(booking_id);
CREATE INDEX IF NOT EXISTS idx_tickets_create_by ON tickets(create_by);
CREATE INDEX IF NOT EXISTS idx_tickets_code ON tickets(ticket_code);

-- Reviews table indexes (if exists)
--CREATE INDEX IF NOT EXISTS idx_reviews_movie_id ON reviews(movie_id);
--CREATE INDEX IF NOT EXISTS idx_reviews_create_by ON reviews(create_by);
--CREATE INDEX IF NOT EXISTS idx_reviews_rating ON reviews(rating);
