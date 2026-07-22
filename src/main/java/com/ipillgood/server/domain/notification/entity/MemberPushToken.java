package com.ipillgood.server.domain.notification.entity;

import com.ipillgood.server.domain.member.entity.Member;
import com.ipillgood.server.domain.notification.entity.enums.PushPlatform;
import com.ipillgood.server.global.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

// 회원 푸시 토큰
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "member_push_token")
public class MemberPushToken extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Enumerated(EnumType.STRING)
    @Column(name = "platform", nullable = false)
    private PushPlatform platform;

    @Column(name = "token", nullable = false, unique = true, length = 512)
    private String token;

    @Column(name = "active", nullable = false)
    private boolean active = true;

    @Column(name = "last_seen_at")
    private LocalDateTime lastSeenAt;

    private MemberPushToken(Member member, PushPlatform platform, String token, LocalDateTime lastSeenAt) {
        this.member = member;
        this.platform = platform;
        this.token = token;
        this.active = true;
        this.lastSeenAt = lastSeenAt;
    }

    public static MemberPushToken create(
            Member member,
            PushPlatform platform,
            String token,
            LocalDateTime lastSeenAt
    ) {
        return new MemberPushToken(member, platform, token, lastSeenAt);
    }

    public void renew(Member member, PushPlatform platform, LocalDateTime lastSeenAt) {
        this.member = member;
        this.platform = platform;
        this.active = true;
        this.lastSeenAt = lastSeenAt;
    }
}
