package com.example.CineBook.repository.impl;

import com.example.CineBook.dto.room.RoomSearchDTO;
import com.example.CineBook.model.Room;
import com.example.CineBook.repository.base.BaseRepositoryImpl;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

@Repository
public class RoomRepositoryImpl extends BaseRepositoryImpl<Room, RoomSearchDTO> {

    public RoomRepositoryImpl() {
        super(Room.class);
    }

    @Override
    protected List<Predicate> buildPredicates(Root<Room> root, CriteriaQuery<?> query, CriteriaBuilder cb, RoomSearchDTO searchDTO) {
        List<Predicate> predicates = new ArrayList<>();

        if (StringUtils.hasText(searchDTO.getName())) {
            predicates.add(cb.like(cb.lower(root.get("name")), "%" + searchDTO.getName().toLowerCase() + "%"));
        }

        if (searchDTO.getBranchId() != null) {
            predicates.add(cb.equal(root.get("branchId"), searchDTO.getBranchId()));
        }

        return predicates;
    }
}
