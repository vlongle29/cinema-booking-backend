package com.example.CineBook.repository.impl;

import com.example.CineBook.dto.showtime.ShowtimeSearchDTO;
import com.example.CineBook.model.Showtime;
import com.example.CineBook.model.Showtime_;
import com.example.CineBook.repository.custom.ShowtimeRepositoryCustom;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Repository
public class ShowtimeRepositoryImpl implements ShowtimeRepositoryCustom {

    @Override
    public Specification<Showtime> searchWithFilters(ShowtimeSearchDTO searchDTO) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // 1. Filter by movie (movieId)
            if (searchDTO.getMovieId() != null) {
                predicates.add(cb.equal(root.get(Showtime_.movieId), searchDTO.getMovieId()));
            }
            // 2. Filter by branch (branchId)
            if (searchDTO.getBranchId() != null) {
                predicates.add(cb.equal(root.get(Showtime_.branchId), searchDTO.getBranchId()));
            }
            // 3. Filter by room (roomId)
            if (searchDTO.getRoomId() != null) {
                predicates.add(cb.equal(root.get(Showtime_.roomId), searchDTO.getRoomId()));
            }
            // 3. Filter by time period
            if (searchDTO.getDate() != null) {
                LocalDate date = searchDTO.getDate();
                predicates.add(cb.between(
                        root.get(Showtime_.startTime),
                        date.atStartOfDay(),
                        date.atTime(LocalTime.MAX)
                ));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
