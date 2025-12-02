package com.example.CineBook.common.security;

import com.example.CineBook.common.constant.PositionEnum;
import com.example.CineBook.common.exception.BusinessException;
import com.example.CineBook.common.exception.MessageCode;
import com.example.CineBook.model.Employee;
import com.example.CineBook.repository.irepository.EmployeeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.UUID;

/**
 * Aspect xử lý kiểm tra position của Employee.
 * Được kích hoạt khi method có annotation @RequirePosition.
 */
@Aspect
@Component
@RequiredArgsConstructor
@Slf4j
public class PositionCheckAspect {

    private final EmployeeRepository employeeRepository;

    /**
     * Kiểm tra position trước khi thực thi method.
     * 
     * @param joinPoint Điểm cắt của AOP
     * @param requirePosition Annotation chứa thông tin position yêu cầu
     * @throws BusinessException Nếu user không phải employee hoặc position không đủ
     */
    @Before("@annotation(requirePosition)")
    public void checkPosition(JoinPoint joinPoint, RequirePosition requirePosition) {
        log.debug("Checking position for method: {}", joinPoint.getSignature().getName());
        
        // Lấy userId từ SecurityContext
        UUID userId = SecurityUtils.getCurrentUserId();
        
        // Kiểm tra user có phải là employee không
        Employee employee = employeeRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(MessageCode.NOT_EMPLOYEE));
        
        // Kiểm tra employee có bị soft delete không
        if (Boolean.TRUE.equals(employee.getIsDelete())) {
            throw new BusinessException(MessageCode.EMPLOYEE_NOT_FOUND);
        }
        
        // Lấy danh sách position được phép
        PositionEnum[] allowedPositions = requirePosition.value();
        
        // Lấy position hiện tại của employee
        PositionEnum currentPosition;
        try {
            currentPosition = PositionEnum.fromValue(employee.getPosition());
        } catch (IllegalArgumentException e) {
            log.error("Invalid position value: {}", employee.getPosition());
            throw new BusinessException(MessageCode.INVALID_POSITION);
        }
        
        // Kiểm tra position có trong danh sách cho phép không
        if (!Arrays.asList(allowedPositions).contains(currentPosition)) {
            log.warn("User {} with position {} tried to access method requiring positions: {}", 
                    userId, currentPosition, Arrays.toString(allowedPositions));
            throw new BusinessException(MessageCode.INSUFFICIENT_POSITION);
        }
        
        log.debug("Position check passed for user {} with position {}", userId, currentPosition);
    }
}
