package com.ipillgood.server.domain.condition.controller.docs;

import com.ipillgood.server.domain.condition.dto.ConditionRequest;
import com.ipillgood.server.domain.condition.dto.ConditionResponse;
import com.ipillgood.server.global.apiPayload.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Condition API", description = "주간 컨디션 체크 조회 및 저장 관련 API")
public interface ConditionControllerDocs {

    @Operation(
            summary = "이번 주 컨디션 체크 상태 조회",
            description = "이번 주 컨디션 체크 완료 여부와 팝업 노출 가능 상태를 조회합니다."
    )
    @SecurityRequirement(name = "JWT TOKEN")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "이번 주 컨디션 체크 상태 조회 성공",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "성공 응답",
                                    value = """
                                            {
                                              "isSuccess": true,
                                              "code": "SUCCESS200_1",
                                              "message": "이번 주 컨디션 체크 상태 조회에 성공했습니다.",
                                              "result": {
                                                "today": "2026-07-26",
                                                "weekStartOn": "2026-07-20",
                                                "weekEndOn": "2026-07-26",
                                                "isSunday": true,
                                                "checkAvailable": true,
                                                "checked": false,
                                                "recordId": null,
                                                "autoPopupAvailable": true,
                                                "autoShownAt": null,
                                                "dismissedAt": null,
                                                "sundayIntakeWarningRequired": true
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
                    description = "초기 설문을 완료해야 이용할 수 있습니다.",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "온보딩 미완료",
                                    value = """
                                            {
                                              "isSuccess": false,
                                              "code": "CONDITION403_2",
                                              "message": "초기 설문을 완료해야 이용할 수 있습니다.",
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
    ApiResponse<ConditionResponse.CurrentWeek> getCurrentWeek(Long memberId);

    @Operation(
            summary = "주간 컨디션 체크 저장",
            description = "일요일 주간 컨디션 체크 입력값과 계산 점수를 저장합니다."
    )
    @SecurityRequirement(name = "JWT TOKEN")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "201",
                    description = "주간 컨디션 체크 저장 성공",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "성공 응답",
                                    value = """
                                            {
                                              "isSuccess": true,
                                              "code": "SUCCESS201_1",
                                              "message": "주간 컨디션 체크 저장에 성공했습니다.",
                                              "result": {
                                                "recordId": 1,
                                                "weekStartOn": "2026-07-20",
                                                "weekEndOn": "2026-07-26",
                                                "checkedOn": "2026-07-26",
                                                "vitalityScore": 4,
                                                "sleepHours": 7,
                                                "sleepMinutes": 30,
                                                "sleepScore": 5,
                                                "intakeDaysCount": 6,
                                                "intakeScore": 5,
                                                "conditionScore": 4.67
                                              }
                                            }
                                            """
                            )
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "요청값 검증에 실패했거나 일요일이 아닙니다.",
                    content = @Content(
                            mediaType = "application/json",
                            examples = {
                                    @ExampleObject(
                                            name = "검증 실패",
                                            value = """
                                                    {
                                                      "isSuccess": false,
                                                      "code": "COMMON400_2",
                                                      "message": "요청값 검증에 실패했습니다.",
                                                      "result": null
                                                    }
                                                    """
                                    ),
                                    @ExampleObject(
                                            name = "일요일이 아닌 요청",
                                            value = """
                                                    {
                                                      "isSuccess": false,
                                                      "code": "CONDITION400_1",
                                                      "message": "일요일에만 컨디션 체크를 저장할 수 있습니다.",
                                                      "result": null
                                                    }
                                                    """
                                    )
                            }
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
                    description = "초기 설문을 완료해야 이용할 수 있습니다.",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "온보딩 미완료",
                                    value = """
                                            {
                                              "isSuccess": false,
                                              "code": "CONDITION403_2",
                                              "message": "초기 설문을 완료해야 이용할 수 있습니다.",
                                              "result": null
                                            }
                                            """
                            )
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "409",
                    description = "이미 이번 주 컨디션 체크를 완료했습니다.",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "이미 완료됨",
                                    value = """
                                            {
                                              "isSuccess": false,
                                              "code": "CONDITION409_1",
                                              "message": "이미 이번 주 컨디션 체크를 완료했습니다.",
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
    ApiResponse<ConditionResponse.Detail> saveWeeklyRecord(
            Long memberId,
            ConditionRequest.SaveWeeklyRecord request
    );

    @Operation(
            summary = "월별 컨디션 그래프 조회",
            description = "월별 주차 컨디션 기록과 월 요약을 조회합니다."
    )
    @SecurityRequirement(name = "JWT TOKEN")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "월별 컨디션 그래프 조회 성공",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "성공 응답",
                                    value = """
                                            {
                                              "isSuccess": true,
                                              "code": "SUCCESS200_1",
                                              "message": "월별 컨디션 그래프 조회에 성공했습니다.",
                                              "result": {
                                                "year": 2026,
                                                "month": 7,
                                                "averageConditionScore": 4.2,
                                                "averageVitalityScore": 4.1,
                                                "averageSleepHours": 7.3,
                                                "averageIntakeDaysCount": 6.5,
                                                "records": [
                                                  {
                                                    "recordId": 1,
                                                    "weekStartOn": "2026-07-20",
                                                    "weekEndOn": "2026-07-26",
                                                    "conditionScore": 4.67
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
                    description = "초기 설문을 완료해야 이용할 수 있습니다.",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "온보딩 미완료",
                                    value = """
                                            {
                                              "isSuccess": false,
                                              "code": "CONDITION403_2",
                                              "message": "초기 설문을 완료해야 이용할 수 있습니다.",
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
    ApiResponse<ConditionResponse.MonthlySummary> getMonthlyRecords(
            Long memberId,
            @Parameter(description = "조회 연도", required = false) String year,
            @Parameter(description = "조회 월", required = false) String month
    );

    @Operation(
            summary = "주차 상세 조회",
            description = "특정 주차 컨디션 기록 상세를 조회합니다."
    )
    @SecurityRequirement(name = "JWT TOKEN")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "주차 상세 조회 성공",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "성공 응답",
                                    value = """
                                            {
                                              "isSuccess": true,
                                              "code": "SUCCESS200_1",
                                              "message": "주차 상세 조회에 성공했습니다.",
                                              "result": {
                                                "recordId": 1,
                                                "weekStartOn": "2026-07-20",
                                                "weekEndOn": "2026-07-26",
                                                "checkedOn": "2026-07-26",
                                                "vitalityScore": 4,
                                                "sleepHours": 7,
                                                "sleepMinutes": 30,
                                                "sleepScore": 5,
                                                "intakeDaysCount": 6,
                                                "intakeScore": 5,
                                                "conditionScore": 4.67
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
                    description = "본인의 컨디션 기록만 조회할 수 있거나, 초기 설문을 완료해야 합니다.",
                    content = @Content(
                            mediaType = "application/json",
                            examples = {
                                    @ExampleObject(
                                            name = "타인의 기록 요청",
                                            value = """
                                                    {
                                                      "isSuccess": false,
                                                      "code": "CONDITION403_1",
                                                      "message": "본인의 컨디션 기록만 조회할 수 있습니다.",
                                                      "result": null
                                                    }
                                                    """
                                    ),
                                    @ExampleObject(
                                            name = "온보딩 미완료",
                                            value = """
                                                    {
                                                      "isSuccess": false,
                                                      "code": "CONDITION403_2",
                                                      "message": "초기 설문을 완료해야 이용할 수 있습니다.",
                                                      "result": null
                                                    }
                                                    """
                                    )
                            }
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "요청한 리소스를 찾을 수 없습니다.",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "존재하지 않는 기록",
                                    value = """
                                            {
                                              "isSuccess": false,
                                              "code": "COMMON404_1",
                                              "message": "요청한 리소스를 찾을 수 없습니다.",
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
    ApiResponse<ConditionResponse.Detail> getWeeklyRecordDetail(
            Long memberId,
            @Parameter(description = "주간 컨디션 기록 ID", required = true) Long recordId
    );
}
