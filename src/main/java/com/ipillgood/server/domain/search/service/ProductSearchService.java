package com.ipillgood.server.domain.search.service;

import com.ipillgood.server.domain.healthconcern.entity.enums.MajorCategory;
import com.ipillgood.server.domain.ingredient.entity.enums.TargetGender;
import com.ipillgood.server.domain.search.code.SearchErrorCode;
import com.ipillgood.server.domain.search.converter.ProductSearchConverter;
import com.ipillgood.server.domain.member.repository.MemberRepository;
import com.ipillgood.server.domain.search.entity.MemberSearchKeyword;
import com.ipillgood.server.domain.search.exception.SearchException;
import com.ipillgood.server.domain.search.repository.MemberSearchKeywordRepository;
import com.ipillgood.server.domain.search.repository.ProductSearchCondition;
import com.ipillgood.server.domain.search.dto.ProductSearchResponse;
import com.ipillgood.server.domain.search.entity.enums.ProductSearchSort;
import com.ipillgood.server.domain.search.repository.ProductSearchProjection;
import com.ipillgood.server.domain.search.repository.ProductSearchRepository;
import com.ipillgood.server.global.enums.AgeGroup;
import com.ipillgood.server.global.enums.Gender;
import com.ipillgood.server.global.pagination.CursorPage;
import com.ipillgood.server.global.s3.S3Service;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
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
    private final MemberRepository memberRepository;
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
        CursorPage<ProductSearchProjection.Product> page = CursorPage.of(
                rows,
                condition.size(),
                lastRow -> ProductSearchCursorCodec.encode(condition.sort(), lastRow));

        List<Long> productIds = page.content().stream()
                .map(ProductSearchProjection.Product::productId)
                .toList();
        List<ProductSearchProjection.Ingredient> ingredientRows =
                productSearchRepository.findIngredientsByProductIds(productIds);

        long totalCount = productSearchRepository.countProducts(condition);

        return ProductSearchConverter.toProductSearch(
                condition.keyword(),
                condition.size(),
                totalCount,
                page.hasNext(),
                page.nextCursor(),
                page.content(),
                ingredientRows,
                s3Service
        );
    }

    public ProductSearchResponse.RecentSearchKeywords getRecentSearchKeywords(Long memberId) {
        List<MemberSearchKeyword> keywords = memberSearchKeywordRepository
                .findTop10ByMemberIdOrderBySearchedAtDesc(memberId);

        return ProductSearchConverter.toRecentSearchKeywords(keywords);
    }

    @Transactional
    public ProductSearchResponse.RecentSearchKeyword storeRecentSearchKeyword(Long memberId, String rawKeyword) {
        String keyword = rawKeyword.trim();

        MemberSearchKeyword recentKeyword = memberSearchKeywordRepository
                .findByMemberIdAndKeyword(memberId, keyword)
                .map(existing -> {
                    existing.updateSearchedAt(LocalDateTime.now());
                    return existing;
                })
                .orElseGet(() -> {
                    MemberSearchKeyword created = memberSearchKeywordRepository.save(
                            MemberSearchKeyword.builder()
                                    .member(memberRepository.getReferenceById(memberId))
                                    .keyword(keyword)
                                    .searchedAt(LocalDateTime.now())
                                    .build());
                    return created;
                });

        return ProductSearchConverter.toRecentSearchKeyword(recentKeyword);
    }


    @Transactional
    public ProductSearchResponse.DeletedKeyword deleteRecentSearchKeyword(Long memberId, Long keywordId) {
        MemberSearchKeyword keyword = memberSearchKeywordRepository.findById(keywordId)
                .orElseThrow(() -> new SearchException(SearchErrorCode.SEARCH_KEYWORD_NOT_FOUND));

        if(!keyword.getMember().getId().equals(memberId)) {
            throw new SearchException(SearchErrorCode.RECENT_KEYWORD_FORBIDDEN);
        }
        memberSearchKeywordRepository.delete(keyword);
        return ProductSearchConverter.toDeletedKeyword(keyword);
    }


    @Transactional
    public ProductSearchResponse.DeletedKeywords deleteAllRecentSearchKeywords(Long memberId) {
         List<MemberSearchKeyword> keywords = memberSearchKeywordRepository.findByMemberId(memberId);

         Integer deletedCount = keywords.size();
         memberSearchKeywordRepository.deleteAll(keywords);
         return ProductSearchConverter.toDeletedKeywords(deletedCount);
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
