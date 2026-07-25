package com.ipillgood.server.domain.search.service;

import com.ipillgood.server.domain.healthconcern.entity.enums.MajorCategory;
import com.ipillgood.server.domain.ingredient.entity.enums.TargetGender;
import com.ipillgood.server.domain.search.converter.ProductSearchConverter;
import com.ipillgood.server.domain.search.entity.MemberSearchKeyword;
import com.ipillgood.server.domain.search.repository.MemberSearchKeywordRepository;
import com.ipillgood.server.domain.search.repository.ProductSearchCondition;
import com.ipillgood.server.domain.search.dto.ProductSearchResponse;
import com.ipillgood.server.domain.search.entity.enums.ProductSearchSort;
import com.ipillgood.server.domain.search.repository.ProductSearchProjection;
import com.ipillgood.server.domain.search.repository.ProductSearchRepository;
import com.ipillgood.server.global.enums.AgeGroup;
import com.ipillgood.server.global.enums.Gender;
import com.ipillgood.server.global.s3.S3Service;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductSearchService {

    private static final int DEFAULT_PRODUCT_SEARCH_PAGE_SIZE = 20;

    private final ProductSearchRepository productSearchRepository;
    private final MemberSearchKeywordRepository memberSearchKeywordRepository;
    private final S3Service s3Service;

    public ProductSearchResponse.ProductSearch searchProducts(
            String keyword,
            ProductSearchSort sort,
            List<AgeGroup> ageGroups,
            Gender gender,
            Boolean mfdsCertified,
            List<MajorCategory> healthConcernMajorCategories,
            Integer size,
            String cursor
    ) {
        ProductSearchCondition condition = toProductSearchCondition(
                keyword, sort, ageGroups, gender, mfdsCertified, healthConcernMajorCategories, size, cursor);

        List<ProductSearchProjection.Product> rows = productSearchRepository.searchProducts(condition);
        boolean hasNext = rows.size() > condition.size();
        List<ProductSearchProjection.Product> pageRows = hasNext ? rows.subList(0, condition.size()) : rows;

        List<Long> productIds = pageRows.stream().map(ProductSearchProjection.Product::productId).toList();
        List<ProductSearchProjection.Ingredient> ingredientRows =
                productSearchRepository.findIngredientsByProductIds(productIds);

        long totalCount = productSearchRepository.countProducts(condition);
        String nextCursor = hasNext
                ? ProductSearchCursorCodec.encode(condition.sort(), pageRows.get(pageRows.size() - 1))
                : null;

        return ProductSearchConverter.toProductSearch(
                condition.keyword(),
                condition.size(),
                totalCount,
                hasNext,
                nextCursor,
                pageRows,
                ingredientRows,
                s3Service
        );
    }

    public ProductSearchResponse.RecentSearchKeywords getRecentSearchKeywords(Long memberId) {
        List<MemberSearchKeyword> keywords = memberSearchKeywordRepository
                .findTop10ByMemberIdOrderBySearchedAtDesc(memberId);

        return ProductSearchConverter.toRecentSearchKeywords(keywords);
    }

    private ProductSearchCondition toProductSearchCondition(
            String keyword,
            ProductSearchSort sort,
            List<AgeGroup> ageGroups,
            Gender gender,
            Boolean mfdsCertified,
            List<MajorCategory> healthConcernMajorCategories,
            Integer size,
            String cursor
    ) {
        ProductSearchSort searchSort = (sort == null) ? ProductSearchSort.REVIEW_COUNT : sort;

        return new ProductSearchCondition(
                normalizeKeyword(keyword),
                searchSort,
                toAgeGroupFilter(ageGroups),
                toTargetGenderFilter(gender),
                Boolean.TRUE.equals(mfdsCertified),
                healthConcernMajorCategories == null ? List.of() : distinct(healthConcernMajorCategories),
                size == null ? DEFAULT_PRODUCT_SEARCH_PAGE_SIZE : size,
                ProductSearchCursorCodec.decode(cursor, searchSort)
        );
    }

    private String normalizeKeyword(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return null;
        }
        return keyword.trim();
    }

    private List<AgeGroup> toAgeGroupFilter(List<AgeGroup> ageGroups) {
        if (ageGroups == null || ageGroups.isEmpty() || ageGroups.contains(AgeGroup.ALL)) {
            return List.of();
        }

        Set<AgeGroup> filter = new LinkedHashSet<>(ageGroups);
        filter.add(AgeGroup.ALL);
        return List.copyOf(filter);
    }

    private List<TargetGender> toTargetGenderFilter(Gender gender) {
        if (gender == null) {
            return List.of();
        }
        return List.of(TargetGender.valueOf(gender.name()), TargetGender.BOTH);
    }

    private <T> List<T> distinct(List<T> values) {
        return List.copyOf(new LinkedHashSet<>(values));
    }
}
