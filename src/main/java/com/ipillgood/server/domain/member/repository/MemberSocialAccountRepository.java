package com.ipillgood.server.domain.member.repository;

import com.ipillgood.server.domain.member.entity.MemberSocialAccount;
import com.ipillgood.server.domain.member.entity.enums.SocialProvider;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

/**
 * 회원 소셜 계정 연동 레포지토리
 */
public interface MemberSocialAccountRepository extends JpaRepository<MemberSocialAccount, Long> {

    /**
     * 소셜 로그인 1단계에서 실행
     * 소셜 식별자로 연동 정보와 회원을 함께 조회
     */
    @Query("""
            select msa
            from MemberSocialAccount msa
            join fetch msa.member
            where msa.provider = :provider
              and msa.providerUserId = :providerUserId
            """)
    Optional<MemberSocialAccount> findWithMemberByProviderAndProviderUserId(
            @Param("provider") SocialProvider provider,
            @Param("providerUserId") String providerUserId);

    /**
     * 소셜 회원가입 / 계정 연동 직전에 실행
     * 이미 연동된 소셜 계정인지 확인
     */
    boolean existsByProviderAndProviderUserId(SocialProvider provider, String providerUserId);
}
