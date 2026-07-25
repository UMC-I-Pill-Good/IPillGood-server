package com.ipillgood.server.domain.healthconcern.controller;

import com.ipillgood.server.domain.healthconcern.controller.docs.HealthConcernApi;
import com.ipillgood.server.domain.healthconcern.dto.HealthConcernResponse;
import com.ipillgood.server.domain.healthconcern.service.HealthConcernService;
import com.ipillgood.server.global.apiPayload.ApiResponse;
import com.ipillgood.server.global.apiPayload.code.GeneralSuccessCode;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/health-concerns")
public class HealthConcernController implements HealthConcernApi {

    private final HealthConcernService healthConcernService;

    @Override
    @GetMapping("/categories")
    public ApiResponse<HealthConcernResponse.CategoryList> getCategories() {
        HealthConcernResponse.CategoryList response = healthConcernService.getCategories();
        return ApiResponse.onSuccess(GeneralSuccessCode.OK, response);
    }
}
