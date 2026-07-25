package com.ipillgood.server.domain.search.repository;

import com.ipillgood.server.domain.healthconcern.entity.QHealthConcernIngredient;
import com.ipillgood.server.domain.healthconcern.entity.enums.MajorCategory;
import com.ipillgood.server.domain.ingredient.entity.QIngredient;
import com.ipillgood.server.domain.ingredient.entity.QIngredientAgeGroup;
import com.ipillgood.server.domain.ingredient.entity.enums.TargetGender;
import com.ipillgood.server.domain.product.entity.QProduct;
import com.ipillgood.server.domain.product.entity.QProductIngredient;
import com.ipillgood.server.domain.review.entity.QProductReview;
import com.ipillgood.server.global.enums.AgeGroup;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;

import java.util.Collection;
import java.util.List;

@RequiredArgsConstructor
public class ProductSearchQueryDslImpl implements ProductSearchQueryDsl {

    private static final QProduct product = QProduct.product;
    private static final QProductIngredient productIngredient = QProductIngredient.productIngredient;
    private static final QIngredient ingredient = QIngredient.ingredient;
    private static final QProductReview review = QProductReview.productReview;

    private final JPAQueryFactory queryFactory;

    @Override
    public List<ProductSearchProjection.Product> searchProducts(ProductSearchCondition condition) {
        NumberExpression<Long> reviewCount = review.id.countDistinct();
        NumberExpression<Double> averageRating = review.rating.avg();

        JPAQuery<ProductSearchProjection.Product> query = queryFactory
                .select(Projections.constructor(ProductSearchProjection.Product.class,
                        product.id,
                        product.brand,
                        product.name,
                        product.mfdsCertified,
                        averageRating,
                        reviewCount))
                .from(product)
                .leftJoin(review).on(review.product.eq(product).and(review.deletedAt.isNull()))
                .where(buildWhere(condition))
                .groupBy(product.id, product.brand, product.name, product.mfdsCertified);

        ProductSearchCondition.Cursor cursor = condition.cursor();
        switch (condition.sort()) {
            case REVIEW_COUNT -> {
                if (cursor != null) {
                    query.having(reviewCount.lt(cursor.reviewCount())
                            .or(reviewCount.eq(cursor.reviewCount())
                                    .and(product.id.lt(cursor.productId()))));
                }
                query.orderBy(reviewCount.desc(), product.id.desc());
            }
            case RATING -> {
                if (cursor != null) {
                    query.having(ratingCursorPredicate(cursor, averageRating));
                }
                NumberExpression<Integer> ratingNullOrder = new CaseBuilder()
                        .when(averageRating.isNull()).then(1).otherwise(0);
                query.orderBy(ratingNullOrder.asc(), averageRating.desc(), product.id.desc());
            }
        }

        return query.limit(condition.size() + 1L).fetch();
    }

    private BooleanExpression ratingCursorPredicate(
            ProductSearchCondition.Cursor cursor,
            NumberExpression<Double> averageRating
    ) {
        if (cursor.ratingMissing()) {
            return averageRating.isNull().and(product.id.lt(cursor.productId()));
        }
        return averageRating.isNull()
                .or(averageRating.lt(cursor.rating()))
                .or(averageRating.eq(cursor.rating()).and(product.id.lt(cursor.productId())));
    }

    @Override
    public long countProducts(ProductSearchCondition condition) {
        return queryFactory
                .select(product.count())
                .from(product)
                .where(buildWhere(condition))
                .fetchOne();
    }

    @Override
    public List<ProductSearchProjection.Ingredient> findIngredientsByProductIds(Collection<Long> productIds) {
        if (productIds.isEmpty()) {
            return List.of();
        }

        return queryFactory
                .select(Projections.constructor(ProductSearchProjection.Ingredient.class,
                        productIngredient.product.id,
                        ingredient.name,
                        ingredient.imageKey))
                .from(productIngredient)
                .join(productIngredient.ingredient, ingredient)
                .where(productIngredient.product.id.in(productIds))
                .fetch();
    }

    private BooleanBuilder buildWhere(ProductSearchCondition condition) {
        BooleanBuilder where = new BooleanBuilder(product.deletedAt.isNull());

        if (condition.keyword() != null) {
            where.and(keywordPredicate(condition.keyword()));
        }
        if (condition.mfdsCertifiedOnly()) {
            where.and(product.mfdsCertified.isTrue());
        }
        if (!condition.ageGroups().isEmpty()) {
            where.and(ageGroupPredicate(condition.ageGroups()));
        }
        if (!condition.targetGenders().isEmpty()) {
            where.and(targetGenderPredicate(condition.targetGenders()));
        }
        if (!condition.majorCategories().isEmpty()) {
            where.and(majorCategoryPredicate(condition.majorCategories()));
        }
        return where;
    }

    // 브랜드명 / 영양제명 / 포함 성분명을 검색 대상으로 적용
    private BooleanExpression keywordPredicate(String keyword) {
        QProductIngredient keywordPi = new QProductIngredient("keywordPi");
        QIngredient keywordIngredient = new QIngredient("keywordIngredient");
        return product.brand.containsIgnoreCase(keyword)
                .or(product.name.containsIgnoreCase(keyword))
                .or(JPAExpressions.selectOne()
                        .from(keywordPi)
                        .join(keywordPi.ingredient, keywordIngredient)
                        .where(keywordPi.product.eq(product)
                                .and(keywordIngredient.name.containsIgnoreCase(keyword)))
                        .exists());
    }

    // 해당 연령대에 권장되는 성분을 하나 이상 포함한 상품
    private BooleanExpression ageGroupPredicate(Collection<AgeGroup> ageGroups) {
        QIngredientAgeGroup ingredientAgeGroup = QIngredientAgeGroup.ingredientAgeGroup;
        QProductIngredient agePi = new QProductIngredient("agePi");
        return JPAExpressions.selectOne()
                .from(ingredientAgeGroup)
                .join(agePi).on(agePi.ingredient.eq(ingredientAgeGroup.ingredient))
                .where(agePi.product.eq(product)
                        .and(ingredientAgeGroup.ageGroup.in(ageGroups)))
                .exists();
    }

    // 해당 성별에 권장되는 성분을 하나 이상 포함한 상품
    private BooleanExpression targetGenderPredicate(Collection<TargetGender> targetGenders) {
        QProductIngredient genderPi = new QProductIngredient("genderPi");
        return JPAExpressions.selectOne()
                .from(genderPi)
                .where(genderPi.product.eq(product)
                        .and(genderPi.ingredient.recommendedGender.in(targetGenders)))
                .exists();
    }

    // 해당 건강 상태 대분류와 연결된 성분을 하나 이상 포함한 상품
    private BooleanExpression majorCategoryPredicate(Collection<MajorCategory> majorCategories) {
        QHealthConcernIngredient healthConcernIngredient = QHealthConcernIngredient.healthConcernIngredient;
        QProductIngredient concernPi = new QProductIngredient("concernPi");
        return JPAExpressions.selectOne()
                .from(healthConcernIngredient)
                .join(concernPi).on(concernPi.ingredient.eq(healthConcernIngredient.ingredient))
                .where(concernPi.product.eq(product)
                        .and(healthConcernIngredient.healthConcern.majorCategory.in(majorCategories)))
                .exists();
    }

}
