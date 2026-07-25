package com.ipillgood.server.domain.member.entity;

import com.ipillgood.server.domain.member.entity.enums.MemberStatus;
import com.ipillgood.server.global.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Locale;

// 회원 기본 정보
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "member")
public class Member extends BaseEntity {

    // 기본 프로필 이미지 키
    private static final String DEFAULT_PROFILE_IMAGE_KEY = "mascot-default";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 10)
    private String nickname;

    @Column(unique = true, length = 10)
    private String username;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(length = 60)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MemberStatus status;

    @Column(name = "profile_image_key", nullable = false)
    private String profileImageKey;

    @Column(name = "onboarding_completed_at")
    private LocalDateTime onboardingCompletedAt;

    @Column(name = "last_login_at")
    private LocalDateTime lastLoginAt;

    @Builder
    public Member(String nickname, String username, String email, String password, Role role, MemberStatus status,
                  String profileImageKey) {
        this.nickname = nickname;
        this.username = username;
        this.email = normalizeEmail(email);
        this.password = password;
        this.role = role == null ? Role.USER : role;
        this.status = status == null ? MemberStatus.ACTIVE : status;
        this.profileImageKey = profileImageKey == null ? DEFAULT_PROFILE_IMAGE_KEY : profileImageKey;
    }

    // 최초 설문 기반 추천 성공 시 온보딩 완료 처리
    public void completeOnboarding(LocalDateTime completedAt) {
        this.onboardingCompletedAt = completedAt;
    }

    // 프로필 관리 화면에서 닉네임 변경
    public void updateNickname(String nickname) {
        this.nickname = nickname;
    }

    // 비밀번호 변경 화면에서 실행 (인코딩된 값을 전달받음)
    public void changePassword(String encodedPassword) {
        this.password = encodedPassword;
    }

    // 소셜 전용 계정 여부 (소셜 회원가입은 비밀번호를 받지 않으므로 비밀번호가 없음)
    public boolean isSocialOnly() {
        return password == null;
    }

    /**
     * 이메일 정규화 (소문자 통일)
     * PostgreSQL은 기본적으로 문자열을 대소문자 구분하기 때문
     */
    public static String normalizeEmail(String email) {
        return email == null ? null : email.toLowerCase(Locale.ROOT);
    }
}
