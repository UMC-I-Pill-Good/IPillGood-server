package com.ipillgood.server.global.security.jwt;

import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/**
 * 요청마다 Access Token을 검증하고 SecurityContext에 인증 정보를 설정하는 Filter
 * <p>
 * Component 어노테이션을 붙이면 서블릿 필터 체인에도 자동 등록되므로,
 * SecurityConfig에서 직접 생성해 등록
 */
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    private static final String BEARER_PREFIX = "Bearer ";

    // 토큰 검증 실패 원인을 JwtAuthenticationEntryPoint에 전달하는 request 속성 키
    public static final String ERROR_CODE_ATTRIBUTE = "jwtErrorCode";

    private final JwtProvider jwtProvider;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        // 1. Authorization 헤더에서 토큰 추출
        String token = resolveToken(request);

        // 2. 토큰이 없으면 인증 정보 설정하지 않고 그대로 통과
        if (token != null) {
            try {
                // 3. 토큰 검증 성공 -> SecurityContext에 인증 정보 저장
                Claims claims = jwtProvider.parseAccessToken(token);

                // 기존 Context를 수정하지 않고 새로 만들어 교체 (스레드 간 경합 방지)
                SecurityContext context = SecurityContextHolder.createEmptyContext();
                context.setAuthentication(toAuthentication(claims));
                SecurityContextHolder.setContext(context);

            } catch (JwtAuthenticationException e) {
                // 4. 토큰 검증 실패 -> 인증 정보 비우고 request에 에러 코드만 담음 (EntryPoint가 처리)
                SecurityContextHolder.clearContext();
                request.setAttribute(ERROR_CODE_ATTRIBUTE, e.getCode());
            }
        }

        // 5. 인증 성공 여부와 상관없이 다음 필터로 전달
        //    요청 차단은 AuthorizationFilter가 진행
        filterChain.doFilter(request, response);
    }

    // Authorization 헤더에서 "Bearer " 접두사 떼고 토큰만 추출
    private String resolveToken(HttpServletRequest request) {
        String header = request.getHeader(HttpHeaders.AUTHORIZATION);

        if (header == null || !header.startsWith(BEARER_PREFIX)) {
            return null;
        }
        return header.substring(BEARER_PREFIX.length());
    }

    // 검증된 Claims -> Spring Security 인증 객체
    private Authentication toAuthentication(Claims claims) {
        Long memberId = jwtProvider.getMemberId(claims);
        String role = jwtProvider.getRole(claims);

        // Spring Security의 hasRole()은 "ROLE_" 접두사가 붙은 권한을 찾음
        List<SimpleGrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("ROLE_" + role));

        // 인증 완료된 객체 생성
        return new UsernamePasswordAuthenticationToken(memberId, null, authorities);
    }
}
