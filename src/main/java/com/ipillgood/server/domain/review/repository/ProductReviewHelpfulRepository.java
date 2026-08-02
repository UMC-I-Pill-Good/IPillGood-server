package com.ipillgood.server.domain.review.repository;

import com.ipillgood.server.domain.member.entity.Member;
import com.ipillgood.server.domain.review.entity.ProductReview;
import com.ipillgood.server.domain.review.entity.ProductReviewHelpful;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProductReviewHelpfulRepository extends JpaRepository<ProductReviewHelpful, Long> {

    boolean existsByMemberAndReview(Member member, ProductReview review);

    Optional<ProductReviewHelpful> findByMemberAndReview(Member member, ProductReview review);
}
