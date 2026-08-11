package com.ipillgood.server.domain.auth.controller.docs;

import com.ipillgood.server.domain.auth.dto.AuthRequest;
import com.ipillgood.server.domain.auth.dto.AuthResponse;
import com.ipillgood.server.global.apiPayload.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/**
 * 인증 관련 API 문서
 * 회원가입, 로그인, 아이디/이메일 중복검사, 소셜 회원가입/계정연동(JSON 응답)
 * 소셜 로그인/콜백(302 리다이렉트 전용)은 SocialOAuthRedirectApi 참고
 */
@Tag(name = "Auth API", description = "인증(회원가입/로그인) 관련 API")
public interface AuthApi {

    @Operation(
            summary = "로컬 회원가입",
            description = """
                    닉네임/아이디/이메일/비밀번호와 약관 동의 목록을 받아 회원가입을 진행합니다.
                    policyAgreements에는 활성 상태인 필수 약관이 모두 agreed=true로 포함되어야 합니다.
                    이 API는 가입만 처리하고 로그인 토큰을 발급하지 않으므로, 가입 후 로그인 API를 따로 호출해야 합니다.
                    """
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "201",
                    description = "회원가입 성공",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "isSuccess": true,
                                              "code": "AUTH201_1",
                                              "message": "로컬 회원가입이 완료되었습니다.",
                                              "result": {
                                                "memberId": 1,
                                                "nickname": "아필굿",
                                                "username": "demouser",
                                                "email": "demo@ipillgood.com",
                                                "profileImageUrl": "https://ipillgood-bucket.s3.ap-northeast-2.amazonaws.com/profileImage/profile1.png",
                                                "onboardingCompleted": false,
                                                "createdAt": "2026-08-11T10:00:00"
                                              }
                                            }
                                            """
                            )
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = """
                            입력 형식 오류, 비밀번호 확인 불일치, 필수 약관 미동의입니다.
                            약관 ID가 없거나 비활성이거나 중복 모순 제출이면 COMMON400_2가 반환됩니다.
                            """,
                    content = @Content(
                            mediaType = "application/json",
                            examples = {
                                    @ExampleObject(
                                            name = "입력 형식 오류",
                                            value = """
                                                    {
                                                      "isSuccess": false,
                                                      "code": "COMMON400_1",
                                                      "message": "잘못된 요청입니다.",
                                                      "result": {
                                                        "password": "8~16자의 영문, 숫자, 특수문자를 조합해 주세요."
                                                      }
                                                    }
                                                    """
                                    ),
                                    @ExampleObject(
                                            name = "비밀번호 확인 불일치",
                                            value = """
                                                    {
                                                      "isSuccess": false,
                                                      "code": "AUTH400_6",
                                                      "message": "비밀번호가 일치하지 않습니다.",
                                                      "result": null
                                                    }
                                                    """
                                    ),
                                    @ExampleObject(
                                            name = "필수 약관 미동의",
                                            value = """
                                                    {
                                                      "isSuccess": false,
                                                      "code": "AUTH400_9",
                                                      "message": "필수 항목에 동의해주세요.",
                                                      "result": null
                                                    }
                                                    """
                                    )
                            }
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "409",
                    description = "아이디 중복 또는 이메일 중복. 이메일 중복은 기존 계정이 로컬인지 소셜인지에 따라 코드가 갈립니다.",
                    content = @Content(
                            mediaType = "application/json",
                            examples = {
                                    @ExampleObject(
                                            name = "아이디 중복",
                                            value = """
                                                    {
                                                      "isSuccess": false,
                                                      "code": "AUTH409_4",
                                                      "message": "이미 사용 중인 아이디입니다.",
                                                      "result": null
                                                    }
                                                    """
                                    ),
                                    @ExampleObject(
                                            name = "이메일 중복 - 기존 로컬 계정",
                                            value = """
                                                    {
                                                      "isSuccess": false,
                                                      "code": "AUTH409_1",
                                                      "message": "이미 사용 중인 이메일입니다. 해당 이메일로 로그인해 주세요.",
                                                      "result": null
                                                    }
                                                    """
                                    ),
                                    @ExampleObject(
                                            name = "이메일 중복 - 기존 소셜 계정",
                                            value = """
                                                    {
                                                      "isSuccess": false,
                                                      "code": "AUTH409_2",
                                                      "message": "이미 해당 이메일로 [카카오/네이버] 계정이 존재해요. 해당 소셜 계정으로 로그인해 주세요.",
                                                      "result": null
                                                    }
                                                    """
                                    )
                            }
                    )
            )
    })
    @SecurityRequirements
    ApiResponse<AuthResponse.SignUp> signUp(@Valid AuthRequest.SignUp request);

    @Operation(
            summary = "로컬 로그인",
            description = """
                    아이디와 비밀번호로 로그인합니다.
                    accessToken은 응답 본문으로, refreshToken은 httpOnly 쿠키로 발급됩니다.
                    아이디가 없는 경우와 비밀번호가 틀린 경우를 구분하지 않고 동일한 401을 반환합니다(계정 존재 여부 노출 방지).
                    """
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "로그인 성공",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "isSuccess": true,
                                              "code": "AUTH200_1",
                                              "message": "로그인에 성공했습니다.",
                                              "result": {
                                                "accessToken": "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiIxIiwicm9sZSI6IlVTRVIifQ.xxxxx",
                                                "tokenType": "Bearer",
                                                "expiresIn": 3600,
                                                "memberId": 1,
                                                "onboardingCompleted": false
                                              }
                                            }
                                            """
                            )
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "아이디 또는 비밀번호가 비어 있음",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "isSuccess": false,
                                              "code": "COMMON400_1",
                                              "message": "잘못된 요청입니다.",
                                              "result": {
                                                "username": "아이디를 입력해주세요.",
                                                "password": "비밀번호를 입력해주세요."
                                              }
                                            }
                                            """
                            )
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "존재하지 않는 아이디이거나 비밀번호가 틀림",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "isSuccess": false,
                                              "code": "AUTH401_1",
                                              "message": "아이디 또는 비밀번호를 확인해주세요.",
                                              "result": null
                                            }
                                            """
                            )
                    )
            )
    })
    @SecurityRequirements
    ApiResponse<AuthResponse.Login> login(@Valid AuthRequest.Login request,
                                          @Parameter(hidden = true) HttpServletResponse response);

    @Operation(
            summary = "토큰 재발급",
            description = """
                    httpOnly 쿠키의 refreshToken을 검증해 액세스 토큰을 재발급하며, refreshToken도 새 토큰으로 교체됩니다.
                    액세스 토큰 만료 시, 그리고 소셜 로그인 콜백 직후 accessToken을 받을 때 모두 이 API를 씁니다.
                    요청 본문과 Authorization 헤더 없이 쿠키만으로 동작하며, 응답 형식은 로그인과 같습니다.
                    """
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "재발급 성공",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "isSuccess": true,
                                              "code": "AUTH200_3",
                                              "message": "토큰 재발급에 성공했습니다.",
                                              "result": {
                                                "accessToken": "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiIxIiwicm9sZSI6IlVTRVIifQ.xxxxx",
                                                "tokenType": "Bearer",
                                                "expiresIn": 3600,
                                                "memberId": 1,
                                                "onboardingCompleted": false
                                              }
                                            }
                                            """
                            )
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "refreshToken 쿠키가 없거나, 만료·위조되었거나, 이미 사용되어 폐기된 경우. 재로그인이 필요합니다.",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "isSuccess": false,
                                              "code": "AUTH401_2",
                                              "message": "다시 로그인해 주세요.",
                                              "result": null
                                            }
                                            """
                            )
                    )
            )
    })
    @SecurityRequirements
    ApiResponse<AuthResponse.Login> reissue(
            @Parameter(description = "httpOnly 쿠키로 전달되는 refreshToken (요청 본문 아님)", hidden = true)
            String refreshToken,
            @Parameter(hidden = true) HttpServletResponse response);

    @Operation(
            summary = "로그아웃",
            description = """
                    이 기기(세션)의 리프레시 토큰만 폐기합니다. 다른 기기에서의 로그인은 유지됩니다.
                    refreshToken 쿠키도 함께 삭제됩니다.
                    이미 발급된 액세스 토큰은 만료 시점까지 유효하므로, 클라이언트에서도 저장한 토큰을 지워야 합니다.
                    """
    )
    @SecurityRequirement(name = "JWT TOKEN")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "로그아웃 성공",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "isSuccess": true,
                                              "code": "AUTH200_2",
                                              "message": "로그아웃에 성공했습니다.",
                                              "result": null
                                            }
                                            """
                            )
                    )
            )
    })
    ApiResponse<Void> logout(@Parameter(hidden = true) Long memberId,
                             @Parameter(hidden = true) String sessionId,
                             @Parameter(hidden = true) HttpServletResponse response);

    @Operation(
            summary = "아이디 중복확인",
            description = """
                    입력한 아이디가 이미 사용 중인지 확인합니다. 회원가입 화면의 중복확인 버튼에서 사용합니다. result는 항상 null입니다.
                    회원가입 API도 아이디 중복을 다시 검사하므로, 이 API 통과 후에도 가입 시점에 409가 날 수 있습니다.
                    """
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "사용 가능한 아이디",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "isSuccess": true,
                                              "code": "AUTH200_4",
                                              "message": "사용 가능한 아이디입니다.",
                                              "result": null
                                            }
                                            """
                            )
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "아이디 형식 오류 (영문·숫자 2~10자)",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "isSuccess": false,
                                              "code": "COMMON400_1",
                                              "message": "잘못된 요청입니다.",
                                              "result": {
                                                "checkUsername.username": "공백을 제외하고 2~10자의 영문, 숫자를 조합해 주세요."
                                              }
                                            }
                                            """
                            )
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "409",
                    description = "이미 사용 중인 아이디",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "isSuccess": false,
                                              "code": "AUTH409_4",
                                              "message": "이미 사용 중인 아이디입니다.",
                                              "result": null
                                            }
                                            """
                            )
                    )
            )
    })
    @SecurityRequirements
    ApiResponse<Void> checkUsername(
            @Parameter(description = "중복 확인할 아이디입니다. 영문·숫자 2~10자.", example = "demouser")
            @Pattern(regexp = "^[a-zA-Z0-9]{2,10}$", message = "공백을 제외하고 2~10자의 영문, 숫자를 조합해 주세요.")
            String username);

    @Operation(
            summary = "이메일 중복확인",
            description = """
                    입력한 이메일이 이미 사용 중인지 확인합니다. result는 항상 null입니다.
                    """
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "사용 가능한 이메일",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "isSuccess": true,
                                              "code": "AUTH200_5",
                                              "message": "사용 가능한 이메일입니다.",
                                              "result": null
                                            }
                                            """
                            )
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "이메일 형식 오류",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "isSuccess": false,
                                              "code": "COMMON400_1",
                                              "message": "잘못된 요청입니다.",
                                              "result": {
                                                "checkEmail.email": "올바른 이메일 형식이 아닙니다."
                                              }
                                            }
                                            """
                            )
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "409",
                    description = "이미 사용 중인 이메일. 기존 계정 종류에 따라 코드가 갈립니다.",
                    content = @Content(
                            mediaType = "application/json",
                            examples = {
                                    @ExampleObject(
                                            name = "기존 로컬 계정",
                                            value = """
                                                    {
                                                      "isSuccess": false,
                                                      "code": "AUTH409_1",
                                                      "message": "이미 사용 중인 이메일입니다. 해당 이메일로 로그인해 주세요.",
                                                      "result": null
                                                    }
                                                    """
                                    ),
                                    @ExampleObject(
                                            name = "기존 소셜 전용 계정",
                                            value = """
                                                    {
                                                      "isSuccess": false,
                                                      "code": "AUTH409_2",
                                                      "message": "이미 해당 이메일로 [카카오/네이버] 계정이 존재해요. 해당 소셜 계정으로 로그인해 주세요.",
                                                      "result": null
                                                    }
                                                    """
                                    )
                            }
                    )
            )
    })
    @SecurityRequirements
    ApiResponse<Void> checkEmail(
            @Parameter(description = "중복 확인할 이메일입니다.", example = "demo@ipillgood.com")
            @NotBlank(message = "올바른 이메일 형식이 아닙니다.")
            @Email(message = "올바른 이메일 형식이 아닙니다.")
            String email);

    @Operation(
            summary = "카카오 회원가입",
            description = """
                    카카오 로그인 콜백이 발급한 socialSignupToken과 약관 동의로 회원가입을 완료합니다.
                    이메일/닉네임은 요청으로 받지 않고, 토큰에 연결된 콜백 조회값을 그대로 씁니다.
                    가입과 동시에 로그인 처리됩니다. (accessToken은 Body, refreshToken은 httpOnly 쿠키)
                    socialSignupToken은 발급 후 5분간 유효한 1회용 토큰입니다.
                    """
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "201",
                    description = "소셜 회원가입 성공 (가입 즉시 로그인 처리)",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "isSuccess": true,
                                              "code": "AUTH201_2",
                                              "message": "소셜 회원가입이 완료되었습니다.",
                                              "result": {
                                                "memberId": 2,
                                                "provider": "KAKAO",
                                                "nickname": "아필굿",
                                                "email": "demo@kakao.com",
                                                "profileImageUrl": "https://ipillgood-bucket.s3.ap-northeast-2.amazonaws.com/profileImage/profile1.png",
                                                "onboardingCompleted": false,
                                                "createdAt": "2026-08-11T10:00:00",
                                                "accessToken": "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiIyIiwicm9sZSI6IlVTRVIifQ.xxxxx",
                                                "tokenType": "Bearer",
                                                "expiresIn": 3600
                                              }
                                            }
                                            """
                            )
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = """
                            필수 약관에 동의하지 않은 경우입니다.
                            약관 ID가 없거나 비활성이거나 중복 모순 제출이면 COMMON400_2가 반환됩니다.
                            """,
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "isSuccess": false,
                                              "code": "AUTH400_9",
                                              "message": "필수 항목에 동의해주세요.",
                                              "result": null
                                            }
                                            """
                            )
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "socialSignupToken이 만료·위조되었거나 이미 사용됨, 또는 provider 불일치. 소셜 로그인부터 다시 시작해야 합니다.",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "isSuccess": false,
                                              "code": "AUTH401_3",
                                              "message": "계정 연동 토큰이 유효하지 않습니다. 다시 시도해주세요.",
                                              "result": null
                                            }
                                            """
                            )
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "409",
                    description = "약관 동의 화면에 머무는 사이 같은 이메일 또는 같은 소셜 계정으로 가입이 완료된 경우",
                    content = @Content(
                            mediaType = "application/json",
                            examples = {
                                    @ExampleObject(
                                            name = "이미 연동된 소셜 계정",
                                            value = """
                                                    {
                                                      "isSuccess": false,
                                                      "code": "AUTH409_2",
                                                      "message": "이미 해당 이메일로 [카카오/네이버] 계정이 존재해요. 해당 소셜 계정으로 로그인해 주세요.",
                                                      "result": null
                                                    }
                                                    """
                                    ),
                                    @ExampleObject(
                                            name = "이미 가입된 이메일",
                                            value = """
                                                    {
                                                      "isSuccess": false,
                                                      "code": "AUTH409_1",
                                                      "message": "이미 사용 중인 이메일입니다. 해당 이메일로 로그인해 주세요.",
                                                      "result": null
                                                    }
                                                    """
                                    )
                            }
                    )
            )
    })
    @SecurityRequirements
    ApiResponse<AuthResponse.SocialSignUp> kakaoSignUp(@Valid AuthRequest.SocialSignUp request,
                                                       @Parameter(hidden = true) HttpServletResponse response);

    @Operation(
            summary = "네이버 회원가입",
            description = """
                    네이버 로그인 콜백이 발급한 socialSignupToken과 약관 동의로 회원가입을 완료합니다.
                    이메일/닉네임은 요청으로 받지 않고, 토큰에 연결된 콜백 조회값을 그대로 씁니다.
                    가입과 동시에 로그인 처리됩니다. (accessToken은 Body, refreshToken은 httpOnly 쿠키)
                    socialSignupToken은 발급 후 5분간 유효한 1회용 토큰입니다.
                    """
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "201",
                    description = "소셜 회원가입 성공 (가입 즉시 로그인 처리)",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "isSuccess": true,
                                              "code": "AUTH201_2",
                                              "message": "소셜 회원가입이 완료되었습니다.",
                                              "result": {
                                                "memberId": 3,
                                                "provider": "NAVER",
                                                "nickname": "아필굿",
                                                "email": "demo@naver.com",
                                                "profileImageUrl": "https://ipillgood-bucket.s3.ap-northeast-2.amazonaws.com/profileImage/profile1.png",
                                                "onboardingCompleted": false,
                                                "createdAt": "2026-08-11T10:00:00",
                                                "accessToken": "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiIzIiwicm9sZSI6IlVTRVIifQ.xxxxx",
                                                "tokenType": "Bearer",
                                                "expiresIn": 3600
                                              }
                                            }
                                            """
                            )
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = """
                            필수 약관에 동의하지 않은 경우입니다.
                            약관 ID가 없거나 비활성이거나 중복 모순 제출이면 COMMON400_2가 반환됩니다.
                            """,
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "isSuccess": false,
                                              "code": "AUTH400_9",
                                              "message": "필수 항목에 동의해주세요.",
                                              "result": null
                                            }
                                            """
                            )
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "socialSignupToken이 만료·위조되었거나 이미 사용됨, 또는 provider 불일치",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "isSuccess": false,
                                              "code": "AUTH401_3",
                                              "message": "계정 연동 토큰이 유효하지 않습니다. 다시 시도해주세요.",
                                              "result": null
                                            }
                                            """
                            )
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "409",
                    description = "약관 동의 화면에 머무는 사이 같은 이메일 또는 같은 소셜 계정으로 가입이 완료된 경우",
                    content = @Content(
                            mediaType = "application/json",
                            examples = {
                                    @ExampleObject(
                                            name = "이미 연동된 소셜 계정",
                                            value = """
                                                    {
                                                      "isSuccess": false,
                                                      "code": "AUTH409_2",
                                                      "message": "이미 해당 이메일로 [카카오/네이버] 계정이 존재해요. 해당 소셜 계정으로 로그인해 주세요.",
                                                      "result": null
                                                    }
                                                    """
                                    ),
                                    @ExampleObject(
                                            name = "이미 가입된 이메일",
                                            value = """
                                                    {
                                                      "isSuccess": false,
                                                      "code": "AUTH409_1",
                                                      "message": "이미 사용 중인 이메일입니다. 해당 이메일로 로그인해 주세요.",
                                                      "result": null
                                                    }
                                                    """
                                    )
                            }
                    )
            )
    })
    @SecurityRequirements
    ApiResponse<AuthResponse.SocialSignUp> naverSignUp(@Valid AuthRequest.SocialSignUp request,
                                                       @Parameter(hidden = true) HttpServletResponse response);

    @Operation(
            summary = "카카오 계정 연동",
            description = """
                    카카오 로그인 콜백이 발급한 accountLinkToken으로 기존 회원에 카카오 계정을 연동합니다.
                    같은 이메일의 로컬 계정이 이미 있는 사용자가 카카오로 로그인했을 때 쓰는 흐름입니다.
                    연동과 동시에 로그인 처리됩니다. (accessToken은 Body, refreshToken은 httpOnly 쿠키)
                    응답의 linked는 항상 true이고, linkedAt은 연동이 기록된 시각입니다.
                    """
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "계정 연동 성공 (연동 즉시 로그인 처리)",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "isSuccess": true,
                                              "code": "AUTH200_7",
                                              "message": "소셜 계정 연동에 성공했습니다.",
                                              "result": {
                                                "linked": true,
                                                "provider": "KAKAO",
                                                "linkedAt": "2026-08-11T10:00:00",
                                                "accessToken": "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiIxIiwicm9sZSI6IlVTRVIifQ.xxxxx",
                                                "tokenType": "Bearer",
                                                "expiresIn": 3600,
                                                "memberId": 1,
                                                "onboardingCompleted": true
                                              }
                                            }
                                            """
                            )
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "accountLinkToken이 만료·위조되었거나 이미 사용됨, provider 불일치, 또는 연동 대상 회원이 사라진 경우",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "isSuccess": false,
                                              "code": "AUTH401_3",
                                              "message": "계정 연동 토큰이 유효하지 않습니다. 다시 시도해주세요.",
                                              "result": null
                                            }
                                            """
                            )
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "409",
                    description = "해당 소셜 계정이 이미 다른 회원에게 연동된 경우",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "isSuccess": false,
                                              "code": "AUTH409_2",
                                              "message": "이미 해당 이메일로 [카카오/네이버] 계정이 존재해요. 해당 소셜 계정으로 로그인해 주세요.",
                                              "result": null
                                            }
                                            """
                            )
                    )
            )
    })
    @SecurityRequirements
    ApiResponse<AuthResponse.SocialLink> kakaoLink(@Valid AuthRequest.SocialLink request,
                                                   @Parameter(hidden = true) HttpServletResponse response);

    @Operation(
            summary = "네이버 계정 연동",
            description = """
                    네이버 로그인 콜백이 발급한 accountLinkToken으로 기존 회원에 네이버 계정을 연동합니다.
                    같은 이메일의 로컬 계정이 이미 있는 사용자가 네이버로 로그인했을 때 쓰는 흐름입니다.
                    연동과 동시에 로그인 처리됩니다. (accessToken은 Body, refreshToken은 httpOnly 쿠키)
                    응답의 linked는 항상 true이고, linkedAt은 연동이 기록된 시각입니다.
                    """
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "계정 연동 성공 (연동 즉시 로그인 처리)",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "isSuccess": true,
                                              "code": "AUTH200_7",
                                              "message": "소셜 계정 연동에 성공했습니다.",
                                              "result": {
                                                "linked": true,
                                                "provider": "NAVER",
                                                "linkedAt": "2026-08-11T10:00:00",
                                                "accessToken": "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiIxIiwicm9sZSI6IlVTRVIifQ.xxxxx",
                                                "tokenType": "Bearer",
                                                "expiresIn": 3600,
                                                "memberId": 1,
                                                "onboardingCompleted": true
                                              }
                                            }
                                            """
                            )
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "accountLinkToken이 만료·위조되었거나 이미 사용됨, provider 불일치, 또는 연동 대상 회원이 사라진 경우",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "isSuccess": false,
                                              "code": "AUTH401_3",
                                              "message": "계정 연동 토큰이 유효하지 않습니다. 다시 시도해주세요.",
                                              "result": null
                                            }
                                            """
                            )
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "409",
                    description = "해당 소셜 계정이 이미 다른 회원에게 연동된 경우",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "isSuccess": false,
                                              "code": "AUTH409_2",
                                              "message": "이미 해당 이메일로 [카카오/네이버] 계정이 존재해요. 해당 소셜 계정으로 로그인해 주세요.",
                                              "result": null
                                            }
                                            """
                            )
                    )
            )
    })
    @SecurityRequirements
    ApiResponse<AuthResponse.SocialLink> naverLink(@Valid AuthRequest.SocialLink request,
                                                   @Parameter(hidden = true) HttpServletResponse response);
}
