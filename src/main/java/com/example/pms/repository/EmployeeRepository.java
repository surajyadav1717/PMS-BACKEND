package com.example.pms.repository;

import com.example.pms.entity.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
public interface EmployeeRepository extends JpaRepository<Employee, Long> {
 Optional<Employee> findByEmail(String email);
 List<Employee> findByManagerId(Long managerId);
 List<Employee> findByDepartmentId(Long departmentId);
}