package com.ipillgood.server.domain.healthconcern.controller.docs;

import com.ipillgood.server.domain.healthconcern.dto.HealthConcernResponse;
import com.ipillgood.server.global.apiPayload.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
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
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "인증이 필요합니다.",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "isSuccess": false,
                                              "code": "COMMON401_1",
                                              "message": "인증이 필요합니다.",
                                              "result": null
                                            }
                                            """
                            )
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "500",
                    description = "서버 내부 오류",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "isSuccess": false,
                                              "code": "COMMON500_1",
                                              "message": "예기치 못한 서버 오류가 발생했습니다.",
                                              "result": null
                                            }
                                            """
                            )
                    )
            )
    })
    ApiResponse<HealthConcernResponse.CategoryList> getCategories();
}
