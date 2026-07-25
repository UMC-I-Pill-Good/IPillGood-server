package com.ipillgood.server.domain.notification.controller;

import com.ipillgood.server.domain.notification.controller.docs.NotificationApi;
import com.ipillgood.server.domain.notification.dto.NotificationRequest;
import com.ipillgood.server.domain.notification.dto.NotificationResponse;
import com.ipillgood.server.domain.notification.service.NotificationService;
import com.ipillgood.server.global.apiPayload.ApiResponse;
import com.ipillgood.server.global.apiPayload.code.GeneralSuccessCode;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestBody;
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

    @Override
    @GetMapping("/intake")
    public ApiResponse<NotificationResponse.IntakeNotificationSettings> getIntakeNotificationSettings(
            @AuthenticationPrincipal Long memberId
    ) {
        NotificationResponse.IntakeNotificationSettings response =
                notificationService.getIntakeNotificationSettings(memberId);
        return ApiResponse.onSuccess(GeneralSuccessCode.OK, response);
    }

    @Override
    @PatchMapping("/me")
    public ApiResponse<NotificationResponse.AppPushSetting> updateAppPushSetting(
            @AuthenticationPrincipal Long memberId,
            @RequestBody(required = false) NotificationRequest.UpdateAppPushSetting request
    ) {
        NotificationResponse.AppPushSetting response = notificationService.updateAppPushSetting(memberId, request);
        return ApiResponse.onSuccess(GeneralSuccessCode.OK, response);
    }

    @Override
    @PatchMapping("/intake")
    public ApiResponse<NotificationResponse.IntakePushSetting> updateIntakePushSetting(
            @AuthenticationPrincipal Long memberId,
            @RequestBody(required = false) NotificationRequest.UpdateIntakePushSetting request
    ) {
        NotificationResponse.IntakePushSetting response = notificationService.updateIntakePushSetting(
                memberId,
                request
        );
        return ApiResponse.onSuccess(GeneralSuccessCode.OK, response);
    }

    @Override
    @PatchMapping("/intake/active-products/{activeProductId}")
    public ApiResponse<NotificationResponse.ActiveProductNotificationSetting> updateActiveProductNotificationSetting(
            @AuthenticationPrincipal Long memberId,
            @PathVariable Long activeProductId,
            @RequestBody(required = false) NotificationRequest.UpdateActiveProductNotificationSetting request
    ) {
        NotificationResponse.ActiveProductNotificationSetting response =
                notificationService.updateActiveProductNotificationSetting(memberId, activeProductId, request);
        return ApiResponse.onSuccess(GeneralSuccessCode.OK, response);
    }
}
