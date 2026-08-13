package com.ipillgood.server.domain.notification.entity;

import com.ipillgood.server.domain.member.entity.Member;
import com.ipillgood.server.global.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

// 회원 알림 설정
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "member_notification_setting")
public class MemberNotificationSetting extends BaseEntity {

    @Id
    @Column(name = "member_id")
    private Long memberId;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "member_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Member member;

    @Column(name = "push_enabled", nullable = false)
    private boolean pushEnabled = false;

    @Column(name = "intake_push_enabled", nullable = false)
    private boolean intakePushEnabled = true;

    private MemberNotificationSetting(Member member) {
        this.member = member;
        this.pushEnabled = false;
        this.intakePushEnabled = true;
    }

    public static MemberNotificationSetting createDefault(Member member) {
        return new MemberNotificationSetting(member);
    }

    public void changePushEnabled(boolean pushEnabled) {
        this.pushEnabled = pushEnabled;
    }

    public void changeIntakePushEnabled(boolean intakePushEnabled) {
        this.intakePushEnabled = intakePushEnabled;
    }
}
