package com.ipillgood.server.domain.review.code;

import com.ipillgood.server.global.apiPayload.code.BaseErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ProductReviewErrorCode implements BaseErrorCode {

    // 409
    REVIEW_ALREADY_EXISTS(HttpStatus.CONFLICT, "REVIEW409_1", "해당 상품에 이미 리뷰를 등록했습니다."),
    ;

    private final HttpStatus status;
    private final String code;
    private final String message;
}
