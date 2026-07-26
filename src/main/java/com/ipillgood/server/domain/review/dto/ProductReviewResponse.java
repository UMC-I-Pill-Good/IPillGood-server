package com.ipillgood.server.domain.review.dto;

import com.ipillgood.server.global.s3.dto.PresignedUpload;
import io.swagger.v3.oas.annotations.media.Schema;
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
}
