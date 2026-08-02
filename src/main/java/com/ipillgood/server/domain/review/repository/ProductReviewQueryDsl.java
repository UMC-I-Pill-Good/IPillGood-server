package com.ipillgood.server.domain.review.repository;

import java.util.Collection;
import java.util.List;

public interface ProductReviewQueryDsl {

    List<ProductReviewProjection.Review> findReviews(ProductReviewCondition condition);

    List<ProductReviewProjection.ReviewImage> findImagesByReviewIds(Collection<Long> reviewIds);

    List<Long> findHelpfulReviewIds(Long memberId, Collection<Long> reviewIds);
}
