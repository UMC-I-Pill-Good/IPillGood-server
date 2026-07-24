package com.ipillgood.server.domain.member.service;

import com.ipillgood.server.domain.member.entity.Member;
import com.ipillgood.server.domain.member.entity.MemberSocialAccount;
import com.ipillgood.server.domain.member.entity.enums.SocialProvider;
import com.ipillgood.server.domain.member.repository.MemberRepository;
import com.ipillgood.server.domain.member.repository.MemberSocialAccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * 회원/소셜 계정 데이터를 소유하는 서비스
 * - 인증 흐름(auth)은 이 서비스를 통해서만 회원·소셜 계정을 조회하거나 생성함
 * - 여기서는 조회와 저장만 담당하고, 그 결과로 무엇을 할지(분기·예외·토큰 발급)는 auth가 정함
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberService {

    private final MemberRepository memberRepository;
    private final MemberSocialAccountRepository memberSocialAccountRepository;

    /**
     * 소셜 로그인 1단계에서 실행
     * 소셜 식별자로 이미 연동된 회원을 조회
     */
    public Optional<Member> findBySocialAccount(SocialProvider provider, String providerUserId) {
        return memberSocialAccountRepository
                .findWithMemberByProviderAndProviderUserId(provider, providerUserId)
                .map(MemberSocialAccount::getMember);
    }

    /**
     * 소셜 로그인 2단계에서 실행
     * 이메일로 기존 회원을 조회 (계정 연동 대상 탐지 목적)
     */
    public Optional<Member> findByEmail(String email) {
        return memberRepository.findByEmail(Member.normalizeEmail(email));
    }

    /**
     * 소셜 회원가입 / 계정 연동 직전에 실행
     * 이 소셜 계정이 이미 어떤 회원에게든 연동되어 있는지 확인
     */
    public boolean isSocialAccountLinked(SocialProvider provider, String providerUserId) {
        return memberSocialAccountRepository.existsByProviderAndProviderUserId(provider, providerUserId);
    }

    /**
     * 소셜 계정 연동 시 실행
     * 연동 대상 회원을 ID로 조회
     */
    public Optional<Member> findById(Long memberId) {
        return memberRepository.findById(memberId);
    }

    /**
     * 소셜 회원가입 시 실행
     * 회원과 소셜 계정 연동 정보를 함께 생성
     */
    @Transactional
    public Member createSocialMember(String email, String nickname,
                                     SocialProvider provider, String providerUserId) {

        // username과 비밀번호는 없음
        Member member = memberRepository.save(Member.builder()
                .nickname(nickname)
                .email(email)
                .build());

        memberSocialAccountRepository.save(MemberSocialAccount.of(member, provider, providerUserId, email));
        return member;
    }

    /**
     * 계정 연동 시 실행
     * 기존 회원에 새 소셜 계정을 연동
     * 로컬 가입 회원에 소셜을 연동하거나, 다른 소셜 계정을 추가할 때 사용
     */
    @Transactional
    public MemberSocialAccount linkSocialAccount(Member member, SocialProvider provider,
                                                 String providerUserId, String providerEmail) {
        return memberSocialAccountRepository.save(
                MemberSocialAccount.of(member, provider, providerUserId, providerEmail));
    }
}
