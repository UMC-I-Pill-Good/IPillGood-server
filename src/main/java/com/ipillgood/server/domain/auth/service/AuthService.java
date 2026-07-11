package com.ipillgood.server.domain.auth.service;

import com.ipillgood.server.domain.auth.code.AuthErrorCode;
import com.ipillgood.server.domain.auth.converter.AuthConverter;
import com.ipillgood.server.domain.auth.dto.AuthRequest;
import com.ipillgood.server.domain.auth.dto.AuthResponse;
import com.ipillgood.server.domain.auth.exception.AuthException;
import com.ipillgood.server.domain.member.entity.Member;
import com.ipillgood.server.domain.member.repository.MemberRepository;
import com.ipillgood.server.global.security.jwt.JwtProvider;
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

    // 로컬 회원가입
    @Transactional
    public AuthResponse.SignUp signUp(AuthRequest.SignUp request) {

        // Valid로 검증할 수 없는 3가지 예외처리
        // 1. 비밀번호 + 비밀번호 확인
        if (!request.password().equals(request.passwordConfirm())) {
            throw new AuthException(AuthErrorCode.PASSWORD_CONFIRM_MISMATCH);
        }

        // 2. 아이디 중복 확인
        if (memberRepository.existsByUsername(request.username())) {
            throw new AuthException(AuthErrorCode.DUPLICATE_USERNAME);
        }

        // 3. 이메일 중복 확인
        if (memberRepository.existsByEmail(request.email())) {
            throw new AuthException(AuthErrorCode.DUPLICATE_EMAIL);
        }

        // 비밀번호 암호화
        String encodedPassword = passwordEncoder.encode(request.password());

        // Member 엔티티 생성
        Member member = AuthConverter.toMember(request, encodedPassword);

        // DB 저장
        Member savedMember = memberRepository.save(member);

        return AuthConverter.toSignUpResponse(savedMember);
    }

    // 로컬 로그인
    public AuthResponse.Login login(AuthRequest.Login request) {
        // 1. 아이디로 회원 조회
        Member member = memberRepository.findByUsername(request.username())
                .orElseThrow(() -> new AuthException(AuthErrorCode.LOGIN_FAILED));

        // 2. 비밀번호 검증
        if (!passwordEncoder.matches(request.password(), member.getPassword())) {
            throw new AuthException(AuthErrorCode.LOGIN_FAILED);
        }

        // 3. 액세스/리프레시 토큰 발급
        String role = member.getRole().name();
        String accessToken = jwtProvider.createAccessToken(member.getId(), role);
        String refreshToken = jwtProvider.createRefreshToken(member.getId(), role);

        return AuthConverter.toLoginResponse(accessToken, refreshToken);
    }

    // 아이디 중복확인
    public void checkUsernameDuplicate(String username) {
        if (memberRepository.existsByUsername(username)) {
            throw new AuthException(AuthErrorCode.DUPLICATE_USERNAME);
        }
    }

    // 이메일 중복확인
    public void checkEmailDuplicate(String email) {
        if (memberRepository.existsByEmail(email)) {
            throw new AuthException(AuthErrorCode.DUPLICATE_EMAIL);
        }
    }
}
