package com.example.CineBook.repository.impl;

import com.example.CineBook.dto.branch.BranchSearchDTO;
import com.example.CineBook.dto.customer.CustomerSearchDTO;
import com.example.CineBook.model.Branch;
import com.example.CineBook.model.Branch_;
import com.example.CineBook.model.Customer;
import com.example.CineBook.model.SysUser;
import com.example.CineBook.repository.custom.CustomerRepositoryCustom;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class CustomerRepositoryImpl implements CustomerRepositoryCustom {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public Page<Customer> searchWithFilters(CustomerSearchDTO searchDTO, Pageable pageable) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();

        CriteriaQuery<Customer> query = cb.createQuery(Customer.class);
        Root<Customer> customerRoot = query.from(Customer.class);
        Root<SysUser> userRoot = query.from(SysUser.class);

        List<Predicate> predicates = new ArrayList<>();

        predicates.add(cb.equal(customerRoot.get("userId"), userRoot.get("id")));
        predicates.add(cb.equal(customerRoot.get("isDelete"), false));

        if (StringUtils.hasText(searchDTO.getKeyword()))    {
            String keywordPattern = "%" + searchDTO.getKeyword().toLowerCase() + "%";
            predicates.add(cb.or(
                    cb.like(cb.lower(userRoot.get("username")), keywordPattern),
                    cb.like(cb.lower(userRoot.get("email")), keywordPattern),
                    cb.like(cb.lower(userRoot.get("phone")), keywordPattern)
            ));
        }

        if (StringUtils.hasText(searchDTO.getCity())) {
            predicates.add(cb.like(cb.lower(customerRoot.get("city")), "%" + searchDTO.getCity().toLowerCase() + "%"));
        }

        query.select(customerRoot).where(predicates.toArray(new Predicate[0]));
        query.orderBy(cb.desc(customerRoot.get("createTime")));

        TypedQuery<Customer> typedQuery = entityManager.createQuery(query);
        typedQuery.setFirstResult((int) pageable.getOffset());
        typedQuery.setMaxResults(pageable.getPageSize());

        List<Customer> customers = typedQuery.getResultList();

        Long total = countTotal(cb, searchDTO);

        return new PageImpl<>(customers, pageable, total);
    }

    private Long countTotal(CriteriaBuilder cb, CustomerSearchDTO searchDTO) {
        CriteriaQuery<Long> countQuery = cb.createQuery(Long.class);
        Root<Customer> customerRoot = countQuery.from(Customer.class);
        Root<SysUser> userRoot = countQuery.from(SysUser.class);

        List<Predicate> predicates = new ArrayList<>();

        predicates.add(cb.equal(customerRoot.get("userId"), userRoot.get("id")));
        predicates.add(cb.equal(customerRoot.get("isDelete"), false));

        if (StringUtils.hasText(searchDTO.getKeyword())) {
            String keywordPattern = "%" + searchDTO.getKeyword().toLowerCase() + "%";
            predicates.add(cb.or(
                    cb.like(cb.lower(userRoot.get("username")), keywordPattern),
                    cb.like(cb.lower(userRoot.get("email")), keywordPattern),
                    cb.like(cb.lower(userRoot.get("phone")), keywordPattern)
            ));
        }

        if (StringUtils.hasText(searchDTO.getCity())) {
            predicates.add(cb.like(cb.lower(customerRoot.get("city")), "%" + searchDTO.getCity().toLowerCase() + "%"));
        }

        countQuery.select(cb.count(customerRoot)).where(predicates.toArray(new Predicate[0]));

        return entityManager.createQuery(countQuery).getSingleResult();
    }
}
