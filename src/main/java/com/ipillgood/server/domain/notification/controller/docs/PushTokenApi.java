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

@Tag(name = "Notification API", description = "알림 설정 및 푸시 토큰 관련 API")
public interface PushTokenApi {

    @Operation(
            summary = "푸시 토큰 등록",
            description = "웹앱에서 발급받은 FCM 푸시 토큰을 등록하거나 갱신합니다."
    )
    @SecurityRequirement(name = "JWT TOKEN")
    @RequestBody(
            description = "푸시 토큰 플랫폼과 FCM 등록 토큰을 전달합니다.",
            required = true,
            content = @Content(
                    mediaType = "application/json",
                    examples = @ExampleObject(
                            value = """
                                    {
                                      "platform": "WEB",
                                      "token": "fcm_registration_token_sample"
                                    }
                                    """
                    )
            )
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "푸시 토큰 등록 성공",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "isSuccess": true,
                                              "code": "SUCCESS200_1",
                                              "message": "요청이 성공적으로 처리되었습니다.",
                                              "result": {
                                                "pushTokenId": 21,
                                                "platform": "WEB",
                                                "active": true,
                                                "lastSeenAt": "2026-07-22T10:30:00"
                                              }
                                            }
                                            """
                            )
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "푸시 토큰 등록 요청 오류",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "isSuccess": false,
                                              "code": "NOTIFICATION400_3",
                                              "message": "푸시 토큰 등록 요청이 올바르지 않습니다.",
                                              "result": null
                                            }
                                            """
                            )
                    )
            )
    })
    ApiResponse<NotificationResponse.PushTokenRegistration> registerPushToken(
            @Parameter(hidden = true)
            Long memberId,
            NotificationRequest.RegisterPushToken request
    );

    @Operation(
            summary = "푸시 토큰 비활성화",
            description = "로그아웃 등에서 현재 사용자의 특정 푸시 토큰을 발송 대상에서 제외합니다."
    )
    @SecurityRequirement(name = "JWT TOKEN")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "푸시 토큰 비활성화 성공",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "isSuccess": true,
                                              "code": "SUCCESS200_1",
                                              "message": "요청이 성공적으로 처리되었습니다.",
                                              "result": {
                                                "pushTokenId": 21,
                                                "active": false
                                              }
                                            }
                                            """
                            )
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "푸시 토큰 ID 오류",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "isSuccess": false,
                                              "code": "NOTIFICATION400_4",
                                              "message": "푸시 토큰 ID가 올바르지 않습니다.",
                                              "result": null
                                            }
                                            """
                            )
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "푸시 토큰 없음",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "isSuccess": false,
                                              "code": "NOTIFICATION404_1",
                                              "message": "푸시 토큰을 찾을 수 없습니다.",
                                              "result": null
                                            }
                                            """
                            )
                    )
            )
    })
    ApiResponse<NotificationResponse.PushTokenDeactivation> deactivatePushToken(
            @Parameter(hidden = true)
            Long memberId,

            @Parameter(description = "비활성화할 회원 푸시 토큰 ID")
            Long pushTokenId
    );
}
