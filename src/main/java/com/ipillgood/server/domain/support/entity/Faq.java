package com.ipillgood.server.domain.support.entity;

import com.ipillgood.server.domain.support.entity.enums.FaqCategory;
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

// FAQ 콘텐츠
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "faq")
public class Faq extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "category", nullable = false)
    private FaqCategory category;

    @Column(name = "question", nullable = false)
    private String question;

    @Column(name = "answer", nullable = false, columnDefinition = "TEXT")
    private String answer;

    @Column(name = "display_order", nullable = false)
    private Integer displayOrder;

    @Column(name = "active", nullable = false)
    private boolean active = true;

    @Builder
    public Faq(FaqCategory category, String question, String answer, Integer displayOrder, boolean active) {
        this.category = category;
        this.question = question;
        this.answer = answer;
        this.displayOrder = displayOrder;
        this.active = active;
    }

    // 관리자 FAQ 수정 - 질문/답변/카테고리 갱신 (노출 순서, 활성화 여부는 그대로 유지)
    public void updateContent(FaqCategory category, String question, String answer) {
        this.category = category;
        this.question = question;
        this.answer = answer;
    }
}
