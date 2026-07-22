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
                                                "pushEnabled": true
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
}
