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
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

// 컨디션 체크 팝업 이력
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
        name = "condition_popup_log",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_condition_popup_log",
                columnNames = {"member_id", "week_start_on"}
        )
)
public class ConditionPopupLog extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Column(name = "week_start_on", nullable = false)
    private LocalDate weekStartOn;

    @Column(name = "auto_shown_at")
    private LocalDateTime autoShownAt;

    @Column(name = "dismissed_at")
    private LocalDateTime dismissedAt;

    private ConditionPopupLog(Member member, LocalDate weekStartOn) {
        this.member = member;
        this.weekStartOn = weekStartOn;
    }

    public static ConditionPopupLog create(Member member, LocalDate weekStartOn) {
        return new ConditionPopupLog(member, weekStartOn);
    }

    // 같은 주에 이미 자동 노출 기록이 있으면 최초 노출 시각을 유지한다
    public LocalDateTime markAutoShown(LocalDateTime shownAt) {
        if (autoShownAt == null) {
            autoShownAt = shownAt;
        }
        return autoShownAt;
    }

    // 같은 주에 이미 닫힘 기록이 있으면 최초 닫힘 시각을 유지한다
    public LocalDateTime markDismissed(LocalDateTime dismissedAt) {
        if (this.dismissedAt == null) {
            this.dismissedAt = dismissedAt;
        }
        return this.dismissedAt;
    }
}
