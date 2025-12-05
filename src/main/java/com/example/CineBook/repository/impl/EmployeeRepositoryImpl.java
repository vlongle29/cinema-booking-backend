package com.example.CineBook.repository.impl;

import com.example.CineBook.dto.employee.EmployeeSearchDTO;
import com.example.CineBook.model.Employee;
import com.example.CineBook.model.Employee_;
import com.example.CineBook.model.SysUser;
import com.example.CineBook.model.SysUser_;
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

@Repository
public class EmployeeRepositoryImpl implements EmployeeRepositoryCustom {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public Page<Employee> searchWithFilters(EmployeeSearchDTO searchDTO, Pageable pageable) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Employee> query = cb.createQuery(Employee.class);
        Root<Employee> employee = query.from(Employee.class);
        Root<SysUser> sysUser = query.from(SysUser.class);

        List<Predicate> predicates = buildPredicates(cb, employee, sysUser, searchDTO);

        query.where(predicates.toArray(new Predicate[0]));
        query.orderBy(cb.desc(employee.get(Employee_.createTime)));

        List<Employee> employees = entityManager.createQuery(query)
                .setFirstResult((int) pageable.getOffset())
                .setMaxResults(pageable.getPageSize())
                .getResultList();

        Long total = countTotal(cb, searchDTO);

        return new PageImpl<>(employees, pageable, total);
    }

    private List<Predicate> buildPredicates(CriteriaBuilder cb, Root<Employee> employee, 
                                           Root<SysUser> sysUser, EmployeeSearchDTO searchDTO) {
        List<Predicate> predicates = new ArrayList<>();

        predicates.add(cb.equal(employee.get(Employee_.userId), sysUser.get(SysUser_.id)));
        predicates.add(cb.equal(employee.get(Employee_.isDelete), false));

        if (StringUtils.hasText(searchDTO.getName())) {
            predicates.add(cb.like(cb.lower(sysUser.get(SysUser_.name)),
                    "%" + searchDTO.getName().toLowerCase() + "%"));
        }

        if (StringUtils.hasText(searchDTO.getEmployeeCode())) {
            predicates.add(cb.like(employee.get(Employee_.employeeCode),
                    "%" + searchDTO.getEmployeeCode() + "%"));
        }

        if (searchDTO.getBranchId() != null) {
            predicates.add(cb.equal(employee.get(Employee_.branchId), searchDTO.getBranchId()));
        }

        return predicates;
    }

    private Long countTotal(CriteriaBuilder cb, EmployeeSearchDTO searchDTO) {
        CriteriaQuery<Long> countQuery = cb.createQuery(Long.class);
        Root<Employee> countEmployee = countQuery.from(Employee.class);
        Root<SysUser> countUser = countQuery.from(SysUser.class);

        List<Predicate> countPredicates = buildPredicates(cb, countEmployee, countUser, searchDTO);

        countQuery.select(cb.count(countEmployee));
        countQuery.where(countPredicates.toArray(new Predicate[0]));

        return entityManager.createQuery(countQuery).getSingleResult();
    }
}
