package com.ipillgood.server.domain.recommendation.entity;

import com.ipillgood.server.domain.member.entity.Member;
import com.ipillgood.server.domain.recommendation.entity.enums.RecommendationFeedbackResponse;
import com.ipillgood.server.global.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

// 추천 피드백 주기
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
        name = "recommendation_feedback_cycle",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_recommendation_feedback_cycle",
                columnNames = {"recommendation_id", "cycle_due_on"}
        )
)
public class RecommendationFeedbackCycle extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recommendation_id", nullable = false)
    private Recommendation recommendation;

    @Column(name = "cycle_due_on", nullable = false)
    private LocalDate cycleDueOn;

    @Enumerated(EnumType.STRING)
    @Column(name = "response_type")
    private RecommendationFeedbackResponse responseType;

    @Column(name = "responded_at")
    private LocalDateTime respondedAt;

    @Column(name = "next_cycle_due_on")
    private LocalDate nextCycleDueOn;

    @Builder
    public RecommendationFeedbackCycle(Member member, Recommendation recommendation, LocalDate cycleDueOn) {
        this.member = member;
        this.recommendation = recommendation;
        this.cycleDueOn = cycleDueOn;
    }

    public void respond(RecommendationFeedbackResponse responseType, LocalDateTime respondedAt, LocalDate nextCycleDueOn) {
        this.responseType = responseType;
        this.respondedAt = respondedAt;
        this.nextCycleDueOn = nextCycleDueOn;
    }
}
