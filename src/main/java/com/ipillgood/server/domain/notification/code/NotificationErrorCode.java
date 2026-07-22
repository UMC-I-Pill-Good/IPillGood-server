package com.ipillgood.server.domain.notification.code;

import com.ipillgood.server.global.apiPayload.code.BaseErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum NotificationErrorCode implements BaseErrorCode {

    // 400 BAD_REQUEST - 알림 설정 요청 값 검증
    APP_PUSH_SETTING_REQUEST_INVALID(HttpStatus.BAD_REQUEST, "NOTIFICATION400_1", "앱 푸시 설정 변경 요청이 올바르지 않습니다."),
    PUSH_TOKEN_REGISTER_REQUEST_INVALID(HttpStatus.BAD_REQUEST, "NOTIFICATION400_3", "푸시 토큰 등록 요청이 올바르지 않습니다."),
    PUSH_TOKEN_ID_INVALID(HttpStatus.BAD_REQUEST, "NOTIFICATION400_4", "푸시 토큰 ID가 올바르지 않습니다."),

    // 403 FORBIDDEN - 알림 설정 접근 조건
    ONBOARDING_NOT_COMPLETED(HttpStatus.FORBIDDEN, "NOTIFICATION403_1", "초기 설문을 완료해야 알림 설정을 이용할 수 있습니다."),

    // 404 NOT_FOUND - 알림 리소스 조회 실패
    PUSH_TOKEN_NOT_FOUND(HttpStatus.NOT_FOUND, "NOTIFICATION404_1", "푸시 토큰을 찾을 수 없습니다."),
    ;

    private final HttpStatus status;
    private final String code;
    private final String message;
}
