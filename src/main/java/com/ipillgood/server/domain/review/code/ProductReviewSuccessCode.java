package com.ipillgood.server.domain.review.code;

import com.ipillgood.server.global.apiPayload.code.BaseSuccessCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ProductReviewSuccessCode implements BaseSuccessCode {

    // 200
    REVIEW_IMAGE_UPLOAD_URL_SUCCESS(HttpStatus.OK, "REVIEW200_1", "후기 이미지 업로드 URL 발급에 성공했습니다."),
    VIEW_REVIEWS_SUCCESS(HttpStatus.OK, "REVIEW200_2", "해당 상품 리뷰 목록 조회에 성공했습니다."),

    // 201
    REVIEW_CREATE_SUCCESS(HttpStatus.CREATED, "REVIEW201_1", "해당 영양제에 대한 리뷰가 등록되었습니다."),
    ;

    private final HttpStatus status;
    private final String code;
    private final String message;
}
