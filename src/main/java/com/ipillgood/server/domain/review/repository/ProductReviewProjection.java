package com.ipillgood.server.domain.review.repository;

import java.time.LocalDateTime;

public class ProductReviewProjection {

    public record ReviewSummary(
            Double ratingAverage,
            Long reviewCount
    ) {}

    public record Review(
            Long reviewId,
            Long memberId,
            String nickname,
            String profileImageKey,
            Short rating,
            String content,
            Integer helpfulCount,
            LocalDateTime createdAt
    ) {}

    public record ReviewImage(
            Long reviewId,
            String imageKey
    ) {}
}
