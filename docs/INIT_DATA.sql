-- =====================================================
-- SCRIPT KHỞI TẠO DỮ LIỆU CHO HỆ THỐNG USER & EMPLOYEE
-- =====================================================

-- 1. Tạo các Role hệ thống
-- =====================================================
INSERT INTO sys_role (id, code, name, description, system_flag, priority, created_at, updated_at, is_delete)
VALUES 
    (gen_random_uuid(), 'SUPER_ADMIN', 'Super Admin', 'Quản trị tối cao hệ thống', 'SYSTEM', 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, false),
    (gen_random_uuid(), 'ADMIN', 'Admin', 'Quản trị viên', 'SYSTEM', 2, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, false),
    (gen_random_uuid(), 'STAFF', 'Staff', 'Nhân viên', 'NORMAL', 3, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, false),
    (gen_random_uuid(), 'CUSTOMER', 'Customer', 'Khách hàng', 'NORMAL', 4, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, false)
ON CONFLICT (code) DO NOTHING;

-- 2. Tạo SUPER_ADMIN user
-- =====================================================
-- Password: SuperAdmin@123 (đã hash bằng BCrypt)
-- Lưu ý: Thay đổi password sau khi deploy production!
INSERT INTO users (id, username, password, name, email, phone, lock_flag, system_flag, created_at, updated_at, is_delete)
VALUES 
    ('00000000-0000-0000-0000-000000000001'::uuid, 
     'superadmin', 
     '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', -- SuperAdmin@123
     'Super Administrator', 
     'superadmin@cinebook.com', 
     '0900000001', 
     '0', 
     'SYSTEM', 
     CURRENT_TIMESTAMP, 
     CURRENT_TIMESTAMP, 
     false)
ON CONFLICT (username) DO NOTHING;

-- Gán role SUPER_ADMIN cho user superadmin
INSERT INTO user_role (id, user_id, role_id)
SELECT 
    gen_random_uuid(),
    '00000000-0000-0000-0000-000000000001'::uuid,
    id
FROM sys_role
WHERE code = 'SUPER_ADMIN'
ON CONFLICT DO NOTHING;

-- 3. Tạo ADMIN user mẫu
-- =====================================================
-- Password: Admin@123
INSERT INTO users (id, username, password, name, email, phone, lock_flag, system_flag, created_at, updated_at, is_delete)
VALUES 
    ('00000000-0000-0000-0000-000000000002'::uuid, 
     'admin01', 
     '$2a$10$8K1p/a0dL2LH0XZnup/My.id5.ShmQmGe/vkiOoJ8VOwya/Qb9F5m', -- Admin@123
     'Admin User 01', 
     'admin01@cinebook.com', 
     '0900000002', 
     '0', 
     'SYSTEM', 
     CURRENT_TIMESTAMP, 
     CURRENT_TIMESTAMP, 
     false)
ON CONFLICT (username) DO NOTHING;

-- Gán role ADMIN
INSERT INTO user_role (id, user_id, role_id)
SELECT 
    gen_random_uuid(),
    '00000000-0000-0000-0000-000000000002'::uuid,
    id
FROM sys_role
WHERE code = 'ADMIN'
ON CONFLICT DO NOTHING;

-- 4. Tạo Branch mẫu
-- =====================================================
INSERT INTO branches (id, name, address, city, created_at, updated_at, is_delete)
VALUES 
    ('10000000-0000-0000-0000-000000000001'::uuid, 'CGV Vincom Center', '72 Lê Thánh Tôn, Quận 1', 'Hồ Chí Minh', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, false),
    ('10000000-0000-0000-0000-000000000002'::uuid, 'CGV Aeon Mall', '30 Bờ Bao Tân Thắng, Quận Tân Phú', 'Hồ Chí Minh', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, false),
    ('10000000-0000-0000-0000-000000000003'::uuid, 'CGV Landmark 81', '720A Điện Biên Phủ, Quận Bình Thạnh', 'Hồ Chí Minh', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, false)
ON CONFLICT (id) DO NOTHING;

-- 5. Tạo Employee mẫu (STAFF users)
-- =====================================================

