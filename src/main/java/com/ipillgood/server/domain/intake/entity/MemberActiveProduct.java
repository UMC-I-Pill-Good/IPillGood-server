package com.ipillgood.server.domain.intake.entity;

import com.ipillgood.server.domain.cabinet.entity.MemberProduct;
import com.ipillgood.server.domain.intake.entity.enums.IntakeFrequency;
import com.ipillgood.server.domain.member.entity.Member;
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
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

// 섭취 중인 상품
// 중단되지 않은 섭취 상품만 중복 방지
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "member_active_product")
public class MemberActiveProduct extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_product_id", nullable = false)
    private MemberProduct memberProduct;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Column(name = "started_on", nullable = false)
    private LocalDate startedOn;

    @Column(name = "stopped_on")
    private LocalDate stoppedOn;

    @Column(name = "intake_time", nullable = false)
    private LocalTime intakeTime;

    @Enumerated(EnumType.STRING)
    @Column(name = "frequency", nullable = false)
    private IntakeFrequency frequency;

    @Column(name = "frequency_interval_days", nullable = false)
    private Short frequencyIntervalDays;

    @Column(name = "schedule_anchor_on", nullable = false)
    private LocalDate scheduleAnchorOn;

    @Column(name = "notification_enabled", nullable = false)
    private boolean notificationEnabled = true;

    @Column(name = "review_prompt_dismissed_at")
    private LocalDateTime reviewPromptDismissedAt;

    private MemberActiveProduct(
            MemberProduct memberProduct,
            Member member,
            LocalDate startedOn,
            LocalTime intakeTime,
            IntakeFrequency frequency
    ) {
        this.memberProduct = memberProduct;
        this.member = member;
        this.startedOn = startedOn;
        this.intakeTime = intakeTime;
        this.frequency = frequency;
        this.frequencyIntervalDays = frequency.getIntervalDays();
        this.scheduleAnchorOn = startedOn;
        this.notificationEnabled = true;
    }

    public static MemberActiveProduct create(
            MemberProduct memberProduct,
            Member member,
            LocalDate startedOn,
            LocalTime intakeTime,
            IntakeFrequency frequency
    ) {
        return new MemberActiveProduct(memberProduct, member, startedOn, intakeTime, frequency);
    }

    public LocalDateTime dismissReviewPrompt(LocalDateTime dismissedAt) {
        if (reviewPromptDismissedAt == null) {
            reviewPromptDismissedAt = dismissedAt;
        }
        return reviewPromptDismissedAt;
    }

    public void markStopped(LocalDate stoppedOn) {
        this.stoppedOn = stoppedOn;
    }
}
