package com.ipillgood.server.domain.intake.controller.docs;

import com.ipillgood.server.domain.intake.dto.IntakeRequest;
import com.ipillgood.server.domain.intake.dto.IntakeResponse;
import com.ipillgood.server.global.apiPayload.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Intake API", description = "복용 루틴 관련 API")
public interface IntakeApi {

    @Operation(
            summary = "오늘 복용 상태 조회",
            description = "홈에서 사용할 오늘 복용 예정/완료 상태와 자동 팝업 필요 여부를 조회합니다."
    )
    @SecurityRequirement(name = "JWT TOKEN")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "오늘 복용 상태 조회 성공",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "isSuccess": true,
                                              "code": "SUCCESS200_1",
                                              "message": "요청이 성공적으로 처리되었습니다.",
                                              "result": {
                                                "currentDate": "2026-07-21",
                                                "scheduledCount": 2,
                                                "takenCount": 1,
                                                "allCompleted": false,
                                                "missedNoticeVisible": true,
                                                "autoPopupShown": false,
                                                "autoPopupRequired": true,
                                                "scheduledProducts": [
                                                  {
                                                    "activeProductId": 7,
                                                    "memberProductId": 15,
                                                    "productId": 112,
                                                    "productName": "뉴트리코어 유기농 비타민D 1000IU",
                                                    "taken": true,
                                                    "takenAt": "2026-07-21T08:45:00"
                                                  }
                                                ]
                                              }
                                            }
                                            """
                            )
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "초기 설문 미완료",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "isSuccess": false,
                                              "code": "INTAKE403_1",
                                              "message": "초기 설문을 완료해야 이용할 수 있습니다.",
                                              "result": null
                                            }
                                            """
                            )
                    )
            )
    })
    ApiResponse<IntakeResponse.TodayIntakeStatus> getTodayIntakeStatus(
            @Parameter(hidden = true)
            Long memberId
    );

    @Operation(
            summary = "복용 캘린더 조회",
            description = "월별 복용 완료 상태와 연속 섭취 포함 여부를 날짜별로 조회합니다."
    )
    @SecurityRequirement(name = "JWT TOKEN")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "복용 캘린더 조회 성공",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "isSuccess": true,
                                              "code": "SUCCESS200_1",
                                              "message": "요청이 성공적으로 처리되었습니다.",
                                              "result": {
                                                "year": 2026,
                                                "month": 7,
                                                "days": [
                                                  {
                                                    "date": "2026-07-21",
                                                    "dayOfMonth": 21,
                                                    "hasTakenRecords": true,
                                                    "allCompleted": false,
                                                    "streakStatus": "PENDING",
                                                    "streakIncluded": false,
                                                    "selectable": true,
                                                    "takenCount": 1,
                                                    "completedAt": null
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
                    description = "캘린더 조회 기간 오류",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "isSuccess": false,
                                              "code": "INTAKE400_5",
                                              "message": "복용 캘린더 조회 기간이 올바르지 않습니다.",
                                              "result": null
                                            }
                                            """
                            )
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "초기 설문 미완료",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "isSuccess": false,
                                              "code": "INTAKE403_1",
                                              "message": "초기 설문을 완료해야 이용할 수 있습니다.",
                                              "result": null
                                            }
                                            """
                            )
                    )
            )
    })
    ApiResponse<IntakeResponse.Calendar> getIntakeCalendar(
            @Parameter(hidden = true)
            Long memberId,
            @Parameter(
                    description = "조회할 연도입니다.",
                    schema = @Schema(type = "integer", example = "2026")
            )
            String year,
            @Parameter(
                    description = "조회할 월이며 1부터 12까지 입력합니다.",
                    schema = @Schema(type = "integer", example = "7")
            )
            String month
    );

    @Operation(
            summary = "날짜별 섭취 완료 목록 조회",
            description = "특정 날짜에 실제 섭취 완료한 영양제 목록을 조회합니다. 오늘 이후 날짜는 조회할 수 없습니다."
    )
    @SecurityRequirement(name = "JWT TOKEN")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "날짜별 섭취 완료 목록 조회 성공",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "isSuccess": true,
                                              "code": "SUCCESS200_1",
                                              "message": "요청이 성공적으로 처리되었습니다.",
                                              "result": {
                                                "date": "2026-07-21",
                                                "takenCount": 2,
                                                "products": [
                                                  {
                                                    "activeProductId": 7,
                                                    "productId": 112,
                                                    "productName": "뉴트리코어 유기농 비타민D 1000IU",
                                                    "takenAt": "2026-07-21T08:45:00"
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
                    description = "날짜 요청 오류",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "isSuccess": false,
                                              "code": "INTAKE400_6",
                                              "message": "날짜 요청이 올바르지 않습니다.",
                                              "result": null
                                            }
                                            """
                            )
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "초기 설문 미완료",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "isSuccess": false,
                                              "code": "INTAKE403_1",
                                              "message": "초기 설문을 완료해야 이용할 수 있습니다.",
                                              "result": null
                                            }
                                            """
                            )
                    )
            )
    })
    ApiResponse<IntakeResponse.DailyTakenProducts> getDailyTakenProducts(
            @Parameter(hidden = true)
            Long memberId,
            @Parameter(
                    description = "조회할 날짜입니다. yyyy-MM-dd 형식으로 입력합니다.",
                    schema = @Schema(type = "string", example = "2026-07-21")
            )
            String date
    );

    @Operation(
            summary = "연속 섭취일 조회",
            description = "연속 섭취일과 마스코트 성장 단계를 조회합니다."
    )
    @SecurityRequirement(name = "JWT TOKEN")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "연속 섭취일 조회 성공",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "isSuccess": true,
                                              "code": "SUCCESS200_1",
                                              "message": "요청이 성공적으로 처리되었습니다.",
                                              "result": {
                                                "currentDate": "2026-07-21",
                                                "currentDateStreakStatus": "COMPLETED",
                                                "streakDays": 15,
                                                "mascotStage": "FLOWER",
                                                "mascotStageLabel": "꽃",
                                                "activeProductCount": 2,
                                                "lastRoutineDate": "2026-07-21",
                                                "nextStageThresholdDays": 30
                                              }
                                            }
                                            """
                            )
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "초기 설문 미완료",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "isSuccess": false,
                                              "code": "INTAKE403_1",
                                              "message": "초기 설문을 완료해야 이용할 수 있습니다.",
                                              "result": null
                                            }
                                            """
                            )
                    )
            )
    })
    ApiResponse<IntakeResponse.IntakeStreak> getIntakeStreak(
            @Parameter(hidden = true)
            Long memberId
    );

    @Operation(
            summary = "오늘 복용 팝업 노출 기록",
            description = "홈 자동 팝업을 실제 노출한 뒤 오늘의 자동 팝업 노출 이력을 기록합니다."
    )
    @SecurityRequirement(name = "JWT TOKEN")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "오늘 복용 팝업 노출 기록 성공",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "isSuccess": true,
                                              "code": "SUCCESS200_1",
                                              "message": "요청이 성공적으로 처리되었습니다.",
                                              "result": {
                                                "currentDate": "2026-07-21",
                                                "autoPopupShown": true,
                                                "autoPopupShownAt": "2026-07-21T09:00:00"
                                              }
                                            }
                                            """
                            )
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "초기 설문 미완료",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "isSuccess": false,
                                              "code": "INTAKE403_1",
                                              "message": "초기 설문을 완료해야 이용할 수 있습니다.",
                                              "result": null
                                            }
                                            """
                            )
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "409",
                    description = "오늘 복용 팝업 노출 대상 아님",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "isSuccess": false,
                                              "code": "INTAKE409_2",
                                              "message": "오늘 복용 팝업 노출 대상이 아닙니다.",
                                              "result": null
                                            }
                                            """
                            )
                    )
            )
    })
    ApiResponse<IntakeResponse.TodayPopupShown> recordTodayPopupShown(
            @Parameter(hidden = true)
            Long memberId
    );

    @Operation(
            summary = "오늘 복용 체크 저장",
            description = "오늘 복용 예정 영양제의 체크 상태를 저장하고 저장 결과를 반환합니다."
    )
    @SecurityRequirement(name = "JWT TOKEN")
    @RequestBody(
            required = true,
            description = "오늘 섭취 완료로 저장할 활성 섭취 중 상품 ID 목록을 전달합니다.",
            content = @Content(
                    schema = @Schema(implementation = IntakeRequest.SaveTodayIntakeRecords.class),
                    examples = @ExampleObject(
                            value = """
                                    {
                                      "takenActiveProductIds": [
                                        7
                                      ]
                                    }
                                    """
                    )
            )
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "오늘 복용 체크 저장 성공",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "isSuccess": true,
                                              "code": "SUCCESS200_1",
                                              "message": "요청이 성공적으로 처리되었습니다.",
                                              "result": {
                                                "currentDate": "2026-07-21",
                                                "scheduledCount": 2,
                                                "takenCount": 1,
                                                "allCompleted": false,
                                                "completedAt": null,
                                                "missedNoticeVisible": true,
                                                "records": [
                                                  {
                                                    "activeProductId": 7,
                                                    "productId": 112,
                                                    "productName": "뉴트리코어 유기농 비타민D 1000IU",
                                                    "scheduled": true,
                                                    "taken": true,
                                                    "takenAt": "2026-07-21T08:45:00"
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
                    description = "오늘 복용 체크 요청 오류",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "isSuccess": false,
                                              "code": "INTAKE400_4",
                                              "message": "오늘 복용 체크 요청이 올바르지 않습니다.",
                                              "result": null
                                            }
                                            """
                            )
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "초기 설문 미완료",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "isSuccess": false,
                                              "code": "INTAKE403_1",
                                              "message": "초기 설문을 완료해야 이용할 수 있습니다.",
                                              "result": null
                                            }
                                            """
                            )
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "활성 섭취 중 상품 없음",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "isSuccess": false,
                                              "code": "INTAKE404_2",
                                              "message": "활성 섭취 중 상품을 찾을 수 없습니다.",
                                              "result": null
                                            }
                                            """
                            )
                    )
            )
    })
    ApiResponse<IntakeResponse.SaveTodayIntakeRecords> saveTodayIntakeRecords(
            @Parameter(hidden = true)
            Long memberId,
            IntakeRequest.SaveTodayIntakeRecords request
    );

    @Operation(
            summary = "섭취 중 영양제 목록 조회",
            description = "홈에서 표시할 현재 섭취 중 영양제 카드 목록을 활성 등록 순서로 조회합니다."
    )
    @SecurityRequirement(name = "JWT TOKEN")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "섭취 중 영양제 목록 조회 성공",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "isSuccess": true,
                                              "code": "SUCCESS200_1",
                                              "message": "요청이 성공적으로 처리되었습니다.",
                                              "result": {
                                                "totalCount": 2,
                                                "activeProducts": [
                                                  {
                                                    "activeProductId": 7,
                                                    "memberProductId": 15,
                                                    "productId": 112,
                                                    "productName": "뉴트리코어 유기농 비타민D 1000IU",
                                                    "thumbnailImageUrl": "https://ipillgood-bucket.s3.ap-northeast-2.amazonaws.com/ingredients/2.png"
                                                  }
                                                ]
                                              }
                                            }
                                            """
                            )
                    )
            )
    })
    ApiResponse<IntakeResponse.ActiveProducts> getActiveProducts(
            @Parameter(hidden = true)
            Long memberId
    );

    @Operation(
            summary = "섭취 중 영양제 등록",
            description = "캐비닛 보유 영양제를 섭취 중 영양제로 등록하고 복용 시간과 주기를 저장합니다."
    )
    @SecurityRequirement(name = "JWT TOKEN")
    @RequestBody(
            required = true,
            description = "섭취 중으로 등록할 회원 캐비닛 상품 ID와 복용 시간, 복용 주기를 전달합니다.",
            content = @Content(
                    schema = @Schema(implementation = IntakeRequest.RegisterActiveProduct.class),
                    examples = @ExampleObject(
                            value = """
                                    {
                                      "memberProductId": 16,
                                      "intakeTime": "08:30",
                                      "frequency": "EVERY_DAY"
                                    }
                                    """
                    )
            )
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "201",
                    description = "섭취 중 영양제 등록 성공",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "isSuccess": true,
                                              "code": "SUCCESS201_1",
                                              "message": "리소스가 성공적으로 생성되었습니다.",
                                              "result": {
                                                "activeProductId": 8,
                                                "memberProductId": 16,
                                                "productId": 124,
                                                "productName": "헬로바이오 맥스 비타민C 3000",
                                                "thumbnailImageUrl": "https://ipillgood-bucket.s3.ap-northeast-2.amazonaws.com/ingredients/other1.png",
                                                "notificationEnabled": true,
                                                "intakeTime": "08:30",
                                                "frequency": "EVERY_DAY",
                                                "frequencyLabel": "매일"
                                              }
                                            }
                                            """
                            )
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "등록/병용 확인 요청 오류",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "isSuccess": false,
                                              "code": "INTAKE400_2",
                                              "message": "등록/병용 확인 요청이 올바르지 않습니다.",
                                              "result": null
                                            }
                                            """
                            )
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "등록 대상 캐비닛 상품 없음",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "isSuccess": false,
                                              "code": "INTAKE404_1",
                                              "message": "섭취 중으로 등록할 캐비닛 상품을 찾을 수 없습니다.",
                                              "result": null
                                            }
                                            """
                            )
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "409",
                    description = "이미 섭취 중이거나 오늘 재등록할 수 없는 영양제",
                    content = @Content(
                            mediaType = "application/json",
                            examples = {
                                    @ExampleObject(
                                            name = "이미 섭취 중",
                                            value = """
                                                    {
                                                      "isSuccess": false,
                                                      "code": "INTAKE409_1",
                                                      "message": "이미 섭취 중인 영양제입니다.",
                                                      "result": null
                                                    }
                                                    """
                                    ),
                                    @ExampleObject(
                                            name = "당일 재등록 제한",
                                            value = """
                                                    {
                                                      "isSuccess": false,
                                                      "code": "INTAKE409_3",
                                                      "message": "오늘 삭제한 영양제는 내일부터 다시 추가할 수 있습니다.",
                                                      "result": null
                                                    }
                                                    """
                                    )
                            }
                    )
            )
    })
    ApiResponse<IntakeResponse.RegisterActiveProduct> registerActiveProduct(
            @Parameter(hidden = true)
            Long memberId,
            IntakeRequest.RegisterActiveProduct request
    );

    @Operation(
            summary = "섭취 중 영양제 설정 변경",
            description = "복용 시간, 복용 주기, 개별 알림 설정을 변경하고 현재 설정 정보를 반환합니다."
    )
    @SecurityRequirement(name = "JWT TOKEN")
    @RequestBody(
            required = true,
            description = "변경할 복용 시간, 복용 주기, 개별 알림 설정 중 하나 이상을 전달합니다.",
            content = @Content(
                    schema = @Schema(implementation = IntakeRequest.UpdateActiveProductSettings.class),
                    examples = @ExampleObject(
                            value = """
                                    {
                                      "intakeTime": "21:00",
                                      "frequency": "EVERY_2_DAYS",
                                      "notificationEnabled": false
                                    }
                                    """
                    )
            )
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "섭취 중 영양제 설정 변경 성공",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "isSuccess": true,
                                              "code": "SUCCESS200_1",
                                              "message": "요청이 성공적으로 처리되었습니다.",
                                              "result": {
                                                "activeProductId": 7,
                                                "memberProductId": 15,
                                                "productId": 112,
                                                "brand": "뉴트리코어",
                                                "productName": "뉴트리코어 유기농 비타민D 1000IU",
                                                "thumbnailImageUrl": "https://ipillgood-bucket.s3.ap-northeast-2.amazonaws.com/ingredients/2.png",
                                                "startedOn": "2026-07-01",
                                                "intakeDayCount": 21,
                                                "notificationEnabled": false,
                                                "intakeTime": "21:00",
                                                "frequency": "EVERY_2_DAYS",
                                                "frequencyLabel": "2일에 한 번",
                                                "frequencyIntervalDays": 2,
                                                "scheduleAnchorOn": "2026-07-21"
                                              }
                                            }
                                            """
                            )
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "설정 변경 요청 오류",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "isSuccess": false,
                                              "code": "INTAKE400_3",
                                              "message": "섭취 중 영양제 설정 변경 요청이 올바르지 않습니다.",
                                              "result": null
                                            }
                                            """
                            )
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "활성 섭취 중 상품 없음",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "isSuccess": false,
                                              "code": "INTAKE404_2",
                                              "message": "활성 섭취 중 상품을 찾을 수 없습니다.",
                                              "result": null
                                            }
                                            """
                            )
                    )
            )
    })
    ApiResponse<IntakeResponse.UpdateActiveProductSettings> updateActiveProductSettings(
            @Parameter(hidden = true)
            Long memberId,
            @Parameter(
                    description = "설정을 변경할 활성 섭취 중 상품 ID",
                    schema = @Schema(type = "integer", format = "int64", example = "7")
            )
            String activeProductId,
            IntakeRequest.UpdateActiveProductSettings request
    );

    @Operation(
            summary = "섭취 중 영양제 제거",
            description = "홈에서 제거한 섭취 중 영양제를 중단 처리하고 제거된 상품 정보를 반환합니다."
    )
    @SecurityRequirement(name = "JWT TOKEN")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "섭취 중 영양제 제거 성공",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "isSuccess": true,
                                              "code": "SUCCESS200_1",
                                              "message": "요청이 성공적으로 처리되었습니다.",
                                              "result": {
                                                "activeProductId": 7,
                                                "memberProductId": 15,
                                                "productId": 112,
                                                "productName": "뉴트리코어 유기농 비타민D 1000IU",
                                                "stoppedOn": "2026-07-21"
                                              }
                                            }
                                            """
                            )
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "활성 섭취 중 상품 없음",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "isSuccess": false,
                                              "code": "INTAKE404_2",
                                              "message": "활성 섭취 중 상품을 찾을 수 없습니다.",
                                              "result": null
                                            }
                                            """
                            )
                    )
            )
    })
    ApiResponse<IntakeResponse.RemoveActiveProduct> removeActiveProduct(
            @Parameter(hidden = true)
            Long memberId,
            @Parameter(
                    description = "제거할 활성 섭취 중 상품 ID",
                    schema = @Schema(type = "integer", format = "int64", example = "7")
            )
            String activeProductId
    );

    @Operation(
            summary = "섭취 중 등록 전 병용 금기 확인",
            description = "새로 등록하려는 영양제와 현재 섭취 중 영양제 간 주의 또는 금기 성분 조합을 확인합니다."
    )
    @SecurityRequirement(name = "JWT TOKEN")
    @RequestBody(
            required = true,
            description = "섭취 중으로 등록하려는 회원 캐비닛 상품 ID를 전달합니다.",
            content = @Content(
                    schema = @Schema(implementation = IntakeRequest.CompatibilityCheck.class),
                    examples = @ExampleObject(
                            value = """
                                    {
                                      "memberProductId": 16
                                    }
                                    """
                    )
            )
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "병용 금기 확인 성공",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "isSuccess": true,
                                              "code": "SUCCESS200_1",
                                              "message": "요청이 성공적으로 처리되었습니다.",
                                              "result": {
                                                "hasConflicts": true,
                                                "conflicts": [
                                                  {
                                                    "combinationType": "CAUTION",
                                                    "currentIngredientId": 10,
                                                    "currentIngredientName": "칼슘",
                                                    "targetIngredientId": 18,
                                                    "targetIngredientName": "철",
                                                    "reason": "체내 흡수 경로가 겹쳐 동시 복용 시 서로의 흡수를 방해할 수 있어요."
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
                    description = "등록/병용 확인 요청 오류",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "isSuccess": false,
                                              "code": "INTAKE400_2",
                                              "message": "등록/병용 확인 요청이 올바르지 않습니다.",
                                              "result": null
                                            }
                                            """
                            )
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "등록 대상 캐비닛 상품 없음",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "isSuccess": false,
                                              "code": "INTAKE404_1",
                                              "message": "섭취 중으로 등록할 캐비닛 상품을 찾을 수 없습니다.",
                                              "result": null
                                            }
                                            """
                            )
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "409",
                    description = "이미 섭취 중이거나 오늘 재등록할 수 없는 영양제",
                    content = @Content(
                            mediaType = "application/json",
                            examples = {
                                    @ExampleObject(
                                            name = "이미 섭취 중",
                                            value = """
                                                    {
                                                      "isSuccess": false,
                                                      "code": "INTAKE409_1",
                                                      "message": "이미 섭취 중인 영양제입니다.",
                                                      "result": null
                                                    }
                                                    """
                                    ),
                                    @ExampleObject(
                                            name = "당일 재등록 제한",
                                            value = """
                                                    {
                                                      "isSuccess": false,
                                                      "code": "INTAKE409_3",
                                                      "message": "오늘 삭제한 영양제는 내일부터 다시 추가할 수 있습니다.",
                                                      "result": null
                                                    }
                                                    """
                                    )
                            }
                    )
            )
    })
    ApiResponse<IntakeResponse.CompatibilityCheck> checkCompatibility(
            @Parameter(hidden = true)
            Long memberId,
            IntakeRequest.CompatibilityCheck request
    );
}
