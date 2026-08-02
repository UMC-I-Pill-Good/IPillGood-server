package com.ipillgood.server.domain.review.dto;

import com.ipillgood.server.domain.review.entity.enums.ProductReviewSort;
import com.ipillgood.server.global.enums.AgeGroup;
import com.ipillgood.server.global.enums.Gender;
import com.ipillgood.server.global.s3.dto.PresignedUpload;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.List;

public class ProductReviewResponse {

    @Schema(description = "후기 이미지 업로드 URL 발급 응답")
    public record ImagePresigns(
            @Schema(description = "발급된 presigned 업로드 정보 목록")
            List<PresignedUpload> images
    ) {
    }

    public record ReviewSummary(
            Double ratingAverage,
            Integer reviewCount
    ) {}

    @Schema(description = "상품 후기 목록 조회 응답")
    @Builder
    public record ProductReviews(
            @Schema(description = "영양제 상품 ID", example = "101")
            Long productId,

            @Schema(description = "삭제되지 않은 전체 후기 수", example = "42")
            Integer reviewCount,

            @Schema(description = "평균 별점(소수 둘째 자리에서 반올림). 후기가 없으면 0.0", example = "4.5")
            Double ratingAverage,

            @Schema(description = "조회에 적용된 정렬 기준", example = "LATEST")
            ProductReviewSort sort,

            @Schema(description = "페이지 크기", example = "20")
            Integer size,

            @Schema(description = "다음 페이지 존재 여부", example = "true")
            Boolean hasNext,

            @Schema(description = "다음 커서. hasNext가 false면 null",
                    nullable = true, example = "42|2026-07-20T15:00:00")
            String nextCursor,

            @Schema(description = "후기 목록")
            List<Review> reviews
    ) {

        @Schema(description = "상품 후기 항목")
        @Builder
        public record Review(
                @Schema(description = "후기 ID", example = "42")
                Long reviewId,

                @Schema(description = "작성자 닉네임", example = "약먹는곰")
                String nickname,

                @Schema(description = "작성자 프로필 이미지 URL", nullable = true)
                String profileImageUrl,

                @Schema(description = "작성자 연령대. 설문 미응답 회원은 null", nullable = true, example = "TWENTIES")
                AgeGroup ageGroup,

                @Schema(description = "작성자 성별. 설문 미응답 회원은 null", nullable = true, example = "FEMALE")
                Gender gender,

                @Schema(description = "별점", example = "5")
                Integer rating,

                @Schema(description = "후기 내용", example = "먹고 나서 컨디션이 좋아졌어요.")
                String content,

                @Schema(description = "후기 첨부 이미지 URL 목록")
                List<String> reviewImageUrls,

                @Schema(description = "도움됨 수", example = "12")
                Integer helpfulCount,

                @Schema(description = "요청한 회원이 도움됨을 눌렀는지 여부", example = "false")
                Boolean helpedByMe,

                @Schema(description = "요청한 회원이 작성한 후기인지 여부", example = "false")
                Boolean mine,

                @Schema(description = "작성 일시", example = "2026-07-20T15:00:00")
                LocalDateTime createdAt
        ){}
    }

    @Schema(description = "후기 등록 응답")
    @Builder
    public record ReviewCreate(
            @Schema(description = "등록된 후기 ID", example = "42")
            Long reviewId,

            @Schema(description = "영양제 상품 ID", example = "101")
            Long productId,

            @Schema(description = "별점", example = "5")
            Short rating,

            @Schema(description = "후기 내용", example = "먹고 나서 컨디션이 좋아졌어요.")
            String content,

            @Schema(description = "첨부 이미지 URL 목록 (요청한 key 순서)")
            List<String> imageUrls,

            @Schema(description = "도움됨 수. 등록 직후에는 항상 0", example = "0")
            Integer helpfulCount,

            @Schema(description = "작성 일시", example = "2026-07-20T15:00:00")
            LocalDateTime createdAt
    ) {}

    @Schema(description = "후기 수정 응답")
    @Builder
    public record ReviewUpdate(
            @Schema(description = "수정된 후기 ID", example = "1")
            Long reviewId,

            @Schema(description = "별점", example = "4")
            Short rating,

            @Schema(description = "후기 내용", example = "내용을 수정합니다.")
            String content,

            @Schema(description = "수정 후 첨부된 이미지 URL 목록", example = "[]")
            List<String> imageUrls,

            @Schema(description = "수정 일시", example = "2026-07-20T15:20:00")
            LocalDateTime updatedAt
    ) {}

    @Builder
    @Schema(description = "후기 삭제 응답")
    public record ReviewDelete(
            @Schema(description = "삭제 여부", example = "true")
            boolean deleted,

            @Schema(description = "삭제된 후기 ID", example = "1")
            Long reviewId
    ) {}
}
