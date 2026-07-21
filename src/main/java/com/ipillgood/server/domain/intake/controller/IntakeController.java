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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
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
    @GetMapping("/active-products")
    public ApiResponse<IntakeResponse.ActiveProducts> getActiveProducts(
            @AuthenticationPrincipal Long memberId
    ) {
        IntakeResponse.ActiveProducts response = intakeService.getActiveProducts(memberId);
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
