package com.ipillgood.server.global.security.jwt;

import java.time.Duration;
import java.util.Optional;

/**
 * 리프레시 토큰 저장소 인터페이스
 * 테스트는 인메모리 fake로 대체해 Redis 없이 검증 진행
 * 회원당 현재 유효한 리프레시 토큰 1개만 보관하는 화이트리스트 방식 사용 (RTR 방식과 적합)
 */
public interface RefreshTokenStore {

    // 리프레시 토큰 저장 (ttl 경과 후 자동 만료)
    void save(Long memberId, String refreshToken, Duration ttl);

    // memberId에 저장된 현재 유효한 리프레시 토큰을 조회
    // 값이 없거나 만료됐으면 empty 반환
    Optional<String> find(Long memberId);

    // memberId에 저장된 리프레시 토큰 폐기
    // 로그아웃 시 또는 재발급 요청에 들어온 토큰이 재사용 토큰일 경우 함수 호출
    void delete(Long memberId);
}
