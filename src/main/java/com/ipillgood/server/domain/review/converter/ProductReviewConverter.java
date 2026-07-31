package com.ipillgood.server.domain.review.converter;

import com.ipillgood.server.domain.review.dto.ProductReviewResponse;
import com.ipillgood.server.domain.review.entity.enums.ProductReviewSort;
import com.ipillgood.server.domain.review.repository.ProductReviewProjection;
import com.ipillgood.server.domain.survey.repository.SurveyProjection;
import com.ipillgood.server.global.enums.AgeGroup;
import com.ipillgood.server.global.enums.Gender;
import com.ipillgood.server.global.pagination.CursorPage;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ProductReviewConverter {

    public static ProductReviewResponse.ProductReviews toProductReviews(
            Long productId,
            Long memberId,
            ProductReviewSort sort,
            int size,
            ProductReviewResponse.ReviewSummary summary,
            CursorPage<ProductReviewProjection.Review> page,
            List<ProductReviewProjection.ReviewImage> imageRows,
            Set<Long> helpfulReviewIds,
            Map<Long, SurveyProjection.MemberProfile> profilesByMemberId,
            Function<String, String> toImageUrl
    ) {
        Map<Long, List<String>> imageKeysByReviewId = groupImageKeysByReviewId(imageRows);

        List<ProductReviewResponse.ProductReviews.Review> reviews = page.content().stream()
                .map(row -> toReview(
                        row,
                        memberId,
                        imageKeysByReviewId.getOrDefault(row.reviewId(), List.of()),
                        helpfulReviewIds.contains(row.reviewId()),
                        profilesByMemberId.get(row.memberId()),
                        toImageUrl))
                .toList();

        return ProductReviewResponse.ProductReviews.builder()
                .productId(productId)
                .reviewCount(summary.reviewCount())
                .ratingAverage(summary.ratingAverage())
                .sort(sort)
                .size(size)
                .hasNext(page.hasNext())
                .nextCursor(page.nextCursor())
                .reviews(reviews)
                .build();
    }

    private static ProductReviewResponse.ProductReviews.Review toReview(
            ProductReviewProjection.Review row,
            Long memberId,
            List<String> imageKeys,
            boolean helpedByMe,
            SurveyProjection.MemberProfile profile,
            Function<String, String> toImageUrl
    ) {
        List<String> reviewImageUrls = imageKeys.stream()
                .map(toImageUrl)
                .toList();

        return ProductReviewResponse.ProductReviews.Review.builder()
                .reviewId(row.reviewId())
                .nickname(row.nickname())
                .profileImageUrl(toImageUrl.apply(row.profileImageKey()))
                .ageGroup(toAgeGroup(profile))
                .gender(toGender(profile))
                .rating(row.rating() == null ? null : row.rating().intValue())
                .content(row.content())
                .reviewImageUrls(reviewImageUrls)
                .helpfulCount(row.helpfulCount())
                .helpedByMe(helpedByMe)
                .mine(memberId.equals(row.memberId()))
                .createdAt(row.createdAt())
                .build();
    }

    private static AgeGroup toAgeGroup(SurveyProjection.MemberProfile profile) {
        return profile == null ? null : AgeGroup.fromBirthYear(profile.birthYear());
    }

    private static Gender toGender(SurveyProjection.MemberProfile profile) {
        return profile == null ? null : profile.gender();
    }

    private static Map<Long, List<String>> groupImageKeysByReviewId(
            List<ProductReviewProjection.ReviewImage> imageRows
    ) {
        Map<Long, List<String>> grouped = new HashMap<>();
        for (ProductReviewProjection.ReviewImage row : imageRows) {
            grouped.computeIfAbsent(row.reviewId(), reviewId -> new ArrayList<>()).add(row.imageKey());
        }
        return grouped;
    }
}
