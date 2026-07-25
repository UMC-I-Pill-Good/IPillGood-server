package com.ipillgood.server.domain.member.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public class MemberRequest {

    /**
     * 프로필 수정 요청
     */
    public record UpdateProfile(
            @NotBlank(message = "1~10자 이내로 입력해주세요.")
            @Pattern(regexp = "^[가-힣a-zA-Z0-9]{1,10}$", message = "1~10자 이내로 입력해주세요.")
            String nickname
    ) {
    }
}
