package com.ipillgood.server.global.security.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.io.DecodingException;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SecurityException;
import io.jsonwebtoken.security.WeakKeyException;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

@Component
public class JwtProvider {

    /**
     * JWT Claims Key
     * 예) { "role": "USER", "type": "access" }
     */
    private static final String CLAIM_ROLE = "role";
    private static final String CLAIM_TYPE = "type";

    // JWT type Claim 값 (Access / Refresh 구분)
    private static final String TYPE_ACCESS = "access";
    private static final String TYPE_REFRESH = "refresh";

    @Value("${jwt.secret}")
    private String secret;

    // Access Token 유효 시간(ms)
    @Value("${jwt.access-token-validity}")
    private long accessTokenValidity;

    // Refresh Token 유효 시간(ms)
    @Value("${jwt.refresh-token-validity}")
    private long refreshTokenValidity;

    // JWT 서명/검증에 사용하는 시크릿키
    private SecretKey secretKey;

    // Bean 생성 후 한 번만 실행
    // 매 요청마다 SecretKey를 생성하지 않도록 미리 생성해 둔다.
    @PostConstruct
    public void init() {
        try {
            // base64로 인코딩된 secret을 원래의 바이트 값으로 되돌린 뒤 secretKey 객체로 변환
            secretKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret));
        } catch (DecodingException | WeakKeyException e) {
            // base64 형식이 아니거나(DecodingException) 최소 길이 불만족(WeakKeyException) => SecretKey 생성 실패
            // 단, 요청 인증 문제가 아니라 서버 설정 오류이므로 IllegalStateException 사용
            // 실제 실패 원인 e를 cause로 넘겨 로그에 남긴다.
            throw new IllegalStateException(JwtErrorCode.SECRET_KEY_INVALID.getMessage(), e);
        }
    }

    // Access Token 생성
    public String createAccessToken(Long memberId, String role) {
        return createToken(memberId, role, TYPE_ACCESS, accessTokenValidity);
    }

    // Refresh Token 생성
    public String createRefreshToken(Long memberId, String role) {
        return createToken(memberId, role, TYPE_REFRESH, refreshTokenValidity);
    }

    /**
     * Access/Refresh Token 생성 공통 메서드
     *
     * @param memberId 회원 PK
     * @param role     사용자 권한
     * @param type     토큰 종류 (access/refresh)
     * @param validity 토큰 유효 시간
     * @return 생성된 토큰 문자열
     */
    private String createToken(Long memberId, String role, String type, long validity) {
        Date now = new Date();
        Date expiration = new Date(now.getTime() + validity);

        return Jwts.builder()
                .subject(String.valueOf(memberId))      // 토큰 주체(PK 저장)
                .claim(CLAIM_ROLE, role)                // 권한(USER/ADMIN)
                .claim(CLAIM_TYPE, type)                // access / refresh 구분
                .issuedAt(now)                          // 토큰 발급 시간
                .expiration(expiration)                 // 토큰 만료 시간
                .signWith(secretKey)                    // secretKey 객체로 서명
                .compact();
    }

    // 인증에 사용할 액세스 파싱
    // 리프레시 토큰은 서명이 유효해도 인증 수단으로 쓸 수 없음 -> 토큰 타입 검증 필요
    public Claims parseAccessToken(String token) {
        Claims claims = parseClaims(token);
        if (!TYPE_ACCESS.equals(claims.get(CLAIM_TYPE, String.class))) {
            throw new JwtAuthenticationException(JwtErrorCode.TOKEN_INVALID_TYPE);
        }
        return claims;
    }

    // 파싱 성공 = 유효한 토큰
    // 파싱 실패 시 JwtAuthenticationException을 던지고, JwtAuthFilter는 이 예외 하나만 처리하면 됨
    private Claims parseClaims(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(secretKey)          // 서명 검증
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (ExpiredJwtException e) {
            throw new JwtAuthenticationException(JwtErrorCode.TOKEN_EXPIRED);       // 만료된 토큰
        } catch (MalformedJwtException e) {
            throw new JwtAuthenticationException(JwtErrorCode.TOKEN_MALFORMED);     // 토큰 형식 깨짐
        } catch (UnsupportedJwtException e) {
            throw new JwtAuthenticationException(JwtErrorCode.TOKEN_UNSUPPORTED);   // 지원하지 않는 토큰
        } catch (SecurityException e) {
            throw new JwtAuthenticationException(JwtErrorCode.TOKEN_INVALID);       // 서명 위조, 불일치
        } catch (IllegalArgumentException e) {
            throw new JwtAuthenticationException(JwtErrorCode.TOKEN_EMPTY);         // null 또는 빈 토큰
        } catch (JwtException e) {
            throw new JwtAuthenticationException(JwtErrorCode.TOKEN_INVALID);       // 그 외 모든 예외 (유효하지 않음 처리)
        }
    }

    // JWT subject -> 회원 PK(Long)로 변환
    public Long getMemberId(Claims claims) {
        return Long.valueOf(claims.getSubject());
    }

    // JWT role 클레임 추출
    public String getRole(Claims claims) {
        return claims.get(CLAIM_ROLE, String.class);
    }
}
