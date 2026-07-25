package com.ipillgood.server.domain.notification.entity;

import com.ipillgood.server.domain.member.entity.Member;
import com.ipillgood.server.domain.notification.entity.enums.NotificationDeliveryStatus;
import com.ipillgood.server.domain.notification.entity.enums.NotificationType;
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
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.LocalDateTime;

// 푸시 발송 이력
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
        name = "notification_delivery_log",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_notification_delivery_log_token_type_scheduled",
                columnNames = {"member_push_token_id", "notification_type", "scheduled_at"}
        )
)
public class NotificationDeliveryLog extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_push_token_id")
    private MemberPushToken memberPushToken;

    @Enumerated(EnumType.STRING)
    @Column(name = "notification_type", nullable = false)
    private NotificationType notificationType;

    @Column(name = "title", length = 100)
    private String title;

    @Column(name = "body", nullable = false, length = 500)
    private String body;

    @Column(name = "target_route", length = 100)
    private String targetRoute;

    @Column(name = "scheduled_at", nullable = false)
    private LocalDateTime scheduledAt;

    @Column(name = "sent_at")
    private LocalDateTime sentAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private NotificationDeliveryStatus status;

    @Column(name = "retry_count", nullable = false)
    private short retryCount = 0;

    @Column(name = "failure_reason", columnDefinition = "TEXT")
    private String failureReason;

    private NotificationDeliveryLog(
            Member member,
            MemberPushToken memberPushToken,
            NotificationType notificationType,
            String title,
            String body,
            String targetRoute,
            LocalDateTime scheduledAt
    ) {
        this.member = member;
        this.memberPushToken = memberPushToken;
        this.notificationType = notificationType;
        this.title = title;
        this.body = body;
        this.targetRoute = targetRoute;
        this.scheduledAt = scheduledAt;
        this.status = NotificationDeliveryStatus.PENDING;
        this.retryCount = 0;
    }

    public static NotificationDeliveryLog createPending(
            Member member,
            MemberPushToken memberPushToken,
            NotificationType notificationType,
            String title,
            String body,
            String targetRoute,
            LocalDateTime scheduledAt
    ) {
        return new NotificationDeliveryLog(
                member,
                memberPushToken,
                notificationType,
                title,
                body,
                targetRoute,
                scheduledAt
        );
    }

    public void markSent(LocalDateTime sentAt, short retryCount) {
        this.sentAt = sentAt;
        this.status = NotificationDeliveryStatus.SENT;
        this.retryCount = retryCount;
        this.failureReason = null;
    }

    public void markFailed(String failureReason) {
        this.status = NotificationDeliveryStatus.FAILED;
        this.retryCount = 0;
        this.failureReason = failureReason;
    }

    public void markRetryFailed(String failureReason) {
        this.status = NotificationDeliveryStatus.RETRY_FAILED;
        this.retryCount = 1;
        this.failureReason = failureReason;
    }
}
