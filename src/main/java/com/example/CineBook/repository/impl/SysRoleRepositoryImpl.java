package com.example.CineBook.repository.impl;

import com.example.CineBook.dto.sysPermission.SysPermissionResponse;
import com.example.CineBook.dto.sysRole.SysRoleResponse;
import com.example.CineBook.dto.sysRole.SysRoleSearchDTO;
import com.example.CineBook.dto.sysUserRole.UserRoleProjection;
import com.example.CineBook.model.*;
import com.example.CineBook.repository.base.BaseRepositoryImpl;
import com.example.CineBook.repository.custom.SysRoleRepositoryCustom;
import jakarta.persistence.criteria.*;
import org.springframework.stereotype.Repository;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Repository
public class SysRoleRepositoryImpl extends BaseRepositoryImpl<SysRole, SysRoleSearchDTO> implements SysRoleRepositoryCustom {

    public SysRoleRepositoryImpl() {
        super(SysRole.class);
    }

    /**
     * Ghi đè phương thức buildPredicates để thêm logic lọc cụ thể cho SysRole.
     * Chữ ký phương thức này phải khớp với lớp cha BaseRepositoryImpl.
     *
     * @param root      Đối tượng Root của Criteria API cho SysRole.
     * @param query     Đối tượng CriteriaQuery của truy vấn hiện tại. Cần thiết để khớp với lớp cha.
     * @param cb        Đối tượng CriteriaBuilder của Criteria API.
     * @param searchDTO DTO chứa các tham số tìm kiếm cho SysRole.
     * @return Danh sách các Predicate sẽ được áp dụng cho mệnh đề WHERE.
     */
    @Override
    protected List<Predicate> buildPredicates(Root<SysRole> root, CriteriaQuery<?> query, CriteriaBuilder cb, SysRoleSearchDTO searchDTO) {
        List<Predicate> predicates = new ArrayList<>();
        // Điều kiện mặc định: luôn chỉ lấy các vai trò chưa bị xóa mềm
        predicates.add(cb.equal(root.get(SysRole_.IS_DELETE), false));

        if (searchDTO == null) {
            return predicates;
        }

        // Lọc theo tên vai trò (không phân biệt hoa thường, tìm kiếm gần đúng)
        if (StringUtils.hasText(searchDTO.getName())) {
            predicates.add(cb.like(cb.lower(root.get(SysRole_.name)), "%" + searchDTO.getName().trim().toLowerCase() + "%"));
        }

        // Lọc theo mã vai trò (không phân biệt hoa thường, tìm kiếm gần đúng)
        if (StringUtils.hasText(searchDTO.getCode())) {
            predicates.add(cb.like(cb.lower(root.get(SysRole_.CODE)), "%" + searchDTO.getCode().trim().toLowerCase() + "%"));
        }

        return predicates;
    }

    /**
     * Lấy danh sách role của user theo userId (chuẩn 3NF, join qua bảng SysUserRole)
     * Tối ưu bằng cách sử dụng Constructor Projection của JPA.
     */
    @Override
    public List<SysRoleResponse> getRoleInfoByUserId(UUID userId) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<SysRoleResponse> cq = cb.createQuery(SysRoleResponse.class);
        Root<SysRole> roleRoot = cq.from(SysRole.class);
        Root<SysUserRole> userRoleRoot = cq.from(SysUserRole.class);

        // Sử dụng cb.construct để ánh xạ trực tiếp kết quả truy vấn vào DTO
        cq.select(cb.construct(
                SysRoleResponse.class,
                roleRoot.get(SysRole_.id),
                roleRoot.get(SysRole_.code),
                roleRoot.get(SysRole_.name)
        )).distinct(true);

        cq.where(
                cb.and(
                        cb.equal(userRoleRoot.get(SysUserRole_.userId), userId),
                        cb.equal(userRoleRoot.get(SysUserRole_.roleId), roleRoot.get(SysRole_.id))
                )
        );

        return entityManager.createQuery(cq).getResultList();
    }

    @Override
    public List<UserRoleProjection> findUserRolesByUserIds(List<UUID> userIds) {
        if (CollectionUtils.isEmpty(userIds)) {
            return Collections.emptyList();
        }

        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<UserRoleProjection> query = cb.createQuery(UserRoleProjection.class);

        // FROM SysUserRole
        Root<SysUserRole> userRoleRoot = query.from(SysUserRole.class);

        // JOIN SysRole
        Join<SysUserRole, SysRole> roleRoot = userRoleRoot.join("role");

        // SELECT
        query.select(cb.construct(
                UserRoleProjection.class,
                userRoleRoot.get("userId"),
                roleRoot.get("id"),
                roleRoot.get("name"),
                roleRoot.get("code")
        ));

        // WHERE user_id IN (...)
        Predicate predicate = userRoleRoot.get("userId").in(userIds);
        query.where(predicate);

        return entityManager.createQuery(query).getResultList();
    }


    @Override
    public List<String> getRoleCodesByUserId(UUID userId) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<String> query = cb.createQuery(String.class);

        // Join SysUserRole và SysRole để lấy role codes
        Root<SysUserRole> userRoleRoot = query.from(SysUserRole.class);
        Root<SysRole> roleRoot = query.from(SysRole.class);

        // Associate user_role with role and filter userId
        query.select(roleRoot.get("code")).distinct(true);
        query.where(cb.and(
                cb.equal(userRoleRoot.get("userId"), userId),
                cb.equal(userRoleRoot.get("roleId"), roleRoot.get("id")))
        );

        return entityManager.createQuery(query).getResultList();
    }

    /**
     * Lấy danh sách quyền (permission) của một vai trò (role) theo roleId.
     * Tối ưu bằng cách sử dụng Constructor Projection của JPA để ánh xạ kết quả vào PermissionResponse DTO.
     *
     * @param roleId ID của vai trò cần tìm kiếm quyền.
     * @return Danh sách các PermissionResponse tương ứng với vai trò.
     */
    @Override
    public List<SysPermissionResponse> findPermissionsByRoleId(UUID roleId) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<SysPermissionResponse> cq = cb.createQuery(SysPermissionResponse.class);
        Root<SysPermission> permissionRoot = cq.from(SysPermission.class);
        Root<SysRolePermission> rolePermissionRoot = cq.from(SysRolePermission.class);

        // Sử dụng cb.construct để ánh xạ trực tiếp kết quả truy vấn vào DTO
        // Giả định PermissionResponse có constructor nhận (id, code, name)
        cq.select(cb.construct(
                SysPermissionResponse.class,
                permissionRoot.get(SysPermission_.id),
                permissionRoot.get(SysPermission_.permission),
                permissionRoot.get(SysPermission_.description)
        )).distinct(true);

        cq.where(cb.and(
                // Lọc theo roleId từ bảng trung gian SysRolePermission
                cb.equal(rolePermissionRoot.get(SysRolePermission_.roleId), roleId),
                // Join bảng SysRolePermission với SysPermission
                cb.equal(rolePermissionRoot.get(SysRolePermission_.permissionId), permissionRoot.get(SysPermission_.id)),
                // Chỉ lấy các permission chưa bị xóa mềm (good practice)
                cb.equal(permissionRoot.get(SysPermission_.isDelete), false)
        ));

        return entityManager.createQuery(cq).getResultList();
    }




}
