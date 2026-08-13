package com.ipillgood.server.domain.condition.entity;

import com.ipillgood.server.domain.member.entity.Member;
import com.ipillgood.server.global.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.math.BigDecimal;
import java.time.LocalDate;

// 주간 컨디션 기록
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
        name = "condition_weekly_record",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_condition_weekly_record",
                columnNames = {"member_id", "week_start_on"}
        )
)
public class ConditionWeeklyRecord extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Member member;

    @Column(name = "week_start_on", nullable = false)
    private LocalDate weekStartOn;

    @Column(name = "week_end_on", nullable = false)
    private LocalDate weekEndOn;

    @Column(name = "checked_on", nullable = false)
    private LocalDate checkedOn;

    @Column(name = "vitality_score", nullable = false)
    private Short vitalityScore;

    @Column(name = "sleep_hours", nullable = false)
    private Short sleepHours;

    @Column(name = "sleep_minutes", nullable = false)
    private Short sleepMinutes;

    @Column(name = "sleep_score", nullable = false)
    private Short sleepScore;

    @Column(name = "intake_days_count", nullable = false)
    private Short intakeDaysCount;

    @Column(name = "intake_score", nullable = false)
    private Short intakeScore;

    @Column(name = "condition_score", nullable = false, precision = 3, scale = 2)
    private BigDecimal conditionScore;

    @Builder
    public ConditionWeeklyRecord(Member member, LocalDate weekStartOn, LocalDate weekEndOn, LocalDate checkedOn,
                                  Short vitalityScore, Short sleepHours, Short sleepMinutes, Short sleepScore,
                                  Short intakeDaysCount, Short intakeScore, BigDecimal conditionScore) {
        this.member = member;
        this.weekStartOn = weekStartOn;
        this.weekEndOn = weekEndOn;
        this.checkedOn = checkedOn;
        this.vitalityScore = vitalityScore;
        this.sleepHours = sleepHours;
        this.sleepMinutes = sleepMinutes;
        this.sleepScore = sleepScore;
        this.intakeDaysCount = intakeDaysCount;
        this.intakeScore = intakeScore;
        this.conditionScore = conditionScore;
    }
}
