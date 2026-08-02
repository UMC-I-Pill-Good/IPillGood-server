package com.ipillgood.server.domain.review.code;

import com.ipillgood.server.global.apiPayload.code.BaseErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ProductReviewErrorCode implements BaseErrorCode {

    // 403 FORBIDDEN
    REVIEW_UPDATE_FORBIDDEN(HttpStatus.FORBIDDEN, "REVIEW403_1", "본인이 작성한 후기만 수정할 수 있습니다."),
    REVIEW_DELETE_FORBIDDEN(HttpStatus.FORBIDDEN, "REVIEW403_2", "본인이 작성한 후기만 삭제할 수 있습니다."),
    REVIEW_VIEW_FORBIDDEN(HttpStatus.FORBIDDEN, "REVIEW403_3", "본인이 작성한 후기만 조회할 수 있습니다."),
    REVIEW_HELPFUL_FORBIDDEN(HttpStatus.FORBIDDEN, "REVIEW403_4", "본인이 작성한 후기에 도움됨을 누를 수 없습니다."),

    // 404 NOT_FOUND
    REVIEW_NOT_FOUND(HttpStatus.NOT_FOUND, "REVIEW404_1", "해당 후기가 존재하지 않습니다."),
    REVIEW_HELPFUL_NOT_FOUND(HttpStatus.NOT_FOUND, "REVIEW404_2", "해당 후기에 도움됨 이력을 남긴 적이 없습니다."),

    // 409
    REVIEW_ALREADY_EXISTS(HttpStatus.CONFLICT, "REVIEW409_1", "해당 상품에 이미 리뷰를 등록했습니다."),
    HELPFUL_ALREADY_EXISTS(HttpStatus.CONFLICT, "REVIEW409_2", "이미 도움됨 이력을 표시한 리뷰입니다."),
    ;

    private final HttpStatus status;
    private final String code;
    private final String message;
}
