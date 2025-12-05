package com.example.CineBook.service.impl;

import com.example.CineBook.common.constant.GenderEnum;
import com.example.CineBook.common.constant.SystemFlag;
import com.example.CineBook.common.dto.response.PageResponse;
import com.example.CineBook.common.exception.BusinessException;
import com.example.CineBook.common.exception.MessageCode;
import com.example.CineBook.common.security.SecurityUtils;
import com.example.CineBook.dto.customer.CustomerCreateRequest;
import com.example.CineBook.dto.customer.CustomerResponse;
import com.example.CineBook.dto.customer.CustomerSearchDTO;
import com.example.CineBook.dto.customer.CustomerUpdateRequest;
import com.example.CineBook.dto.sysUser.UserCreateRequest;
import com.example.CineBook.dto.sysUser.UserInfoResponse;
import com.example.CineBook.mapper.CustomerMapper;
import com.example.CineBook.model.Customer;
import com.example.CineBook.model.SysRole;
import com.example.CineBook.model.SysUser;
import com.example.CineBook.repository.irepository.CustomerRepository;
import com.example.CineBook.repository.irepository.SysRoleRepository;
import com.example.CineBook.repository.irepository.SysUserRepository;
import com.example.CineBook.service.CustomerService;
import com.example.CineBook.service.SysUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CustomerServiceImpl implements CustomerService {

    private final CustomerRepository customerRepository;
    private final SysUserRepository sysUserRepository;
    private final SysRoleRepository sysRoleRepository;
    private final SysUserService sysUserService;
    private final CustomerMapper customerMapper;

    private static final String ROLE_CUSTOMER_CODE = "CUSTOMER";

    @Override
    @Transactional
    public CustomerResponse createCustomer(CustomerCreateRequest request) {
        // Find CUSTOMER role
        SysRole customerRole = sysRoleRepository.findByCode(ROLE_CUSTOMER_CODE)
                .orElseThrow(() -> new BusinessException(MessageCode.SYS_ROLE_NOT_FOUND));

        // Create User with CUSTOMER role
        UserCreateRequest userReq = UserCreateRequest.builder()
                .username(request.getUsername())
                .name(request.getName())
                .email(request.getEmail())
                .phone(request.getPhone())
                .password(request.getPassword())
                .roleIds(List.of(customerRole.getId()))
                .systemFlag(SystemFlag.NORMAL.getValue())
                .build();

        UserInfoResponse createdUser = sysUserService.createUser(userReq);

        // Create Customer profile
        Customer customer = customerMapper.toEntity(request, createdUser.getId());
        Customer saved = customerRepository.save(customer);

        return buildResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public CustomerResponse getCustomerById(UUID userId) {
        Customer customer = customerRepository.findByUserId(userId)
                .orElseThrow(() -> new BusinessException(MessageCode.ENTITY_NOT_FOUND));

        if (Boolean.TRUE.equals(customer.getIsDelete())) {
            throw new BusinessException(MessageCode.ENTITY_NOT_FOUND);
        }

        return buildResponse(customer);
    }

    @Override
    @Transactional
    public CustomerResponse updateCustomer(UUID userId, CustomerUpdateRequest request) {
        Customer customer = customerRepository.findByUserId(userId)
                .orElseThrow(() -> new BusinessException(MessageCode.ENTITY_NOT_FOUND));

        SysUser user = sysUserRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(MessageCode.USER_NOT_FOUND));

        // Update User info
        if (request.getName() != null) {
            user.setName(request.getName());
        }
        if (request.getPhone() != null) {
            user.setPhone(request.getPhone());
        }
        sysUserRepository.save(user);

        // Update Customer info
        if (request.getDateOfBirth() != null) {
            customer.setDateOfBirth(request.getDateOfBirth());
        }
        if (request.getGender() != null) {
            customer.setGender(request.getGender());
        }
        if (request.getAddress() != null) {
            customer.setAddress(request.getAddress());
        }
        if (request.getCity() != null) {
            customer.setCity(request.getCity());
        }

        Customer updated = customerRepository.save(customer);

        customerMapper.toResponse(updated);
        return buildResponse(updated);
    }

    @Override
    @Transactional
    public void deleteCustomer(UUID userId) {
        Customer customer = customerRepository.findByUserId(userId)
                .orElseThrow(() -> new BusinessException(MessageCode.ENTITY_NOT_FOUND));

        customer.setIsDelete(true);
        customerRepository.save(customer);

        // Soft delete user
        SysUser user = sysUserRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(MessageCode.USER_NOT_FOUND));
        user.setIsDelete(true);
        sysUserRepository.save(user);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<CustomerResponse> searchCustomers(CustomerSearchDTO searchDTO) {
        Pageable pageable = PageRequest.of(searchDTO.getPage(), searchDTO.getSize());
        Page<Customer> entityPage = customerRepository.searchWithFilters(searchDTO, pageable);
        Page<CustomerResponse> responsePage = customerMapper.mapPage(entityPage, Collections.emptyMap());
        return PageResponse.of(responsePage);
    }

    @Override
    @Transactional(readOnly = true)
    public CustomerResponse getCurrentCustomer() {
        UUID userId = SecurityUtils.getCurrentUserId();
        return getCustomerById(userId);
    }

    @Override
    @Transactional
    public CustomerResponse updateCurrentCustomer(CustomerUpdateRequest request) {
        UUID userId = SecurityUtils.getCurrentUserId();
        return updateCustomer(userId, request);
    }

    private CustomerResponse buildResponse(Customer customer) {
        SysUser user = sysUserRepository.findById(customer.getUserId())
                .orElseThrow(() -> new BusinessException(MessageCode.USER_NOT_FOUND));

        return CustomerResponse.builder()
                .userId(customer.getUserId())
                .username(user.getUsername())
                .name(user.getName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .dateOfBirth(customer.getDateOfBirth())
                .gender(customer.getGender())
                .address(customer.getAddress())
                .city(customer.getCity())
                .membershipLevel(customer.getMembershipLevel())
                .loyaltyPoints(customer.getLoyaltyPoints())
                .build();
    }
}
