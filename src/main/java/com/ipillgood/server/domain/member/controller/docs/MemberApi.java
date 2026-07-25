package com.ipillgood.server.domain.member.controller.docs;

import com.ipillgood.server.domain.member.dto.MemberRequest;
import com.ipillgood.server.domain.member.dto.MemberResponse;
import com.ipillgood.server.global.apiPayload.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * 마이페이지 관련 API 문서
 */
@Tag(name = "Member API", description = "마이페이지 회원 조회/수정/탈퇴 관련 API")
public interface MemberApi {

    @Operation(summary = "내 정보 조회",
            description = "내 기본 정보, 로그인 방식, 온보딩 완료 여부를 조회합니다.")
    ApiResponse<MemberResponse.MyInfo> getMyInfo(Long memberId);

    @Operation(summary = "프로필 수정",
            description = "내 닉네임을 수정합니다.")
    ApiResponse<MemberResponse.ProfileUpdated> updateProfile(Long memberId, MemberRequest.UpdateProfile request);

    @Operation(summary = "비밀번호 변경",
            description = "로컬 로그인 사용자의 비밀번호를 변경합니다.")
    ApiResponse<Void> changePassword(Long memberId, MemberRequest.ChangePassword request);

    @Operation(summary = "회원 탈퇴",
            description = "회원 계정과 사용자 종속 데이터를 삭제합니다.")
    ApiResponse<Void> withdraw(Long memberId);
}
