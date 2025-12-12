package com.example.CineBook.repository.impl;

import com.example.CineBook.dto.showtime.ShowtimeSearchDTO;
import com.example.CineBook.model.Branch;
import com.example.CineBook.model.Branch_;
import com.example.CineBook.model.Showtime;
import com.example.CineBook.model.Showtime_;
import com.example.CineBook.repository.custom.ShowtimeRepositoryCustom;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.criteria.*;
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
import java.util.UUID;

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
            // 4. Filter by city (cityId) - using subquery
            if (searchDTO.getCityId() != null) {
                Subquery<UUID> branchSubquery = query.subquery(UUID.class);
                Root<Branch> branchRoot = branchSubquery.from(Branch.class);
                branchSubquery.select(branchRoot.get(Branch_.id))
                    .where(cb.equal(branchRoot.get(Branch_.cityId), searchDTO.getCityId()));
                predicates.add(root.get(Showtime_.branchId).in(branchSubquery));
            }
            // 5. Filter by format
            if (searchDTO.getFormat() != null) {
                predicates.add(cb.equal(root.get(Showtime_.format), searchDTO.getFormat()));
            }
            // 6. Filter by time period
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
