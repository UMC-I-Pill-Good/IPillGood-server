package com.ipillgood.server.domain.review.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import java.util.List;

public class ReviewRequest {

    @Schema(description = "후기 이미지 업로드 URL 발급 요청")
    public record ImagePresign(
            @Schema(description = "업로드할 이미지들의 content-type 목록 (최대 3개)",
                    example = "[\"image/jpeg\", \"image/png\"]")
            @NotEmpty(message = "업로드할 이미지 정보가 없습니다.")
            @Size(max = 3, message = "이미지는 최대 3개까지 업로드할 수 있습니다.")
            List<String> contentTypes
    ) {
    }
}
