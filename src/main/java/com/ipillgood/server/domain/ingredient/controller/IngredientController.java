package com.ipillgood.server.domain.ingredient.controller;

import com.ipillgood.server.domain.ingredient.controller.docs.IngredientApi;
import com.ipillgood.server.domain.ingredient.dto.IngredientResponse;
import com.ipillgood.server.domain.ingredient.service.IngredientService;
import com.ipillgood.server.global.apiPayload.ApiResponse;
import com.ipillgood.server.global.apiPayload.code.GeneralSuccessCode;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class IngredientController implements IngredientApi {

    private final IngredientService ingredientService;

    @Override
    @GetMapping("/ingredients")
    public ApiResponse<IngredientResponse.IngredientList> getIngredients() {
        IngredientResponse.IngredientList response = ingredientService.getIngredients();
        return ApiResponse.onSuccess(GeneralSuccessCode.OK, response);
    }

    @Override
    @GetMapping("/ingredients/{ingredientId}")
    public ApiResponse<IngredientResponse.IngredientDetail> getIngredient(
            @PathVariable Long ingredientId,
            @AuthenticationPrincipal Long memberId
    ) {
        IngredientResponse.IngredientDetail response = ingredientService.getIngredient(ingredientId, memberId);
        return ApiResponse.onSuccess(GeneralSuccessCode.OK, response);
    }

    @Override
    @GetMapping("/contraindications")
    public ApiResponse<IngredientResponse.ContraindicationList> getContraindications() {
        IngredientResponse.ContraindicationList response = ingredientService.getContraindications();
        return ApiResponse.onSuccess(GeneralSuccessCode.OK, response);
    }
}
