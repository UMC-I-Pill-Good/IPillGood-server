package com.ipillgood.server.domain.cabinet.repository;

import com.ipillgood.server.domain.cabinet.entity.QMemberProduct;
import com.ipillgood.server.domain.ingredient.entity.QIngredient;
import com.ipillgood.server.domain.product.entity.QProduct;
import com.ipillgood.server.domain.product.entity.QProductIngredient;
import com.ipillgood.server.domain.review.entity.QProductReview;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.List;

@RequiredArgsConstructor
public class MemberProductQueryDslImpl implements MemberProductQueryDsl {

    private static final QProduct product = QProduct.product;
    private static final QProductIngredient productIngredient = QProductIngredient.productIngredient;
    private static final QIngredient ingredient = QIngredient.ingredient;
    private static final QProductReview review = QProductReview.productReview;
    private static final QMemberProduct memberProduct = QMemberProduct.memberProduct;

    private final JPAQueryFactory queryFactory;

    @Override
    public Page<CabinetProductCandidateRow> findProductCandidatesOrderByReviewCountDesc(
            Long memberId,
            String keyword,
            Pageable pageable
    ) {
        return findProductCandidates(memberId, keyword, pageable, CandidateSort.REVIEW_COUNT_DESC);
    }

    @Override
    public Page<CabinetProductCandidateRow> findProductCandidatesOrderByRatingDesc(
            Long memberId,
            String keyword,
            Pageable pageable
    ) {
        return findProductCandidates(memberId, keyword, pageable, CandidateSort.RATING_DESC);
    }

    private Page<CabinetProductCandidateRow> findProductCandidates(
            Long memberId,
            String keyword,
            Pageable pageable,
            CandidateSort sort
    ) {
        NumberExpression<Long> ingredientCount = productIngredient.id.countDistinct();
        NumberExpression<Double> averageRating = review.rating.avg();
        NumberExpression<Long> reviewCount = review.id.countDistinct();
        NumberExpression<Long> ownedCount = memberProduct.id.countDistinct();
        BooleanExpression isOwned = new CaseBuilder()
                .when(ownedCount.gt(0L)).then(true)
                .otherwise(false);

        JPAQuery<CabinetProductCandidateRow> query = queryFactory
                .select(Projections.constructor(CabinetProductCandidateRow.class,
                        product.id,
                        product.brand,
                        product.name,
                        ingredientCount,
                        ingredient.imageKey.min(),
                        averageRating,
                        reviewCount,
                        isOwned))
                .from(product)
                .leftJoin(productIngredient).on(productIngredient.product.eq(product))
                .leftJoin(productIngredient.ingredient, ingredient)
                .leftJoin(review).on(review.product.eq(product).and(review.deletedAt.isNull()))
                .leftJoin(memberProduct).on(memberProduct.product.eq(product)
                        .and(memberProduct.member.id.eq(memberId))
                        .and(memberProduct.deletedAt.isNull()))
                .where(buildWhere(keyword))
                .groupBy(product.id, product.brand, product.name);

        applyOrder(query, sort, averageRating, reviewCount);

        List<CabinetProductCandidateRow> candidates = query
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();
        long totalCount = countProductCandidates(keyword);

        return new PageImpl<>(candidates, pageable, totalCount);
    }

    private void applyOrder(
            JPAQuery<CabinetProductCandidateRow> query,
            CandidateSort sort,
            NumberExpression<Double> averageRating,
            NumberExpression<Long> reviewCount
    ) {
        switch (sort) {
            case REVIEW_COUNT_DESC -> query.orderBy(reviewCount.desc(), product.name.asc(), product.id.asc());
            case RATING_DESC -> {
                NumberExpression<Integer> ratingNullOrder = new CaseBuilder()
                        .when(averageRating.isNull()).then(1)
                        .otherwise(0);
                query.orderBy(ratingNullOrder.asc(), averageRating.desc(), product.name.asc(), product.id.asc());
            }
        }
    }

    private long countProductCandidates(String keyword) {
        Long count = queryFactory
                .select(product.id.countDistinct())
                .from(product)
                .where(buildWhere(keyword))
                .fetchOne();
        return count == null ? 0L : count;
    }

    private BooleanBuilder buildWhere(String keyword) {
        BooleanBuilder where = new BooleanBuilder(product.deletedAt.isNull());
        if (keyword != null) {
            where.and(keywordPredicate(keyword));
        }
        return where;
    }

    private BooleanExpression keywordPredicate(String keyword) {
        QProductIngredient keywordProductIngredient = new QProductIngredient("keywordProductIngredient");
        QIngredient keywordIngredient = new QIngredient("keywordIngredient");

        return product.brand.containsIgnoreCase(keyword)
                .or(product.name.containsIgnoreCase(keyword))
                .or(JPAExpressions.selectOne()
                        .from(keywordProductIngredient)
                        .join(keywordProductIngredient.ingredient, keywordIngredient)
                        .where(keywordProductIngredient.product.eq(product)
                                .and(keywordIngredient.name.containsIgnoreCase(keyword)))
                        .exists());
    }

    private enum CandidateSort {
        REVIEW_COUNT_DESC,
        RATING_DESC
    }
}
