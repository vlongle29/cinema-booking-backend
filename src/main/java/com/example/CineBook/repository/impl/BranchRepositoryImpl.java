package com.example.CineBook.repository.impl;

import com.example.CineBook.dto.branch.BranchSearchDTO;
import com.example.CineBook.model.Branch;
import com.example.CineBook.model.Branch_;
import com.example.CineBook.repository.custom.BranchRepositoryCustom;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

@Repository
public class BranchRepositoryImpl implements BranchRepositoryCustom {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public Page<Branch> searchWithFilters(BranchSearchDTO searchDTO, Pageable pageable) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Branch> query = cb.createQuery(Branch.class);
        Root<Branch> branch = query.from(Branch.class);

        List<Predicate> predicates = buildPredicates(cb, branch, searchDTO);

        query.where(predicates.toArray(new Predicate[0]));
        query.orderBy(cb.desc(branch.get(Branch_.createTime)));

        List<Branch> branches = entityManager.createQuery(query)
                .setFirstResult((int) pageable.getOffset())
                .setMaxResults(pageable.getPageSize())
                .getResultList();

        Long total = countTotal(cb, searchDTO);

        return new PageImpl<>(branches, pageable, total);
    }

    private List<Predicate> buildPredicates(CriteriaBuilder cb, Root<Branch> branch, BranchSearchDTO searchDTO) {
        List<Predicate> predicates = new ArrayList<>();

        predicates.add(cb.equal(branch.get(Branch_.isDelete), false));

        if (StringUtils.hasText(searchDTO.getName())) {
            predicates.add(cb.like(cb.lower(branch.get(Branch_.name)),
                    "%" + searchDTO.getName().toLowerCase() + "%"));
        }

        if (StringUtils.hasText(searchDTO.getCity())) {
            predicates.add(cb.like(cb.lower(branch.get(Branch_.city)),
                    "%" + searchDTO.getCity().toLowerCase() + "%"));
        }

        if (searchDTO.getManagerId() != null) {
            predicates.add(cb.equal(branch.get(Branch_.managerId), searchDTO.getManagerId()));
        }

        return predicates;
    }

    private Long countTotal(CriteriaBuilder cb, BranchSearchDTO searchDTO) {
        CriteriaQuery<Long> countQuery = cb.createQuery(Long.class);
        Root<Branch> countRoot = countQuery.from(Branch.class);

        List<Predicate> countPredicates = buildPredicates(cb, countRoot, searchDTO);

        countQuery.select(cb.count(countRoot));
        countQuery.where(countPredicates.toArray(new Predicate[0]));

        return entityManager.createQuery(countQuery).getSingleResult();
    }
}
