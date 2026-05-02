package com.example.CineBook.repository.impl;

import com.example.CineBook.common.constant.BookingStatus;
import com.example.CineBook.dto.booking.BookingSearchDTO;
import com.example.CineBook.model.Booking;
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
        StringBuilder jpql = new StringBuilder("SELECT b FROM Booking b WHERE b.isDelete = false");
        Map<String, Object> params = new HashMap<>();
        
        // Filter by bookingCode
        if (searchDTO.getBookingCode() != null && !searchDTO.getBookingCode().isEmpty()) {
            jpql.append(" AND LOWER(b.bookingCode) LIKE :bookingCode");
            params.put("bookingCode", "%" + searchDTO.getBookingCode().toLowerCase() + "%");
        }
        
        // Filter by status
        if (searchDTO.getStatus() != null && !searchDTO.getStatus().isEmpty()) {
            jpql.append(" AND b.status = :status");
            params.put("status", BookingStatus.valueOf(searchDTO.getStatus()));
        }
        
        // Filter by keyword
        if (searchDTO.getKeyword() != null && !searchDTO.getKeyword().isEmpty()) {
            jpql.append(" AND LOWER(b.bookingCode) LIKE :keyword");
            params.put("keyword", "%" + searchDTO.getKeyword().toLowerCase() + "%");
        }
        
        jpql.append(" ORDER BY b.bookingDate DESC");
        
        // Execute query
        TypedQuery<Booking> query = entityManager.createQuery(jpql.toString(), Booking.class);
        params.forEach(query::setParameter);
        
        query.setFirstResult((int) pageable.getOffset());
        query.setMaxResults(pageable.getPageSize());
        List<Booking> results = query.getResultList();
        
        // Count query
        String countJpql = "SELECT COUNT(b) FROM Booking b WHERE b.isDelete = false";
        StringBuilder countQuery = new StringBuilder(countJpql);
        
        if (searchDTO.getBookingCode() != null && !searchDTO.getBookingCode().isEmpty()) {
            countQuery.append(" AND LOWER(b.bookingCode) LIKE :bookingCode");
        }
        if (searchDTO.getStatus() != null && !searchDTO.getStatus().isEmpty()) {
            countQuery.append(" AND b.status = :status");
        }
        if (searchDTO.getKeyword() != null && !searchDTO.getKeyword().isEmpty()) {
            countQuery.append(" AND LOWER(b.bookingCode) LIKE :keyword");
        }
        
        TypedQuery<Long> countTypedQuery = entityManager.createQuery(countQuery.toString(), Long.class);
        params.forEach(countTypedQuery::setParameter);
        Long total = countTypedQuery.getSingleResult();
        
        return new PageImpl<>(results, pageable, total);
    }
}
