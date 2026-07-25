package com.ipillgood.server.domain.recommendation.entity;

import com.ipillgood.server.domain.member.entity.Member;
import com.ipillgood.server.domain.recommendation.entity.enums.RecommendationStatus;
import com.ipillgood.server.domain.survey.entity.SurveyResponse;
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
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.LocalDateTime;

// 추천 실행 단위
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "recommendation")
public class Recommendation extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "survey_response_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private SurveyResponse surveyResponse;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private RecommendationStatus status;

    @Column(name = "health_summary", columnDefinition = "TEXT")
    private String healthSummary;

    @Column(name = "failure_reason", columnDefinition = "TEXT")
    private String failureReason;

    @Column(name = "started_at", nullable = false)
    private LocalDateTime startedAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Column(name = "activated_at")
    private LocalDateTime activatedAt;

    @Builder
    public Recommendation(Member member, SurveyResponse surveyResponse, RecommendationStatus status,
                           LocalDateTime startedAt) {
        this.member = member;
        this.surveyResponse = surveyResponse;
        this.status = status;
        this.startedAt = startedAt;
    }

    // AI 추천 성공 처리 (활성 추천으로 전환)
    public void markSuccess(String healthSummary, LocalDateTime completedAt) {
        this.status = RecommendationStatus.SUCCESS;
        this.healthSummary = healthSummary;
        this.completedAt = completedAt;
        this.activatedAt = completedAt;
    }

    // 안전 후보 검증까지는 정상 처리됐으나 추천 결과가 없는 경우
    public void markNoResult(String healthSummary, LocalDateTime completedAt) {
        this.status = RecommendationStatus.NO_RESULT;
        this.healthSummary = healthSummary;
        this.completedAt = completedAt;
    }

    // Gemini 호출/파싱 등 시스템 오류로 실패 처리
    public void markFailed(String failureReason, LocalDateTime completedAt) {
        this.status = RecommendationStatus.FAILED;
        this.failureReason = failureReason;
        this.completedAt = completedAt;
    }

    // FAILED/NO_RESULT 상태에서 재시도 요청 시 PENDING으로 초기화
    public void markRetried(LocalDateTime startedAt) {
        this.status = RecommendationStatus.PENDING;
        this.failureReason = null;
        this.startedAt = startedAt;
        this.completedAt = null;
    }
}
