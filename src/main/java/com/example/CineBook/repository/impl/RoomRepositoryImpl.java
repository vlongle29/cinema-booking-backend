package com.example.CineBook.repository.impl;

import com.example.CineBook.dto.room.RoomSearchDTO;
import com.example.CineBook.model.Room;
import com.example.CineBook.model.Room_;
import com.example.CineBook.repository.base.BaseRepositoryImpl;
import com.example.CineBook.repository.custom.RoomRepositoryCustom;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Repository
public class RoomRepositoryImpl extends BaseRepositoryImpl<Room, RoomSearchDTO> implements RoomRepositoryCustom {

    public RoomRepositoryImpl() {
        super(Room.class);
    }

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public Page<Room> searchWithFilters(RoomSearchDTO searchDTO, Pageable pageable) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Room> query = cb.createQuery(Room.class);
        Root<Room> room = query.from(Room.class);

        List<Predicate> predicates = new ArrayList<>();

        // Filter by soft delete
        predicates.add(cb.equal(room.get(Room_.isDelete), false));

        // Filter by name
        if (StringUtils.hasText(searchDTO.getName())) {
            predicates.add(cb.like(cb.lower(room.get(Room_.name)), 
                "%" + searchDTO.getName().toLowerCase() + "%"));
        }

        // Filter by branchId
        if (searchDTO.getBranchId() != null) {
            predicates.add(cb.equal(room.get(Room_.branchId), searchDTO.getBranchId()));
        }

        if (searchDTO.getManageId() != null) {
            jakarta.persistence.criteria.Subquery<UUID> branchSubquery = query.subquery(UUID.class);
            Root<com.example.CineBook.model.Branch> branchRoot = branchSubquery.from(com.example.CineBook.model.Branch.class);
            branchSubquery.select(branchRoot.get("id"))
                    .where(cb.equal(branchRoot.get("managerId"), searchDTO.getManageId()));
            predicates.add(room.get(Room_.branchId).in(branchSubquery));
        }

        query.where(predicates.toArray(new Predicate[0]));
        query.orderBy(cb.desc(room.get(Room_.createTime)));

        // Pagination
        List<Room> rooms = entityManager.createQuery(query)
                .setFirstResult((int) pageable.getOffset())
                .setMaxResults(pageable.getPageSize())
                .getResultList();

        // Count total
        CriteriaQuery<Long> countQuery = cb.createQuery(Long.class);
        Root<Room> countRoot = countQuery.from(Room.class);

        List<Predicate> countPredicates = new ArrayList<>();
        countPredicates.add(cb.equal(countRoot.get(Room_.isDelete), false));

        if (StringUtils.hasText(searchDTO.getName())) {
            countPredicates.add(cb.like(cb.lower(countRoot.get(Room_.name)),
                    "%" + searchDTO.getName().toLowerCase() + "%"));
        }
        if (searchDTO.getBranchId() != null) {
            countPredicates.add(cb.equal(countRoot.get(Room_.branchId), searchDTO.getBranchId()));
        }

        if (searchDTO.getManageId() != null) {
            jakarta.persistence.criteria.Subquery<UUID> branchSubquery = countQuery.subquery(UUID.class);
            Root<com.example.CineBook.model.Branch> branchRoot = branchSubquery.from(com.example.CineBook.model.Branch.class);
            branchSubquery.select(branchRoot.get("id"))
                    .where(cb.equal(branchRoot.get("managerId"), searchDTO.getManageId()));
            countPredicates.add(countRoot.get(Room_.branchId).in(branchSubquery));
        }

        countQuery.select(cb.count(countRoot));
        countQuery.where(countPredicates.toArray(new Predicate[0]));
        Long total = entityManager.createQuery(countQuery).getSingleResult();

        return new PageImpl<>(rooms, pageable, total);
    }
}
