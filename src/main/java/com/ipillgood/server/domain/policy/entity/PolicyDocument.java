package com.ipillgood.server.domain.policy.entity;

import com.ipillgood.server.domain.policy.entity.enums.PolicyDocumentType;
import com.ipillgood.server.global.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

// 약관/정책 문서
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
        name = "policy_document",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_policy_document",
                columnNames = {"document_type", "version"}
        )
)
public class PolicyDocument extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "document_type", nullable = false, length = 50)
    private PolicyDocumentType documentType;

    @Column(name = "title", nullable = false, length = 100)
    private String title;

    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(name = "required", nullable = false)
    private boolean required;

    @Column(name = "version", nullable = false, length = 30)
    private String version;

    @Column(name = "effective_at", nullable = false)
    private LocalDateTime effectiveAt;

    @Column(name = "active", nullable = false)
    private boolean active = true;

    @Builder
    private PolicyDocument(PolicyDocumentType documentType, String title, String content,
                          boolean required, String version, LocalDateTime effectiveAt, boolean active) {
        this.documentType = documentType;
        this.title = title;
        this.content = content;
        this.required = required;
        this.version = version;
        this.effectiveAt = effectiveAt;
        this.active = active;
    }
}
