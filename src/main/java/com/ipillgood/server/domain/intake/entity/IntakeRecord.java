package com.ipillgood.server.domain.intake.entity;

import com.ipillgood.server.domain.product.entity.Product;
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

import java.time.LocalDateTime;

// intake_record 테이블 매핑
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
        name = "intake_record",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_intake_record",
                columnNames = {"intake_day_id", "product_id"}
        )
)
public class IntakeRecord extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "intake_day_id", nullable = false)
    private IntakeDay intakeDay;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_active_product_id")
    private MemberActiveProduct memberActiveProduct;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(name = "scheduled", nullable = false)
    private boolean scheduled;

    @Column(name = "taken", nullable = false)
    private boolean taken = false;

    @Column(name = "taken_at")
    private LocalDateTime takenAt;
}
