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
}
