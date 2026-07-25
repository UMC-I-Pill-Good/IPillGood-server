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
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.LocalDateTime;

// 상품별 복용 기록
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
    @OnDelete(action = OnDeleteAction.CASCADE)
    private IntakeDay intakeDay;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_active_product_id")
    @OnDelete(action = OnDeleteAction.CASCADE)
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

    private IntakeRecord(IntakeDay intakeDay, MemberActiveProduct memberActiveProduct, Product product) {
        this.intakeDay = intakeDay;
        this.memberActiveProduct = memberActiveProduct;
        this.product = product;
        this.scheduled = true;
    }

    public static IntakeRecord createScheduled(IntakeDay intakeDay, MemberActiveProduct memberActiveProduct) {
        return new IntakeRecord(
                intakeDay,
                memberActiveProduct,
                memberActiveProduct.getMemberProduct().getProduct()
        );
    }

    public void saveTodayState(MemberActiveProduct memberActiveProduct, boolean taken, LocalDateTime takenAt) {
        this.memberActiveProduct = memberActiveProduct;
        this.product = memberActiveProduct.getMemberProduct().getProduct();
        this.scheduled = true;

        if (taken) {
            markTaken(takenAt);
            return;
        }
        markUntaken();
    }

    private void markTaken(LocalDateTime takenAt) {
        if (!taken || this.takenAt == null) {
            this.takenAt = takenAt;
        }
        this.taken = true;
    }

    private void markUntaken() {
        this.taken = false;
        this.takenAt = null;
    }
}
