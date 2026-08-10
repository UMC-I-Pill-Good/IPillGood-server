package com.ipillgood.server.domain.recommendation.code;

import com.ipillgood.server.global.apiPayload.code.BaseErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum RecommendationErrorCode implements BaseErrorCode {

    CURRENT_RECOMMENDATION_NOT_FOUND(HttpStatus.NOT_FOUND, "RECOMMENDATION404_1", "현재 활성 추천 결과가 없습니다."),
    INVALID_RETRY_STATUS(HttpStatus.CONFLICT, "RECOMMENDATION409_1", "재시도할 수 없는 추천 상태입니다."),
    FEEDBACK_CYCLE_ALREADY_RESPONDED(HttpStatus.CONFLICT, "RECOMMENDATION409_2", "이미 응답한 피드백 사이클입니다."),
    FEEDBACK_CYCLE_NOT_DUE(HttpStatus.CONFLICT, "RECOMMENDATION409_3", "아직 응답할 수 없는 피드백 사이클입니다."),
    NOT_CONFIRMABLE_STATUS(HttpStatus.CONFLICT, "RECOMMENDATION409_4", "확인 처리할 수 없는 추천 상태입니다."),
    ;

    private final HttpStatus status;
    private final String code;
    private final String message;
}
