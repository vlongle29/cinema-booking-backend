package com.example.CineBook.repository.impl;

import com.example.CineBook.dto.movie.MovieSearchDTO;
import com.example.CineBook.model.Movie;
import com.example.CineBook.model.Movie_;
import com.example.CineBook.model.Showtime;
import com.example.CineBook.model.Showtime_;
import com.example.CineBook.repository.custom.MovieRepositoryCustom;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Subgraph;
import jakarta.persistence.criteria.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Repository
public class MovieRepositoryImpl implements MovieRepositoryCustom {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public Page<Movie> searchWithFilters(MovieSearchDTO searchDTO, Pageable pageable) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Movie> query = cb.createQuery(Movie.class);
        Root<Movie> movie = query.from(Movie.class);

        List<Predicate> predicates = buildPredicates(cb, movie, query, searchDTO);

        query.where(predicates.toArray(new Predicate[0]));
        query.orderBy(cb.desc(movie.get(Movie_.releaseDate)));

        List<Movie> movies = entityManager.createQuery(query)
                .setFirstResult((int) pageable.getOffset())
                .setMaxResults(pageable.getPageSize())
                .getResultList();

        Long total = countTotal(cb, searchDTO);

        return new PageImpl<>(movies, pageable, total);
    }

    private List<Predicate> buildPredicates(CriteriaBuilder cb, Root<Movie> movie, CriteriaQuery<?> query, MovieSearchDTO searchDTO) {
        List<Predicate> predicates = new ArrayList<>();

        predicates.add(cb.equal(movie.get(Movie_.isDelete), false));

        if (StringUtils.hasText(searchDTO.getKeyword())) {
            predicates.add(cb.like(cb.lower(movie.get(Movie_.title)),
                    "%" + searchDTO.getKeyword().toLowerCase() + "%"));
        }

        if (StringUtils.hasText(searchDTO.getStatus())) {
            LocalDate now = LocalDate.now();

            // Subquery: Check if there will be showtime in the future
            Subquery<Long> showtimeSub = query.subquery(Long.class);
            Root<Showtime> st = showtimeSub.from(Showtime.class);

            showtimeSub.select(cb.count(st));
            showtimeSub.where(
                    cb.equal(st.get(Showtime_.movieId), movie.get(Movie_.id)),
                    cb.equal(st.get(Showtime_.isDelete), false),
                    cb.greaterThanOrEqualTo(st.get(Showtime_.startTime), now.atStartOfDay()));

            if ("SHOWING".equals(searchDTO.getStatus())) {
                // There will be at least one showtime in the future
                predicates.add(cb.greaterThan(showtimeSub, 0L));
            } else if ("COMING_SOON".equals(searchDTO.getStatus())) {
                // Not shown yet: releaseDate > today
                predicates.add(cb.greaterThan(movie.get(Movie_.releaseDate), LocalDate.now()));
                // There will be no showtime in the future
                predicates.add(cb.equal(showtimeSub, 0L));
            } else {
                // For other statuses (ENDED), use movie.status field
                predicates.add(cb.equal(movie.get(Movie_.status), searchDTO.getStatus()));
            }
        }

        if (searchDTO.getReleaseDateFrom() != null) {
            predicates.add(cb.greaterThanOrEqualTo(movie.get(Movie_.releaseDate), searchDTO.getReleaseDateFrom()));
        }

        if (searchDTO.getReleaseDateTo() != null) {
            predicates.add(cb.lessThanOrEqualTo(movie.get(Movie_.releaseDate), searchDTO.getReleaseDateTo()));
        }

        return predicates;
    }

    private Long countTotal(CriteriaBuilder cb, MovieSearchDTO searchDTO) {
        CriteriaQuery<Long> countQuery = cb.createQuery(Long.class);
        Root<Movie> countRoot = countQuery.from(Movie.class);

        List<Predicate> countPredicates = buildPredicates(cb, countRoot, countQuery,  searchDTO);

        countQuery.select(cb.count(countRoot));
        countQuery.where(countPredicates.toArray(new Predicate[0]));

        return entityManager.createQuery(countQuery).getSingleResult();
    }
}
