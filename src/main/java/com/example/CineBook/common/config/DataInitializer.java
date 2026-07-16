package com.example.CineBook.common.config;

import com.example.CineBook.common.constant.LockFlag;
import com.example.CineBook.common.constant.SystemFlag;
import com.example.CineBook.model.*;
import com.example.CineBook.repository.irepository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

import static java.util.stream.Collectors.toList;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final SysUserRepository userRepository;
    private final SysRoleRepository roleRepository;
    private final SysUserRoleRepository userRoleRepository;
    private final SeatTypeRepository seatTypeRepository;
    private final PasswordEncoder passwordEncoder;
    private final SysPermissionRepository permissionRepository;
    private final SysRolePermissionRepository rolePermissionRepository;

    @Override
    public void run(String... args) {
        initSuperAdmin();
        initSeatTypes();
        initPermissions();
    }

    private void initSuperAdmin() {
        if (userRepository.existsByUsername("superadmin")) {
            log.info("Super admin already exists");
            return;
        }

        SysRole superAdminRole = roleRepository.findByCode("SUPER_ADMIN").orElse(null);
        if (superAdminRole == null) {
            superAdminRole = new SysRole();
            superAdminRole.setCode("SUPER_ADMIN");
            superAdminRole.setName("Super Administrator");
            superAdminRole.setDescription("Super administrator with full access");
            superAdminRole.setIsDelete(false);
            superAdminRole = roleRepository.save(superAdminRole);
            log.info("SUPER_ADMIN role created");
        }

        SysUser superAdmin = new SysUser();
        superAdmin.setUsername("superadmin");
        superAdmin.setPassword(passwordEncoder.encode("Admin@123"));
        superAdmin.setName("Super Admin");
        superAdmin.setEmail("superadmin@cinebook.com");
        superAdmin.setPhone("0000000000");
        superAdmin.setTypeAccount("SUPER_ADMIN");
        superAdmin.setSystemFlag(SystemFlag.SYSTEM.getValue());
        superAdmin.setLockFlag(LockFlag.NORMAL.getValue());
        superAdmin.setFailedLoginAttempts(0);
        superAdmin.setIsDelete(false);
        superAdmin = userRepository.save(superAdmin);

        SysUserRole userRole = new SysUserRole();
        userRole.setUserId(superAdmin.getId());
        userRole.setRoleId(superAdminRole.getId());
        userRoleRepository.save(userRole);

        log.info("Super admin created successfully - Username: superadmin, Password: Admin@123");
    }

    private void initPermissions() {
        if (permissionRepository.count() > 0) {
            log.info("Permissions already initialized");
            return;
        }

        String[][] perms = {
            // User
            {"user:view", "Xem người dùng"},
            {"user:create", "Tạo người dùng"},
            {"user:update", "Cập nhật người dùng"},
            {"user:delete", "Xóa người dùng"},
            // Role
            {"role:view", "Xem vai trò"},
            {"role:create", "Tạo vai trò"},
            {"role:update", "Cập nhật vai trò"},
            {"role:delete", "Xóa vai trò"},
            {"role:manage", "Gán quyền cho vai trò"},
            // Permission
            {"permission:view",   "Xem quyền"},
            {"permission:create", "Tạo quyền"},
            {"permission:update", "Cập nhật quyền"},
            {"permission:delete", "Xóa quyền"},
            // Movie
            {"movie:view", "Xem phim"},
            {"movie:create", "Tạo phim"},
            {"movie:update", "Cập nhật phim"},
            {"movie:delete", "Xóa phim"},
            // Branch
            {"branch:view", "Xem chi nhánh"},
            {"branch:create", "Tạo chi nhánh"},
            {"branch:update", "Cập nhật chi nhánh"},
            {"branch:delete", "Xóa chi nhánh"},
            // Room
            {"room:view", "Xem phòng chiếu"},
            {"room:create", "Tạo phòng chiếu"},
            {"room:update", "Cập nhật phòng chiếu"},
            {"room:delete", "Xóa phòng chiếu"},
            // Seat
            {"seat:view", "Xem ghế"},
            {"seat:create", "Tạo ghế"},
            {"seat:delete", "Xóa ghế"},
            // Showtime
            {"showtime:view", "Xem suất chiếu"},
            {"showtime:create", "Tạo suất chiếu"},
            {"showtime:update", "Cập nhật suất chiếu"},
            {"showtime:delete", "Xóa suất chiếu"},
            // Booking
            {"booking:view", "Xem booking"},
            {"booking:cancel", "Hủy booking"},
            {"booking:checkin", "Check-in booking"},
            // Payment
            {"payment:view", "Xem thanh toán"},
            {"payment:refund", "Hoàn tiền"},
            // Promotion
            {"promotion:view", "Xem khuyến mãi"},
            {"promotion:create", "Tạo khuyến mãi"},
            {"promotion:update", "Cập nhật khuyến mãi"},
            {"promotion:delete", "Xóa khuyến mãi"},
            // Employee
            {"employee:view", "Xem nhân viên"},
            {"employee:create", "Tạo nhân viên"},
            {"employee:update", "Cập nhật nhân viên"},
            {"employee:delete", "Xóa nhân viên"},
            // Customer
            {"customer:view", "Xem khách hàng"},
            {"customer:update", "Cập nhật khách hàng"},
            {"customer:delete", "Xóa khách hàng"},
            // Genre, City, Product
            {"genre:manage", "Quản lý thể loại phim"},
            {"city:manage", "Quản lý thành phố"},
            {"product:manage", "Quản lý sản phẩm"},
        };

        for (String[] p : perms) {
            SysPermission permission = SysPermission.builder()
                    .code(p[0])
                    .permission(p[0])
                    .description(p[1])
                    .systemFlag(SystemFlag.SYSTEM.getValue())
                    .build();
            permissionRepository.save(permission);
        }
        log.info("Permissions initialized successfully ({} permissions)", perms.length);

        // Assign all permissions to SUPER_ADMIN
        roleRepository.findByCode("SUPER_ADMIN").ifPresent(superAdminRole -> {
            List<SysRolePermission> allPerms = permissionRepository.findAll()
                            .stream()
                                    .map(permission -> SysRolePermission.builder()
                                    .roleId(superAdminRole.getId())
                                    .permissionId(permission.getId())
                                                    .build()).toList();
            rolePermissionRepository.saveAll(allPerms);
            log.info("All permissions assigned to SUPER_ADMIN role");
        });
    }

    private void initSeatTypes() {
        if (seatTypeRepository.count() > 0) {
            log.info("Seat types already initialized");
            return;
        }

        SeatType standard = SeatType.builder()
                .code("STANDARD")
                .name("Ghế Thường")
                .basePrice(new BigDecimal("45000"))
                .description("Ghế tiêu chuẩn")
                .build();

        SeatType vip = SeatType.builder()
                .code("VIP")
                .name("Ghế VIP")
                .basePrice(new BigDecimal("70000"))
                .description("Ghế VIP cao cấp")
                .build();

        SeatType couple = SeatType.builder()
                .code("COUPLE")
                .name("Ghế Đôi")
                .basePrice(new BigDecimal("150000"))
                .description("Ghế đôi dành cho cặp đôi")
                .build();

        seatTypeRepository.save(standard);
        seatTypeRepository.save(vip);
        seatTypeRepository.save(couple);

        log.info("Seat types initialized successfully");
    }
}
