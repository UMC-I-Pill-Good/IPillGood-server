package com.ipillgood.server.global.config;

import com.ipillgood.server.global.security.jwt.JwtAuthFilter;
import com.ipillgood.server.global.security.jwt.JwtAuthenticationEntryPoint;
import com.ipillgood.server.global.security.jwt.JwtProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtProvider jwtProvider;
    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

    // 인증 없이 접근 허용 API
    private final String[] allowUris = {
            "/api/v1/auth/signup",
            "/api/v1/auth/login",
            "/api/v1/auth/reissue",
            "/api/v1/auth/check-username",
            "/api/v1/auth/check-email"
    };

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // CSRF 비활성화
                .csrf(AbstractHttpConfigurer::disable)

                // 기본 폼 로그인 방식 및 http basic 인증 비활성화
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)

                // JWT 사용하므로 stateless 설정
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                // URL별 권한 설정
                .authorizeHttpRequests(auth -> auth
                        // swagger 관련 URL
                        .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()
                        // 접근 허용 API
                        .requestMatchers(allowUris).permitAll()
                        // 나머지 모든 경로는 인증 필요
                        .anyRequest().authenticated()
                )

                // 인증 실패 시 401 응답 처리
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint(jwtAuthenticationEntryPoint)
                )

                // JWT 인증 후 인가 검사(AuthorizationFilter) 진행
                .addFilterBefore(new JwtAuthFilter(jwtProvider), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    // 비밀번호는 BCrypt로 암호화하여 저장
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
