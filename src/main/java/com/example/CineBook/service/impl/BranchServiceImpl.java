package com.example.CineBook.service.impl;

import com.example.CineBook.common.dto.response.PageResponse;
import com.example.CineBook.common.exception.BusinessException;
import com.example.CineBook.common.exception.MessageCode;
import com.example.CineBook.dto.branch.BranchRequest;
import com.example.CineBook.dto.branch.BranchResponse;
import com.example.CineBook.dto.branch.BranchSearchDTO;
import com.example.CineBook.mapper.BranchMapper;
import com.example.CineBook.model.Branch;
import com.example.CineBook.repository.irepository.BranchRepository;
import com.example.CineBook.service.BranchService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BranchServiceImpl implements BranchService {

    private final BranchRepository branchRepository;
    private final BranchMapper branchMapper;

    @Override
    @Transactional
    public BranchResponse createBranch(BranchRequest request) {
        if (branchRepository.existsByName(request.getName())) {
            throw new BusinessException(MessageCode.BRANCH_ALREADY_EXISTS);
        }

        Branch branch = branchMapper.toEntity(request);
        Branch saved = branchRepository.save(branch);
        return branchMapper.toResponse(saved);
    }

    @Override
    public BranchResponse getBranchById(UUID id) {
        Branch branch = branchRepository.findById(id)
                .orElseThrow(() -> new BusinessException(MessageCode.BRANCH_NOT_FOUND));
        return branchMapper.toResponse(branch);
    }

    @Override
    @Transactional
    public BranchResponse updateBranch(UUID id, BranchRequest request) {
        Branch branch = branchRepository.findById(id)
                .orElseThrow(() -> new BusinessException(MessageCode.BRANCH_NOT_FOUND));
        branchMapper.updateEntity(request, branch);
        Branch updated = branchRepository.save(branch);
        return branchMapper.toResponse(updated);
    }

    @Override
    @Transactional
    public void deleteBranch(UUID id) {
        if (!branchRepository.existsById(id)) {
            throw new BusinessException(MessageCode.BRANCH_NOT_FOUND);
        }
        branchRepository.softDeleteById(id);
    }

    @Override
    public PageResponse<BranchResponse> searchBranches(BranchSearchDTO searchDTO) {
        Page<Branch> entityPage = branchRepository.findAllWithFilters(searchDTO);
        Page<BranchResponse> responsePage = entityPage.map(branchMapper::toResponse);
        return PageResponse.of(responsePage);
    }

    @Override
    public PageResponse<BranchResponse> getAllBranches(BranchSearchDTO searchDTO) {
        Page<Branch> entityPage = branchRepository.findAllWithFilters(searchDTO);
        Page<BranchResponse> responsePage = entityPage.map(branchMapper::toResponse);
        return PageResponse.of(responsePage);
    }
}
