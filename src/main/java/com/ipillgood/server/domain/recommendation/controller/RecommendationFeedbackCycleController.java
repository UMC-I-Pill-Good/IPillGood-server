package com.ipillgood.server.domain.recommendation.controller;

import com.ipillgood.server.domain.recommendation.code.RecommendationSuccessCode;
import com.ipillgood.server.domain.recommendation.controller.docs.RecommendationFeedbackCycleControllerDocs;
import com.ipillgood.server.domain.recommendation.dto.RecommendationFeedbackCycleRequest;
import com.ipillgood.server.domain.recommendation.dto.RecommendationFeedbackCycleResponse;
import com.ipillgood.server.domain.recommendation.service.RecommendationFeedbackCycleService;
import com.ipillgood.server.global.apiPayload.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/recommendation-feedback-cycles")
public class RecommendationFeedbackCycleController implements RecommendationFeedbackCycleControllerDocs {

    private final RecommendationFeedbackCycleService recommendationFeedbackCycleService;

    @Override
    @GetMapping("/due")
    public ApiResponse<RecommendationFeedbackCycleResponse.Due> getDue(@AuthenticationPrincipal Long memberId) {
        RecommendationFeedbackCycleResponse.Due response = recommendationFeedbackCycleService.getDue(memberId);
        return ApiResponse.onSuccess(RecommendationSuccessCode.FEEDBACK_DUE_SUCCESS, response);
    }

    @Override
    @PostMapping("/{cycleId}/response")
    public ApiResponse<RecommendationFeedbackCycleResponse.Respond> respond(
            @AuthenticationPrincipal Long memberId,
            @PathVariable Long cycleId,
            @RequestBody RecommendationFeedbackCycleRequest.Respond request) {
        RecommendationFeedbackCycleResponse.Respond response =
                recommendationFeedbackCycleService.respond(memberId, cycleId, request);
        return ApiResponse.onSuccess(RecommendationSuccessCode.FEEDBACK_RESPONSE_SUCCESS, response);
    }
}
