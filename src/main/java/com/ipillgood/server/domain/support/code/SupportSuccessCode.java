package com.ipillgood.server.domain.support.code;

import com.ipillgood.server.global.apiPayload.code.BaseSuccessCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum SupportSuccessCode implements BaseSuccessCode {

    FAQ_LIST_FOUND(HttpStatus.OK, "SUPPORT200_1", "FAQ 목록 조회에 성공했습니다."),
    SUPPORT_INFO_FOUND(HttpStatus.OK, "SUPPORT200_2", "문의/고객센터 조회에 성공했습니다."),
    ADMIN_FAQ_LIST_FOUND(HttpStatus.OK, "SUPPORT200_3", "FAQ 목록 조회에 성공했습니다."),
    ADMIN_FAQ_UPDATED(HttpStatus.OK, "SUPPORT200_4", "FAQ 수정에 성공했습니다."),
    ADMIN_FAQ_DELETED(HttpStatus.OK, "SUPPORT200_5", "삭제 처리되었습니다."),
    ADMIN_FAQ_CREATED(HttpStatus.CREATED, "SUPPORT201_1", "FAQ 등록에 성공했습니다."),
    ;

    private final HttpStatus status;
    private final String code;
    private final String message;
}
