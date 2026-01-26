package com.example.CineBook.service.impl;

import com.example.CineBook.common.constant.RoleEnum;
import com.example.CineBook.common.dto.response.PageResponse;
import com.example.CineBook.common.exception.BusinessException;
import com.example.CineBook.common.exception.MessageCode;
import com.example.CineBook.dto.branch.BranchRequest;
import com.example.CineBook.dto.branch.BranchResponse;
import com.example.CineBook.dto.branch.BranchSearchDTO;
import com.example.CineBook.dto.branch.BranchUpdateRequest;
import com.example.CineBook.dto.sysRole.SysRoleResponse;
import com.example.CineBook.mapper.BranchMapper;
import com.example.CineBook.model.*;
import com.example.CineBook.repository.irepository.*;
import com.example.CineBook.service.BranchService;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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
    private final RoomRepository roomRepository;
    private final SeatRepository seatRepository;
    private final SysRoleRepository sysRoleRepository;
    private final CityRepository cityRepository;

    @CachePut(value = "branchCache", key = "#result.id")
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

    @Cacheable(value = "branchCache", key = "#id")
    @Override
    public BranchResponse getBranchById(UUID id) {
        Branch branch = branchRepository.findById(id)
                .orElseThrow(() -> new BusinessException(MessageCode.BRANCH_NOT_FOUND));
        return branchMapper.toResponse(branch);
    }

    @CacheEvict(value = "branchCache", key = "#id")
    @Override
    @Transactional
    public BranchResponse updateBranch(UUID id, BranchUpdateRequest request) {
        Branch branch = branchRepository.findById(id)
                .orElseThrow(() -> new BusinessException(MessageCode.BRANCH_NOT_FOUND));

        cityRepository.findById(request.getCityId())
                .orElseThrow(() -> new BusinessException(MessageCode.CITY_NOT_FOUND));

        if (request.getManagerId() != null) {
            List<SysRoleResponse> roles = sysRoleRepository.getRoleInfoByUserId(request.getManagerId());
            boolean isAdmin = roles.stream()
                    .anyMatch(role -> RoleEnum.ADMIN.getValue().equals(role.getCode()));

            if (!isAdmin) {
                throw new BusinessException(MessageCode.ROLE_MUST_BE_ADMIN);
            }
        }

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

        long roomCount = roomRepository.countByBranchId(id);
        if (roomCount > 0) {
            throw new BusinessException(MessageCode.BRANCH_HAS_ROOMS);
        }

        branchRepository.softDeleteById(id);
    }

    @Override
    @Transactional
    public void deleteBranchCascade(UUID id) {
        if (!branchRepository.existsById(id)) {
            throw new BusinessException(MessageCode.BRANCH_NOT_FOUND);
        }

        List<Room> rooms = roomRepository.findByBranchId(id);
        if (!rooms.isEmpty()) {
            List<UUID> roomIds = rooms.stream().map(Room::getId).collect(Collectors.toList());

            for (UUID roomId : roomIds) {
                List<Seat> seats = seatRepository.findByRoomId(roomId);
                if (!seats.isEmpty()) {
                    for (Seat seat : seats) {
                        seat.setIsDelete(true);
                    }
                    seatRepository.saveAll(seats);
                }
            }

            roomRepository.softDeleteByIds(roomIds);
        }

        branchRepository.softDeleteById(id);
    }

    @Override
    public PageResponse<BranchResponse> searchBranches(BranchSearchDTO searchDTO) {
        Pageable pageable = PageRequest.of(searchDTO.getPage() - 1, searchDTO.getSize());
        Page<Branch> entityPage = branchRepository.searchWithFilters(searchDTO, pageable);
        Page<BranchResponse> responsePage = entityPage.map(branchMapper::toResponse);
        return PageResponse.of(responsePage);
    }

    @Override
    public PageResponse<BranchResponse> getAllBranches(BranchSearchDTO searchDTO) {
        Pageable pageable = PageRequest.of(searchDTO.getPage() - 1, searchDTO.getSize());
        Page<Branch> entityPage = branchRepository.searchWithFilters(searchDTO, pageable);
        Page<BranchResponse> responsePage = entityPage.map(branchMapper::toResponse);
        return PageResponse.of(responsePage);
    }

    @Override
    @Transactional
    public BranchResponse restoreBranch(UUID id) {
        if (!branchRepository.existsById(id)) {
            throw new BusinessException(MessageCode.BRANCH_NOT_FOUND);
        }
        // Restore branch
        branchRepository.restoreById(id);

        // Restore all rooms
        List<Room> rooms = roomRepository.findByBranchId(id);
        if (!rooms.isEmpty()) {
            List<UUID> roomIds = rooms.stream().map(Room::getId).collect(Collectors.toList());
            roomRepository.restoreByIds(roomIds);

            // Restore all seats
            for (UUID roomId : roomIds) {
                List<Seat> seats = seatRepository.findByRoomId(roomId);
                if (!seats.isEmpty()) {
                    for (Seat seat : seats) {
                        seat.setIsDelete(false);
                    }
                    seatRepository.saveAll(seats);
                }
            }
        }
        Branch branch = branchRepository.findById(id)
                .orElseThrow(() -> new BusinessException(MessageCode.BRANCH_NOT_FOUND));
        return branchMapper.toResponse(branch);
    }
}
