package com.ipillgood.server.domain.member.service;

import com.ipillgood.server.domain.member.code.MemberErrorCode;
import com.ipillgood.server.domain.member.converter.MemberConverter;
import com.ipillgood.server.domain.member.dto.MemberRequest;
import com.ipillgood.server.domain.member.dto.MemberResponse;
import com.ipillgood.server.domain.member.entity.Member;
import com.ipillgood.server.domain.member.entity.MemberSocialAccount;
import com.ipillgood.server.domain.member.entity.enums.SocialProvider;
import com.ipillgood.server.domain.member.exception.MemberException;
import com.ipillgood.server.domain.member.repository.MemberRepository;
import com.ipillgood.server.domain.member.repository.MemberSocialAccountRepository;
import com.ipillgood.server.global.s3.S3Service;
import com.ipillgood.server.global.security.jwt.RefreshTokenStore;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
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
    private final S3Service s3Service;
    private final PasswordEncoder passwordEncoder;
    private final RefreshTokenStore refreshTokenStore;

    /**
     * 마이페이지 진입 시 실행
     * 내 기본 정보, 로그인 방식, 온보딩 완료 여부를 조회
     */
    public MemberResponse.MyInfo getMyInfo(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new MemberException(MemberErrorCode.MEMBER_NOT_FOUND));

        List<SocialProvider> socialProviders = memberSocialAccountRepository.findByMember(member).stream()
                .map(MemberSocialAccount::getProvider)
                .toList();

        // 로컬으로만 로그인한 경우 socialProviders 빈 값으로 return
        return MemberConverter.toMyInfo(member, socialProviders, s3Service::getPublicUrl);
    }

    /**
     * 프로필 관리 화면에서 실행
     * 닉네임 변경 (같은 값으로 변경해도 성공 처리)
     */
    @Transactional
    public MemberResponse.ProfileUpdated updateNickname(Long memberId, MemberRequest.UpdateProfile request) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new MemberException(MemberErrorCode.MEMBER_NOT_FOUND));

        member.updateNickname(request.nickname());
        return MemberConverter.toProfileUpdated(member);
    }

    /**
     * 비밀번호 변경 화면에서 실행
     * 소셜 전용 계정 차단 -> 현재 비밀번호 검증 -> 새 비밀번호 확인 일치 검증 순서로 처리
     */
    @Transactional
    public void changePassword(Long memberId, MemberRequest.ChangePassword request) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new MemberException(MemberErrorCode.MEMBER_NOT_FOUND));

        // 1. 소셜 계정은 비밀번호 변경 불가능
        if (member.isSocialOnly()) {
            throw new MemberException(MemberErrorCode.SOCIAL_ONLY_ACCOUNT);
        }

        // 2. 기존 비밀번호 비교 불일치
        if (!passwordEncoder.matches(request.currentPassword(), member.getPassword())) {
            throw new MemberException(MemberErrorCode.CURRENT_PASSWORD_MISMATCH);
        }

        // 3. 새 비밀번호 - 새 비밀번호 확인 불일치
        if (!request.newPassword().equals(request.newPasswordConfirm())) {
            throw new MemberException(MemberErrorCode.NEW_PASSWORD_CONFIRM_MISMATCH);
        }

        // 4. 비밀번호 변경 성공
        member.changePassword(passwordEncoder.encode(request.newPassword()));
    }

    /**
     * 회원 탈퇴 시 실행
     * 리프레시 토큰 폐기 후 회원 삭제
     */
    @Transactional
    public void withdraw(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new MemberException(MemberErrorCode.MEMBER_NOT_FOUND));

        refreshTokenStore.delete(memberId);
        memberRepository.delete(member);
    }

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
