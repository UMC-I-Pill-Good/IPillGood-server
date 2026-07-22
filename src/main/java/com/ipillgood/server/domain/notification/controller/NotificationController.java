package com.ipillgood.server.domain.notification.controller;

import com.ipillgood.server.domain.notification.controller.docs.NotificationApi;
import com.ipillgood.server.domain.notification.dto.NotificationResponse;
import com.ipillgood.server.domain.notification.service.NotificationService;
import com.ipillgood.server.global.apiPayload.ApiResponse;
import com.ipillgood.server.global.apiPayload.code.GeneralSuccessCode;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/notification-settings")
public class NotificationController implements NotificationApi {

    private final NotificationService notificationService;

    @Override
    @GetMapping("/me")
    public ApiResponse<NotificationResponse.AppPushSetting> getAppPushSetting(
            @AuthenticationPrincipal Long memberId
    ) {
        NotificationResponse.AppPushSetting response = notificationService.getAppPushSetting(memberId);
        return ApiResponse.onSuccess(GeneralSuccessCode.OK, response);
    }
}
