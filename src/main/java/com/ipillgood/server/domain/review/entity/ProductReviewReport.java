package com.ipillgood.server.domain.review.entity;

import com.ipillgood.server.domain.member.entity.Member;
import com.ipillgood.server.domain.review.entity.enums.ProductReviewReportReason;
import com.ipillgood.server.domain.review.entity.enums.ReviewReportStatus;
import com.ipillgood.server.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.LocalDateTime;

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

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    @Builder.Default
    private ReviewReportStatus status = ReviewReportStatus.PENDING;

    @Column(name = "process_reason", length = 200)
    private String processReason;

    @Column(name = "processed_at")
    private LocalDateTime processedAt;

    // 관리자 신고 처리 - 신고 건 단위로 처리 상태/사유/처리일시를 갱신한다.
    public void process(ReviewReportStatus status, String processReason, LocalDateTime processedAt) {
        this.status = status;
        this.processReason = processReason;
        this.processedAt = processedAt;
    }
}
