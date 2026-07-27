package com.ipillgood.server.domain.review.entity;

import com.ipillgood.server.domain.member.entity.Member;
import com.ipillgood.server.domain.review.entity.enums.ProductReviewReportReason;
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

// 후기 신고 이력
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "product_review_report")
public class ProductReviewReport extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "review_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private ProductReview review;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reporter_member_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Member reporterMember;

    @Enumerated(EnumType.STRING)
    @Column(name = "reason", nullable = false)
    private ProductReviewReportReason reason;

    @Column(name = "detail", length = 200)
    private String detail;
}
