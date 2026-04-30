package com.example.CineBook.common.security;

import com.example.CineBook.common.exception.BusinessException;
import com.example.CineBook.common.exception.MessageCode;
import com.example.CineBook.model.Employee;
import com.example.CineBook.model.Position;
import com.example.CineBook.repository.irepository.EmployeeRepository;
import com.example.CineBook.repository.irepository.PositionRepository;
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
    private final PositionRepository positionRepository;

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
        
        // Bypass position check cho SUPER_ADMIN và ADMIN
        if (SecurityUtils.hasRole("SUPER_ADMIN") || SecurityUtils.hasRole("ADMIN")) {
            log.debug("Bypassing position check for SUPER_ADMIN/ADMIN user: {}", userId);
            return;
        }
        
        // Kiểm tra user có phải là employee không
        Employee employee = employeeRepository.findByUserId(userId)
                .orElseThrow(() -> new BusinessException(MessageCode.NOT_EMPLOYEE));
        
        // Kiểm tra employee có bị soft delete không
        if (Boolean.TRUE.equals(employee.getIsDelete())) {
            throw new BusinessException(MessageCode.EMPLOYEE_NOT_FOUND);
        }
        
        // Lấy danh sách position code được phép
        String[] allowedPositionCodes = requirePosition.value();
        
        // Lấy position hiện tại của employee từ DB
        UUID currentPositionId = employee.getPositionId();
        if (currentPositionId == null) {
            log.warn("Employee {} has no position assigned", userId);
            throw new BusinessException(MessageCode.INVALID_POSITION);
        }
        
        Position currentPosition = positionRepository.findById(currentPositionId)
                .orElseThrow(() -> new BusinessException(MessageCode.POSITION_NOT_FOUND));
        
        // Kiểm tra position code có trong danh sách cho phép không
        boolean isAllowed = Arrays.asList(allowedPositionCodes).contains(currentPosition.getCode());
        
        if (!isAllowed) {
            log.warn("User {} with position {} tried to access method requiring positions: {}", 
                    userId, currentPosition.getCode(), Arrays.toString(allowedPositionCodes));
            throw new BusinessException(MessageCode.INSUFFICIENT_POSITION);
        }
        
        log.debug("Position check passed for user {} with position {}", userId, currentPosition.getCode());
    }
}
