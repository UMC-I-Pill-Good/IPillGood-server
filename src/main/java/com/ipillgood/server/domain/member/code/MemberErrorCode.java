package com.ipillgood.server.domain.member.code;

import com.ipillgood.server.global.apiPayload.code.BaseErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum MemberErrorCode implements BaseErrorCode {

    // 400 BAD_REQUEST - 비밀번호 변경
    CURRENT_PASSWORD_MISMATCH(HttpStatus.BAD_REQUEST, "MEMBER400_1", "현재 비밀번호가 올바르지 않습니다."),
    NEW_PASSWORD_CONFIRM_MISMATCH(HttpStatus.BAD_REQUEST, "MEMBER400_2", "비밀번호가 일치하지 않습니다."),

    // 403 FORBIDDEN
    SOCIAL_ONLY_ACCOUNT(HttpStatus.FORBIDDEN, "MEMBER403_1", "비밀번호를 설정한 계정만 변경할 수 있습니다."),

    // 404 NOT_FOUND
    MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "MEMBER404_1", "회원을 찾을 수 없습니다."),
    ;

    private final HttpStatus status;
    private final String code;
    private final String message;
}
