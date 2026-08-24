package com.example.pms.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "performance_review_items")
@Getter @Setter @NoArgsConstructor
public class PerformanceReviewItem {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "review_id", nullable = false)
    private PerformanceReview review;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "criterion_id", nullable = false)
    private PerformanceCriterion criterion;
    private Double score;
    @Column(columnDefinition = "TEXT") private String comments;
}
