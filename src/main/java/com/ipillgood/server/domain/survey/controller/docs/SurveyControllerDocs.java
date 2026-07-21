package com.ipillgood.server.domain.survey.controller.docs;

import com.ipillgood.server.domain.survey.dto.SurveyRequest;
import com.ipillgood.server.domain.survey.dto.SurveyResult;
import com.ipillgood.server.global.apiPayload.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

/**
 * 설문 응답 관련 API 문서
 * 초기 설문/재설문 저장 및 추천 생성 시작 (화면1~5)
 */
@Tag(name = "Survey API", description = "초기 설문 및 재설문 응답 관련 API")
public interface SurveyControllerDocs {

    @Operation(
            summary = "설문 응답 저장 및 추천 생성 시작",
            description = "초기 설문 또는 재설문 응답을 저장하고, 같은 요청 안에서 추천 생성을 시작합니다. "
                    + "추천 생성은 비동기로 처리되며 응답의 recommendationStatus는 항상 PENDING입니다."
    )
    @SecurityRequirement(name = "JWT TOKEN")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "202",
                    description = "설문 응답 저장 및 추천 생성 시작 성공",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "성공 응답",
                                    value = """
                                            {
                                              "isSuccess": true,
                                              "code": "SUCCESS202_1",
                                              "message": "설문 응답 저장 및 추천 생성 시작에 성공했습니다.",
                                              "result": {
                                                "surveyResponseId": 1,
                                                "recommendationId": 1,
                                                "recommendationStatus": "PENDING",
                                                "completedAt": "2026-07-20T13:00:00"
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
                            examples = {
                                    @ExampleObject(
                                            name = "요청값 검증 실패",
                                            value = """
                                                    {
                                                      "isSuccess": false,
                                                      "code": "COMMON400_1",
                                                      "message": "잘못된 요청입니다.",
                                                      "result": null
                                                    }
                                                    """
                                    ),
                                    @ExampleObject(
                                            name = "없음 선택과 상세 선택 동시 요청",
                                            value = """
                                                    {
                                                      "isSuccess": false,
                                                      "code": "SURVEY400_1",
                                                      "message": "없음 선택과 상세 선택을 동시에 요청할 수 없습니다.",
                                                      "result": null
                                                    }
                                                    """
                                    ),
                                    @ExampleObject(
                                            name = "생리 정보 불완전",
                                            value = """
                                                    {
                                                      "isSuccess": false,
                                                      "code": "SURVEY400_2",
                                                      "message": "마지막 생리 시작일과 주기를 모두 입력해 주세요.",
                                                      "result": null
                                                    }
                                                    """
                                    ),
                                    @ExampleObject(
                                            name = "건강 고민 개수 오류",
                                            value = """
                                                    {
                                                      "isSuccess": false,
                                                      "code": "SURVEY400_3",
                                                      "message": "건강 고민은 1~3개까지 선택할 수 있습니다.",
                                                      "result": null
                                                    }
                                                    """
                                    ),
                                    @ExampleObject(
                                            name = "금기 조건 상세 선택 누락",
                                            value = """
                                                    {
                                                      "isSuccess": false,
                                                      "code": "SURVEY400_4",
                                                      "message": "기저질환, 복용약, 알러지는 없음 또는 상세 항목을 선택해야 합니다.",
                                                      "result": null
                                                    }
                                                    """
                                    ),
                                    @ExampleObject(
                                            name = "복용 성분 선택 누락",
                                            value = """
                                                    {
                                                      "isSuccess": false,
                                                      "code": "SURVEY400_5",
                                                      "message": "현재 복용 성분은 없음 또는 성분을 선택해야 합니다.",
                                                      "result": null
                                                    }
                                                    """
                                    ),
                                    @ExampleObject(
                                            name = "여성 임신 여부 누락",
                                            value = """
                                                    {
                                                      "isSuccess": false,
                                                      "code": "SURVEY400_6",
                                                      "message": "여성 사용자는 임신 여부를 선택해야 합니다.",
                                                      "result": null
                                                    }
                                                    """
                                    )
                            }
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "JWT 액세스 토큰 누락, 만료 또는 유효하지 않은 토큰",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "인증 필요",
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
    ApiResponse<SurveyResult.Submit> submit(Long memberId, @Valid SurveyRequest.Submit request);
}
