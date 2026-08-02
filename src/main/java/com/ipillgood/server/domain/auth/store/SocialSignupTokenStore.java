package com.ipillgood.server.domain.auth.store;

import java.util.Optional;

/**
 * 소셜 회원가입 임시 토큰 저장소 규약
 * - 테스트는 인메모리 fake로 대체함
 * - [완전 신규 사용자]로 판정한 시점 <-> 회원가입 요청이 들어올 때까지 짧은 대기 구간을 이어주는 역할
 */
public interface SocialSignupTokenStore {

    /**
     * 소셜 로그인 콜백이 [완전 신규 사용자]로 판정했을 때 실행
     * - 회원가입 대기 정보를 저장하고 임시 토큰을 발급
     * - 토큰 값은 호출자가 정하지 못하고 구현체가 추측 불가능하게 생성함
     * - 일정 시간이 지나면 자동 만료
     */
    String issue(PendingSocialSignup pendingSocialSignup);

    /**
     * 소셜 회원가입 요청이 들어왔을 때 실행
     * - 토큰에 해당하는 회원가입 대기 정보를 꺼내면서 즉시 폐기 (1회용)
     * - 토큰이 없거나 이미 사용됐으면 empty를 반환
     * - 같은 토큰으로 동시에 두 요청이 들어와도 한쪽만 값을 받도록 보장
     */
    Optional<PendingSocialSignup> consume(String socialSignupToken);
}
