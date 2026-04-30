package com.example.CineBook.repository.impl;

import com.example.CineBook.dto.promotion.PromotionSearchDTO;
import com.example.CineBook.model.Promotion;
import com.example.CineBook.repository.custom.PromotionRepositoryCustom;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Repository
public class PromotionRepositoryImpl implements PromotionRepositoryCustom {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public Page<Promotion> searchPromotions(PromotionSearchDTO searchDTO) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Promotion> query = cb.createQuery(Promotion.class);
        Root<Promotion> root = query.from(Promotion.class);

        List<Predicate> predicates = buildPredicates(cb, root, searchDTO);
        query.where(predicates.toArray(new Predicate[0]));
        query.orderBy(cb.desc(root.get("createdAt")));

        Pageable pageable = PageRequest.of(searchDTO.getPage(), searchDTO.getSize());
        List<Promotion> results = entityManager.createQuery(query)
                .setFirstResult((int) pageable.getOffset())
                .setMaxResults(pageable.getPageSize())
                .getResultList();

        Long total = countTotal(cb, searchDTO);
        return new PageImpl<>(results, pageable, total);
    }

    private List<Predicate> buildPredicates(CriteriaBuilder cb, Root<Promotion> root, PromotionSearchDTO searchDTO) {
        List<Predicate> predicates = new ArrayList<>();
        predicates.add(cb.equal(root.get("isDelete"), false));

        if (StringUtils.hasText(searchDTO.getCode())) {
            predicates.add(cb.like(cb.lower(root.get("code")), "%" + searchDTO.getCode().toLowerCase() + "%"));
        }

        if (StringUtils.hasText(searchDTO.getName())) {
            predicates.add(cb.like(cb.lower(root.get("name")), "%" + searchDTO.getName().toLowerCase() + "%"));
        }

        if (searchDTO.getDiscountType() != null) {
            predicates.add(cb.equal(root.get("discountType"), searchDTO.getDiscountType()));
        }

        if (searchDTO.getActive() != null && searchDTO.getActive()) {
            LocalDateTime now = LocalDateTime.now();
            predicates.add(cb.lessThanOrEqualTo(root.get("startDate"), now));
            predicates.add(cb.greaterThanOrEqualTo(root.get("endDate"), now));
        }

        return predicates;
    }

    private Long countTotal(CriteriaBuilder cb, PromotionSearchDTO searchDTO) {
        CriteriaQuery<Long> countQuery = cb.createQuery(Long.class);
        Root<Promotion> countRoot = countQuery.from(Promotion.class);
        List<Predicate> countPredicates = buildPredicates(cb, countRoot, searchDTO);
        countQuery.select(cb.count(countRoot));
        countQuery.where(countPredicates.toArray(new Predicate[0]));
        return entityManager.createQuery(countQuery).getSingleResult();
    }
}
