package com.example.pms.api;

import com.example.pms.domain.ReviewStatus;
import com.example.pms.domain.ReviewType;
import com.example.pms.entity.*;
import com.example.pms.repository.*;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/reviews")
public class ReviewController {
    private final PerformanceReviewRepository reviews;
    private final EmployeeRepository employees;
    private final ReviewCycleRepository cycles;
    private final PerformanceCriterionRepository criteria;
    private final AppUserRepository users;
    private final AuditLogRepository audit;

    public ReviewController(PerformanceReviewRepository reviews, EmployeeRepository employees,
                            ReviewCycleRepository cycles, PerformanceCriterionRepository criteria,
                            AppUserRepository users, AuditLogRepository audit) {
        this.reviews = reviews; this.employees = employees; this.cycles = cycles;
        this.criteria = criteria; this.users = users; this.audit = audit;
    }

    public record ItemRequest(Long criterionId, Double score, String comments) {}
    public record ReviewRequest(Long employeeId, Long cycleId, ReviewType reviewType,
                                String managerComments, List<ItemRequest> items) {}
    public record ReviewDto(Long id, Long employeeId, String employee, String reviewer,
                            String head, ReviewType type, ReviewStatus status, Double score,
                            String comments) {}

    @GetMapping
    public List<ReviewDto> list() {
        var username = org.springframework.security.core.context.SecurityContextHolder.getContext()
                .getAuthentication().getName();
        var user = users.findByUsername(username).orElseThrow();
        List<PerformanceReview> result = switch (user.getRole()) {
            case ADMIN, HR -> reviews.findAll();
            case HEAD -> reviews.findByEmployee_Head_Id(user.getEmployee().getId());
            case MANAGER -> reviews.findByReviewerId(user.getEmployee().getId());
            case EMPLOYEE -> reviews.findByEmployeeId(user.getEmployee().getId());
        };
        return result.stream().map(this::dto).toList();
    }

    @PostMapping
    @PreAuthorize("hasRole('MANAGER')")
    public ReviewDto create(@RequestBody ReviewRequest request) {
        var employee = employees.findById(request.employeeId()).orElseThrow();
        var cycle = cycles.findById(request.cycleId()).orElseThrow();
        var current = currentEmployee();
        if (current == null) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "User is not linked to an employee");

        // Every employee review must have that employee's manager as the rating owner.
        Employee manager = employee.getManager();
        if (manager == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Employee does not have a manager assigned");
        }
        if (!current.getId().equals(manager.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only the employee's manager can create the rating");
        }

        var review = new PerformanceReview();
        review.setEmployee(employee);
        review.setReviewer(manager);
        review.setReviewCycle(cycle);
        review.setReviewType(request.reviewType());
        review.setManagerComments(request.managerComments());
        review.setStatus(ReviewStatus.DRAFT);
        if (request.items() != null) {
            request.items().forEach(item -> {
                var criterion = criteria.findById(item.criterionId()).orElseThrow();
                var ri = new PerformanceReviewItem();
                ri.setReview(review); ri.setCriterion(criterion);
                ri.setScore(item.score()); ri.setComments(item.comments());
                review.getItems().add(ri);
            });
        }
        calculateScore(review);
        return dto(reviews.save(review));
    }

    @PostMapping("/{id}/submit")
    @PreAuthorize("hasRole('MANAGER')")
    public ReviewDto submit(@PathVariable Long id) {
        var review = reviews.findById(id).orElseThrow();
        assertManagerOwner(review);
        if (!(review.getStatus() == ReviewStatus.DRAFT || review.getStatus() == ReviewStatus.REJECTED)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Only draft or rejected reviews can be submitted");
        }
        if (review.getEmployee().getHead() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Employee does not have a Head assigned");
        }
        review.setStatus(ReviewStatus.PENDING_HEAD_APPROVAL);
        review.setSubmittedAt(LocalDateTime.now());
        review.setManagerApprovedAt(LocalDateTime.now());
        audit(id, "MANAGER_APPROVED_REVIEW");
        return dto(reviews.save(review));
    }

