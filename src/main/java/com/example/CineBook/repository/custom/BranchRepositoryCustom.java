package com.example.CineBook.repository.custom;

import com.example.CineBook.dto.branch.BranchSearchDTO;
import com.example.CineBook.model.Branch;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.NoRepositoryBean;

@NoRepositoryBean
public interface BranchRepositoryCustom {
    Page<Branch> searchWithFilters(BranchSearchDTO searchDTO, Pageable pageable);
}
