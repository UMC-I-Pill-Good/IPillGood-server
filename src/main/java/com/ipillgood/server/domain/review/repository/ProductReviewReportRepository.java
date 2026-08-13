package com.ipillgood.server.domain.review.repository;

import com.ipillgood.server.domain.member.entity.Member;
import com.ipillgood.server.domain.review.entity.ProductReview;
import com.ipillgood.server.domain.review.entity.ProductReviewReport;
import com.ipillgood.server.domain.review.entity.enums.ReviewReportStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductReviewReportRepository extends JpaRepository<ProductReviewReport, Long> {
    boolean existsByReporterMemberAndReview(Member member, ProductReview review);

    // 관리자 신고 목록 조회 - 후기 내용 키워드/처리 상태 조건, 최신 신고순
    @Query("""
            select rr from ProductReviewReport rr
            join rr.review r
            where (:keyword is null or lower(r.content) like concat('%', cast(:keyword as string), '%'))
              and (:statuses is null or rr.status in :statuses)
            order by rr.id desc
            """)
    Page<ProductReviewReport> searchForAdmin(
            @Param("keyword") String keyword,
            @Param("statuses") List<ReviewReportStatus> statuses,
            Pageable pageable
    );

    // 관리자 신고 상세 조회 - 후기/작성자 정보까지 함께 조회
    @EntityGraph(attributePaths = {"review", "review.member"})
    Optional<ProductReviewReport> findWithDetailsById(Long id);
}
