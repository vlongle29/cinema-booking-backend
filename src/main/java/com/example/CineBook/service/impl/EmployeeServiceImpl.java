package com.example.CineBook.service.impl;

import com.example.CineBook.common.dto.response.PageResponse;
import com.example.CineBook.common.exception.BusinessException;
import com.example.CineBook.common.exception.MessageCode;
import com.example.CineBook.dto.employee.EmployeeCreateRequest;
import com.example.CineBook.dto.employee.EmployeeResponse;
import com.example.CineBook.dto.employee.EmployeeSearchDTO;
import com.example.CineBook.dto.employee.EmployeeUpdateRequest;
import com.example.CineBook.dto.sysUser.UserCreateRequest;
import com.example.CineBook.dto.sysUser.UserInfoResponse;
import com.example.CineBook.mapper.EmployeeMapper;
import com.example.CineBook.model.Branch;
import com.example.CineBook.model.Employee;
import com.example.CineBook.model.SysRole;
import com.example.CineBook.repository.irepository.BranchRepository;
import com.example.CineBook.repository.irepository.EmployeeRepository;
import com.example.CineBook.repository.irepository.SysRoleRepository;
import com.example.CineBook.repository.irepository.SysUserRepository;
import com.example.CineBook.service.EmployeeService;
import com.example.CineBook.service.SysUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class EmployeeServiceImpl implements EmployeeService {

    private final EmployeeMapper employeeMapper;
    private final EmployeeRepository employeeRepository;
    private final SysRoleRepository sysRoleRepository;
    private final SysUserService sysUserService;
    private final BranchRepository branchRepository;

    private static final String ROLE_STAFF_CODE = "STAFF";

    @Override
    @Transactional
    public EmployeeResponse createEmployee(EmployeeCreateRequest request) {
        // Validate employee code uniqueness
        if (request.getEmployeeCode() != null && employeeRepository.existsByEmployeeCode(request.getEmployeeCode())) {
            throw new BusinessException(MessageCode.EMPLOYEE_CODE_ALREADY_EXISTS);
        }

        // Validate position
        try {
            com.example.CineBook.common.constant.PositionEnum.fromValue(request.getPosition());
        } catch (IllegalArgumentException e) {
            throw new BusinessException(MessageCode.INVALID_POSITION);
        }

        // find staff role id by code
        SysRole staffRole = sysRoleRepository.findByCode(ROLE_STAFF_CODE)
                .orElseThrow(() -> new BusinessException(MessageCode.STAFF_ROLE_NOT_CONFIGURED));

        // Assign branch for employee
        branchRepository.findById(request.getBranchId())
                .orElseThrow(() -> new BusinessException(MessageCode.BRANCH_NOT_FOUND));

        // Build user create request and assign STAFF role
        UserCreateRequest userReq = UserCreateRequest.builder()
                .username(request.getUsername())
                .name(request.getName())
                .email(request.getEmail())
                .phone(request.getPhone())
                .password(request.getPassword())
                .roleIds(List.of(staffRole.getId()))
                .build();

        // Create user (this method will also insert user_role)
        UserInfoResponse createdUser = sysUserService.createUser(userReq);

        // Create employee profile
        Employee employee = employeeMapper.toEntity(request, createdUser.getId());
        Employee saved = employeeRepository.save(employee);

        return employeeMapper.toResponse(saved);
    }

    @Override
    public PageResponse<EmployeeResponse> getEmployeesByBranch(UUID branchId, int page, int size) {
        branchRepository.findById(branchId)
                .orElseThrow(() -> new BusinessException(MessageCode.BRANCH_NOT_FOUND));

        EmployeeSearchDTO searchDTO = new EmployeeSearchDTO();
        searchDTO.setBranchId(branchId);
        searchDTO.setPage(page);
        searchDTO.setSize(size);

        Page<Employee> employeePage = employeeRepository.findAllWithFilters(searchDTO);
        Page<EmployeeResponse> responsePage = employeeMapper.mapPage(employeePage, Collections.emptyMap());

        return PageResponse.of(responsePage);
    }

    @Override
    public EmployeeResponse getEmployeeInfo(UUID userId) {
        Employee employee = employeeRepository.findByUserId(userId)
                .orElseThrow(() -> new BusinessException(MessageCode.EMPLOYEE_NOT_FOUND));

        return employeeMapper.toResponse(employee);
    }

    @Override
    @Transactional
    public EmployeeResponse updateEmployee(UUID userId, EmployeeUpdateRequest request) {
        Employee employee = employeeRepository.findByUserId(userId)
                .orElseThrow(() -> new BusinessException(MessageCode.EMPLOYEE_NOT_FOUND));

        if (request.getSalary() != null) {
            employee.setSalary(request.getSalary());
        }
        if (request.getHireDate() != null) {
            employee.setHireDate(request.getHireDate());
        }
        if (request.getBranchId() != null) {
            branchRepository.findById(request.getBranchId())
                    .orElseThrow(() -> new BusinessException(MessageCode.BRANCH_NOT_FOUND));
            employee.setBranchId(request.getBranchId());
        }
        if (request.getPosition() != null) {
            try {
                com.example.CineBook.common.constant.PositionEnum.fromValue(request.getPosition());
                employee.setPosition(request.getPosition());
            } catch (IllegalArgumentException e) {
                throw new BusinessException(MessageCode.INVALID_POSITION);
            }
        }

        Employee updated = employeeRepository.save(employee);
        return employeeMapper.toResponse(updated);
    }

    @Override
    @Transactional
    public EmployeeResponse transferEmployee(UUID userId, UUID branchId) {
        Employee employee = employeeRepository.findByUserId(userId)
                .orElseThrow(() -> new BusinessException(MessageCode.EMPLOYEE_NOT_FOUND));

        branchRepository.findById(branchId)
                .orElseThrow(() -> new BusinessException(MessageCode.BRANCH_NOT_FOUND));

        employee.setBranchId(branchId);
        Employee updated = employeeRepository.save(employee);

        return employeeMapper.toResponse(updated);
    }

    @Override
    @Transactional
    public void setBranchManager(UUID branchId, UUID managerId) {
        Branch branch = branchRepository.findById(branchId)
                .orElseThrow(() -> new BusinessException(MessageCode.BRANCH_NOT_FOUND));

        Employee employee = employeeRepository.findByUserId(managerId)
                .orElseThrow(() -> new BusinessException(MessageCode.EMPLOYEE_NOT_FOUND));

        branch.setManagerId(managerId);
        branchRepository.save(branch);
    }

    /**
     * Search Employee with pagination and filter.
     *
     * @param searchDTO The search criteria and pagination information.
     * @return
     */
    @Override
    public PageResponse<EmployeeResponse> searchEmployees(EmployeeSearchDTO searchDTO) {
        Page<Employee> entityPage = employeeRepository.findAllWithFilters(searchDTO);
        Page<EmployeeResponse> responsePage = employeeMapper.mapPage(entityPage, Collections.emptyMap());

        return PageResponse.of(responsePage);
    }





}
