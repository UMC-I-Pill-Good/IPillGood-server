package com.ipillgood.server.domain.ingredient.controller.docs;

import com.ipillgood.server.domain.ingredient.dto.IngredientResponse;
import com.ipillgood.server.global.apiPayload.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Ingredient API", description = "영양성분 및 금기 조건 관련 API")
public interface IngredientApi {

    @Operation(
            summary = "영양성분 목록 조회",
            description = "초기 설문에서 현재 섭취 중인 영양제 성분 선택에 사용할 목록을 조회합니다."
    )
    @SecurityRequirement(name = "JWT TOKEN")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "영양성분 목록 조회 성공",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "isSuccess": true,
                                              "code": "SUCCESS200_1",
                                              "message": "요청이 성공적으로 처리되었습니다.",
                                              "result": {
                                                "ingredients": [
                                                  {
                                                    "ingredientId": 2,
                                                    "name": "비타민 D",
                                                    "imageUrl": "https://ipillgood-bucket.s3.ap-northeast-2.amazonaws.com/ingredients/2.png"
                                                  }
                                                ]
                                              }
                                            }
                                            """
                            )
                    )
            )
    })
    ApiResponse<IngredientResponse.IngredientList> getIngredients();

    @Operation(
            summary = "영양성분 상세 조회",
            description = "성분 상세 화면에 필요한 설명, 효능, 주의사항, 병용 금기 조합, 대체 음식, 캐비닛 보유 여부와 섭취 중 여부를 조회합니다."
    )
    @SecurityRequirement(name = "JWT TOKEN")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "영양성분 상세 조회 성공",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "isSuccess": true,
                                              "code": "SUCCESS200_1",
                                              "message": "요청이 성공적으로 처리되었습니다.",
                                              "result": {
                                                "ingredientId": 2,
                                                "name": "비타민 D",
                                                "description": "칼슘 흡수와 뼈 건강에 도움을 주는 성분입니다.",
                                                "imageUrl": "https://ipillgood-bucket.s3.ap-northeast-2.amazonaws.com/ingredients/2.png",
                                                "effects": ["칼슘 흡수에 도움"],
                                                "cautions": ["과다 섭취 시 고칼슘혈증 위험이 있습니다."],
                                                "contraindicatedCombinations": [
                                                  {
                                                    "targetIngredientId": 10,
                                                    "targetIngredientName": "칼슘",
                                                    "type": "CAUTION",
                                                    "reason": "동시 복용 시 흡수에 영향을 줄 수 있습니다."
                                                  }
                                                ],
                                                "recommendedIntake": "3 ~ 10μg",
                                                "recommendedIntakeTime": "식후 섭취 권장",
                                                "hasCabinetProduct": false,
                                                "hasIntakeProduct": false,
                                                "alternativeFoods": [
                                                  {
                                                    "name": "연어",
                                                    "contentPer100g": "100g당 비타민 D 10μg"
                                                  }
                                                ]
                                              }
                                            }
                                            """
                            )
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "영양성분 ID 오류",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "isSuccess": false,
                                              "code": "INGREDIENT400_1",
                                              "message": "영양성분 ID가 올바르지 않습니다.",
                                              "result": null
                                            }
                                            """
                            )
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "영양성분 없음",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "isSuccess": false,
                                              "code": "INGREDIENT404_1",
                                              "message": "영양성분을 찾을 수 없습니다.",
                                              "result": null
                                            }
                                            """
                            )
                    )
            )
    })
    ApiResponse<IngredientResponse.IngredientDetail> getIngredient(
            @Parameter(
                    description = "조회할 영양성분 ID",
                    schema = @Schema(type = "integer", format = "int64", example = "2")
            )
            Long ingredientId,

            @Parameter(hidden = true)
            Long memberId
    );

    @Operation(
            summary = "금기 조건 목록 조회",
            description = "초기 설문에서 사용하는 복용약, 기저질환, 알러지 선택지를 조회합니다."
    )
    @SecurityRequirement(name = "JWT TOKEN")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "금기 조건 목록 조회 성공",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "isSuccess": true,
                                              "code": "SUCCESS200_1",
                                              "message": "요청이 성공적으로 처리되었습니다.",
                                              "result": {
                                                "groups": [
                                                  {
                                                    "type": "UNDERLYING_DISEASE",
                                                    "label": "기저질환",
                                                    "items": [
                                                      {
                                                        "contraindicationId": 23,
                                                        "conditionName": "고칼슘혈증"
                                                      }
                                                    ]
                                                  }
                                                ]
                                              }
                                            }
                                            """
                            )
                    )
            )
    })
    ApiResponse<IngredientResponse.ContraindicationList> getContraindications();
}
