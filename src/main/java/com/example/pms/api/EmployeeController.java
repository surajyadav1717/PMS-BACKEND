package com.example.pms.api;

import com.example.pms.domain.Role;
import com.example.pms.entity.Employee;
import com.example.pms.repository.AppUserRepository;
import com.example.pms.repository.EmployeeRepository;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/employees")
public class EmployeeController {
    private final EmployeeRepository employees;
    private final AppUserRepository users;

    public EmployeeController(EmployeeRepository employees, AppUserRepository users) {
        this.employees = employees; this.users = users;
    }

    public record EmployeeDto(Long id, String employeeCode, String name, String email,
                              String department, String designation, String manager, String status) {}

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','HR','HEAD','MANAGER')")
    public List<EmployeeDto> list() {
        return employees.findAll().stream().map(this::toDto).toList();
    }

    @GetMapping("/{id}")
    public EmployeeDto get(@PathVariable Long id) {
        return toDto(employees.findById(id).orElseThrow());
    }

    private EmployeeDto toDto(Employee e) {
        return new EmployeeDto(e.getId(), e.getEmployeeCode(), e.getFullName(), e.getEmail(),
                e.getDepartment() == null ? null : e.getDepartment().getName(),
                e.getDesignation(),
                e.getManager() == null ? null : e.getManager().getFullName(),
                e.getStatus());
    }
}