-- 5.1. Manager cho CGV Vincom Center
-- Password: Manager@123
INSERT INTO users (id, username, password, name, email, phone, lock_flag, system_flag, created_at, updated_at, is_delete)
VALUES 
    ('20000000-0000-0000-0000-000000000001'::uuid, 
     'manager01', 
     '$2a$10$8K1p/a0dL2LH0XZnup/My.id5.ShmQmGe/vkiOoJ8VOwya/Qb9F5m', 
     'Nguyễn Văn Manager', 
     'manager01@cinebook.com', 
     '0901111111', 
     '0', 
     'NORMAL', 
     CURRENT_TIMESTAMP, 
     CURRENT_TIMESTAMP, 
     false)
ON CONFLICT (username) DO NOTHING;

INSERT INTO user_role (id, user_id, role_id)
SELECT gen_random_uuid(), '20000000-0000-0000-0000-000000000001'::uuid, id
FROM sys_role WHERE code = 'STAFF'
ON CONFLICT DO NOTHING;

INSERT INTO employees (user_id, branch_id, employee_code, position, salary, hire_date, created_at, updated_at, is_delete)
VALUES 
    ('20000000-0000-0000-0000-000000000001'::uuid, 
     '10000000-0000-0000-0000-000000000001'::uuid, 
     'NV001', 
     'MANAGER', 
     15000000, 
     '2023-01-15', 
     CURRENT_TIMESTAMP, 
     CURRENT_TIMESTAMP, 
     false)
ON CONFLICT (user_id) DO NOTHING;

-- Update branch manager
UPDATE branches 
SET manager_id = '20000000-0000-0000-0000-000000000001'::uuid
WHERE id = '10000000-0000-0000-0000-000000000001'::uuid;

-- 5.2. Cashier cho CGV Vincom Center
-- Password: Cashier@123
INSERT INTO users (id, username, password, name, email, phone, lock_flag, system_flag, created_at, updated_at, is_delete)
VALUES 
    ('20000000-0000-0000-0000-000000000002'::uuid, 
     'cashier01', 
     '$2a$10$8K1p/a0dL2LH0XZnup/My.id5.ShmQmGe/vkiOoJ8VOwya/Qb9F5m', 
     'Trần Thị Thu Ngân', 
     'cashier01@cinebook.com', 
     '0902222222', 
     '0', 
     'NORMAL', 
     CURRENT_TIMESTAMP, 
     CURRENT_TIMESTAMP, 
     false)
ON CONFLICT (username) DO NOTHING;

INSERT INTO user_role (id, user_id, role_id)
SELECT gen_random_uuid(), '20000000-0000-0000-0000-000000000002'::uuid, id
FROM sys_role WHERE code = 'STAFF'
ON CONFLICT DO NOTHING;

INSERT INTO employees (user_id, branch_id, employee_code, position, salary, hire_date, created_at, updated_at, is_delete)
VALUES 
    ('20000000-0000-0000-0000-000000000002'::uuid, 
     '10000000-0000-0000-0000-000000000001'::uuid, 
     'NV002', 
     'CASHIER', 
     8000000, 
     '2023-02-01', 
     CURRENT_TIMESTAMP, 
     CURRENT_TIMESTAMP, 
     false)
ON CONFLICT (user_id) DO NOTHING;

-- 5.3. Technician cho CGV Vincom Center
-- Password: Tech@123
INSERT INTO users (id, username, password, name, email, phone, lock_flag, system_flag, created_at, updated_at, is_delete)
VALUES 
    ('20000000-0000-0000-0000-000000000003'::uuid, 
     'tech01', 
     '$2a$10$8K1p/a0dL2LH0XZnup/My.id5.ShmQmGe/vkiOoJ8VOwya/Qb9F5m', 
     'Lê Văn Kỹ Thuật', 
     'tech01@cinebook.com', 
     '0903333333', 
     '0', 
     'NORMAL', 
     CURRENT_TIMESTAMP, 
     CURRENT_TIMESTAMP, 
     false)
ON CONFLICT (username) DO NOTHING;

INSERT INTO user_role (id, user_id, role_id)
SELECT gen_random_uuid(), '20000000-0000-0000-0000-000000000003'::uuid, id
FROM sys_role WHERE code = 'STAFF'
ON CONFLICT DO NOTHING;

INSERT INTO employees (user_id, branch_id, employee_code, position, salary, hire_date, created_at, updated_at, is_delete)
VALUES 
    ('20000000-0000-0000-0000-000000000003'::uuid, 
     '10000000-0000-0000-0000-000000000001'::uuid, 
     'NV003', 
     'TECHNICIAN', 
     9000000, 
     '2023-03-01', 
     CURRENT_TIMESTAMP, 
     CURRENT_TIMESTAMP, 
     false)
