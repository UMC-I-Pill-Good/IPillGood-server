package com.ipillgood.server.domain.intake.entity;

import com.ipillgood.server.domain.intake.entity.enums.IntakeFrequency;
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
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.LocalDate;

// 섭취 중 상품의 복용 주기 변경 이력
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "member_active_product_schedule_history")
public class MemberActiveProductScheduleHistory extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_active_product_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private MemberActiveProduct memberActiveProduct;

    @Enumerated(EnumType.STRING)
    @Column(name = "frequency", nullable = false)
    private IntakeFrequency frequency;

    @Column(name = "frequency_interval_days", nullable = false)
    private Short frequencyIntervalDays;

    @Column(name = "schedule_anchor_on", nullable = false)
    private LocalDate scheduleAnchorOn;

    @Column(name = "effective_from", nullable = false)
    private LocalDate effectiveFrom;

    @Column(name = "effective_to")
    private LocalDate effectiveTo;

    private MemberActiveProductScheduleHistory(
            MemberActiveProduct memberActiveProduct,
            IntakeFrequency frequency,
            Short frequencyIntervalDays,
            LocalDate scheduleAnchorOn,
            LocalDate effectiveFrom
    ) {
        this.memberActiveProduct = memberActiveProduct;
        this.frequency = frequency;
        this.frequencyIntervalDays = frequencyIntervalDays;
        this.scheduleAnchorOn = scheduleAnchorOn;
        this.effectiveFrom = effectiveFrom;
    }

    public static MemberActiveProductScheduleHistory createInitial(MemberActiveProduct activeProduct) {
        return new MemberActiveProductScheduleHistory(
                activeProduct,
                activeProduct.getFrequency(),
                activeProduct.getFrequencyIntervalDays(),
                activeProduct.getScheduleAnchorOn(),
                activeProduct.getScheduleAnchorOn()
        );
    }

    public static MemberActiveProductScheduleHistory createChanged(MemberActiveProduct activeProduct) {
        return new MemberActiveProductScheduleHistory(
                activeProduct,
                activeProduct.getFrequency(),
                activeProduct.getFrequencyIntervalDays(),
                activeProduct.getScheduleAnchorOn(),
                activeProduct.getScheduleAnchorOn()
        );
    }

    public void close(LocalDate effectiveTo) {
        this.effectiveTo = effectiveTo;
    }

    public void changeFrequency(IntakeFrequency frequency, LocalDate scheduleAnchorOn) {
        this.frequency = frequency;
        this.frequencyIntervalDays = frequency.getIntervalDays();
        this.scheduleAnchorOn = scheduleAnchorOn;
    }
}
