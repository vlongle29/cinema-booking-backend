package com.example.CineBook.service.impl;

import com.example.CineBook.common.constant.PositionEnum;
import com.example.CineBook.common.constant.RoleEnum;
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
import com.example.CineBook.model.Position;
import com.example.CineBook.model.SysRole;
import com.example.CineBook.model.SysUser;
import com.example.CineBook.model.SysUserRole;
import com.example.CineBook.repository.irepository.BranchRepository;
import com.example.CineBook.repository.irepository.EmployeeRepository;
import com.example.CineBook.repository.irepository.PositionRepository;
import com.example.CineBook.repository.irepository.SysRoleRepository;
import com.example.CineBook.repository.irepository.SysUserRepository;
import com.example.CineBook.repository.irepository.SysUserRoleRepository;
import com.example.CineBook.service.EmployeeService;
import com.example.CineBook.service.SysUserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmployeeServiceImpl implements EmployeeService {

    private final EmployeeMapper employeeMapper;
    private final EmployeeRepository employeeRepository;
    private final SysRoleRepository sysRoleRepository;
    private final SysUserService sysUserService;
    private final BranchRepository branchRepository;
    private final PositionRepository positionRepository;
    private final SysUserRepository sysUserRepository;
    private final SysUserRoleRepository sysUserRoleRepository;

    private static final String ROLE_STAFF_CODE = "STAFF";
    private static final String ROLE_MANAGER_CODE = "MANAGER";

    @Override
    @Transactional
    public EmployeeResponse createEmployee(EmployeeCreateRequest request) {
        // Auto-generate a unique employee code: NV001, NV002, ...
        String generatedCode = generateUniqueEmployeeCode();

        // Validate position exists
        if (request.getPositionId() != null) {
            positionRepository.findById(request.getPositionId())
                    .orElseThrow(() -> new BusinessException(MessageCode.POSITION_NOT_FOUND));
        }

        // find staff role id by code
        SysRole staffRole = sysRoleRepository.findByCode(ROLE_STAFF_CODE)
                .orElseThrow(() -> new BusinessException(MessageCode.STAFF_ROLE_NOT_CONFIGURED));
        SysRole managerRole = sysRoleRepository.findByCode(ROLE_MANAGER_CODE)
                .orElseThrow(() -> new BusinessException(MessageCode.MANAGER_ROLE_NOT_CONFIGURED));

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
                .branchId(request.getBranchId())
                .build();

        Position position = positionRepository.findById(request.getPositionId())
                .orElseThrow(() -> new BusinessException(MessageCode.POSITION_NOT_FOUND));

        if (!position.getCode().equals(PositionEnum.BRANCH_MANAGER.name())) {
            userReq.setRoleIds(List.of(staffRole.getId()));
            userReq.setTypeAccount(RoleEnum.STAFF.name());
        } else {
            userReq.setRoleIds(List.of(managerRole.getId()));
            userReq.setTypeAccount(RoleEnum.MANAGER.name());
        }

        // Create user (this method will also insert user_role)
        UserInfoResponse createdUser = sysUserService.createUser(userReq);

        // Create employee profile with auto-generated code
        Employee employee = employeeMapper.toEntity(request, createdUser.getId());
        employee.setEmployeeCode(generatedCode);
        Employee saved = employeeRepository.save(employee);

        return enrichEmployeeResponse(saved);
    }

    /**
     * Generates a unique employee code in the format NVxxx (e.g. NV001, NV042).
     * Keeps incrementing the suffix until a free slot is found.
     */
    private String generateUniqueEmployeeCode() {
        long count = employeeRepository.count();
        int seq = (int) (count + 1);
        String code;
        do {
            code = String.format("NV%03d", seq);
            seq++;
        } while (employeeRepository.existsByEmployeeCode(code));
        return code;
    }


    @Override
    public PageResponse<EmployeeResponse> getEmployeesByBranch(UUID branchId, int page, int size) {
        branchRepository.findById(branchId)
                .orElseThrow(() -> new BusinessException(MessageCode.BRANCH_NOT_FOUND));

        EmployeeSearchDTO searchDTO = new EmployeeSearchDTO();
        searchDTO.setBranchId(branchId);
        searchDTO.setPage(page);
        searchDTO.setSize(size);

        Pageable pageable = PageRequest.of(page, size);
        Page<Employee> employeePage = employeeRepository.searchWithFilters(searchDTO, pageable);
        return PageResponse.of(toEnrichedPage(employeePage));
    }

    @Override
    public EmployeeResponse getEmployeeInfo(UUID userId) {
        Employee employee = employeeRepository.findByUserId(userId)
                .orElseThrow(() -> new BusinessException(MessageCode.EMPLOYEE_NOT_FOUND));

        return enrichEmployeeResponse(employee);
    }

    private EmployeeResponse enrichEmployeeResponse(Employee employee) {
        EmployeeResponse response = employeeMapper.toResponse(employee);
        
        // Fetch user info
        sysUserRepository.findById(employee.getUserId()).ifPresent(user -> {
            response.setUsername(user.getUsername());
            response.setName(user.getName());
            response.setEmail(user.getEmail());
            response.setPhone(user.getPhone());
        });
        
        // Fetch position name
        if (employee.getPositionId() != null) {
            positionRepository.findById(employee.getPositionId())
                    .ifPresent(position -> response.setPositionName(position.getName()));
        }
        
        // Fetch branch name
        if (employee.getBranchId() != null) {
            branchRepository.findById(employee.getBranchId())
                    .ifPresent(branch -> response.setBranchName(branch.getName()));
        }
        
        return response;
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
        if (request.getPositionId() != null) {
            positionRepository.findById(request.getPositionId())
                    .orElseThrow(() -> new BusinessException(MessageCode.POSITION_NOT_FOUND));
            employee.setPositionId(request.getPositionId());
        }

        Employee updated = employeeRepository.save(employee);
        return enrichEmployeeResponse(updated);
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

        return enrichEmployeeResponse(updated);
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
        Pageable pageable = PageRequest.of(searchDTO.getPage() - 1, searchDTO.getSize());
        Page<Employee> entityPage = employeeRepository.searchWithFilters(searchDTO, pageable);
        return PageResponse.of(toEnrichedPage(entityPage));
    }

    @Override
    @Transactional
    public void deactivateEmployee(UUID userId) {
        Employee employee = employeeRepository.findByUserId(userId)
                .orElseThrow(() -> new BusinessException(MessageCode.EMPLOYEE_NOT_FOUND));

        employeeRepository.softDeleteById(employee.getId());

        SysUser user = sysUserRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(MessageCode.USER_NOT_FOUND));
        user.setIsDelete(true);
        sysUserRepository.save(user);
    }

    private Page<EmployeeResponse> toEnrichedPage(Page<Employee> entityPage) {
        List<Employee> employees = entityPage.getContent();
        if (employees.isEmpty()) return entityPage.map(e -> employeeMapper.toResponse(e));

        Set<UUID> userIds = employees.stream().map(Employee::getUserId).collect(Collectors.toSet());
        Set<UUID> branchIds = employees.stream().map(Employee::getBranchId).filter(Objects::nonNull).collect(Collectors.toSet());
        Set<UUID> positionIds = employees.stream().map(Employee::getPositionId).filter(Objects::nonNull).collect(Collectors.toSet());

        Map<UUID, SysUser> userMap = sysUserRepository.findAllById(userIds)
                .stream().collect(Collectors.toMap(SysUser::getId, u -> u));
        Map<UUID, String> branchMap = branchRepository.findAllById(branchIds)
                .stream().collect(Collectors.toMap(Branch::getId, Branch::getName));
        Map<UUID, String> positionMap = positionRepository.findAllById(positionIds)
                .stream().collect(Collectors.toMap(Position::getId, Position::getName));

        // Build userId -> roleName map
        List<SysUserRole> userRoles = userIds.stream()
                .flatMap(uid -> sysUserRoleRepository.findByUserId(uid).stream())
                .collect(Collectors.toList());
        Set<UUID> allRoleIds = userRoles.stream().map(SysUserRole::getRoleId).collect(Collectors.toSet());
        Map<UUID, String> roleNameMap = sysRoleRepository.findAllById(allRoleIds)
                .stream().collect(Collectors.toMap(SysRole::getId, SysRole::getName));
        Map<UUID, String> userRoleMap = new java.util.HashMap<>();
        userRoles.forEach(ur -> userRoleMap.putIfAbsent(ur.getUserId(), roleNameMap.get(ur.getRoleId())));

        List<EmployeeResponse> responses = employees.stream().map(employee -> {
            EmployeeResponse response = employeeMapper.toResponse(employee);
            SysUser user = userMap.get(employee.getUserId());
            if (user != null) {
                response.setUsername(user.getUsername());
                response.setName(user.getName());
                response.setEmail(user.getEmail());
                response.setPhone(user.getPhone());
                response.setHireDate(user.getCreateTime() != null
                        ? user.getCreateTime().atZone(java.time.ZoneId.systemDefault()).toLocalDate()
                        : null);
            }
            response.setBranchName(branchMap.get(employee.getBranchId()));
            response.setPositionName(positionMap.get(employee.getPositionId()));
            response.setRole(userRoleMap.get(employee.getUserId()));
            return response;
        }).collect(Collectors.toList());

        return new org.springframework.data.domain.PageImpl<>(responses, entityPage.getPageable(), entityPage.getTotalElements());
    }





}
