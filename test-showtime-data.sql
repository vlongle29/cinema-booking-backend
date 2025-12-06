-- Script để tạo dữ liệu test cho module Showtimes
-- Chạy script này sau khi đã có dữ liệu Branch, Room, và Movie

-- =====================================================
-- 1. Kiểm tra dữ liệu hiện có
-- =====================================================

-- Kiểm tra Branches
SELECT id, name, address FROM branches WHERE del_flag = 'ACTIVE' LIMIT 5;

-- Kiểm tra Rooms
SELECT id, name, branch_id, capacity FROM rooms WHERE del_flag = 'ACTIVE' LIMIT 5;

-- Kiểm tra Movies
SELECT id, title, duration_minutes, status FROM movies WHERE del_flag = 'ACTIVE' LIMIT 5;

-- =====================================================
-- 2. Tạo dữ liệu test (nếu chưa có)
-- =====================================================

-- Tạo Branch test (nếu chưa có)
INSERT INTO branches (id, name, address, city, create_time, update_time, del_flag)
VALUES 
    (gen_random_uuid(), 'CGV Vincom Center', '72 Lê Thánh Tôn, Q1', 'Hồ Chí Minh', NOW(), NOW(), 'ACTIVE'),
    (gen_random_uuid(), 'CGV Aeon Mall', '30 Bờ Bao Tân Thắng, Q.Tân Phú', 'Hồ Chí Minh', NOW(), NOW(), 'ACTIVE')
ON CONFLICT DO NOTHING;

-- Lấy branch_id để tạo rooms
DO $$
DECLARE
    branch_id_1 UUID;
    branch_id_2 UUID;
    movie_id_1 UUID;
    movie_id_2 UUID;
    room_id_1 UUID;
    room_id_2 UUID;
BEGIN
    -- Lấy branch IDs
    SELECT id INTO branch_id_1 FROM branches WHERE name = 'CGV Vincom Center' LIMIT 1;
    SELECT id INTO branch_id_2 FROM branches WHERE name = 'CGV Aeon Mall' LIMIT 1;
    
    -- Tạo Rooms
    INSERT INTO rooms (id, branch_id, name, capacity, type, create_time, update_time, del_flag)
    VALUES 
        (gen_random_uuid(), branch_id_1, 'Phòng 1', 100, 'STANDARD_2D', NOW(), NOW(), 'ACTIVE'),
        (gen_random_uuid(), branch_id_1, 'Phòng 2', 150, 'IMAX', NOW(), NOW(), 'ACTIVE'),
        (gen_random_uuid(), branch_id_2, 'Phòng 1', 120, 'STANDARD_3D', NOW(), NOW(), 'ACTIVE')
    ON CONFLICT DO NOTHING;
    
    -- Tạo Movies
    INSERT INTO movies (id, title, description, director, duration_minutes, release_date, status, create_time, update_time, del_flag)
    VALUES 
        (gen_random_uuid(), 'Avatar: The Way of Water', 'Phần tiếp theo của Avatar', 'James Cameron', 192, '2024-01-15', 'NOW_SHOWING', NOW(), NOW(), 'ACTIVE'),
        (gen_random_uuid(), 'Oppenheimer', 'Câu chuyện về cha đẻ bom nguyên tử', 'Christopher Nolan', 180, '2024-01-20', 'NOW_SHOWING', NOW(), NOW(), 'ACTIVE')
    ON CONFLICT DO NOTHING;
    
    -- Lấy IDs để tạo showtimes
    SELECT id INTO room_id_1 FROM rooms WHERE name = 'Phòng 1' AND branch_id = branch_id_1 LIMIT 1;
    SELECT id INTO room_id_2 FROM rooms WHERE name = 'Phòng 2' AND branch_id = branch_id_1 LIMIT 1;
    SELECT id INTO movie_id_1 FROM movies WHERE title = 'Avatar: The Way of Water' LIMIT 1;
    SELECT id INTO movie_id_2 FROM movies WHERE title = 'Oppenheimer' LIMIT 1;
    
    -- Tạo Showtimes
    INSERT INTO showtimes (id, movie_id, room_id, branch_id, start_time, end_time, base_price, status, create_time, update_time, del_flag)
    VALUES 
        -- Avatar - Phòng 1 - Sáng
        (gen_random_uuid(), movie_id_1, room_id_1, branch_id_1, 
         '2024-01-15 09:00:00', '2024-01-15 12:12:00', 100000, 'OPEN', NOW(), NOW(), 'ACTIVE'),
        
        -- Avatar - Phòng 1 - Chiều
        (gen_random_uuid(), movie_id_1, room_id_1, branch_id_1, 
         '2024-01-15 14:00:00', '2024-01-15 17:12:00', 120000, 'OPEN', NOW(), NOW(), 'ACTIVE'),
        
        -- Avatar - Phòng 1 - Tối
        (gen_random_uuid(), movie_id_1, room_id_1, branch_id_1, 
         '2024-01-15 19:00:00', '2024-01-15 22:12:00', 150000, 'OPEN', NOW(), NOW(), 'ACTIVE'),
        
        -- Oppenheimer - Phòng 2 - Sáng
        (gen_random_uuid(), movie_id_2, room_id_2, branch_id_1, 
         '2024-01-15 10:00:00', '2024-01-15 13:00:00', 130000, 'OPEN', NOW(), NOW(), 'ACTIVE'),
        
        -- Oppenheimer - Phòng 2 - Chiều
        (gen_random_uuid(), movie_id_2, room_id_2, branch_id_1, 
         '2024-01-15 15:00:00', '2024-01-15 18:00:00', 150000, 'OPEN', NOW(), NOW(), 'ACTIVE')
    ON CONFLICT DO NOTHING;
    
END $$;

-- =====================================================
-- 3. Kiểm tra dữ liệu đã tạo
-- =====================================================

-- Xem tất cả showtimes với thông tin đầy đủ
SELECT 
    s.id,
    m.title as movie_title,
    r.name as room_name,
    b.name as branch_name,
    s.start_time,
    s.end_time,
    s.base_price,
    s.status
FROM showtimes s
JOIN movies m ON s.movie_id = m.id
JOIN rooms r ON s.room_id = r.id
JOIN branches b ON s.branch_id = b.id
WHERE s.del_flag = 'ACTIVE'
ORDER BY s.start_time;

-- =====================================================
-- 4. Test queries
-- =====================================================

-- Tìm showtimes theo movie
SELECT s.*, m.title 
FROM showtimes s 
JOIN movies m ON s.movie_id = m.id 
WHERE m.title LIKE '%Avatar%' AND s.del_flag = 'ACTIVE';

-- Tìm showtimes theo branch
SELECT s.*, b.name 
FROM showtimes s 
JOIN branches b ON s.branch_id = b.id 
WHERE b.name LIKE '%Vincom%' AND s.del_flag = 'ACTIVE';

-- Tìm showtimes theo ngày
SELECT * FROM showtimes 
WHERE DATE(start_time) = '2024-01-15' 
AND del_flag = 'ACTIVE'
ORDER BY start_time;

-- Kiểm tra overlap (không nên có kết quả)
SELECT 
    s1.id as showtime1_id,
    s1.start_time as start1,
    s1.end_time as end1,
    s2.id as showtime2_id,
    s2.start_time as start2,
    s2.end_time as end2
FROM showtimes s1
JOIN showtimes s2 ON s1.room_id = s2.room_id AND s1.id != s2.id
WHERE s1.del_flag = 'ACTIVE' 
  AND s2.del_flag = 'ACTIVE'
  AND s1.start_time < s2.end_time 
  AND s2.start_time < s1.end_time;
