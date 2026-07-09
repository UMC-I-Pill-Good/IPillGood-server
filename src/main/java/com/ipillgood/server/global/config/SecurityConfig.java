package com.ipillgood.server.global.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    // 인증 없이 접근 허용 API
    private final String[] allowUris = {
            "/api/v1/auth/signup",
            "/api/v1/auth/login",
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
                );

        return http.build();
    }

    // 비밀번호는 BCrypt로 암호화하여 저장
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
