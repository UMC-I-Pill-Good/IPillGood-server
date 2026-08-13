package com.ipillgood.server.global.security.jwt;

import java.time.Duration;
import java.util.Optional;

/**
 * 리프레시 토큰 저장소 인터페이스
 * 테스트는 인메모리 fake로 대체해 Redis 없이 검증 진행
 * 회원당 기기(세션)별로 리프레시 토큰을 보관하는 화이트리스트 방식 사용 (다중 기기 로그인 지원, RTR 방식과 적합)
 */
public interface RefreshTokenStore {

    // 특정 세션(기기)의 리프레시 토큰 저장 (ttl 경과 후 자동 만료)
    void save(Long memberId, String sessionId, String refreshToken, Duration ttl);

    // memberId + sessionId에 저장된 현재 유효한 리프레시 토큰을 조회
    // 값이 없거나 만료됐으면 empty 반환
    Optional<String> find(Long memberId, String sessionId);

    // 특정 세션(기기)의 리프레시 토큰만 폐기
    // 특정 기기만 로그아웃 + 재발급 요청에 들어온 토큰이 재사용 토큰일 경우에도 호출
    void delete(Long memberId, String sessionId);

    // memberId의 모든 세션(기기) 리프레시 토큰을 폐기
    // 비밀번호 변경, 회원 탈퇴 시 전체 기기 로그아웃
    void deleteAll(Long memberId);
}
