package com.ipillgood.server.domain.review.repository;

import com.ipillgood.server.domain.member.entity.QMember;
import com.ipillgood.server.domain.review.entity.QProductReview;
import com.ipillgood.server.domain.review.entity.QProductReviewHelpful;
import com.ipillgood.server.domain.review.entity.QProductReviewImage;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;

import java.util.Collection;
import java.util.List;

@RequiredArgsConstructor
public class ProductReviewQueryDslImpl implements ProductReviewQueryDsl {

    private static final QProductReview review = QProductReview.productReview;
    private static final QProductReviewImage reviewImage = QProductReviewImage.productReviewImage;
    private static final QProductReviewHelpful reviewHelpful = QProductReviewHelpful.productReviewHelpful;
    private static final QMember member = QMember.member;

    private final JPAQueryFactory queryFactory;

    @Override
    public List<ProductReviewProjection.Review> findReviews(ProductReviewCondition condition) {
        JPAQuery<ProductReviewProjection.Review> query = queryFactory
                .select(Projections.constructor(ProductReviewProjection.Review.class,
                        review.id,
                        member.id,
                        member.nickname,
                        member.profileImageKey,
                        review.rating,
                        review.content,
                        review.helpfulCount,
                        review.createdAt))
                .from(review)
                .join(review.member, member)
                .where(review.product.eq(condition.product()),
                        review.deletedAt.isNull(),
                        review.hidden.isFalse());

        ProductReviewCondition.Cursor cursor = condition.cursor();
        switch (condition.sort()) {
            case LATEST -> {
                if (cursor != null) {
                    query.where(review.createdAt.lt(cursor.createdAt())
                            .or(review.createdAt.eq(cursor.createdAt())
                                    .and(review.id.lt(cursor.reviewId()))));
                }
                query.orderBy(review.createdAt.desc(), review.id.desc());
            }
            case LIKE_COUNT_DESC -> {
                if (cursor != null) {
                    query.where(review.helpfulCount.lt(cursor.helpfulCount())
                            .or(review.helpfulCount.eq(cursor.helpfulCount())
                                    .and(review.id.lt(cursor.reviewId()))));
                }
                query.orderBy(review.helpfulCount.desc(), review.id.desc());
            }
        }

        return query.limit(condition.size() + 1L).fetch();
    }

    @Override
    public List<ProductReviewProjection.ReviewImage> findImagesByReviewIds(Collection<Long> reviewIds) {
        if (reviewIds.isEmpty()) {
            return List.of();
        }

        return queryFactory
                .select(Projections.constructor(ProductReviewProjection.ReviewImage.class,
                        reviewImage.review.id,
                        reviewImage.imageKey))
                .from(reviewImage)
                .where(reviewImage.review.id.in(reviewIds))
                .orderBy(reviewImage.review.id.asc(), reviewImage.displayOrder.asc())
                .fetch();
    }

    @Override
    public List<Long> findHelpfulReviewIds(Long memberId, Collection<Long> reviewIds) {
        if (memberId == null || reviewIds.isEmpty()) {
            return List.of();
        }

        return queryFactory
                .select(reviewHelpful.review.id)
                .from(reviewHelpful)
                .where(reviewHelpful.member.id.eq(memberId),
                        reviewHelpful.review.id.in(reviewIds))
                .fetch();
    }
}
