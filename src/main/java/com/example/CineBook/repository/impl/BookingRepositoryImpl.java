package com.example.CineBook.repository.impl;

import com.example.CineBook.common.constant.BookingStatus;
import com.example.CineBook.dto.booking.BookingSearchDTO;
import com.example.CineBook.model.Booking;
import com.example.CineBook.model.SysUser;
import com.example.CineBook.repository.custom.BookingRepositoryCustom;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class BookingRepositoryImpl implements BookingRepositoryCustom {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public Page<Booking> searchWithFilters(BookingSearchDTO searchDTO, Pageable pageable) {
        StringBuilder jpql = new StringBuilder(
            "SELECT b FROM Booking b LEFT JOIN SysUser u ON u.id = b.userId WHERE b.isDelete = false");
        Map<String, Object> params = new HashMap<>();

        if (searchDTO.getBookingCode() != null && !searchDTO.getBookingCode().isEmpty()) {
            jpql.append(" AND LOWER(b.bookingCode) LIKE :bookingCode");
            params.put("bookingCode", "%" + searchDTO.getBookingCode().toLowerCase() + "%");
        }

        if (searchDTO.getStatus() != null && !searchDTO.getStatus().isEmpty()) {
            jpql.append(" AND b.status = :status");
            params.put("status", BookingStatus.valueOf(searchDTO.getStatus()));
        }

        if (searchDTO.getKeyword() != null && !searchDTO.getKeyword().isEmpty()) {
            jpql.append(" AND (LOWER(b.bookingCode) LIKE :keyword"
                + " OR LOWER(u.name) LIKE :keyword"
                + " OR LOWER(u.email) LIKE :keyword"
                + " OR LOWER(u.phone) LIKE :keyword)");
            params.put("keyword", "%" + searchDTO.getKeyword().toLowerCase() + "%");
        }

        jpql.append(" ORDER BY b.bookingDate DESC");

        TypedQuery<Booking> query = entityManager.createQuery(jpql.toString(), Booking.class);
        params.forEach(query::setParameter);
        query.setFirstResult((int) pageable.getOffset());
        query.setMaxResults(pageable.getPageSize());
        List<Booking> results = query.getResultList();

        // Count query
        StringBuilder countJpql = new StringBuilder(
            "SELECT COUNT(b) FROM Booking b LEFT JOIN SysUser u ON u.id = b.userId WHERE b.isDelete = false");
        if (searchDTO.getBookingCode() != null && !searchDTO.getBookingCode().isEmpty())
            countJpql.append(" AND LOWER(b.bookingCode) LIKE :bookingCode");
        if (searchDTO.getStatus() != null && !searchDTO.getStatus().isEmpty())
            countJpql.append(" AND b.status = :status");
        if (searchDTO.getKeyword() != null && !searchDTO.getKeyword().isEmpty())
            countJpql.append(" AND (LOWER(b.bookingCode) LIKE :keyword"
                + " OR LOWER(u.name) LIKE :keyword"
                + " OR LOWER(u.email) LIKE :keyword"
                + " OR LOWER(u.phone) LIKE :keyword)");
        TypedQuery<Long> countTypedQuery = entityManager.createQuery(countJpql.toString(), Long.class);
        params.forEach(countTypedQuery::setParameter);
        Long total = countTypedQuery.getSingleResult();

        return new PageImpl<>(results, pageable, total);
    }
}
