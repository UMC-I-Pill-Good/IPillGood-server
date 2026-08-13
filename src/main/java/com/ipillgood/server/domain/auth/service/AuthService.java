package com.ipillgood.server.domain.auth.service;

import com.ipillgood.server.domain.auth.code.AuthErrorCode;
import com.ipillgood.server.domain.auth.converter.AuthConverter;
import com.ipillgood.server.domain.auth.dto.AuthRequest;
import com.ipillgood.server.domain.auth.dto.AuthResponse;
import com.ipillgood.server.domain.auth.exception.AuthException;
import com.ipillgood.server.domain.member.entity.Member;
import com.ipillgood.server.domain.member.repository.MemberRepository;
import com.ipillgood.server.domain.policy.service.PolicyService;
import com.ipillgood.server.global.s3.S3Service;
import com.ipillgood.server.global.security.jwt.CookieUtil;
import com.ipillgood.server.global.security.jwt.JwtProvider;
import com.ipillgood.server.global.security.jwt.RefreshTokenStore;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;
    private final RefreshTokenStore refreshTokenStore;
    private final PolicyService policyService;
    private final S3Service s3Service;
    private final CookieUtil cookieUtil;

    // 로컬 회원가입
    @Transactional
    public AuthResponse.SignUp signUp(AuthRequest.SignUp request) {

        // Valid로 검증할 수 없는 예외처리
        // 1. 비밀번호 + 비밀번호 확인
        if (!request.password().equals(request.passwordConfirm())) {
            throw new AuthException(AuthErrorCode.PASSWORD_CONFIRM_MISMATCH);
        }

        // 2. 아이디 중복 확인
        if (memberRepository.existsByUsername(request.username())) {
            throw new AuthException(AuthErrorCode.DUPLICATE_USERNAME);
        }

        // 3. 이메일 중복 확인 - 기존 계정이 로컬/소셜인지에 따라 다르게 안내
        validateEmailAvailable(request.email());

        // 4. 비밀번호 암호화 + 회원 저장
        String encodedPassword = passwordEncoder.encode(request.password());
        Member savedMember = memberRepository.save(AuthConverter.toMember(request, encodedPassword));

        // 5. 약관 동의 검증 + 이력 저장
        policyService.agreeToPolicies(savedMember, request.policyAgreements());

        return AuthConverter.toSignUpResponse(savedMember, s3Service::getPublicUrl);
    }

    // 로컬 로그인
    public AuthResponse.Login login(AuthRequest.Login request, HttpServletResponse response) {
        // 1. 아이디로 회원 조회
        Member member = memberRepository.findByUsername(request.username())
                .orElseThrow(() -> new AuthException(AuthErrorCode.LOGIN_FAILED));

        // 2. 비밀번호 검증
        if (!passwordEncoder.matches(request.password(), member.getPassword())) {
            throw new AuthException(AuthErrorCode.LOGIN_FAILED);
        }

        // 3. 액세스/리프레시 토큰 발급 + 세션(기기) 식별자 발급
        String role = member.getRole().name();
        String sessionId = jwtProvider.generateSessionId();
        String accessToken = jwtProvider.createAccessToken(member.getId(), role, sessionId);
        String refreshToken = jwtProvider.createRefreshToken(member.getId(), role, sessionId);

        // 4. 리프레시 토큰 저장 (재발급 검증용)
        refreshTokenStore.save(member.getId(), sessionId, refreshToken, jwtProvider.getRefreshTokenValidity());
        cookieUtil.setRefreshTokenCookie(response, refreshToken, jwtProvider.getRefreshTokenValidity());

        return AuthConverter.toLoginResponse(member, accessToken, jwtProvider.getAccessTokenExpiresIn());
    }

    // 토큰 재발급 (Refresh Token Rotation)
    // refreshToken은 요청 본문이 아니라 httpOnly 쿠키에서 읽어온 값
    public AuthResponse.Login reissue(String refreshToken, HttpServletResponse response) {

        // 쿠키가 없으면 재로그인 진행
        if (refreshToken == null || refreshToken.isBlank()) {
            throw new AuthException(AuthErrorCode.REFRESH_TOKEN_INVALID);
        }

        // 1. 리프레시 토큰 검증
        // memberId = 토큰 주인(회원) 식별용, sessionId = 기기(세션) 식별용
        Claims claims = jwtProvider.parseRefreshToken(refreshToken);
        Long memberId = jwtProvider.getMemberId(claims);
        String sessionId = jwtProvider.getSessionId(claims);

        // 2. 저장된 리프레시 토큰 조회 (없으면 로그아웃/만료 상태)
        String storedToken = refreshTokenStore.find(memberId, sessionId)
                .orElseThrow(() -> new AuthException(AuthErrorCode.REFRESH_TOKEN_INVALID));

        // 3. 재사용 감지: 저장값과 다르면 탈취 의심 토큰 -> 해당 세션(기기)만 폐기 후 차단
        if (!storedToken.equals(refreshToken)) {
            refreshTokenStore.delete(memberId, sessionId);
            throw new AuthException(AuthErrorCode.REFRESH_TOKEN_INVALID);
        }

        // 4. 회원 재조회 (현재 role 반영, 탈퇴한 회원 방어)
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new AuthException(AuthErrorCode.REFRESH_TOKEN_INVALID));

        // 5. 새 액세스 토큰 발급 + 리프레시 토큰 Rotation 진행 (세션ID는 그대로 사용 - 같은 기기이므로)
        String role = member.getRole().name();
        String newAccessToken = jwtProvider.createAccessToken(member.getId(), role, sessionId);
        String newRefreshToken = jwtProvider.createRefreshToken(member.getId(), role, sessionId);
        refreshTokenStore.save(member.getId(), sessionId, newRefreshToken, jwtProvider.getRefreshTokenValidity());
        cookieUtil.setRefreshTokenCookie(response, newRefreshToken, jwtProvider.getRefreshTokenValidity());

        return AuthConverter.toLoginResponse(member, newAccessToken, jwtProvider.getAccessTokenExpiresIn());
    }

    // 로그아웃 (이 기기(세션)의 리프레시 토큰만 폐기, 다른 기기 로그인은 유지됨)
    // 쿠키 삭제는 컨트롤러에서 처리 (저장소 삭제와 달리 별도 값이 필요 없는 순수 응답 헤더 조작)
    public void logout(Long memberId, String sessionId) {
        refreshTokenStore.delete(memberId, sessionId);
    }

    // 아이디 중복확인
    public void checkUsernameDuplicate(String username) {
        if (memberRepository.existsByUsername(username)) {
            throw new AuthException(AuthErrorCode.DUPLICATE_USERNAME);
        }
    }

    // 이메일 중복확인
    public void checkEmailDuplicate(String email) {
        validateEmailAvailable(email);
    }

    /**
     * 이미 가입된 이메일인 경우, 기존 계정의 종류에 따라 다른 방식으로 안내
     * 1. 소셜 전용 계정(비밀번호 없음): 해당 소셜 계정으로 로그인 안내 (AUTH409_2)
     * 2. 로컬 계정(비밀번호 보유): 해당 이메일로 로그인 안내 (AUTH409_1)
     */
    private void validateEmailAvailable(String email) {
        memberRepository.findByEmail(Member.normalizeEmail(email)).ifPresent(member -> {

            // 1. 소셜 계정 존재
            if (member.isSocialOnly()) {
                throw new AuthException(AuthErrorCode.SOCIAL_ACCOUNT_ALREADY_EXISTS);
            }

            // 2. 로컬 계정 존재
            throw new AuthException(AuthErrorCode.DUPLICATE_EMAIL);
        });
    }
}
