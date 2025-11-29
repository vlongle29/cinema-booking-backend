package com.example.CineBook.repository.impl;

import com.example.CineBook.dto.employee.EmployeeSearchDTO;
import com.example.CineBook.model.Branch;
import com.example.CineBook.model.Employee;
import com.example.CineBook.model.Employee_;
import com.example.CineBook.repository.base.BaseRepositoryImpl;
import com.example.CineBook.repository.custom.EmployeeRepositoryCustom;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Predicate;
import org.springframework.stereotype.Repository;
import java.util.ArrayList;
import java.util.List;


@Repository
public class EmployeeRepositoryImpl extends BaseRepositoryImpl<Employee, EmployeeSearchDTO> implements EmployeeRepositoryCustom {
    public EmployeeRepositoryImpl() {
        super(Employee.class);
    }

    @Override
    protected List<Predicate> buildPredicates(Root<Employee> root, CriteriaQuery<?> query,
                                                                           CriteriaBuilder cb, EmployeeSearchDTO searchDTO) {
        List<Predicate> predicates = new ArrayList<>();

        // Filter by branchId
        if (searchDTO.getBranchId() != null) {
            predicates.add(cb.equal(root.get(Employee_.branchId), searchDTO.getBranchId()));
        }

        return predicates;
    }
}
