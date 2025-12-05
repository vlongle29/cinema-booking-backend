package com.example.CineBook.repository.impl;

import com.example.CineBook.dto.customer.CustomerSearchDTO;
import com.example.CineBook.model.Customer;
import com.example.CineBook.model.Customer_;
import com.example.CineBook.model.SysUser;
import com.example.CineBook.model.SysUser_;
import com.example.CineBook.repository.custom.CustomerRepositoryCustom;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.criteria.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

@Repository
public class CustomerRepositoryImpl implements CustomerRepositoryCustom {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public Page<Customer> searchWithFilters(CustomerSearchDTO searchDTO, Pageable pageable) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Customer> query = cb.createQuery(Customer.class);
        Root<Customer> customer = query.from(Customer.class);
        Root<SysUser> sysUser = query.from(SysUser.class);

        List<Predicate> predicates = new ArrayList<>();

        // Filter by soft delete
        predicates.add(cb.equal(customer.get(Customer_.userId), sysUser.get(SysUser_.id)));
        predicates.add(cb.equal(customer.get(Customer_.isDelete), false));

        // Search by keyword (name, email, phone)
        if (StringUtils.hasText(searchDTO.getKeyword())) {
            String keyword = "%" + searchDTO.getKeyword().toLowerCase() + "%";
            Predicate namePredicate = cb.like(cb.lower(sysUser.get(SysUser_.name)), keyword);
            Predicate emailPredicate = cb.like(cb.lower(sysUser.get(SysUser_.email)), keyword);
            Predicate phonePredicate = cb.like(sysUser.get(SysUser_.phone), keyword);
            predicates.add(cb.or(namePredicate, emailPredicate, phonePredicate));
        }

        // Filter by membership level
        if (StringUtils.hasText(searchDTO.getMembershipLevel())) {
            predicates.add(cb.equal(customer.get(Customer_.membershipLevel), searchDTO.getMembershipLevel()));
        }

        // Filter by city
        if (StringUtils.hasText(searchDTO.getCity())) {
            predicates.add(cb.like(cb.lower(customer.get(Customer_.city)), 
                "%" + searchDTO.getCity().toLowerCase() + "%"));
        }

        query.where(predicates.toArray(new Predicate[0]));
        query.orderBy(cb.desc(customer.get(Customer_.createTime)));

        // Pagination
        List<Customer> customers = entityManager.createQuery(query)
                .setFirstResult((int) pageable.getOffset())
                .setMaxResults(pageable.getPageSize())
                .getResultList();

        // Count total
        CriteriaQuery<Long> countQuery = cb.createQuery(Long.class);
        Root<Customer> countRoot = countQuery.from(Customer.class);
        Root<SysUser> countUser = countQuery.from(SysUser.class);

        List<Predicate> countPredicates = new ArrayList<>();
        countPredicates.add(cb.equal(countRoot.get(Customer_.userId), countUser.get(SysUser_.id)));
        countPredicates.add(cb.equal(countRoot.get(Customer_.isDelete), false));

        if (StringUtils.hasText(searchDTO.getKeyword())) {
            String keyword = "%" + searchDTO.getKeyword().toLowerCase() + "%";
            countPredicates.add(cb.or(
                    cb.like(cb.lower(countUser.get(SysUser_.name)), keyword),
                    cb.like(cb.lower(countUser.get(SysUser_.email)), keyword),
                    cb.like(countUser.get(SysUser_.phone), keyword)
            ));
        }
        if (StringUtils.hasText(searchDTO.getMembershipLevel())) {
            countPredicates.add(cb.equal(countRoot.get(Customer_.membershipLevel), searchDTO.getMembershipLevel()));
        }
        if (StringUtils.hasText(searchDTO.getCity())) {
            countPredicates.add(cb.like(cb.lower(countRoot.get(Customer_.city)),
                    "%" + searchDTO.getCity().toLowerCase() + "%"));
        }

        countQuery.select(cb.count(countRoot));
        countQuery.where(countPredicates.toArray(new Predicate[0]));
        Long total = entityManager.createQuery(countQuery).getSingleResult();

        return new PageImpl<>(customers, pageable, total);
    }
}
