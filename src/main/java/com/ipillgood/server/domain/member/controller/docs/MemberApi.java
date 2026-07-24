package com.ipillgood.server.domain.member.controller.docs;

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
}
