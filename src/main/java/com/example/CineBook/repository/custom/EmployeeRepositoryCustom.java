package com.example.CineBook.repository.custom;

import com.example.CineBook.dto.employee.EmployeeSearchDTO;
import com.example.CineBook.model.Employee;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface EmployeeRepositoryCustom {
    Page<Employee> searchWithFilters(EmployeeSearchDTO searchDTO, Pageable pageable);
}
