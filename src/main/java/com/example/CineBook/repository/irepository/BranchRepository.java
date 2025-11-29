package com.example.CineBook.repository.irepository;

import com.example.CineBook.dto.branch.BranchSearchDTO;
import com.example.CineBook.model.Branch;
import com.example.CineBook.repository.base.BaseRepositoryCustom;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface BranchRepository extends JpaRepository<Branch, UUID>, BaseRepositoryCustom<Branch, BranchSearchDTO> {
    boolean existsByName(String name);
}
