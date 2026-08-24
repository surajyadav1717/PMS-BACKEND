package com.example.pms.api;

import com.example.pms.domain.ReviewStatus;
import com.example.pms.entity.AppUser;
import com.example.pms.entity.PerformanceReview;
import com.example.pms.repository.AppUserRepository;
import com.example.pms.repository.EmployeeRepository;
import com.example.pms.repository.GoalRepository;
import com.example.pms.repository.PerformanceReviewRepository;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {
    private final AppUserRepository users;
    private final EmployeeRepository employees;
    private final PerformanceReviewRepository reviews;
    private final GoalRepository goals;

    public DashboardController(AppUserRepository users, EmployeeRepository employees,
                               PerformanceReviewRepository reviews, GoalRepository goals) {
        this.users = users; this.employees = employees; this.reviews = reviews; this.goals = goals;
    }

    @GetMapping
    public Map<String,Object> dashboard() {
        AppUser u = currentUser();
        List<PerformanceReview> data = switch (u.getRole()) {
            case ADMIN, HR -> reviews.findAll();
            case HEAD -> reviews.findByEmployee_Head_Id(u.getEmployee().getId());
            case MANAGER -> reviews.findByReviewerId(u.getEmployee().getId());
            case EMPLOYEE -> reviews.findByEmployeeId(u.getEmployee().getId());
        };

        double avg = data.stream().filter(r -> r.getOverallScore() != null)
                .mapToDouble(PerformanceReview::getOverallScore).average().orElse(0);
        long pending = data.stream().filter(r -> r.getStatus() != ReviewStatus.APPROVED &&
                r.getStatus() != ReviewStatus.ACKNOWLEDGED).count();

        Map<String,Object> result = new LinkedHashMap<>();
        result.put("role", u.getRole().name());
        result.put("totalEmployees", employees.count());
        result.put("totalReviews", data.size());
        result.put("pendingReviews", pending);
        result.put("averageScore", Math.round(avg * 100.0) / 100.0);
        result.put("completedReviews", data.stream().filter(r ->
                r.getStatus() == ReviewStatus.APPROVED || r.getStatus() == ReviewStatus.ACKNOWLEDGED).count());
        result.put("goals", u.getEmployee() == null ? goals.count() : goals.findByEmployeeId(u.getEmployee().getId()).size());

        Map<String, Long> status = data.stream().collect(Collectors.groupingBy(
                r -> r.getStatus().name(), LinkedHashMap::new, Collectors.counting()));
        result.put("reviewStatus", status);

        Map<String, Double> department = data.stream().filter(r -> r.getOverallScore() != null)
                .collect(Collectors.groupingBy(
                        r -> r.getEmployee().getDepartment() == null ? "Unassigned" : r.getEmployee().getDepartment().getName(),
                        LinkedHashMap::new,
                        Collectors.averagingDouble(PerformanceReview::getOverallScore)));
        result.put("departmentPerformance", department);
        return result;
    }

    private AppUser currentUser() {
        String username = org.springframework.security.core.context.SecurityContextHolder.getContext()
                .getAuthentication().getName();
        return users.findByUsername(username).orElseThrow();
    }
}
