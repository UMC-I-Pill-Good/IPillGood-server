package com.ipillgood.server.domain.recommendation.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ipillgood.server.domain.recommendation.code.RecommendationSuccessCode;
import com.ipillgood.server.domain.recommendation.controller.docs.RecommendationControllerApi;
import com.ipillgood.server.domain.recommendation.dto.RecommendationResponse;
import com.ipillgood.server.domain.recommendation.service.RecommendationService;
import com.ipillgood.server.global.apiPayload.ApiResponse;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/recommendations")
public class RecommendationController implements RecommendationControllerApi {

    private final RecommendationService recommendationService;

    @Override
    @GetMapping("/current")
    public ApiResponse<RecommendationResponse.Detail> getCurrentRecommendation(@AuthenticationPrincipal Long memberId) {
        RecommendationResponse.Detail response = recommendationService.getCurrentRecommendation(memberId);
        return ApiResponse.onSuccess(RecommendationSuccessCode.CURRENT_RECOMMENDATION_SUCCESS, response);
    }

    @Override
    @GetMapping("/{recommendationId}")
    public ApiResponse<RecommendationResponse.Detail> getRecommendation(@AuthenticationPrincipal Long memberId,
                                                                          @PathVariable Long recommendationId) {
        RecommendationResponse.Detail response = recommendationService.getRecommendation(memberId, recommendationId);
        return ApiResponse.onSuccess(RecommendationSuccessCode.RECOMMENDATION_DETAIL_SUCCESS, response);
    }

    @Override
    @PostMapping("/{recommendationId}/retry")
    public ApiResponse<RecommendationResponse.Retry> retryRecommendation(@AuthenticationPrincipal Long memberId,
                                                                           @PathVariable Long recommendationId) {
        RecommendationResponse.Retry response = recommendationService.retry(memberId, recommendationId);
        return ApiResponse.onSuccess(RecommendationSuccessCode.RECOMMENDATION_RETRY_SUCCESS, response);
    }

    @Override
    @PostMapping("/{recommendationId}/confirm")
    public ApiResponse<RecommendationResponse.Confirm> confirmRecommendation(@AuthenticationPrincipal Long memberId,
                                                                               @PathVariable Long recommendationId) {
        RecommendationResponse.Confirm response = recommendationService.confirm(memberId, recommendationId);
        return ApiResponse.onSuccess(RecommendationSuccessCode.RECOMMENDATION_CONFIRM_SUCCESS, response);
    }
}
