package com.ipillgood.server.domain.review.repository;

import com.ipillgood.server.domain.member.entity.Member;
import com.ipillgood.server.domain.product.entity.Product;
import com.ipillgood.server.domain.review.entity.ProductReview;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductReviewRepository extends JpaRepository<ProductReview, Long>, ProductReviewQueryDsl {

    @Query("""
            select coalesce(avg(r.rating), 0.0) as ratingAverage,
                count(r) as reviewCount
            from ProductReview r
            where r.product = :product
              and r.deletedAt is null
    """)
    ProductReviewProjection.ReviewSummary findActiveSummaryByProduct(@Param("product") Product product);

    boolean existsByMemberAndProductAndDeletedAtIsNull(Member member, Product product);
}
