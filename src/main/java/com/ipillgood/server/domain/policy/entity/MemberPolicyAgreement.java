package com.ipillgood.server.domain.policy.entity;

import com.ipillgood.server.domain.member.entity.Member;
import com.ipillgood.server.global.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.LocalDateTime;

// 회원 약관 동의 이력
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
        name = "member_policy_agreement",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_member_policy_agreement",
                columnNames = {"member_id", "policy_document_id"}
        )
)
public class MemberPolicyAgreement extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "policy_document_id", nullable = false)
    private PolicyDocument policyDocument;

    @Column(name = "agreed", nullable = false)
    private boolean agreed;

    @Column(name = "agreed_at")
    private LocalDateTime agreedAt;

    @Builder(access = AccessLevel.PRIVATE)
    private MemberPolicyAgreement(Member member, PolicyDocument policyDocument, boolean agreed, LocalDateTime agreedAt) {
        this.member = member;
        this.policyDocument = policyDocument;
        this.agreed = agreed;
        this.agreedAt = agreedAt;
    }

    // 동의 이력 생성 (동의한 약관만 동의 일시 기록)
    public static MemberPolicyAgreement of(Member member, PolicyDocument policyDocument, boolean agreed) {
        return MemberPolicyAgreement.builder()
                .member(member)
                .policyDocument(policyDocument)
                .agreed(agreed)
                .agreedAt(agreed ? LocalDateTime.now() : null)
                .build();
    }
}
