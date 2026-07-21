package com.ipillgood.server.domain.recommendation.code;

import com.ipillgood.server.global.apiPayload.code.BaseSuccessCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum RecommendationSuccessCode implements BaseSuccessCode {

    CURRENT_RECOMMENDATION_SUCCESS(HttpStatus.OK, "SUCCESS200_1", "현재 추천 결과 조회에 성공했습니다."),
    RECOMMENDATION_DETAIL_SUCCESS(HttpStatus.OK, "SUCCESS200_1", "추천 생성 상태/결과 조회에 성공했습니다."),
    RECOMMENDATION_RETRY_SUCCESS(HttpStatus.ACCEPTED, "SUCCESS202_1", "추천 생성 재시도에 성공했습니다."),
    ;

    private final HttpStatus status;
    private final String code;
    private final String message;
}
