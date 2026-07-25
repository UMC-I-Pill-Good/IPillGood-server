package com.ipillgood.server.domain.member.controller;

import com.ipillgood.server.domain.member.code.MemberSuccessCode;
import com.ipillgood.server.domain.member.controller.docs.MemberApi;
import com.ipillgood.server.domain.member.dto.MemberRequest;
import com.ipillgood.server.domain.member.dto.MemberResponse;
import com.ipillgood.server.domain.member.service.MemberService;
import com.ipillgood.server.global.apiPayload.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/members")
public class MemberController implements MemberApi {

    private final MemberService memberService;

    // 내 정보 조회
    @Override
    @GetMapping("/me")
    public ApiResponse<MemberResponse.MyInfo> getMyInfo(@AuthenticationPrincipal Long memberId) {
        MemberResponse.MyInfo response = memberService.getMyInfo(memberId);
        return ApiResponse.onSuccess(MemberSuccessCode.MY_INFO_FOUND, response);
    }

    // 프로필 수정
    @Override
    @PatchMapping("/me/profile")
    public ApiResponse<MemberResponse.ProfileUpdated> updateProfile(
            @AuthenticationPrincipal Long memberId,
            @Valid @RequestBody MemberRequest.UpdateProfile request) {
        MemberResponse.ProfileUpdated response = memberService.updateNickname(memberId, request);
        return ApiResponse.onSuccess(MemberSuccessCode.PROFILE_UPDATED, response);
    }
}
