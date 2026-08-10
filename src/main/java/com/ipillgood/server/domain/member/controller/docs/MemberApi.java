package com.ipillgood.server.domain.member.controller.docs;

import com.ipillgood.server.domain.member.dto.MemberRequest;
import com.ipillgood.server.domain.member.dto.MemberResponse;
import com.ipillgood.server.global.apiPayload.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

/**
 * 마이페이지 관련 API 문서
 */
@Tag(name = "Member API", description = "마이페이지 회원 조회/수정/탈퇴 관련 API")
public interface MemberApi {

    @Operation(
            summary = "내 정보 조회",
            description = """
                    마이페이지 진입 시 내 기본 정보, 로그인 수단, 온보딩 완료 여부를 조회합니다.
                    loginProviders는 LOCAL/KAKAO/NAVER 중 이 회원이 쓸 수 있는 수단이며 최소 1개가 담깁니다.
                    LOCAL이 있으면 비밀번호 보유 회원이고, 없으면 비밀번호 변경 API가 403으로 차단됩니다.
                    onboardingCompleted가 false면 온보딩 화면으로 유도합니다.
                    """
    )
    @SecurityRequirement(name = "JWT TOKEN")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "내 정보 조회 성공",
                    content = @Content(
                            mediaType = "application/json",
                            examples = {
                                    @ExampleObject(
                                            name = "로컬 가입 회원 (비밀번호 변경 가능)",
                                            value = """
                                                    {
                                                      "isSuccess": true,
                                                      "code": "MEMBER200_1",
                                                      "message": "내 정보 조회에 성공했습니다.",
                                                      "result": {
                                                        "memberId": 1,
                                                        "nickname": "아필굿",
                                                        "profileImageUrl": "https://ipillgood-bucket.s3.ap-northeast-2.amazonaws.com/profileImage/profile1.png",
                                                        "loginProviders": ["LOCAL"],
                                                        "onboardingCompleted": true
                                                      }
                                                    }
                                                    """
                                    ),
                                    @ExampleObject(
                                            name = "로컬 가입 + 소셜 연동 회원 (비밀번호 변경 가능)",
                                            value = """
                                                    {
                                                      "isSuccess": true,
                                                      "code": "MEMBER200_1",
                                                      "message": "내 정보 조회에 성공했습니다.",
                                                      "result": {
                                                        "memberId": 2,
                                                        "nickname": "아필굿",
                                                        "profileImageUrl": "https://ipillgood-bucket.s3.ap-northeast-2.amazonaws.com/profileImage/profile1.png",
                                                        "loginProviders": ["LOCAL", "KAKAO"],
                                                        "onboardingCompleted": true
                                                      }
                                                    }
                                                    """
                                    ),
                                    @ExampleObject(
                                            name = "소셜 전용 회원 (비밀번호 변경 불가)",
                                            value = """
                                                    {
                                                      "isSuccess": true,
                                                      "code": "MEMBER200_1",
                                                      "message": "내 정보 조회에 성공했습니다.",
                                                      "result": {
                                                        "memberId": 3,
                                                        "nickname": "아필굿",
                                                        "profileImageUrl": "https://ipillgood-bucket.s3.ap-northeast-2.amazonaws.com/profileImage/profile1.png",
                                                        "loginProviders": ["KAKAO"],
                                                        "onboardingCompleted": false
                                                      }
                                                    }
                                                    """
                                    )
                            }
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "토큰은 유효하지만 해당 회원이 존재하지 않는 경우 (탈퇴 후 남은 액세스 토큰으로 요청한 경우 등)",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "isSuccess": false,
                                              "code": "MEMBER404_1",
                                              "message": "회원을 찾을 수 없습니다.",
                                              "result": null
                                            }
                                            """
                            )
                    )
            )
    })
    ApiResponse<MemberResponse.MyInfo> getMyInfo(@Parameter(hidden = true) Long memberId);

    @Operation(
            summary = "프로필 수정",
            description = """
                    내 닉네임을 수정합니다. 프로필 관리 화면에서 사용합니다.
                    닉네임은 공백을 제외한 한글/영문/숫자 1~10자입니다.
                    현재와 같은 닉네임으로 요청해도 성공 처리되며, 닉네임 중복은 검사하지 않습니다.
                    """
    )
    @SecurityRequirement(name = "JWT TOKEN")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "프로필 수정 성공",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "isSuccess": true,
                                              "code": "MEMBER200_2",
                                              "message": "프로필 수정에 성공했습니다.",
                                              "result": {
                                                "memberId": 1,
                                                "nickname": "아필굿"
                                              }
                                            }
                                            """
                            )
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "닉네임 형식 오류 (공백 포함, 특수문자 포함, 10자 초과 등)",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "isSuccess": false,
                                              "code": "COMMON400_1",
                                              "message": "잘못된 요청입니다.",
                                              "result": {
                                                "nickname": "공백을 제외하고 한글/영문/숫자만 1~10자 이내로 입력해주세요."
                                              }
                                            }
                                            """
                            )
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "토큰은 유효하지만 해당 회원이 존재하지 않는 경우",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "isSuccess": false,
                                              "code": "MEMBER404_1",
                                              "message": "회원을 찾을 수 없습니다.",
                                              "result": null
                                            }
                                            """
                            )
                    )
            )
    })
    ApiResponse<MemberResponse.ProfileUpdated> updateProfile(@Parameter(hidden = true) Long memberId,
                                                             @Valid MemberRequest.UpdateProfile request);

    @Operation(
            summary = "비밀번호 변경",
            description = """
                    비밀번호를 가진 회원만 변경할 수 있습니다. result는 항상 null입니다.
                    검증 순서는 소셜 전용 계정 차단 -> 현재 비밀번호 확인 -> 새 비밀번호 확인값 일치 순입니다.
                    소셜 연동 여부가 아니라 비밀번호 보유 여부가 기준이라, loginProviders에 LOCAL이 없으면 403입니다.
                    성공하면 모든 기기의 리프레시 토큰이 폐기되어 다른 기기는 재로그인이 필요합니다.
                    """
    )
    @SecurityRequirement(name = "JWT TOKEN")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "비밀번호 변경 성공",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "isSuccess": true,
                                              "code": "MEMBER200_3",
                                              "message": "비밀번호 변경에 성공했습니다.",
                                              "result": null
                                            }
                                            """
                            )
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "현재 비밀번호 불일치, 새 비밀번호 확인값 불일치, 또는 새 비밀번호 형식 오류",
                    content = @Content(
                            mediaType = "application/json",
                            examples = {
                                    @ExampleObject(
                                            name = "현재 비밀번호 불일치",
                                            value = """
                                                    {
                                                      "isSuccess": false,
                                                      "code": "MEMBER400_1",
                                                      "message": "현재 비밀번호가 올바르지 않습니다.",
                                                      "result": null
                                                    }
                                                    """
                                    ),
                                    @ExampleObject(
                                            name = "새 비밀번호 확인값 불일치",
                                            value = """
                                                    {
                                                      "isSuccess": false,
                                                      "code": "MEMBER400_2",
                                                      "message": "비밀번호가 일치하지 않습니다.",
                                                      "result": null
                                                    }
                                                    """
                                    ),
                                    @ExampleObject(
                                            name = "새 비밀번호 형식 오류",
                                            value = """
                                                    {
                                                      "isSuccess": false,
                                                      "code": "COMMON400_1",
                                                      "message": "잘못된 요청입니다.",
                                                      "result": {
                                                        "newPassword": "8~16자의 영문, 숫자, 특수문자를 조합해 주세요."
                                                      }
                                                    }
                                                    """
                                    )
                            }
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "비밀번호가 없는 소셜 전용 계정이라 변경할 수 없는 경우",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "isSuccess": false,
                                              "code": "MEMBER403_1",
                                              "message": "비밀번호를 설정한 계정만 변경할 수 있습니다.",
                                              "result": null
                                            }
                                            """
                            )
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "토큰은 유효하지만 해당 회원이 존재하지 않는 경우",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "isSuccess": false,
                                              "code": "MEMBER404_1",
                                              "message": "회원을 찾을 수 없습니다.",
                                              "result": null
                                            }
                                            """
                            )
                    )
            )
    })
    ApiResponse<Void> changePassword(@Parameter(hidden = true) Long memberId,
                                     @Valid MemberRequest.ChangePassword request);

    @Operation(
            summary = "회원 탈퇴",
            description = """
                    회원 계정과 사용자 종속 데이터를 삭제합니다. result는 항상 null입니다.
                    모든 기기의 리프레시 토큰을 폐기한 뒤 회원을 삭제합니다.
                    삭제는 되돌릴 수 없으므로, 클라이언트에서 확인 절차를 거친 뒤 호출해야 합니다.
                    탈퇴 후 남아 있는 액세스 토큰으로 다른 API를 호출하면 404가 반환됩니다.
                    """
    )
    @SecurityRequirement(name = "JWT TOKEN")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "회원 탈퇴 성공",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "isSuccess": true,
                                              "code": "MEMBER200_4",
                                              "message": "회원 탈퇴에 성공했습니다.",
                                              "result": null
                                            }
                                            """
                            )
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "이미 탈퇴했거나 존재하지 않는 회원인 경우",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "isSuccess": false,
                                              "code": "MEMBER404_1",
                                              "message": "회원을 찾을 수 없습니다.",
                                              "result": null
                                            }
                                            """
                            )
                    )
            )
    })
    ApiResponse<Void> withdraw(@Parameter(hidden = true) Long memberId);
}
