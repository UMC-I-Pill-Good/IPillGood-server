package com.ipillgood.server.domain.member.code;

import com.ipillgood.server.global.apiPayload.code.BaseSuccessCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum MemberSuccessCode implements BaseSuccessCode {

    MY_INFO_FOUND(HttpStatus.OK, "MEMBER200_1", "내 정보 조회에 성공했습니다."),
    PROFILE_UPDATED(HttpStatus.OK, "MEMBER200_2", "프로필 수정에 성공했습니다."),
    PASSWORD_CHANGED(HttpStatus.OK, "MEMBER200_3", "비밀번호 변경에 성공했습니다."),
    ;

    private final HttpStatus status;
    private final String code;
    private final String message;
}
