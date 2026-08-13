package com.ipillgood.server.domain.healthconcern.controller.docs;

import com.ipillgood.server.domain.healthconcern.dto.HealthConcernResponse;
import com.ipillgood.server.global.apiPayload.ApiResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Health Concern API", description = "건강 상태 카테고리 및 추천 성분 관련 API")
public interface HealthConcernApi {

    @Operation(
            summary = "건강 상태 카테고리 목록 조회",
            description = "건강 상태 선택 화면에 필요한 대분류·소분류 전체 카테고리 목록을 조회합니다."
    )
    @SecurityRequirement(name = "JWT TOKEN")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "건강 상태 카테고리 목록 조회 성공",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "isSuccess": true,
                                              "code": "SUCCESS200_1",
                                              "message": "요청이 성공적으로 처리되었습니다.",
                                              "result": {
                                                "majorCategories": [
                                                  {
                                                    "type": "NERVOUS_SYSTEM",
                                                    "label": "신경계",
                                                    "minorCategories": [
                                                      { "type": "COGNITIVE_MEMORY", "label": "인지 기능/기억력" },
                                                      { "type": "TENSION", "label": "긴장" },
                                                      { "type": "SLEEP_QUALITY", "label": "수면의 질" },
                                                      { "type": "FATIGUE", "label": "피로" }
                                                    ]
                                                  },
                                                  {
                                                    "type": "SENSORY_SYSTEM",
                                                    "label": "감각계",
                                                    "minorCategories": [
                                                      { "type": "TEETH", "label": "치아" },
                                                      { "type": "EYES", "label": "눈" },
                                                      { "type": "SKIN", "label": "피부" }
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
    ApiResponse<HealthConcernResponse.CategoryList> getCategories();

    @Operation(
            summary = "건강 상태 추천 성분 조회",
            description = "선택한 건강 상태(대분류/소분류)에 대한 감퇴 원인과 추천 성분 목록을 조회합니다."
    )
    @SecurityRequirement(name = "JWT TOKEN")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "건강 상태 추천 성분 조회 성공",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "isSuccess": true,
                                              "code": "SUCCESS200_1",
                                              "message": "건강 상태 추천 성분 조회에 성공했습니다.",
                                              "result": {
                                                "healthConcernId": 1,
                                                "majorCategory": "NERVOUS_SYSTEM",
                                                "minorCategory": "SLEEP_QUALITY",
                                                "declineCause": "수면 리듬 변화가 원인일 수 있습니다.",
                                                "recommendedIngredients": [
                                                  {
                                                    "ingredientId": 1,
                                                    "name": "마그네슘",
                                                    "description": "긴장 완화와 수면 관리에 도움을 줄 수 있습니다.",
                                                    "imageUrl": "https://ipillgood-bucket.s3.ap-northeast-2.amazonaws.com/ingredients/magnesium.png",
                                                    "effectKeywords": ["긴장 완화"],
                                                    "hasCabinetProduct": true
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
                    description = "요청값 검증에 실패했습니다.",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
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
            )
    })
    ApiResponse<HealthConcernResponse.RecommendedIngredients> getRecommendedIngredients(
            @Parameter(description = "건강 상태 대분류", required = true, example = "NERVOUS_SYSTEM")
            String majorCategory,

            @Parameter(description = "건강 상태 소분류", required = true, example = "SLEEP_QUALITY")
            String minorCategory,

            @Parameter(hidden = true)
            Long memberId
    );
}
