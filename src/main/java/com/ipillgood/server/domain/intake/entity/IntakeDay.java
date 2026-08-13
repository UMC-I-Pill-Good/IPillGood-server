package com.ipillgood.server.domain.intake.entity;

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
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.LocalDate;
import java.time.LocalDateTime;

// 일자별 복용 묶음
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
        name = "intake_day",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_intake_day",
                columnNames = {"member_id", "intake_on"}
        )
)
public class IntakeDay extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Member member;

    @Column(name = "intake_on", nullable = false)
    private LocalDate intakeOn;

    @Column(name = "auto_popup_shown_at")
    private LocalDateTime autoPopupShownAt;

    @Column(name = "all_completed", nullable = false)
    private boolean allCompleted = false;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    private IntakeDay(Member member, LocalDate intakeOn) {
        this.member = member;
        this.intakeOn = intakeOn;
    }

    public static IntakeDay create(Member member, LocalDate intakeOn) {
        return new IntakeDay(member, intakeOn);
    }

    public LocalDateTime markAutoPopupShown(LocalDateTime shownAt) {
        if (autoPopupShownAt == null) {
            autoPopupShownAt = shownAt;
        }
        return autoPopupShownAt;
    }

    public void changeCompletion(boolean allCompleted, LocalDateTime completedAt) {
        if (allCompleted) {
            if (!this.allCompleted || this.completedAt == null) {
                this.completedAt = completedAt;
            }
            this.allCompleted = true;
            return;
        }

        this.allCompleted = false;
        this.completedAt = null;
    }
}
