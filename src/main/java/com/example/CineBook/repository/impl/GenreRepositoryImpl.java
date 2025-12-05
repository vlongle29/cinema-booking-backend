package com.example.CineBook.repository.impl;

import com.example.CineBook.dto.genre.GenreSearchDTO;
import com.example.CineBook.model.Genre;
import com.example.CineBook.model.Genre_;
import com.example.CineBook.repository.base.BaseRepositoryImpl;
import com.example.CineBook.repository.custom.GenreRepositoryCustom;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

@Repository
public class GenreRepositoryImpl extends BaseRepositoryImpl<Genre, GenreSearchDTO> implements GenreRepositoryCustom {

    public GenreRepositoryImpl() {
        super(Genre.class);
    }

    @Override
    protected List<Predicate> buildPredicates(Root<Genre> root, CriteriaQuery<?> query, CriteriaBuilder cb, GenreSearchDTO searchDTO) {
        List<Predicate> predicates = new ArrayList<>();

        // Filter by soft delete
        predicates.add(cb.equal(root.get(Genre_.isDelete), false));

        // Search by keyword (name)
        if (StringUtils.hasText(searchDTO.getKeyword())) {
            String keyword = "%" + searchDTO.getKeyword().toLowerCase() + "%";
            predicates.add(cb.like(cb.lower(root.get(Genre_.name)), keyword));
        }

        return predicates;
    }
}
