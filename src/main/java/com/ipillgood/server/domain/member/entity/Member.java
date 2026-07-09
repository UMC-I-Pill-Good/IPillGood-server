package com.ipillgood.server.domain.member.entity;

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

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "member")
public class Member extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 10)
    private String nickname;

    // 회원가입 시 '아이디' 필드
    @Column(nullable = false, unique = true, length = 10)
    private String username;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false, length = 60)  // BCrypt 고정 60자 해시 문자열
    private String password;

    // 관리자 권한 부여 로직은 추후 구현
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    @Builder
    public Member(String nickname, String username, String email, String password, Role role) {
        this.nickname = nickname;
        this.username = username;
        this.email = email;
        this.password = password;
        this.role = role;
    }
}
