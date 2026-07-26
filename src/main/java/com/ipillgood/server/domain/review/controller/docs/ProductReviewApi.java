package com.ipillgood.server.domain.review.controller.docs;

import com.ipillgood.server.domain.review.dto.ProductReviewRequest;
import com.ipillgood.server.domain.review.dto.ProductReviewResponse;
import com.ipillgood.server.global.apiPayload.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Review API", description = "후기 관련 API")
public interface ProductReviewApi {

    @Operation(
            summary = "후기 이미지 업로드 URL 발급",
            description = "후기에 첨부할 이미지를 S3에 직접 업로드하기 위한 presigned PUT URL을 발급합니다. "
                    + "클라이언트는 올릴 이미지들의 content-type 목록을 전달하고, 응답의 uploadUrl에 "
                    + "이미지 raw 바이너리를 (요청과 동일한 Content-Type 헤더로) PUT한 뒤, "
                    + "후기 등록 시 key 목록을 함께 전송합니다. 지원 형식: image/jpeg, image/png, image/webp."
    )
    @SecurityRequirement(name = "JWT TOKEN")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "업로드 URL 발급 성공",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "isSuccess": true,
                                              "code": "REVIEW200_1",
                                              "message": "후기 이미지 업로드 URL 발급에 성공했습니다.",
                                              "result": {
                                                "images": [
                                                  {
                                                    "uploadUrl": "https://ipillgood-bucket.s3.ap-northeast-2.amazonaws.com/reviews/3f2a9c1e-0b4d-4a2f-9c3e-1a2b3c4d5e6f.jpg?X-Amz-Algorithm=AWS4-HMAC-SHA256&X-Amz-Expires=300&X-Amz-Signature=...",
                                                    "key": "reviews/3f2a9c1e-0b4d-4a2f-9c3e-1a2b3c4d5e6f.jpg"
                                                  }
                                                ]
                                              }
                                            }
                                            """
                            )
                    )
            )
    })
    ApiResponse<ProductReviewResponse.ImagePresigns> createImageUploadUrls(
            @Parameter(hidden = true)
            Long memberId,
            ProductReviewRequest.ImagePresign request
    );
}
