package com.example.CineBook.service;

import com.example.CineBook.common.dto.response.PageResponse;
import com.example.CineBook.dto.branch.BranchRequest;
import com.example.CineBook.dto.branch.BranchResponse;
import com.example.CineBook.dto.branch.BranchSearchDTO;

import java.util.List;
import java.util.UUID;

public interface BranchService {
    BranchResponse createBranch(BranchRequest request);
    BranchResponse getBranchById(UUID id);
    BranchResponse updateBranch(UUID id, BranchRequest request);
    void deleteBranch(UUID id);
    void deleteBranchCascade(UUID id);
    PageResponse<BranchResponse> searchBranches(BranchSearchDTO searchDTO);
    PageResponse<BranchResponse> getAllBranches(BranchSearchDTO searchDTO);
    BranchResponse restoreBranch(UUID id);
}
