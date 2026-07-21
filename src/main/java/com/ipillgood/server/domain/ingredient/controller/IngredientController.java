package com.ipillgood.server.domain.ingredient.controller;

import com.ipillgood.server.domain.ingredient.code.IngredientSuccessCode;
import com.ipillgood.server.domain.ingredient.controller.docs.IngredientApi;
import com.ipillgood.server.domain.ingredient.dto.IngredientResponse;
import com.ipillgood.server.domain.ingredient.service.IngredientService;
import com.ipillgood.server.global.apiPayload.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class IngredientController implements IngredientApi {

    private final IngredientService ingredientService;

    @Override
    @GetMapping("/contraindications")
    public ApiResponse<IngredientResponse.ContraindicationList> getContraindications(
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String keyword
    ) {
        IngredientResponse.ContraindicationList response = ingredientService.getContraindications(type, keyword);
        return ApiResponse.onSuccess(IngredientSuccessCode.CONTRAINDICATION_LIST_SUCCESS, response);
    }
}

