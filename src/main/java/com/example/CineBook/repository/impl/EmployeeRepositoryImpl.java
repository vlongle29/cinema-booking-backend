package com.example.CineBook.repository.impl;

import com.example.CineBook.dto.employee.EmployeeSearchDTO;
import com.example.CineBook.model.Employee;
import com.example.CineBook.model.Employee_;
import com.example.CineBook.model.SysUser;
import com.example.CineBook.model.SysUser_;
import com.example.CineBook.model.SysUserRole;
import com.example.CineBook.repository.custom.EmployeeRepositoryCustom;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.criteria.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Repository
public class EmployeeRepositoryImpl implements EmployeeRepositoryCustom {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public Page<Employee> searchWithFilters(EmployeeSearchDTO searchDTO, Pageable pageable) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Employee> query = cb.createQuery(Employee.class);
        Root<Employee> employee = query.from(Employee.class);

        query.where(buildPredicates(cb, query, employee, searchDTO).toArray(new Predicate[0]));
        query.orderBy(cb.desc(employee.get(Employee_.createTime)));

        List<Employee> employees = entityManager.createQuery(query)
                .setFirstResult((int) pageable.getOffset())
                .setMaxResults(pageable.getPageSize())
                .getResultList();

        return new PageImpl<>(employees, pageable, countTotal(cb, searchDTO));
    }

    private List<Predicate> buildPredicates(CriteriaBuilder cb, CriteriaQuery<?> query,
                                            Root<Employee> employee, EmployeeSearchDTO searchDTO) {
        List<Predicate> predicates = new ArrayList<>();

        predicates.add(cb.equal(employee.get(Employee_.isDelete), false));

        if (StringUtils.hasText(searchDTO.getName())) {
            Subquery<UUID> subquery = query.subquery(UUID.class);
            Root<SysUser> userRoot = subquery.from(SysUser.class);
            subquery.select(userRoot.get(SysUser_.id))
                    .where(cb.like(cb.lower(userRoot.get(SysUser_.name)),
                            "%" + searchDTO.getName().toLowerCase() + "%"));
            predicates.add(employee.get(Employee_.userId).in(subquery));
        }

        if (StringUtils.hasText(searchDTO.getEmployeeCode())) {
            predicates.add(cb.like(employee.get(Employee_.employeeCode),
                    "%" + searchDTO.getEmployeeCode() + "%"));
        }

        if (searchDTO.getBranchId() != null) {
            predicates.add(cb.equal(employee.get(Employee_.branchId), searchDTO.getBranchId()));
        }

        if (StringUtils.hasText(searchDTO.getRoleId())) {
            Subquery<UUID> subquery = query.subquery(UUID.class);
            Root<SysUserRole> userRoleRoot = subquery.from(SysUserRole.class);
            subquery.select(userRoleRoot.get("userId"))
                    .where(cb.equal(userRoleRoot.get("roleId"), UUID.fromString(searchDTO.getRoleId())));
            predicates.add(employee.get(Employee_.userId).in(subquery));
        }

        if (searchDTO.getManageId() != null) {
            Subquery<UUID> branchSubquery = query.subquery(UUID.class);
            Root<com.example.CineBook.model.Branch> branchRoot = branchSubquery.from(com.example.CineBook.model.Branch.class);
            branchSubquery.select(branchRoot.get("id"))
                    .where(cb.equal(branchRoot.get("managerId"), searchDTO.getManageId()));
            predicates.add(employee.get(Employee_.branchId).in(branchSubquery));
        }

        return predicates;
    }

    private Long countTotal(CriteriaBuilder cb, EmployeeSearchDTO searchDTO) {
        CriteriaQuery<Long> countQuery = cb.createQuery(Long.class);
        Root<Employee> countEmployee = countQuery.from(Employee.class);

        countQuery.select(cb.count(countEmployee));
        countQuery.where(buildPredicates(cb, countQuery, countEmployee, searchDTO).toArray(new Predicate[0]));

        return entityManager.createQuery(countQuery).getSingleResult();
    }
}
