package com.ipillgood.server.domain.review.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

import java.util.List;

public class ProductReviewRequest {

    @Schema(description = "후기 이미지 업로드 URL 발급 요청")
    public record ImagePresign(
            @Schema(description = "업로드할 이미지들의 content-type 목록 (최대 3개)",
                    example = "[\"image/jpeg\", \"image/png\"]")
            @NotEmpty(message = "업로드할 이미지 정보가 없습니다.")
            @Size(max = 3, message = "이미지는 최대 3개까지 업로드할 수 있습니다.")
            List<String> contentTypes
    ) {
    }

    @Schema(description = "후기 등록 요청")
    public record Review(
            @Schema(description = "별점 (1~5)", example = "5")
            @NotNull(message = "평점을 입력해주세요.")
            @Min(value = 1, message = "1점 이상의 평점을 입력해주세요.")
            @Max(value = 5, message = "5점 이하의 평점을 입력해주세요.")
            Short rating,

            @Schema(description = "후기 내용 (최대 300자)", example = "먹고 나서 컨디션이 좋아졌어요.")
            @Size(max = 300, message = "리뷰 내용은 300자를 넘을 수 없습니다.")
            @NotBlank(message = "리뷰 내용을 입력해주세요.")
            String content,

            @Schema(description = "업로드 URL 발급 API로 받은 이미지 key 목록 (최대 3개). "
                    + "배열 순서가 후기 이미지 노출 순서가 됩니다.",
                    example = "[\"reviews/3f2a9c1e-0b4d-4a2f-9c3e-1a2b3c4d5e6f.jpg\"]")
            @Size(max = 3, message = "이미지는 최대 3개까지 첨부할 수 있습니다.")
            List<String> imageKeys
    ) {}

    @Schema(description = "후기 수정 요청")
    public record ReviewUpdate(
            @Schema(description = "별점 (1~5)", example = "4")
            @NotNull(message = "평점을 입력해주세요.")
            @Min(value = 1, message = "1점 이상의 평점을 입력해주세요.")
            @Max(value = 5, message = "5점 이하의 평점을 입력해주세요.")
            Short rating,

            @Schema(description = "후기 내용 (최대 300자)", example = "내용을 수정합니다.")
            @Size(max = 300, message = "리뷰 내용은 300자를 넘을 수 없습니다.")
            @NotBlank(message = "리뷰 내용을 입력해주세요.")
            String content,

            @Schema(description = "수정 후 유지할 이미지 key 목록 (최대 3개). "
                    + "전달한 목록으로 기존 이미지를 전부 대체하며, 빈 배열이면 모든 이미지가 삭제됩니다.",
                    example = "[]")
            @Size(max = 3, message = "이미지는 최대 3개까지 첨부할 수 있습니다.")
            List<String> imageKeys
    ) {}
}
