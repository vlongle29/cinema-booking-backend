package com.example.CineBook.repository.impl;

import com.example.CineBook.dto.branch.BranchSearchDTO;
import com.example.CineBook.model.Branch;
import com.example.CineBook.repository.base.BaseRepositoryImpl;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

@Repository
public class BranchRepositoryImpl extends BaseRepositoryImpl<Branch, BranchSearchDTO> {

    public BranchRepositoryImpl() {
        super(Branch.class);
    }

    @Override
    protected List<Predicate> buildPredicates(Root<Branch> root, CriteriaQuery<?> query, CriteriaBuilder cb, BranchSearchDTO searchDTO) {
        List<Predicate> predicates = new ArrayList<>();

        if (StringUtils.hasText(searchDTO.getName())) {
            predicates.add(cb.like(cb.lower(root.get("name")), "%" + searchDTO.getName().toLowerCase() + "%"));
        }

        if (StringUtils.hasText(searchDTO.getCity())) {
            predicates.add(cb.like(cb.lower(root.get("city")), "%" + searchDTO.getCity().toLowerCase() + "%"));
        }

        if (searchDTO.getManagerId() != null) {
            predicates.add(cb.equal(root.get("managerId"), searchDTO.getManagerId()));
        }

        return predicates;
    }
}
