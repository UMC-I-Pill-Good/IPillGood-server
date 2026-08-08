package com.ipillgood.server.domain.notification.controller.docs;

import com.ipillgood.server.domain.notification.dto.NotificationRequest;
import com.ipillgood.server.domain.notification.dto.NotificationResponse;
import com.ipillgood.server.global.apiPayload.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.parameters.RequestBody;

@Tag(name = "Notification API", description = "알림 설정 관련 API")
public interface NotificationApi {

    @Operation(
            summary = "앱 푸시 설정 조회",
            description = "마이페이지 설정 화면에서 앱 전체 푸시 알림 토글의 현재 상태를 조회합니다."
    )
    @SecurityRequirement(name = "JWT TOKEN")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "앱 푸시 설정 조회 성공",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "isSuccess": true,
                                              "code": "SUCCESS200_1",
                                              "message": "요청이 성공적으로 처리되었습니다.",
                                              "result": {
                                                "pushEnabled": false
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
                                              "code": "NOTIFICATION403_1",
                                              "message": "초기 설문을 완료해야 알림 설정을 이용할 수 있습니다.",
                                              "result": null
                                            }
                                            """
                            )
                    )
            )
    })
    ApiResponse<NotificationResponse.AppPushSetting> getAppPushSetting(
            @Parameter(hidden = true)
            Long memberId
    );

    @Operation(
            summary = "복용 알림 설정 통합 조회",
            description = "홈과 마이페이지 알림 설정 화면에서 복용 전체 알림과 영양제별 개별 알림 설정을 조회합니다."
    )
    @SecurityRequirement(name = "JWT TOKEN")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "복용 알림 설정 통합 조회 성공",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "isSuccess": true,
                                              "code": "SUCCESS200_1",
                                              "message": "요청이 성공적으로 처리되었습니다.",
                                              "result": {
                                                "pushEnabled": false,
                                                "intakePushEnabled": true,
                                                "activeProductCount": 2,
                                                "activeProducts": [
                                                  {
                                                    "activeProductId": 7,
                                                    "memberProductId": 15,
                                                    "productId": 112,
                                                    "productName": "뉴트리코어 유기농 비타민D 1000IU",
                                                    "notificationEnabled": true,
                                                    "intakeTime": "08:30"
                                                  },
                                                  {
                                                    "activeProductId": 8,
                                                    "memberProductId": 16,
                                                    "productId": 124,
                                                    "productName": "헬로바이오 맥스 비타민C 3000",
                                                    "notificationEnabled": false,
                                                    "intakeTime": "21:00"
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
                                              "code": "NOTIFICATION403_1",
                                              "message": "초기 설문을 완료해야 알림 설정을 이용할 수 있습니다.",
                                              "result": null
                                            }
                                            """
                            )
                    )
            )
    })
    ApiResponse<NotificationResponse.IntakeNotificationSettings> getIntakeNotificationSettings(
            @Parameter(hidden = true)
            Long memberId
    );

    @Operation(
            summary = "앱 푸시 설정 변경",
            description = "마이페이지 설정 화면에서 앱 전체 푸시 알림 토글 상태를 변경합니다."
    )
    @SecurityRequirement(name = "JWT TOKEN")
    @RequestBody(
            description = "변경할 앱 전체 푸시 알림 ON/OFF 여부를 전달합니다.",
            required = true,
            content = @Content(
                    mediaType = "application/json",
                    examples = @ExampleObject(
                            value = """
                                    {
                                      "pushEnabled": false
                                    }
                                    """
                    )
            )
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "앱 푸시 설정 변경 성공",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "isSuccess": true,
                                              "code": "SUCCESS200_1",
                                              "message": "요청이 성공적으로 처리되었습니다.",
                                              "result": {
                                                "pushEnabled": false
                                              }
                                            }
                                            """
                            )
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "앱 푸시 설정 요청 오류",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "isSuccess": false,
                                              "code": "NOTIFICATION400_1",
                                              "message": "앱 푸시 설정 변경 요청이 올바르지 않습니다.",
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
                                              "code": "NOTIFICATION403_1",
                                              "message": "초기 설문을 완료해야 알림 설정을 이용할 수 있습니다.",
                                              "result": null
                                            }
                                            """
                            )
                    )
            )
    })
    ApiResponse<NotificationResponse.AppPushSetting> updateAppPushSetting(
            @Parameter(hidden = true)
            Long memberId,
            NotificationRequest.UpdateAppPushSetting request
    );

    @Operation(
            summary = "복용 전체 알림 변경",
            description = "홈과 마이페이지 알림 설정 화면에서 복용 전체 알림 토글 상태를 변경합니다."
    )
    @SecurityRequirement(name = "JWT TOKEN")
    @RequestBody(
            description = "변경할 복용 전체 알림 ON/OFF 여부를 전달합니다.",
            required = true,
            content = @Content(
                    mediaType = "application/json",
                    examples = @ExampleObject(
                            value = """
                                    {
                                      "intakePushEnabled": false
                                    }
                                    """
                    )
            )
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "복용 전체 알림 변경 성공",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "isSuccess": true,
                                              "code": "SUCCESS200_1",
                                              "message": "요청이 성공적으로 처리되었습니다.",
                                              "result": {
                                                "pushEnabled": false,
                                                "intakePushEnabled": false
                                              }
                                            }
                                            """
                            )
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "복용 알림 설정 요청 오류",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "isSuccess": false,
                                              "code": "NOTIFICATION400_2",
                                              "message": "복용 알림 설정 변경 요청이 올바르지 않습니다.",
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
                                              "code": "NOTIFICATION403_1",
                                              "message": "초기 설문을 완료해야 알림 설정을 이용할 수 있습니다.",
                                              "result": null
                                            }
                                            """
                            )
                    )
            )
    })
    ApiResponse<NotificationResponse.IntakePushSetting> updateIntakePushSetting(
            @Parameter(hidden = true)
            Long memberId,
            NotificationRequest.UpdateIntakePushSetting request
    );

    @Operation(
            summary = "개별 복용 알림 변경",
            description = "홈과 마이페이지 알림 설정 화면에서 섭취 중 영양제의 개별 복용 알림 토글 상태를 변경합니다."
    )
    @SecurityRequirement(name = "JWT TOKEN")
    @RequestBody(
            description = "변경할 개별 복용 알림 ON/OFF 여부를 전달합니다.",
            required = true,
            content = @Content(
                    mediaType = "application/json",
                    examples = @ExampleObject(
                            value = """
                                    {
                                      "notificationEnabled": false
                                    }
                                    """
                    )
            )
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "개별 복용 알림 변경 성공",
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
                                                "notificationEnabled": false
                                              }
                                            }
                                            """
                            )
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "개별 알림 설정 요청 오류",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "isSuccess": false,
                                              "code": "NOTIFICATION400_6",
                                              "message": "개별 알림 설정 변경 요청이 올바르지 않습니다.",
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
                                              "code": "NOTIFICATION403_1",
                                              "message": "초기 설문을 완료해야 알림 설정을 이용할 수 있습니다.",
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
                                              "code": "NOTIFICATION404_2",
                                              "message": "개별 알림을 변경할 섭취 중 영양제를 찾을 수 없습니다.",
                                              "result": null
                                            }
                                            """
                            )
                    )
            )
    })
    ApiResponse<NotificationResponse.ActiveProductNotificationSetting> updateActiveProductNotificationSetting(
            @Parameter(hidden = true)
            Long memberId,
            @Parameter(description = "개별 알림을 변경할 활성 섭취 중 상품 ID")
            Long activeProductId,
            NotificationRequest.UpdateActiveProductNotificationSetting request
    );
}
