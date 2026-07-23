package com.ipillgood.server.domain.review.code;

import com.ipillgood.server.global.apiPayload.code.BaseSuccessCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ReviewSuccessCode implements BaseSuccessCode {

    // 200
    REVIEW_IMAGE_UPLOAD_URL_SUCCESS(HttpStatus.OK, "REVIEW200_1", "후기 이미지 업로드 URL 발급에 성공했습니다."),
    ;

    private final HttpStatus status;
    private final String code;
    private final String message;
}
