package com.ipillgood.server.domain.intake.controller;

import com.ipillgood.server.domain.intake.controller.docs.IntakeApi;
import com.ipillgood.server.domain.intake.dto.IntakeRequest;
import com.ipillgood.server.domain.intake.dto.IntakeResponse;
import com.ipillgood.server.domain.intake.service.IntakeService;
import com.ipillgood.server.global.apiPayload.ApiResponse;
import com.ipillgood.server.global.apiPayload.code.GeneralSuccessCode;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/intake")
public class IntakeController implements IntakeApi {

    private final IntakeService intakeService;

    @Override
    @GetMapping("/today")
    public ApiResponse<IntakeResponse.TodayIntakeStatus> getTodayIntakeStatus(
            @AuthenticationPrincipal Long memberId
    ) {
        IntakeResponse.TodayIntakeStatus response = intakeService.getTodayIntakeStatus(memberId);
        return ApiResponse.onSuccess(GeneralSuccessCode.OK, response);
    }

    @Override
    @PatchMapping("/today/popup-shown")
    public ApiResponse<IntakeResponse.TodayPopupShown> recordTodayPopupShown(
            @AuthenticationPrincipal Long memberId
    ) {
        IntakeResponse.TodayPopupShown response = intakeService.recordTodayPopupShown(memberId);
        return ApiResponse.onSuccess(GeneralSuccessCode.OK, response);
    }

    @Override
    @PutMapping("/today/records")
    public ApiResponse<IntakeResponse.SaveTodayIntakeRecords> saveTodayIntakeRecords(
            @AuthenticationPrincipal Long memberId,
            @RequestBody(required = false) IntakeRequest.SaveTodayIntakeRecords request
    ) {
        IntakeResponse.SaveTodayIntakeRecords response = intakeService.saveTodayIntakeRecords(memberId, request);
        return ApiResponse.onSuccess(GeneralSuccessCode.OK, response);
    }

    @Override
    @GetMapping("/active-products")
    public ApiResponse<IntakeResponse.ActiveProducts> getActiveProducts(
            @AuthenticationPrincipal Long memberId
    ) {
        IntakeResponse.ActiveProducts response = intakeService.getActiveProducts(memberId);
        return ApiResponse.onSuccess(GeneralSuccessCode.OK, response);
    }

    @Override
    @PostMapping("/active-products")
    public ApiResponse<IntakeResponse.RegisterActiveProduct> registerActiveProduct(
            @AuthenticationPrincipal Long memberId,
            @RequestBody(required = false) IntakeRequest.RegisterActiveProduct request
    ) {
        IntakeResponse.RegisterActiveProduct response = intakeService.registerActiveProduct(memberId, request);
        return ApiResponse.onSuccess(GeneralSuccessCode.CREATED, response);
    }

    @Override
    @PatchMapping("/active-products/{activeProductId}")
    public ApiResponse<IntakeResponse.UpdateActiveProductSettings> updateActiveProductSettings(
            @AuthenticationPrincipal Long memberId,
            @PathVariable String activeProductId,
            @RequestBody(required = false) IntakeRequest.UpdateActiveProductSettings request
    ) {
        IntakeResponse.UpdateActiveProductSettings response =
                intakeService.updateActiveProductSettings(memberId, activeProductId, request);
        return ApiResponse.onSuccess(GeneralSuccessCode.OK, response);
    }

    @Override
    @DeleteMapping("/active-products/{activeProductId}")
    public ApiResponse<IntakeResponse.RemoveActiveProduct> removeActiveProduct(
            @AuthenticationPrincipal Long memberId,
            @PathVariable String activeProductId
    ) {
        IntakeResponse.RemoveActiveProduct response = intakeService.removeActiveProduct(memberId, activeProductId);
        return ApiResponse.onSuccess(GeneralSuccessCode.OK, response);
    }

    @Override
    @PostMapping("/compatibility-checks")
    public ApiResponse<IntakeResponse.CompatibilityCheck> checkCompatibility(
            @AuthenticationPrincipal Long memberId,
            @RequestBody(required = false) IntakeRequest.CompatibilityCheck request
    ) {
        IntakeResponse.CompatibilityCheck response = intakeService.checkCompatibility(memberId, request);
        return ApiResponse.onSuccess(GeneralSuccessCode.OK, response);
    }
}
