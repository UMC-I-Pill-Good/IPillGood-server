package com.ipillgood.server.domain.auth.service;

import com.ipillgood.server.domain.auth.code.AuthErrorCode;
import com.ipillgood.server.domain.auth.dto.AuthRequest;
import com.ipillgood.server.domain.auth.exception.AuthException;
import com.ipillgood.server.domain.member.entity.Member;
import com.ipillgood.server.domain.member.entity.MemberSocialAccount;
import com.ipillgood.server.domain.member.entity.enums.SocialProvider;
import com.ipillgood.server.domain.member.repository.MemberRepository;
import com.ipillgood.server.domain.member.repository.MemberSocialAccountRepository;
import com.ipillgood.server.domain.policy.dto.PolicyRequest;
import com.ipillgood.server.domain.policy.fixture.PolicyFixture;
import com.ipillgood.server.domain.policy.repository.MemberPolicyAgreementRepository;
import com.ipillgood.server.domain.policy.repository.PolicyDocumentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * 로컬 회원가입의 이메일 중복 분기가 기존 계정 종류에 따라 갈리는지 검증
 * (정책서 ERR-01 ① 로컬 재가입 차단 / ③ 소셜 가입 후 로컬 재가입 차단)
 */
@SpringBootTest
@ActiveProfiles("test")
class AuthServiceSignUpTest {

    private static final String EMAIL = "dup@test.com";

    @Autowired
    private AuthService authService;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private MemberSocialAccountRepository memberSocialAccountRepository;

    @Autowired
    private MemberPolicyAgreementRepository memberPolicyAgreementRepository;

    @Autowired
    private PolicyDocumentRepository policyDocumentRepository;

    @BeforeEach
    void setUp() {
        memberPolicyAgreementRepository.deleteAll();
        memberSocialAccountRepository.deleteAll();
        memberRepository.deleteAll();

        policyDocumentRepository.deleteAll();
        policyDocumentRepository.saveAll(PolicyFixture.defaultDocuments());
    }

    // 활성 약관 전체에 동의하는 회원가입 요청
    private AuthRequest.SignUp signUpRequest() {
        List<PolicyRequest.Agreement> agreements = policyDocumentRepository.findByActiveTrue().stream()
                .map(document -> new PolicyRequest.Agreement(document.getId(), true))
                .toList();
        return new AuthRequest.SignUp("테스터", "tester1", EMAIL, "abcd1234", "abcd1234", agreements);
    }

    @Test
    @DisplayName("이미 로컬 계정으로 가입된 이메일이면 AUTH409_1로 차단한다")
    void signUp_throwsDuplicateEmailForLocalAccount() {
        // 비밀번호를 보유한 로컬 회원
        memberRepository.save(Member.builder()
                .nickname("기존").username("existing").email(EMAIL).password("encoded-password").build());

        AuthException exception = assertThrows(AuthException.class,
                () -> authService.signUp(signUpRequest()));

        assertEquals(AuthErrorCode.DUPLICATE_EMAIL.getCode(), exception.getCode().getCode());
    }

    @Test
    @DisplayName("이미 소셜 전용 계정으로 가입된 이메일이면 AUTH409_2로 차단한다")
    void signUp_throwsSocialAccountExistsForSocialOnlyAccount() {
        // 비밀번호가 없는 소셜 전용 회원 + 소셜 계정 연동
        Member social = memberRepository.save(Member.builder()
                .nickname("소셜").email(EMAIL).build());
        memberSocialAccountRepository.save(
                MemberSocialAccount.of(social, SocialProvider.KAKAO, "kakao-123", EMAIL));

        AuthException exception = assertThrows(AuthException.class,
                () -> authService.signUp(signUpRequest()));

        assertEquals(AuthErrorCode.SOCIAL_ACCOUNT_ALREADY_EXISTS.getCode(), exception.getCode().getCode());
    }

    @Test
    @DisplayName("이메일 중복 확인도 기존 계정 종류에 따라 다른 코드로 응답한다")
    void checkEmailDuplicate_distinguishesLocalAndSocial() {
        // 소셜 전용 계정
        memberRepository.save(Member.builder().nickname("소셜").email(EMAIL).build());

        AuthException exception = assertThrows(AuthException.class,
                () -> authService.checkEmailDuplicate(EMAIL));

        assertEquals(AuthErrorCode.SOCIAL_ACCOUNT_ALREADY_EXISTS.getCode(), exception.getCode().getCode());
    }
}
