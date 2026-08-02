package com.ipillgood.server.domain.recommendation.controller.docs;

import com.ipillgood.server.domain.recommendation.dto.RecommendationResponse;
import com.ipillgood.server.global.apiPayload.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Recommendation API", description = "추천 결과 조회 및 재시도 관련 API")
public interface RecommendationControllerDocs {

    @Operation(
            summary = "현재 추천 결과 조회",
            description = "홈에 노출할 현재 활성 추천 결과와 건강 요약을 조회합니다."
    )
    @SecurityRequirement(name = "JWT TOKEN")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "현재 추천 결과 조회 성공",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "성공 응답",
                                    value = """
                                            {
                                              "isSuccess": true,
                                              "code": "SUCCESS200_1",
                                              "message": "현재 추천 결과 조회에 성공했습니다.",
                                              "result": {
                                                "recommendationId": 1,
                                                "status": "SUCCESS",
                                                "healthSummary": "피로와 수면 관리가 필요해 보여요.",
                                                "failureReason": null,
                                                "startedAt": "2026-07-20T13:00:00",
                                                "completedAt": "2026-07-20T13:01:00",
                                                "items": [
                                                  {
                                                    "recommendationItemId": 1,
                                                    "rankNo": 1,
                                                    "ingredientId": 1,
                                                    "ingredientName": "마그네슘",
                                                    "imageUrl": "https://ipillgood-bucket.s3.ap-northeast-2.amazonaws.com/ingredients/magnesium.png",
                                                    "effectKeywords": ["긴장 완화"],
                                                    "recommendedIntake": "1일 300mg",
                                                    "recommendedIntakeTime": "저녁 식후",
                                                    "aiReason": "수면 질 관리에 도움"
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
                                    name = "인증 필요",
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
                    responseCode = "404",
                    description = "현재 활성 추천 결과가 없습니다.",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "활성 추천 없음",
                                    value = """
                                            {
                                              "isSuccess": false,
                                              "code": "RECOMMENDATION404_1",
                                              "message": "현재 활성 추천 결과가 없습니다.",
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
                                    name = "서버 오류",
                                    value = """
                                            {
                                              "isSuccess": false,
                                              "code": "COMMON500_1",
                                              "message": "예기치 않은 서버 에러가 발생했습니다.",
                                              "result": null
                                            }
                                            """
                            )
                    )
            )
    })
    ApiResponse<RecommendationResponse.Detail> getCurrentRecommendation(Long memberId);

    @Operation(
            summary = "추천 생성 상태/결과 조회",
            description = "추천 생성 진행 상태(PENDING) 또는 완료된 결과(SUCCESS/FAILED/NO_RESULT)를 조회합니다. "
                    + "설문 저장 응답의 recommendationId로 폴링합니다."
    )
    @SecurityRequirement(name = "JWT TOKEN")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "추천 생성 상태/결과 조회 성공",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "성공 응답",
                                    value = """
                                            {
                                              "isSuccess": true,
                                              "code": "SUCCESS200_1",
                                              "message": "추천 생성 상태/결과 조회에 성공했습니다.",
                                              "result": {
                                                "recommendationId": 1,
                                                "status": "SUCCESS",
                                                "healthSummary": "피로와 수면 관리가 필요해 보여요.",
                                                "failureReason": null,
                                                "startedAt": "2026-07-20T13:00:00",
                                                "completedAt": "2026-07-20T13:01:00",
                                                "items": [
                                                  {
                                                    "recommendationItemId": 1,
                                                    "rankNo": 1,
                                                    "ingredientId": 1,
                                                    "ingredientName": "마그네슘",
                                                    "imageUrl": "https://ipillgood-bucket.s3.ap-northeast-2.amazonaws.com/ingredients/magnesium.png",
                                                    "effectKeywords": ["긴장 완화"],
                                                    "recommendedIntake": "1일 300mg",
                                                    "recommendedIntakeTime": "저녁 식후",
                                                    "aiReason": "수면 질 관리에 도움"
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
                                    name = "인증 필요",
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
                    responseCode = "403",
                    description = "본인의 추천 결과만 조회할 수 있습니다.",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "타인의 추천 결과 요청",
                                    value = """
                                            {
                                              "isSuccess": false,
                                              "code": "RECOMMENDATION403_1",
                                              "message": "본인의 추천 결과만 접근할 수 있습니다.",
                                              "result": null
                                            }
                                            """
                            )
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "요청한 리소스를 찾을 수 없습니다.",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "존재하지 않는 추천",
                                    value = """
                                            {
                                              "isSuccess": false,
                                              "code": "COMMON404_1",
                                              "message": "요청한 리소스를 찾을 수 없습니다",
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
                                    name = "서버 오류",
                                    value = """
                                            {
                                              "isSuccess": false,
                                              "code": "COMMON500_1",
                                              "message": "예기치 않은 서버 에러가 발생했습니다.",
                                              "result": null
                                            }
                                            """
                            )
                    )
            )
    })
    ApiResponse<RecommendationResponse.Detail> getRecommendation(
            Long memberId,
            @Parameter(description = "추천 ID", required = true) Long recommendationId
    );

    @Operation(
            summary = "추천 생성 재시도",
            description = "추천 생성이 실패(FAILED) 또는 결과 없음(NO_RESULT) 상태일 때 재시도합니다."
    )
    @SecurityRequirement(name = "JWT TOKEN")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "202",
                    description = "추천 생성 재시도 성공",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "성공 응답",
                                    value = """
                                            {
                                              "isSuccess": true,
                                              "code": "SUCCESS202_1",
                                              "message": "추천 생성 재시도에 성공했습니다.",
                                              "result": {
                                                "recommendationId": 1,
                                                "status": "PENDING",
                                                "startedAt": "2026-07-20T13:10:00"
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
                                    name = "인증 필요",
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
                    responseCode = "403",
                    description = "본인의 추천 결과만 재시도할 수 있습니다.",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "타인의 추천 결과 요청",
                                    value = """
                                            {
                                              "isSuccess": false,
                                              "code": "RECOMMENDATION403_1",
                                              "message": "본인의 추천 결과만 접근할 수 있습니다.",
                                              "result": null
                                            }
                                            """
                            )
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "요청한 리소스를 찾을 수 없습니다.",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "존재하지 않는 추천",
                                    value = """
                                            {
                                              "isSuccess": false,
                                              "code": "COMMON404_1",
                                              "message": "요청한 리소스를 찾을 수 없습니다",
                                              "result": null
                                            }
                                            """
                            )
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "409",
                    description = "재시도할 수 없는 추천 상태입니다.",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "재시도 불가 상태",
                                    value = """
                                            {
                                              "isSuccess": false,
                                              "code": "RECOMMENDATION409_1",
                                              "message": "재시도할 수 없는 추천 상태입니다.",
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
                                    name = "서버 오류",
                                    value = """
                                            {
                                              "isSuccess": false,
                                              "code": "COMMON500_1",
                                              "message": "예기치 않은 서버 에러가 발생했습니다.",
                                              "result": null
                                            }
                                            """
                            )
                    )
            )
    })
    ApiResponse<RecommendationResponse.Retry> retryRecommendation(
            Long memberId,
            @Parameter(description = "추천 ID", required = true) Long recommendationId
    );
}
