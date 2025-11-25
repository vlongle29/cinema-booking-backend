package com.example.CineBook.repository.irepository;

import com.example.CineBook.dto.sysRole.SysRoleSearchDTO;
import com.example.CineBook.model.SysRole;
import com.example.CineBook.repository.base.BaseRepositoryCustom;
import com.example.CineBook.repository.custom.SysRoleRepositoryCustom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SysRoleRepository extends BaseRepositoryCustom<SysRole, SysRoleSearchDTO>, SysRoleRepositoryCustom, JpaRepository<SysRole, UUID> {
    Optional<SysRole> findByCode(String code);

    Optional<SysRole> findByCodeIgnoreCase(String code);

    boolean existsByCode(String code);


}
