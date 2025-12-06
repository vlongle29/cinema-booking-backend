package com.example.CineBook.repository.impl;

import com.example.CineBook.dto.position.PositionSearchDTO;
import com.example.CineBook.model.Position;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

@Repository
public class PositionRepositoryImpl {

    public Specification<Position> searchWithFilters(PositionSearchDTO searchDTO) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            predicates.add(cb.isFalse(root.get("isDelete")));

            if (StringUtils.hasText(searchDTO.getCode())) {
                predicates.add(cb.like(cb.lower(root.get("code")), 
                    "%" + searchDTO.getCode().toLowerCase() + "%"));
            }

            if (StringUtils.hasText(searchDTO.getName())) {
                predicates.add(cb.like(cb.lower(root.get("name")), 
                    "%" + searchDTO.getName().toLowerCase() + "%"));
            }

            if (StringUtils.hasText(searchDTO.getSearchTerm())) {
                String searchPattern = "%" + searchDTO.getSearchTerm().toLowerCase() + "%";
                Predicate searchPredicate = cb.or(
                    cb.like(cb.lower(root.get("code")), searchPattern),
                    cb.like(cb.lower(root.get("name")), searchPattern),
                    cb.like(cb.lower(root.get("description")), searchPattern)
                );
                predicates.add(searchPredicate);
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
