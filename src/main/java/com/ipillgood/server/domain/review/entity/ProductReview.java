package com.ipillgood.server.domain.review.entity;

import com.ipillgood.server.domain.member.entity.Member;
import com.ipillgood.server.domain.product.entity.Product;
import com.ipillgood.server.global.entity.BaseSoftDeleteEntity;
import com.ipillgood.server.global.enums.AgeGroup;
import com.ipillgood.server.global.enums.Gender;
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
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

// 상품 후기
// 삭제되지 않은 후기만 중복 방지
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "product_review")
public class ProductReview extends BaseSoftDeleteEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Member member;

    @Enumerated(EnumType.STRING)
    @Column(name = "reviewer_age_group", nullable = false)
    private AgeGroup reviewerAgeGroup;

    @Enumerated(EnumType.STRING)
    @Column(name = "reviewer_gender", nullable = false)
    private Gender reviewerGender;

    @Column(name = "rating", nullable = false)
    private Short rating;

    @Column(name = "content", nullable = false, length = 300)
    private String content;

    @Column(name = "helpful_count", nullable = false)
    private int helpfulCount = 0;
}
