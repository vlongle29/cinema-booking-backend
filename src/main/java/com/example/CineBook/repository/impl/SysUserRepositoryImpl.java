package com.example.CineBook.repository.impl;

import com.example.CineBook.common.constant.LockFlag;
import com.example.CineBook.dto.auth.AuthorityProjection;
import com.example.CineBook.dto.sysUser.SysUserSearchDTO;
import com.example.CineBook.model.*;
import com.example.CineBook.repository.base.BaseRepositoryImpl;
import com.example.CineBook.repository.custom.SysUserRepositoryCustom;
import jakarta.persistence.criteria.*;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Repository
public class SysUserRepositoryImpl extends BaseRepositoryImpl<SysUser, SysUserSearchDTO> implements SysUserRepositoryCustom {
    public SysUserRepositoryImpl() {
        super(SysUser.class);
    }

    /**
     * Xây dựng danh sách các điều kiện lọc (predicates) tùy chỉnh cho việc tìm kiếm SysUser.
     * Điều này tập trung logic lọc vào một nơi để có thể tái sử dụng bởi tất cả các phương thức tìm kiếm.
     *
     * @param root      The root entity (SysUser).
     * @param query     The CriteriaQuery object for creating subqueries.
     * @param cb        The CriteriaBuilder.
     * @param searchDTO The DTO containing search parameters.
     * @return A list of predicates for the WHERE clause.
     */
    @Override
    protected List<Predicate> buildPredicates(Root<SysUser> root, CriteriaQuery<?> query, CriteriaBuilder cb, SysUserSearchDTO searchDTO) {
        List<Predicate> predicates = new ArrayList<>();

        if (searchDTO.getIds() != null && !searchDTO.getIds().isEmpty()) {
            // Lọc theo các id
            predicates.add(root.get(SysUser_.id).in(searchDTO.getIds()));
        }
        // Luôn lọc bỏ các bản ghi đã bị xóa mềm
        predicates.add(cb.isFalse(root.get(SysUser_.isDelete)));

        // Lọc theo từ khóa tìm kiếm chung (searchTerm) trên nhiều trường
        if (StringUtils.hasText(searchDTO.getSearchTerm())) {
            String searchTermPattern = "%" + searchDTO.getSearchTerm().toLowerCase().trim() + "%";
            Predicate searchPredicate = cb.or(
                    cb.like(cb.lower(root.get(SysUser_.username)), searchTermPattern),
                    cb.like(cb.lower(root.get(SysUser_.name)), searchTermPattern),
                    cb.like(cb.lower(root.get(SysUser_.email)), searchTermPattern),
                    cb.like(cb.lower(root.get(SysUser_.phone)), searchTermPattern)
            );
            predicates.add(searchPredicate);
        }

        // Thêm các điều kiện lọc cụ thể cho từng trường
        if (StringUtils.hasText(searchDTO.getUsername())) {
            predicates.add(cb.like(cb.lower(root.get(SysUser_.username)), "%" + searchDTO.getUsername().toLowerCase().trim() + "%"));
        }
        if (StringUtils.hasText(searchDTO.getEmail())) {
            predicates.add(cb.like(cb.lower(root.get(SysUser_.email)), "%" + searchDTO.getEmail().toLowerCase().trim() + "%"));
        }
        if (StringUtils.hasText(searchDTO.getName())) {
            predicates.add(cb.like(cb.lower(root.get(SysUser_.name)), "%" + searchDTO.getName().toLowerCase().trim() + "%"));
        }
        if (StringUtils.hasText(searchDTO.getPhone())) {
            predicates.add(cb.like(cb.lower(root.get(SysUser_.phone)), "%" + searchDTO.getPhone().toLowerCase().trim() + "%"));
        }
        // Lọc theo trạng thái khóa
        if (searchDTO.getLockFlag() != null) {
            predicates.add(cb.equal(root.get(SysUser_.lockFlag), searchDTO.getLockFlag().getValue()));
        }

        // Lọc theo roleIds sử dụng Subquery với EXISTS để tối ưu hiệu năng và tránh giới hạn IN clause
        if (searchDTO.getRoleIds() != null && !searchDTO.getRoleIds().isEmpty()) {

            // SQL tương đương: WHERE EXISTS (SELECT 1 FROM sys_user_role sur WHERE sur.user_id = sys_user.id AND sur.role_id IN (...))
            Subquery<Integer> subquery = query.subquery(Integer.class);
            Root<SysUserRole> subRoot = subquery.from(SysUserRole.class);
            subquery.select(cb.literal(1)); // Chỉ cần select 1 giá trị hằng số để kiểm tra sự tồn tại.

            subquery.where(
                    cb.and(
                            // Điều kiện liên kết (correlation) subquery với truy vấn chính qua userId
                            cb.equal(subRoot.get(SysUserRole_.userId), root.get(SysUser_.id)),
                            // Lọc trong subquery theo danh sách roleId được cung cấp
                            subRoot.get(SysUserRole_.roleId).in(searchDTO.getRoleIds())
                    )
            );
            predicates.add(cb.exists(subquery));
        }
        return predicates;
    }

    @Override
    public List<AuthorityProjection> findAllAuthoritiesByUserId(UUID userId) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<AuthorityProjection> query = cb.createQuery(AuthorityProjection.class);

        Root<SysRole> roleRoot = query.from(SysRole.class);
        Root<SysUserRole> userRoleRoot = query.from(SysUserRole.class);
        Root<SysRolePermission> rolePermissionRoot = query.from(SysRolePermission.class);
        Root<SysPermission> permissionRoot = query.from(SysPermission.class);

        query.multiselect(roleRoot.get(SysRole_.code), permissionRoot.get(SysPermission_.code)).distinct(true);
        query.where(cb.and(
                cb.equal(userRoleRoot.get(SysUserRole_.userId), userId),
                cb.equal(roleRoot.get(SysRole_.id), userRoleRoot.get(SysUserRole_.roleId)),
                cb.equal(roleRoot.get(SysRole_.id), rolePermissionRoot.get(SysRolePermission_.roleId)),
                cb.equal(rolePermissionRoot.get(SysRolePermission_.permissionId), permissionRoot.get(SysPermission_.id))
        ));

        return entityManager.createQuery(query).getResultList();
    }

    @Override
    @Transactional
    public int updateLockStatusForIds(List<UUID> ids, LockFlag lockFlag) {
        if (ids == null || ids.isEmpty() || lockFlag == null) {
            return 0;
        }
        SysUserSearchDTO searchDTO = new SysUserSearchDTO();
        searchDTO.setIds(ids);
        return updateFieldByFilter(searchDTO, "lockFlag", lockFlag.getValue());
    }


}
