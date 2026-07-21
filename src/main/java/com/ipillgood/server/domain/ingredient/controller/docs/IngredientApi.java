package com.ipillgood.server.domain.ingredient.controller.docs;

import com.ipillgood.server.domain.ingredient.dto.IngredientResponse;
import com.ipillgood.server.global.apiPayload.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Ingredient API", description = "영양성분 및 금기 조건 관련 API")
public interface IngredientApi {

    @Operation(
            summary = "금기 조건 목록 조회",
            description = "초기 설문에서 사용하는 복용약, 기저질환, 알러지 선택지를 조회합니다. type 미입력 시 전체를 조회하고, keyword 입력 시 조건명으로 검색합니다."
    )
    @SecurityRequirement(name = "JWT TOKEN")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "금기 조건 목록 조회 성공",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "성공 응답",
                                    value = """
                                            {
                                              "isSuccess": true,
                                              "code": "SUCCESS200_1",
                                              "message": "금기 조건 목록 조회에 성공했습니다.",
                                              "result": {
                                                "contraindications": [
                                                  {
                                                    "contraindicationId": 1,
                                                    "type": "MEDICATION",
                                                    "conditionName": "와파린"
                                                  }
                                                ],
                                                "groupedByType": {
                                                  "MEDICATION": [
                                                    {
                                                      "contraindicationId": 1,
                                                      "conditionName": "와파린"
                                                    }
                                                  ],
                                                  "UNDERLYING_DISEASE": [],
                                                  "ALLERGY": []
                                                }
                                              }
                                            }
                                            """
                            )
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "허용되지 않은 type 요청",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "허용되지 않은 type",
                                    value = """
                                            {
                                              "isSuccess": false,
                                              "code": "COMMON400_2",
                                              "message": "요청값 검증에 실패했습니다.",
                                              "result": null
                                            }
                                            """
                            )
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "JWT 액세스 토큰 누락, 만료 또는 유효하지 않은 토큰",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "토큰 없음",
                                    value = """
                                            {
                                              "isSuccess": false,
                                              "code": "JWT401_4",
                                              "message": "토큰이 없습니다.",
                                              "result": null
                                            }
                                            """
                            )
                    )
            )
    })
    ApiResponse<IngredientResponse.ContraindicationList> getContraindications(
            @Parameter(
                    description = "조회 유형: MEDICATION, UNDERLYING_DISEASE, ALLERGY",
                    required = false
            )
            String type,

            @Parameter(
                    description = "조건명 검색어",
                    required = false
            )
            String keyword
    );
}
