package com.ipillgood.server.domain.member.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public class MemberRequest {

    /**
     * 프로필 수정 요청
     */
    public record UpdateProfile(
            @NotBlank(message = "공백을 제외하고 1~10자의 한글, 영문, 숫자를 입력해 주세요.")
            @Pattern(regexp = "^[가-힣a-zA-Z0-9]{1,10}$", message = "공백을 제외하고 1~10자의 한글, 영문, 숫자를 입력해 주세요.")
            String nickname
    ) {
    }

    /**
     * 비밀번호 변경 요청
     */
    public record ChangePassword(
            @NotBlank(message = "현재 비밀번호를 입력해주세요.")
            String currentPassword,

            @NotBlank(message = "8~16자의 영문, 숫자, 특수문자를 조합해 주세요.")
            @Pattern(regexp = "^(?=.*[a-zA-Z])(?=.*[0-9])(?=.*[!@#$%^&*()_+=-])[A-Za-z0-9!@#$%^&*()_+=-]{8,16}$",
                    message = "8~16자의 영문, 숫자, 특수문자를 조합해 주세요.")
            String newPassword,

            @NotBlank(message = "새 비밀번호 확인을 입력해주세요.")
            String newPasswordConfirm
    ) {
    }
}
