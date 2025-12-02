package com.example.CineBook.common.config;

import com.example.CineBook.common.constant.LockFlag;
import com.example.CineBook.common.constant.SystemFlag;
import com.example.CineBook.model.SysRole;
import com.example.CineBook.model.SysUser;
import com.example.CineBook.model.SysUserRole;
import com.example.CineBook.repository.irepository.SysRoleRepository;
import com.example.CineBook.repository.irepository.SysUserRepository;
import com.example.CineBook.repository.irepository.SysUserRoleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final SysUserRepository userRepository;
    private final SysRoleRepository roleRepository;
    private final SysUserRoleRepository userRoleRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        initSuperAdmin();
    }

    private void initSuperAdmin() {
        // Check if super admin already exists
        if (userRepository.existsByUsername("superadmin")) {
            log.info("Super admin already exists");
            return;
        }

        // Create SUPER_ADMIN role if not exists
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

        // Create super admin user
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

        // Assign SUPER_ADMIN role
        SysUserRole userRole = new SysUserRole();
        userRole.setUserId(superAdmin.getId());
        userRole.setRoleId(superAdminRole.getId());
        userRoleRepository.save(userRole);

        log.info("Super admin created successfully - Username: superadmin, Password: Admin@123");
    }
}
