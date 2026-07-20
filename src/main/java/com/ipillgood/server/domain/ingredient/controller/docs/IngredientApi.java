package com.ipillgood.server.domain.ingredient.controller.docs;

import com.ipillgood.server.domain.ingredient.dto.IngredientResponse;
import com.ipillgood.server.global.apiPayload.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Ingredient API", description = "영양성분 및 금기 조건 관련 API")
public interface IngredientApi {

    @Operation(summary = "금기 조건 목록 조회",
            description = "설문에서 사용할 복용약, 기저질환, 알러지 금기 조건 목록을 조회합니다.")
    ApiResponse<IngredientResponse.ContraindicationList> getContraindications(String type, String keyword);
}

