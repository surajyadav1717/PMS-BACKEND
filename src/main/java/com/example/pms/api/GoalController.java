package com.example.pms.api;

import com.example.pms.domain.GoalStatus;
import com.example.pms.entity.Goal;
import com.example.pms.repository.AppUserRepository;
import com.example.pms.repository.EmployeeRepository;
import com.example.pms.repository.GoalRepository;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/goals")
public class GoalController {
    private final GoalRepository goals;
    private final EmployeeRepository employees;
    private final AppUserRepository users;

    public GoalController(GoalRepository goals, EmployeeRepository employees, AppUserRepository users) {
        this.goals = goals; this.employees = employees; this.users = users;
    }

    public record GoalRequest(Long employeeId, String title, String description,
                              LocalDate startDate, LocalDate targetDate, Double weight) {}
    public record GoalDto(Long id, Long employeeId, String employee, String title,
                          String description, Integer progress, GoalStatus status, LocalDate targetDate) {}

    @GetMapping
    public List<GoalDto> list() {
        var u = currentUser();
        List<Goal> data = switch (u.getRole()) {
            case EMPLOYEE -> goals.findByEmployeeId(u.getEmployee().getId());
            case MANAGER, HEAD -> u.getEmployee() == null ? goals.findAll() : goals.findAll().stream()
                    .filter(g -> g.getEmployee().getManager() != null &&
                            g.getEmployee().getManager().getId().equals(u.getEmployee().getId()))
                    .toList();
            default -> goals.findAll();
        };
        return data.stream().map(this::dto).toList();
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','HR','MANAGER')")
    public GoalDto create(@RequestBody GoalRequest request) {
        var g = new Goal();
        g.setEmployee(employees.findById(request.employeeId()).orElseThrow());
        g.setTitle(request.title());
        g.setDescription(request.description());
        g.setStartDate(request.startDate());
        g.setTargetDate(request.targetDate());
        g.setWeight(request.weight());
        g.setCreatedBy(currentUser().getEmployee());
        return dto(goals.save(g));
    }

    @PatchMapping("/{id}/progress")
    public GoalDto progress(@PathVariable Long id, @RequestParam Integer value) {
        var g = goals.findById(id).orElseThrow();
        g.setProgressPercentage(Math.max(0, Math.min(100, value)));
        g.setStatus(g.getProgressPercentage() >= 100 ? GoalStatus.COMPLETED : GoalStatus.IN_PROGRESS);
        return dto(goals.save(g));
    }

    private com.example.pms.entity.AppUser currentUser() {
        String username = org.springframework.security.core.context.SecurityContextHolder.getContext()
                .getAuthentication().getName();
        return users.findByUsername(username).orElseThrow();
    }

    private GoalDto dto(Goal g) {
        return new GoalDto(g.getId(), g.getEmployee().getId(), g.getEmployee().getFullName(),
                g.getTitle(), g.getDescription(), g.getProgressPercentage(), g.getStatus(), g.getTargetDate());
    }
}
