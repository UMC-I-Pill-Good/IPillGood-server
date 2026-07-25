package com.ipillgood.server.domain.search.controller;

import com.ipillgood.server.domain.healthconcern.entity.enums.MajorCategory;
import com.ipillgood.server.domain.search.code.SearchSuccessCode;
import com.ipillgood.server.domain.search.controller.docs.ProductSearchApi;
import com.ipillgood.server.domain.search.dto.ProductSearchRequest;
import com.ipillgood.server.domain.search.dto.ProductSearchResponse;
import com.ipillgood.server.domain.search.entity.enums.ProductSearchSort;
import com.ipillgood.server.domain.search.service.ProductSearchService;
import com.ipillgood.server.global.apiPayload.ApiResponse;
import com.ipillgood.server.global.enums.AgeGroup;
import com.ipillgood.server.global.enums.Gender;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/search")
public class ProductSearchController implements ProductSearchApi {

    private final ProductSearchService productSearchService;

    @Override
    @GetMapping("/products")
    public ApiResponse<ProductSearchResponse.ProductSearch> searchProducts(
            @AuthenticationPrincipal Long memberId,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) ProductSearchSort sort,
            @RequestParam(required = false) List<AgeGroup> ageGroups,
            @RequestParam(required = false) Gender gender,
            @RequestParam(required = false) Boolean mfdsCertified,
            @RequestParam(required = false) List<MajorCategory> healthConcernMajorCategories,
            @RequestParam(required = false) Integer size,
            @RequestParam(required = false) String cursor
    ) {
        ProductSearchResponse.ProductSearch resDto = productSearchService.searchProducts(
                keyword, sort, ageGroups, gender, mfdsCertified, healthConcernMajorCategories, size, cursor);
        return ApiResponse.onSuccess(SearchSuccessCode.PRODUCT_SEARCH_SUCCESS, resDto);
    }

    @Override
    @GetMapping("/recent-keywords")
    public ApiResponse<ProductSearchResponse.RecentSearchKeywords> getRecentSearchKeywords(
            @AuthenticationPrincipal Long memberId
    ) {
        ProductSearchResponse.RecentSearchKeywords resDto = productSearchService.getRecentSearchKeywords(memberId);
        return ApiResponse.onSuccess(SearchSuccessCode.VIEW_RECENT_SEARCH_KEYWORDS_SUCCESS, resDto);
    }

    @Override
    @PostMapping("/recent-keywords")
    public ApiResponse<ProductSearchResponse.RecentSearchKeyword> storeRecentSearchKeyword(
            @AuthenticationPrincipal Long memberId,
            @RequestBody @Valid ProductSearchRequest.RecentSearchKeyword reqDto
    ) {
        ProductSearchResponse.RecentSearchKeyword resDto = productSearchService.storeRecentSearchKeyword(memberId, reqDto.keyword());
        return ApiResponse.onSuccess(SearchSuccessCode.STORE_RECENT_SEARCH_KEYWORD_SUCCESS, resDto);
    }
}
