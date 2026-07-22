package com.ipillgood.server.domain.review.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

public class ReviewRequest {

    @Schema(description = "후기 이미지 업로드 URL 발급 요청")
    public record ImagePresign(
            @Schema(description = "업로드할 이미지들의 content-type 목록",
                    example = "[\"image/jpeg\", \"image/png\"]")
            List<String> contentTypes
    ) {
    }
}
