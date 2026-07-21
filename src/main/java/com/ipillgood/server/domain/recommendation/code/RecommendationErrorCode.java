package com.ipillgood.server.domain.recommendation.code;

import com.ipillgood.server.global.apiPayload.code.BaseErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum RecommendationErrorCode implements BaseErrorCode {

    CURRENT_RECOMMENDATION_NOT_FOUND(HttpStatus.NOT_FOUND, "RECOMMENDATION404_1", "현재 활성 추천 결과가 없습니다."),
    FORBIDDEN(HttpStatus.FORBIDDEN, "RECOMMENDATION403_1", "본인의 추천 결과만 접근할 수 있습니다."),
    INVALID_RETRY_STATUS(HttpStatus.CONFLICT, "RECOMMENDATION409_1", "재시도할 수 없는 추천 상태입니다."),
    FEEDBACK_CYCLE_FORBIDDEN(HttpStatus.FORBIDDEN, "RECOMMENDATION403_2", "본인의 추천 피드백 사이클만 응답할 수 있습니다."),
    FEEDBACK_CYCLE_ALREADY_RESPONDED(HttpStatus.CONFLICT, "RECOMMENDATION409_2", "이미 응답한 피드백 사이클입니다."),
    ;

    private final HttpStatus status;
    private final String code;
    private final String message;
}