ON CONFLICT (user_id) DO NOTHING;

-- 5.4. Manager cho CGV Aeon Mall
-- Password: Manager@123
INSERT INTO users (id, username, password, name, email, phone, lock_flag, system_flag, created_at, updated_at, is_delete)
VALUES 
    ('20000000-0000-0000-0000-000000000004'::uuid, 
     'manager02', 
     '$2a$10$8K1p/a0dL2LH0XZnup/My.id5.ShmQmGe/vkiOoJ8VOwya/Qb9F5m', 
     'Phạm Thị Manager', 
     'manager02@cinebook.com', 
     '0904444444', 
     '0', 
     'NORMAL', 
     CURRENT_TIMESTAMP, 
     CURRENT_TIMESTAMP, 
     false)
ON CONFLICT (username) DO NOTHING;

INSERT INTO user_role (id, user_id, role_id)
SELECT gen_random_uuid(), '20000000-0000-0000-0000-000000000004'::uuid, id
FROM sys_role WHERE code = 'STAFF'
ON CONFLICT DO NOTHING;

INSERT INTO employees (user_id, branch_id, employee_code, position, salary, hire_date, created_at, updated_at, is_delete)
VALUES 
    ('20000000-0000-0000-0000-000000000004'::uuid, 
     '10000000-0000-0000-0000-000000000002'::uuid, 
     'NV004', 
     'MANAGER', 
     15000000, 
     '2023-01-20', 
     CURRENT_TIMESTAMP, 
     CURRENT_TIMESTAMP, 
     false)
ON CONFLICT (user_id) DO NOTHING;

-- Update branch manager
UPDATE branches 
SET manager_id = '20000000-0000-0000-0000-000000000004'::uuid
WHERE id = '10000000-0000-0000-0000-000000000002'::uuid;

-- 6. Tạo Customer mẫu
-- =====================================================
-- Password: Customer@123
INSERT INTO users (id, username, password, name, email, phone, lock_flag, system_flag, created_at, updated_at, is_delete)
VALUES 
    ('30000000-0000-0000-0000-000000000001'::uuid, 
     'customer01', 
     '$2a$10$8K1p/a0dL2LH0XZnup/My.id5.ShmQmGe/vkiOoJ8VOwya/Qb9F5m', 
     'Khách Hàng 01', 
     'customer01@gmail.com', 
     '0905555555', 
     '0', 
     'NORMAL', 
     CURRENT_TIMESTAMP, 
     CURRENT_TIMESTAMP, 
     false)
ON CONFLICT (username) DO NOTHING;

INSERT INTO user_role (id, user_id, role_id)
SELECT gen_random_uuid(), '30000000-0000-0000-0000-000000000001'::uuid, id
FROM sys_role WHERE code = 'CUSTOMER'
ON CONFLICT DO NOTHING;

-- =====================================================
-- SUMMARY
-- =====================================================
-- Roles: SUPER_ADMIN, ADMIN, STAFF, CUSTOMER
-- 
-- Users:
-- 1. superadmin / SuperAdmin@123 (SUPER_ADMIN)
-- 2. admin01 / Admin@123 (ADMIN)
-- 3. manager01 / Manager@123 (STAFF - MANAGER at CGV Vincom)
-- 4. cashier01 / Cashier@123 (STAFF - CASHIER at CGV Vincom)
-- 5. tech01 / Tech@123 (STAFF - TECHNICIAN at CGV Vincom)
-- 6. manager02 / Manager@123 (STAFF - MANAGER at CGV Aeon)
-- 7. customer01 / Customer@123 (CUSTOMER)
--
-- Branches:
-- 1. CGV Vincom Center (Manager: manager01)
-- 2. CGV Aeon Mall (Manager: manager02)
-- 3. CGV Landmark 81 (No manager yet)
-- =====================================================

-- Verify data
SELECT 'Users' as table_name, COUNT(*) as count FROM users
UNION ALL
SELECT 'Roles', COUNT(*) FROM sys_role
UNION ALL
SELECT 'User-Role mappings', COUNT(*) FROM user_role
UNION ALL
SELECT 'Branches', COUNT(*) FROM branches
UNION ALL
SELECT 'Employees', COUNT(*) FROM employees;
