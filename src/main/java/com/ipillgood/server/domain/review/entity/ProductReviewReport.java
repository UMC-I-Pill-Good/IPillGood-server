package com.ipillgood.server.domain.review.entity;

import com.ipillgood.server.domain.member.entity.Member;
import com.ipillgood.server.domain.review.entity.enums.ProductReviewReportReason;
import com.ipillgood.server.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

// 후기 신고 이력
@Entity
@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
        name = "product_review_report",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_product_review_report",
                columnNames = {"review_id", "reporter_member_id"}
        )
)
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
