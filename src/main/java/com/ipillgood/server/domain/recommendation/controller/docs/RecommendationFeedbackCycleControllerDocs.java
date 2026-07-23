package com.ipillgood.server.domain.recommendation.controller.docs;

import com.ipillgood.server.domain.recommendation.dto.RecommendationFeedbackCycleRequest;
import com.ipillgood.server.domain.recommendation.dto.RecommendationFeedbackCycleResponse;
import com.ipillgood.server.global.apiPayload.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Recommendation Feedback Cycle API", description = "추천 피드백 사이클 조회 및 응답 관련 API")
public interface RecommendationFeedbackCycleControllerDocs {

    @Operation(
            summary = "추천 피드백 대상 조회",
            description = "30일 주기 추천 도움 여부 팝업 노출 대상인지 조회합니다."
    )
    @SecurityRequirement(name = "JWT TOKEN")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "추천 피드백 대상 조회 성공",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "성공 응답",
                                    value = """
                                            {
                                              "isSuccess": true,
                                              "code": "SUCCESS200_1",
                                              "message": "추천 피드백 대상 조회에 성공했습니다.",
                                              "result": {
                                                "due": true,
                                                "cycleId": 1,
                                                "recommendationId": 1,
                                                "cycleDueOn": "2026-08-19"
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
                                              "message": "예기치 못한 서버 오류가 발생했습니다.",
                                              "result": null
                                            }
                                            """
                            )
                    )
            )
    })
    ApiResponse<RecommendationFeedbackCycleResponse.Due> getDue(Long memberId);

    @Operation(
            summary = "추천 피드백 응답 저장",
            description = "추천 도움 여부 팝업 응답을 저장합니다."
    )
    @SecurityRequirement(name = "JWT TOKEN")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "201",
                    description = "추천 피드백 응답 저장 성공",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "성공 응답",
                                    value = """
                                            {
                                              "isSuccess": true,
                                              "code": "SUCCESS201_1",
                                              "message": "추천 피드백 응답 저장에 성공했습니다.",
                                              "result": {
                                                "cycleId": 1,
                                                "responseType": "HELPFUL",
                                                "respondedAt": "2026-08-19T09:00:00",
                                                "nextCycleDueOn": "2026-09-18"
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
                                    name = "검증 실패",
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
                    description = "본인의 추천 피드백 사이클만 응답할 수 있습니다.",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "타인의 피드백 사이클 요청",
                                    value = """
                                            {
                                              "isSuccess": false,
                                              "code": "RECOMMENDATION403_2",
                                              "message": "본인의 추천 피드백 사이클만 응답할 수 있습니다.",
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
                                    name = "존재하지 않는 피드백 사이클",
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
                    description = "이미 응답했거나 아직 응답할 수 없는 피드백 사이클입니다.",
                    content = @Content(
                            mediaType = "application/json",
                            examples = {
                                    @ExampleObject(
                                            name = "이미 응답한 사이클",
                                            value = """
                                                    {
                                                      "isSuccess": false,
                                                      "code": "RECOMMENDATION409_2",
                                                      "message": "이미 응답한 피드백 사이클입니다.",
                                                      "result": null
                                                    }
                                                    """
                                    ),
                                    @ExampleObject(
                                            name = "아직 응답 시점이 아닌 사이클",
                                            value = """
                                                    {
                                                      "isSuccess": false,
                                                      "code": "RECOMMENDATION409_3",
                                                      "message": "아직 응답할 수 없는 피드백 사이클입니다.",
                                                      "result": null
                                                    }
                                                    """
                                    )
                            }
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
                                              "message": "예기치 못한 서버 오류가 발생했습니다.",
                                              "result": null
                                            }
                                            """
                            )
                    )
            )
    })
    ApiResponse<RecommendationFeedbackCycleResponse.Respond> respond(
            Long memberId,
            @Parameter(description = "추천 피드백 사이클 ID", required = true) Long cycleId,
            RecommendationFeedbackCycleRequest.Respond request
    );
}
