package com.example.pms.config;

import com.example.pms.domain.Role;
import com.example.pms.domain.ReviewType;
import com.example.pms.entity.*;
import com.example.pms.repository.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.util.List;

@Configuration
public class DataInitializer {
    @Bean
    CommandLineRunner seed(DepartmentRepository departments, EmployeeRepository employees,
                           AppUserRepository users, ReviewCycleRepository cycles,
                           PerformanceCriterionRepository criteria, PerformanceReviewRepository reviews,
                           PasswordEncoder encoder) {
        return args -> {
            if (users.count() > 0) return;

            Department engineering = new Department();
            engineering.setName("Engineering");
            departments.save(engineering);

            Department hrDept = new Department();
            hrDept.setName("Human Resources");
            departments.save(hrDept);

            Employee head = employee("E001", "Head", "Engineering", "head@company.com", "Department Head", engineering, null);
            employees.save(head);
            Employee manager = employee("E002", "Manager", "One", "manager@company.com", "Engineering Manager", engineering, head);
            employees.save(manager);
            Employee employee = employee("E003", "Employee", "One", "employee@company.com", "Software Engineer", engineering, manager);
            employee.setHead(head);
            employees.save(employee);
            head.setHead(head);
            employees.save(head);

            createUser(users, encoder, "admin", Role.ADMIN, null);
            createUser(users, encoder, "hr", Role.HR, null);
            createUser(users, encoder, "head", Role.HEAD, head);
            createUser(users, encoder, "manager", Role.MANAGER, manager);
            createUser(users, encoder, "employee", Role.EMPLOYEE, employee);

            ReviewCycle weekly = new ReviewCycle();
            weekly.setName("Current Weekly Review");
            weekly.setReviewType(ReviewType.WEEKLY);
            weekly.setStartDate(LocalDate.now().minusDays(7));
            weekly.setEndDate(LocalDate.now());
            cycles.save(weekly);

            ReviewCycle monthly = new ReviewCycle();
            monthly.setName("Current Monthly Review");
            monthly.setReviewType(ReviewType.MONTHLY);
            monthly.setStartDate(LocalDate.now().withDayOfMonth(1));
            monthly.setEndDate(LocalDate.now());
            cycles.save(monthly);

            for (var x : List.of(
                    criterion("Technical Skills", 30.0),
                    criterion("Quality", 20.0),
                    criterion("Delivery", 20.0),
                    criterion("Communication", 10.0),
                    criterion("Teamwork", 10.0),
                    criterion("Ownership", 10.0))) criteria.save(x);
        };
    }

    private Employee employee(String code, String first, String last, String email,
                              String designation, Department d, Employee manager) {
        Employee e = new Employee();
        e.setEmployeeCode(code); e.setFirstName(first); e.setLastName(last);
        e.setEmail(email); e.setDesignation(designation); e.setDepartment(d); e.setManager(manager);
        e.setJoiningDate(LocalDate.now().minusYears(2));
        return e;
    }

    private void createUser(AppUserRepository repo, PasswordEncoder encoder, String username,
                            Role role, Employee employee) {
        AppUser u = new AppUser();
        u.setUsername(username); u.setPasswordHash(encoder.encode("Password@123"));
        u.setRole(role); u.setEmployee(employee); repo.save(u);
    }

    private PerformanceCriterion criterion(String name, double weight) {
        PerformanceCriterion c = new PerformanceCriterion();
        c.setName(name); c.setWeight(weight); c.setDescription(name + " assessment");
        return c;
    }
}
