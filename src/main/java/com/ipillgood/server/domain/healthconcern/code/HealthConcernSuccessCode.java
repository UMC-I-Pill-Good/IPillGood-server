package com.ipillgood.server.domain.healthconcern.code;

import com.ipillgood.server.global.apiPayload.code.BaseSuccessCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum HealthConcernSuccessCode implements BaseSuccessCode {

    RECOMMENDED_INGREDIENTS_FOUND(HttpStatus.OK, "SUCCESS200_1", "건강 상태 추천 성분 조회에 성공했습니다."),
    ;

    private final HttpStatus status;
    private final String code;
    private final String message;
}
