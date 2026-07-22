package com.ipillgood.server.domain.notification.code;

import com.ipillgood.server.global.apiPayload.code.BaseErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum NotificationErrorCode implements BaseErrorCode {

    // 403 FORBIDDEN - 알림 설정 접근 조건
    ONBOARDING_NOT_COMPLETED(HttpStatus.FORBIDDEN, "NOTIFICATION403_1", "초기 설문을 완료해야 알림 설정을 이용할 수 있습니다."),
    ;

    private final HttpStatus status;
    private final String code;
    private final String message;
}
