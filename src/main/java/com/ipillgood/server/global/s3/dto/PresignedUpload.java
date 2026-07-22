package com.ipillgood.server.global.s3.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "presigned 업로드 정보")
public record PresignedUpload(

        @Schema(description = "S3 업로드용 presigned PUT URL",
                example = "https://ipillgood-bucket.s3.ap-northeast-2.amazonaws.com/reviews/uuid.jpg?X-Amz-...")
        String uploadUrl,

        @Schema(description = "업로드 후 서버에 전달할 이미지 키", example = "reviews/uuid.jpg")
        String key
) {
}
