package com.ipillgood.server.global.s3.code;

import com.ipillgood.server.global.apiPayload.code.BaseErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum S3ErrorCode implements BaseErrorCode {

    // 400 - 업로드 요청 검증
    INVALID_CONTENT_TYPE(HttpStatus.BAD_REQUEST, "S3400_1", "지원하지 않는 이미지 형식입니다."),
    EMPTY_UPLOAD_REQUEST(HttpStatus.BAD_REQUEST, "S3400_2", "업로드할 이미지 정보가 없습니다."),
    INVALID_IMAGE_KEY(HttpStatus.BAD_REQUEST, "S3400_3", "유효하지 않은 이미지 키입니다."),

    // 404 - 업로드 여부 확인
    IMAGE_NOT_FOUND(HttpStatus.NOT_FOUND, "S3404_1", "업로드되지 않은 이미지입니다."),

    // 500 - S3 연동 실패
    IMAGE_UPLOAD_URL_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "S3500_1", "이미지 업로드 URL 발급에 실패했습니다."),
    IMAGE_LOOKUP_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "S3500_2", "이미지 조회에 실패했습니다."),
    IMAGE_DELETE_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "S3500_3", "이미지 삭제에 실패했습니다."),
    ;

    private final HttpStatus status;
    private final String code;
    private final String message;
}
