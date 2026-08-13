package com.ipillgood.server.domain.survey.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ipillgood.server.domain.survey.controller.docs.SurveyControllerApi;
import com.ipillgood.server.domain.survey.dto.SurveyRequest;
import com.ipillgood.server.domain.survey.dto.SurveyResult;
import com.ipillgood.server.domain.survey.service.SurveyService;
import com.ipillgood.server.global.apiPayload.ApiResponse;
import com.ipillgood.server.global.apiPayload.code.GeneralSuccessCode;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/surveys")
public class SurveyController implements SurveyControllerApi {

    private final SurveyService surveyService;

    @Override
    @PostMapping("/responses")
    public ApiResponse<SurveyResult.Submit> submit(@AuthenticationPrincipal Long memberId,
                                                     @Valid @RequestBody SurveyRequest.Submit request) {
        SurveyResult.Submit response = surveyService.submit(memberId, request);

        return ApiResponse.onSuccess(GeneralSuccessCode.ACCEPTED, response);
    }
}
