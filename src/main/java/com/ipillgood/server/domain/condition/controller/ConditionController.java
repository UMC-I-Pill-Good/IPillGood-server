package com.ipillgood.server.domain.condition.controller;

import com.ipillgood.server.domain.condition.code.ConditionSuccessCode;
import com.ipillgood.server.domain.condition.controller.docs.ConditionControllerDocs;
import com.ipillgood.server.domain.condition.dto.ConditionRequest;
import com.ipillgood.server.domain.condition.dto.ConditionResponse;
import com.ipillgood.server.domain.condition.service.ConditionService;
import com.ipillgood.server.global.apiPayload.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/conditions")
public class ConditionController implements ConditionControllerDocs {

    private final ConditionService conditionService;

    @Override
    @GetMapping("/current-week")
    public ApiResponse<ConditionResponse.CurrentWeek> getCurrentWeek(@AuthenticationPrincipal Long memberId) {
        ConditionResponse.CurrentWeek response = conditionService.getCurrentWeek(memberId);
        return ApiResponse.onSuccess(ConditionSuccessCode.CURRENT_WEEK_SUCCESS, response);
    }

    @Override
    @PostMapping("/weekly-records")
    public ApiResponse<ConditionResponse.Detail> saveWeeklyRecord(
            @AuthenticationPrincipal Long memberId,
            @RequestBody ConditionRequest.SaveWeeklyRecord request) {
        ConditionResponse.Detail response = conditionService.saveWeeklyRecord(memberId, request);
        return ApiResponse.onSuccess(ConditionSuccessCode.WEEKLY_RECORD_SAVE_SUCCESS, response);
    }

    @Override
    @PatchMapping("/popup-logs/auto-shown")
    public ApiResponse<ConditionResponse.PopupAutoShown> recordPopupAutoShown(
            @AuthenticationPrincipal Long memberId) {
        ConditionResponse.PopupAutoShown response = conditionService.recordPopupAutoShown(memberId);
        return ApiResponse.onSuccess(ConditionSuccessCode.POPUP_AUTO_SHOWN_SUCCESS, response);
    }

    @Override
    @PatchMapping("/popup-logs/current-week/dismissed")
    public ApiResponse<ConditionResponse.PopupDismissed> recordPopupDismissed(
            @AuthenticationPrincipal Long memberId) {
        ConditionResponse.PopupDismissed response = conditionService.recordPopupDismissed(memberId);
        return ApiResponse.onSuccess(ConditionSuccessCode.POPUP_DISMISSED_SUCCESS, response);
    }

    @Override
    @GetMapping("/monthly-records")
    public ApiResponse<ConditionResponse.MonthlySummary> getMonthlyRecords(
            @AuthenticationPrincipal Long memberId,
            @RequestParam(required = false) String year,
            @RequestParam(required = false) String month) {
        ConditionResponse.MonthlySummary response = conditionService.getMonthlyRecords(memberId, year, month);
        return ApiResponse.onSuccess(ConditionSuccessCode.MONTHLY_RECORDS_SUCCESS, response);
    }

    @Override
    @GetMapping("/weekly-records/{recordId}")
    public ApiResponse<ConditionResponse.Detail> getWeeklyRecordDetail(
            @AuthenticationPrincipal Long memberId,
            @PathVariable Long recordId) {
        ConditionResponse.Detail response = conditionService.getWeeklyRecordDetail(memberId, recordId);
        return ApiResponse.onSuccess(ConditionSuccessCode.WEEKLY_RECORD_DETAIL_SUCCESS, response);
    }
}