    @PostMapping("/{id}/approve")
    @PreAuthorize("hasRole('HEAD')")
    public ReviewDto approve(@PathVariable Long id) {
        var review = reviews.findById(id).orElseThrow();
        assertHeadOwner(review);
        if (review.getStatus() != ReviewStatus.PENDING_HEAD_APPROVAL) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Review is not pending Head approval");
        }
        review.setStatus(ReviewStatus.APPROVED);
        review.setHeadApprovedAt(LocalDateTime.now());
        review.setApprovedAt(LocalDateTime.now());
        audit(id, "HEAD_FINAL_APPROVAL");
        return dto(reviews.save(review));
    }

    @PostMapping("/{id}/reject")
    @PreAuthorize("hasRole('HEAD')")
    public ReviewDto reject(@PathVariable Long id, @RequestBody(required = false) RejectRequest request) {
        var review = reviews.findById(id).orElseThrow();
        assertHeadOwner(review);
        if (review.getStatus() != ReviewStatus.PENDING_HEAD_APPROVAL) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Review is not pending Head approval");
        }
        review.setStatus(ReviewStatus.REJECTED);
        review.setRejectedAt(LocalDateTime.now());
        review.setRejectionComments(request == null ? null : request.comments());
        audit(id, "HEAD_REJECTED_REVIEW");
        return dto(reviews.save(review));
    }

    @PostMapping("/{id}/acknowledge")
    @PreAuthorize("hasRole('EMPLOYEE')")
    public ReviewDto acknowledge(@PathVariable Long id) {
        var review = reviews.findById(id).orElseThrow();
        var current = currentEmployee();
        if (current == null || !current.getId().equals(review.getEmployee().getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only the reviewed employee can acknowledge");
        }
        if (review.getStatus() != ReviewStatus.APPROVED) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Only Head-approved reviews can be acknowledged");
        }
        review.setStatus(ReviewStatus.ACKNOWLEDGED);
        review.setAcknowledgedAt(LocalDateTime.now());
        audit(id, "REVIEW_ACKNOWLEDGED");
        return dto(reviews.save(review));
    }

    public record RejectRequest(String comments) {}

    private void assertManagerOwner(PerformanceReview review) {
        var current = currentEmployee();
        if (current == null || review.getReviewer() == null || !current.getId().equals(review.getReviewer().getId())
                || review.getEmployee().getManager() == null
                || !current.getId().equals(review.getEmployee().getManager().getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only the employee's assigned Manager can approve this rating");
        }
    }

    private void assertHeadOwner(PerformanceReview review) {
        var current = currentEmployee();
        var head = review.getEmployee().getHead();
        if (current == null || head == null || !current.getId().equals(head.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only the employee's assigned Head can give final approval");
        }
    }

    private void calculateScore(PerformanceReview review) {
        double weighted = 0, totalWeight = 0;
        for (var item : review.getItems()) {
            if (item.getScore() != null && item.getCriterion().getWeight() != null) {
                weighted += item.getScore() * item.getCriterion().getWeight();
                totalWeight += item.getCriterion().getWeight();
            }
        }
        review.setOverallScore(totalWeight == 0 ? null : Math.round(weighted / totalWeight * 100.0) / 100.0);
    }

    private Employee currentEmployee() {
        var username = org.springframework.security.core.context.SecurityContextHolder.getContext()
                .getAuthentication().getName();
        return users.findByUsername(username).orElseThrow().getEmployee();
    }

    private void audit(Long id, String action) {
        var username = org.springframework.security.core.context.SecurityContextHolder.getContext()
                .getAuthentication().getName();
        var log = new AuditLog();
        log.setUser(users.findByUsername(username).orElse(null));
        log.setEntityType("PERFORMANCE_REVIEW");
        log.setEntityId(id);
        log.setAction(action);
        audit.save(log);
    }

    private ReviewDto dto(PerformanceReview r) {
        String head = r.getEmployee().getHead() == null ? null : r.getEmployee().getHead().getFullName();
        return new ReviewDto(r.getId(), r.getEmployee().getId(), r.getEmployee().getFullName(),
                r.getReviewer().getFullName(), head, r.getReviewType(), r.getStatus(),
                r.getOverallScore(), r.getManagerComments());
    }
}
