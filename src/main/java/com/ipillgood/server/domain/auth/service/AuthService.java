package com.ipillgood.server.domain.auth.service;

import com.ipillgood.server.domain.auth.code.AuthErrorCode;
import com.ipillgood.server.domain.auth.converter.AuthConverter;
import com.ipillgood.server.domain.auth.dto.AuthRequest;
import com.ipillgood.server.domain.auth.dto.AuthResponse;
import com.ipillgood.server.domain.auth.exception.AuthException;
import com.ipillgood.server.domain.member.entity.Member;
import com.ipillgood.server.domain.member.repository.MemberRepository;
import com.ipillgood.server.domain.policy.entity.MemberPolicyAgreement;
import com.ipillgood.server.domain.policy.entity.PolicyDocument;
import com.ipillgood.server.domain.policy.repository.MemberPolicyAgreementRepository;
import com.ipillgood.server.domain.policy.repository.PolicyDocumentRepository;
import com.ipillgood.server.global.apiPayload.code.GeneralErrorCode;
import com.ipillgood.server.global.apiPayload.exception.GeneralException;
import com.ipillgood.server.global.security.jwt.JwtProvider;
import com.ipillgood.server.global.security.jwt.RefreshTokenStore;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;
    private final RefreshTokenStore refreshTokenStore;
    private final PolicyDocumentRepository policyDocumentRepository;
    private final MemberPolicyAgreementRepository memberPolicyAgreementRepository;

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

        // 3. 이메일 중복 확인
        if (memberRepository.existsByEmail(request.email())) {
            throw new AuthException(AuthErrorCode.DUPLICATE_EMAIL);
        }

        // 4. 약관 동의 검증
        Map<Long, Boolean> agreedByDocument = toAgreementMap(request.policyAgreements());
        List<PolicyDocument> submittedDocuments = findActiveDocuments(agreedByDocument.keySet());
        validateRequiredAgreements(agreedByDocument);

        // 5. 비밀번호 암호화 + 회원 저장
        String encodedPassword = passwordEncoder.encode(request.password());
        Member savedMember = memberRepository.save(AuthConverter.toMember(request, encodedPassword));

        // 6. 약관 동의 이력 저장
        saveAgreements(savedMember, submittedDocuments, agreedByDocument);

        return AuthConverter.toSignUpResponse(savedMember);
    }

    // 동의 목록: {문서 ID: 동의 여부} key-value 형식 맵
    private Map<Long, Boolean> toAgreementMap(List<AuthRequest.PolicyAgreement> agreements) {
        Map<Long, Boolean> agreedByDocument = new LinkedHashMap<>();
        for (AuthRequest.PolicyAgreement agreement : agreements) {
            Boolean previous = agreedByDocument.putIfAbsent(agreement.policyDocumentId(), agreement.agreed());

            // 기존에 존재하는 문서와 동의값이 다르면 예외처리
            if (previous != null && !previous.equals(agreement.agreed())) {
                throw new GeneralException(GeneralErrorCode.VALID_FAIL);
            }
        }
        return agreedByDocument;
    }

    // 제출한 문서가 모두 실재하는 활성 문서인지 확인 (존재하지 않거나 비활성 문서가 섞이는 문제 방지)
    private List<PolicyDocument> findActiveDocuments(Set<Long> documentIds) {
        List<PolicyDocument> documents = policyDocumentRepository.findByIdInAndActiveTrue(documentIds);

        // 만약 요청한 문서가 4개인데, 조회된 문서가 3개인 경우
        if (documents.size() != documentIds.size()) {
            throw new GeneralException(GeneralErrorCode.VALID_FAIL);
        }
        return documents;
    }

    // 활성 필수 약관이 모두 동의(true) 상태인지 확인 (누락하거나 false로 보내면 차단)
    private void validateRequiredAgreements(Map<Long, Boolean> agreedByDocument) {
        boolean allRequiredAgreed = policyDocumentRepository.findByActiveTrueAndRequiredTrue().stream()
                .allMatch(document -> Boolean.TRUE.equals(agreedByDocument.get(document.getId())));
        if (!allRequiredAgreed) {
            throw new AuthException(AuthErrorCode.REQUIRED_TERMS_NOT_AGREED);
        }
    }

    // 제출한 약관 동의 이력을 회원에 저장 (동의/거부 모두 기록)
    private void saveAgreements(Member member, List<PolicyDocument> documents, Map<Long, Boolean> agreedByDocument) {
        List<MemberPolicyAgreement> agreements = documents.stream()
                .map(document -> MemberPolicyAgreement.of(
                        member, document, Boolean.TRUE.equals(agreedByDocument.get(document.getId()))))
                .toList();
        memberPolicyAgreementRepository.saveAll(agreements);
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

        // 4. 리프레시 토큰 저장 (재발급 검증용)
        refreshTokenStore.save(member.getId(), refreshToken, jwtProvider.getRefreshTokenValidity());

        return AuthConverter.toLoginResponse(accessToken, refreshToken);
    }

    // 토큰 재발급 (Refresh Token Rotation)
    public AuthResponse.Login reissue(AuthRequest.Reissue request) {

        // 1. 리프레시 토큰 검증
        // memberId = 토큰 주인(회원) 식별용으로 사용
        Claims claims = jwtProvider.parseRefreshToken(request.refreshToken());
        Long memberId = jwtProvider.getMemberId(claims);

        // 2. 저장된 리프레시 토큰 조회 (없으면 로그아웃/만료 상태)
        String storedToken = refreshTokenStore.find(memberId)
                .orElseThrow(() -> new AuthException(AuthErrorCode.REFRESH_TOKEN_INVALID));

        // 3. 재사용 감지: 저장값과 다르면 탈취 의심 토큰 -> 저장분 폐기 후 차단
        if (!storedToken.equals(request.refreshToken())) {
            refreshTokenStore.delete(memberId);
            throw new AuthException(AuthErrorCode.REFRESH_TOKEN_INVALID);
        }

        // 4. 회원 재조회 (현재 role 반영, 탈퇴한 회원 방어)
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new AuthException(AuthErrorCode.REFRESH_TOKEN_INVALID));

        // 5. 새 액세스 토큰 발급 + 리프레시 토큰 Rotation 진행
        String role = member.getRole().name();
        String newAccessToken = jwtProvider.createAccessToken(member.getId(), role);
        String newRefreshToken = jwtProvider.createRefreshToken(member.getId(), role);
        refreshTokenStore.save(member.getId(), newRefreshToken, jwtProvider.getRefreshTokenValidity());

        return AuthConverter.toLoginResponse(newAccessToken, newRefreshToken);
    }

    // 로그아웃 (저장된 리프레시 토큰 폐기)
    public void logout(Long memberId) {
        refreshTokenStore.delete(memberId);
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
