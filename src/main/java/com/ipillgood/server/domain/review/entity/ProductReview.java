package com.ipillgood.server.domain.review.entity;

import com.ipillgood.server.domain.member.entity.Member;
import com.ipillgood.server.domain.product.entity.Product;
import com.ipillgood.server.global.entity.BaseSoftDeleteEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.util.ArrayList;
import java.util.List;

// 상품 후기
// 삭제되지 않은 후기만 중복 방지
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
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

    @Column(name = "rating", nullable = false)
    private Short rating;

    @Column(name = "content", nullable = false, length = 300)
    private String content;

    @Column(name = "helpful_count", nullable = false)
    @Builder.Default
    private int helpfulCount = 0;

    @OneToMany(mappedBy = "review", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("displayOrder ASC")
    @Builder.Default
    List<ProductReviewImage> reviewImages = new ArrayList<>();

    public void addPhoto(String imageKey){
        reviewImages.add(ProductReviewImage.builder()
                .review(this)
                .imageKey(imageKey)
                .displayOrder((short)(reviewImages.size() + 1))
                .build()
        );
    }

    public void updateContent(Short rating, String content) {
        this.rating = rating;
        this.content = content;
    }

    public void clearPhotos() {
        reviewImages.clear();
    }

    public void increaseHelpfulCount() {
        this.helpfulCount++;
    }
}
