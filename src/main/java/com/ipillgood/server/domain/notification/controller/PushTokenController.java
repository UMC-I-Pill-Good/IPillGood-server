package com.ipillgood.server.domain.notification.controller;

import com.ipillgood.server.domain.notification.controller.docs.PushTokenApi;
import com.ipillgood.server.domain.notification.dto.NotificationRequest;
import com.ipillgood.server.domain.notification.dto.NotificationResponse;
import com.ipillgood.server.domain.notification.service.NotificationService;
import com.ipillgood.server.global.apiPayload.ApiResponse;
import com.ipillgood.server.global.apiPayload.code.GeneralSuccessCode;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/push-tokens")
public class PushTokenController implements PushTokenApi {

    private final NotificationService notificationService;

    @Override
    @PostMapping
    public ApiResponse<NotificationResponse.PushTokenRegistration> registerPushToken(
            @AuthenticationPrincipal Long memberId,
            @RequestBody(required = false) NotificationRequest.RegisterPushToken request
    ) {
        NotificationResponse.PushTokenRegistration response = notificationService.registerPushToken(memberId, request);
        return ApiResponse.onSuccess(GeneralSuccessCode.OK, response);
    }

    @Override
    @DeleteMapping("/{pushTokenId}")
    public ApiResponse<NotificationResponse.PushTokenDeactivation> deactivatePushToken(
            @AuthenticationPrincipal Long memberId,
            @PathVariable Long pushTokenId
    ) {
        NotificationResponse.PushTokenDeactivation response =
                notificationService.deactivatePushToken(memberId, pushTokenId);
        return ApiResponse.onSuccess(GeneralSuccessCode.OK, response);
    }
}
