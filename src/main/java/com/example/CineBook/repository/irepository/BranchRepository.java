package com.example.CineBook.repository.irepository;

import com.example.CineBook.dto.branch.BranchSearchDTO;
import com.example.CineBook.model.Branch;
import com.example.CineBook.repository.base.BaseRepositoryCustom;
import com.example.CineBook.repository.custom.BranchRepositoryCustom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.UUID;

public interface BranchRepository extends JpaRepository<Branch, UUID>, BranchRepositoryCustom, BaseRepositoryCustom<Branch, BranchSearchDTO> {
    boolean existsByName(String name);
    
//    @Modifying
//    @Query("UPDATE Branch b SET b.isDelete = true, b.deleteTime = CURRENT_TIMESTAMP WHERE b.id = :id")
//    void softDeleteById(@Param("id") UUID id);
}
