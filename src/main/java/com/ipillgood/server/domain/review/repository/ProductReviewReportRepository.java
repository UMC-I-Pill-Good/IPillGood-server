package com.ipillgood.server.domain.review.repository;

import com.ipillgood.server.domain.member.entity.Member;
import com.ipillgood.server.domain.review.entity.ProductReview;
import com.ipillgood.server.domain.review.entity.ProductReviewReport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductReviewReportRepository extends JpaRepository<ProductReviewReport, Long> {
    boolean existsByReporterMemberAndReview(Member member, ProductReview review);
}
