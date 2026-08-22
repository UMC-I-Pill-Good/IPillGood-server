package com.ipillgood.server.domain.condition.code;

import com.ipillgood.server.global.apiPayload.code.BaseErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ConditionErrorCode implements BaseErrorCode {

    // TODO(시연 임시): 컨디션 체크 허용 요일을 토·일로 확장하면서 메시지 문구만 조정했다.
    //  코드값(NOT_SUNDAY / POPUP_NOT_SUNDAY, CONDITION400_1 / CONDITION400_2)은 클라이언트 호환을 위해 유지한다.
    NOT_SUNDAY(HttpStatus.BAD_REQUEST, "CONDITION400_1", "주말(토·일)에만 컨디션 체크를 저장할 수 있습니다."),
    POPUP_NOT_SUNDAY(HttpStatus.BAD_REQUEST, "CONDITION400_2", "주말(토·일)에만 컨디션 체크 팝업을 기록할 수 있습니다."),
    ONBOARDING_NOT_COMPLETED(HttpStatus.FORBIDDEN, "CONDITION403_2", "초기 설문을 완료해야 이용할 수 있습니다."),
    ALREADY_CHECKED(HttpStatus.CONFLICT, "CONDITION409_1", "이미 이번 주 컨디션 체크를 완료했습니다."),
    ;

    private final HttpStatus status;
    private final String code;
    private final String message;
}
