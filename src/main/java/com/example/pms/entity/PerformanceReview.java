package com.example.pms.entity;

import com.example.pms.domain.ReviewStatus;
import com.example.pms.domain.ReviewType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "performance_reviews")
@Getter @Setter @NoArgsConstructor
public class PerformanceReview {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "reviewer_id", nullable = false)
    private Employee reviewer;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "review_cycle_id", nullable = false)
    private ReviewCycle reviewCycle;
    @Enumerated(EnumType.STRING) private ReviewType reviewType;
    @Enumerated(EnumType.STRING) private ReviewStatus status = ReviewStatus.DRAFT;
    private Double overallScore;
    @Column(columnDefinition = "TEXT") private String managerComments;
    @Column(columnDefinition = "TEXT") private String employeeComments;
    @Column(columnDefinition = "TEXT") private String rejectionComments;
    private LocalDateTime submittedAt;
    private LocalDateTime managerApprovedAt;
    private LocalDateTime headApprovedAt;
    private LocalDateTime rejectedAt;
    private LocalDateTime approvedAt;
    private LocalDateTime acknowledgedAt;
    private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime updatedAt = LocalDateTime.now();

    @OneToMany(mappedBy = "review", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PerformanceReviewItem> items = new ArrayList<>();
}
