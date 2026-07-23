package com.ipillgood.server.domain.condition.code;

import com.ipillgood.server.global.apiPayload.code.BaseSuccessCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ConditionSuccessCode implements BaseSuccessCode {

    CURRENT_WEEK_SUCCESS(HttpStatus.OK, "SUCCESS200_1", "이번 주 컨디션 체크 상태 조회에 성공했습니다."),
    WEEKLY_RECORD_SAVE_SUCCESS(HttpStatus.CREATED, "SUCCESS201_1", "주간 컨디션 체크 저장에 성공했습니다."),
    MONTHLY_RECORDS_SUCCESS(HttpStatus.OK, "SUCCESS200_1", "월별 컨디션 그래프 조회에 성공했습니다."),
    WEEKLY_RECORD_DETAIL_SUCCESS(HttpStatus.OK, "SUCCESS200_1", "주차 상세 조회에 성공했습니다."),
    ;

    private final HttpStatus status;
    private final String code;
    private final String message;
}
