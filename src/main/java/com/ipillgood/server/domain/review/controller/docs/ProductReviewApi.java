package com.ipillgood.server.domain.review.controller.docs;

import com.ipillgood.server.domain.review.dto.ProductReviewRequest;
import com.ipillgood.server.domain.review.dto.ProductReviewResponse;
import com.ipillgood.server.domain.review.entity.enums.ProductReviewSort;
import com.ipillgood.server.global.apiPayload.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

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
            @Valid ProductReviewRequest.ImagePresign request
    );

    @Operation(
            summary = "상품 후기 목록 조회",
            description = "특정 영양제 상품의 후기를 커서 페이지네이션으로 조회합니다. "
    )
    @SecurityRequirement(name = "JWT TOKEN")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "후기 목록 조회 성공",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "isSuccess": true,
                                              "code": "REVIEW200_2",
                                              "message": "해당 상품 리뷰 목록 조회에 성공했습니다.",
                                              "result": {
                                                "productId": 101,
                                                "reviewCount": 42,
                                                "ratingAverage": 4.5,
                                                "sort": "LATEST",
                                                "size": 20,
                                                "hasNext": true,
                                                "nextCursor": "23|2026-07-18T09:12:00",
                                                "reviews": [
                                                  {
                                                    "reviewId": 42,
                                                    "nickname": "약먹는곰",
                                                    "profileImageUrl": "https://cdn.ipillgood.com/profileImage/profile1.png",
                                                    "ageGroup": "TWENTIES",
                                                    "gender": "FEMALE",
                                                    "rating": 5,
                                                    "content": "먹고 나서 컨디션이 좋아졌어요.",
                                                    "reviewImageUrls": [
                                                      "https://cdn.ipillgood.com/reviews/3f2a9c1e-0b4d-4a2f-9c3e-1a2b3c4d5e6f.jpg"
                                                    ],
                                                    "helpfulCount": 12,
                                                    "helpedByMe": false,
                                                    "mine": false,
                                                    "createdAt": "2026-07-20T15:00:00"
                                                  }
                                                ]
                                              }
                                            }
                                            """
                            )
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "파라미터 형식/제약 오류(COMMON400_1) 또는 커서 오류(COMMON400_4)",
                    content = @Content(
                            mediaType = "application/json",
                            examples = {
                                    @ExampleObject(
                                            name = "파라미터 형식/제약 오류",
                                            value = """
                                                    {
                                                      "isSuccess": false,
                                                      "code": "COMMON400_1",
                                                      "message": "잘못된 요청입니다.",
                                                      "result": {
                                                        "size": "1에서 100 사이여야 합니다"
                                                      }
                                                    }
                                                    """
                                    ),
                                    @ExampleObject(
                                            name = "커서 오류",
                                            value = """
                                                    {
                                                      "isSuccess": false,
                                                      "code": "COMMON400_4",
                                                      "message": "유효하지 않은 커서 값입니다.",
                                                      "result": null
                                                    }
                                                    """
                                    )
                            }
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "존재하지 않거나 삭제된 상품",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "isSuccess": false,
                                              "code": "PRODUCT404_1",
                                              "message": "해당 상품은 존재하지 않습니다.",
                                              "result": null
                                            }
                                            """
                            )
                    )
            )
    })
    ApiResponse<ProductReviewResponse.ProductReviews> getReviews(
            @Parameter(hidden = true)
            Long memberId,

            @Parameter(
                    description = "영양제 상품 ID입니다.",
                    example = "101"
            )
            Long productId,
            @Parameter(
                    description = "정렬 기준입니다. 기본값 LATEST.",
                    example = "LATEST"
            )
            ProductReviewSort sort,
            @Parameter(
                    description = "페이지 크기입니다. 1~100, 기본값 20.",
                    example = "20"
            )
            @Min(1) @Max(100) Integer size,
            @Parameter(
                    description = "커서입니다. 이전 응답의 nextCursor 값({후기ID}|{정렬값})을 그대로 전달하며, "
                            + "생략하면 첫 페이지를 조회합니다. "
                            + "정렬값은 LATEST면 최신순, LIKE_COUNT_DESC면 좋아요순입니다. "
                            + "sort를 바꾸면 커서 없이 처음부터 조회하세요.",
                    example = "23|2026-07-18T09:12:00"
            )
            String cursor
    );

    @Operation(
            summary = "상품 후기 등록",
            description = "특정 영양제 상품에 후기를 등록합니다. "
                    + "한 회원은 한 상품에 후기를 하나만 등록할 수 있으며, 기존 후기를 삭제한 뒤에는 다시 등록할 수 있습니다. "
                    + "이미지는 최대 3개까지 첨부할 수 있고, 업로드 URL 발급 API로 받은 key를 배열 순서대로 전달하면 "
                    + "그 순서가 후기 이미지의 노출 순서가 됩니다. "
                    + "key는 후기 이미지 디렉터리에 실제로 업로드된 것이어야 하며, 그렇지 않으면 등록이 거부됩니다."
    )
    @SecurityRequirement(name = "JWT TOKEN")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "201",
                    description = "후기 등록 성공",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "isSuccess": true,
                                              "code": "REVIEW201_1",
                                              "message": "해당 영양제에 대한 리뷰가 등록되었습니다.",
                                              "result": {
                                                "reviewId": 42,
                                                "productId": 101,
                                                "rating": 5,
                                                "content": "먹고 나서 컨디션이 좋아졌어요.",
                                                "imageUrls": [
                                                  "https://cdn.ipillgood.com/reviews/3f2a9c1e-0b4d-4a2f-9c3e-1a2b3c4d5e6f.jpg"
                                                ],
                                                "helpfulCount": 0,
                                                "createdAt": "2026-07-20T15:00:00"
                                              }
                                            }
                                            """
                            )
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "요청 값 검증 실패(COMMON400_1) 또는 후기 이미지 디렉터리가 아닌 키 전달(S3400_3)",
                    content = @Content(
                            mediaType = "application/json",
                            examples = {
                                    @ExampleObject(
                                            name = "요청 값 검증 실패",
                                            value = """
                                                    {
                                                      "isSuccess": false,
                                                      "code": "COMMON400_1",
                                                      "message": "잘못된 요청입니다.",
                                                      "result": {
                                                        "rating": "평점을 입력해주세요.",
                                                        "content": "리뷰 내용을 입력해주세요."
                                                      }
                                                    }
                                                    """
                                    ),
                                    @ExampleObject(
                                            name = "유효하지 않은 이미지 키",
                                            value = """
                                                    {
                                                      "isSuccess": false,
                                                      "code": "S3400_3",
                                                      "message": "유효하지 않은 이미지 키입니다.",
                                                      "result": null
                                                    }
                                                    """
                                    )
                            }
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "존재하지 않거나 삭제된 상품(PRODUCT404_1), 존재하지 않는 회원(MEMBER404_1), "
                            + "또는 S3에 업로드되지 않은 이미지 키(S3404_1)",
                    content = @Content(
                            mediaType = "application/json",
                            examples = {
                                    @ExampleObject(
                                            name = "존재하지 않는 상품",
                                            value = """
                                                    {
                                                      "isSuccess": false,
                                                      "code": "PRODUCT404_1",
                                                      "message": "해당 상품은 존재하지 않습니다.",
                                                      "result": null
                                                    }
                                                    """
                                    ),
                                    @ExampleObject(
                                            name = "업로드되지 않은 이미지",
                                            value = """
                                                    {
                                                      "isSuccess": false,
                                                      "code": "S3404_1",
                                                      "message": "업로드되지 않은 이미지입니다.",
                                                      "result": null
                                                    }
                                                    """
                                    )
                            }
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "409",
                    description = "이미 해당 상품에 후기를 등록한 경우",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "isSuccess": false,
                                              "code": "REVIEW409_1",
                                              "message": "해당 상품에 이미 리뷰를 등록했습니다.",
                                              "result": null
                                            }
                                            """
                            )
                    )
            )
    })
    ApiResponse<ProductReviewResponse.ReviewCreate> createReview(
            @Parameter(hidden = true)
            Long memberId,
            @Parameter(
                    description = "영양제 상품 ID입니다.",
                    example = "101"
            )
            Long productId,
            @Valid ProductReviewRequest.Review reqDto
    );

    @Operation(
            summary = "상품 후기 수정",
            description = "본인이 작성한 후기의 별점·내용·첨부 이미지를 수정합니다. "
                    + "imageKeys는 부분 변경이 아니라 전체 대체입니다. "
                    + "수정 후 유지할 key를 원하는 노출 순서대로 모두 전달해야 하며, 빈 배열이면 모든 이미지가 삭제됩니다. "
                    + "key는 후기 이미지 디렉터리에 실제로 업로드된 것이어야 합니다."
    )
    @SecurityRequirement(name = "JWT TOKEN")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "후기 수정 성공",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "isSuccess": true,
                                              "code": "REVIEW200_3",
                                              "message": "후기 수정에 성공했습니다.",
                                              "result": {
                                                "reviewId": 1,
                                                "rating": 4,
                                                "content": "내용을 수정합니다.",
                                                "imageUrls": [],
                                                "updatedAt": "2026-07-20T15:20:00"
                                              }
                                            }
                                            """
                            )
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "요청 값 검증 실패(COMMON400_1) 또는 후기 이미지 디렉터리가 아닌 키 전달(S3400_3)",
                    content = @Content(
                            mediaType = "application/json",
                            examples = {
                                    @ExampleObject(
                                            name = "요청 값 검증 실패",
                                            value = """
                                                    {
                                                      "isSuccess": false,
                                                      "code": "COMMON400_1",
                                                      "message": "잘못된 요청입니다.",
                                                      "result": {
                                                        "rating": "평점을 입력해주세요."
                                                      }
                                                    }
                                                    """
                                    ),
                                    @ExampleObject(
                                            name = "유효하지 않은 이미지 키",
                                            value = """
                                                    {
                                                      "isSuccess": false,
                                                      "code": "S3400_3",
                                                      "message": "유효하지 않은 이미지 키입니다.",
                                                      "result": null
                                                    }
                                                    """
                                    )
                            }
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "본인이 작성하지 않은 후기를 수정하려는 경우",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "isSuccess": false,
                                              "code": "REVIEW403_1",
                                              "message": "본인이 작성한 후기만 수정할 수 있습니다.",
                                              "result": null
                                            }
                                            """
                            )
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "존재하지 않거나 삭제된 후기(REVIEW404_1) 또는 업로드되지 않은 이미지 키(S3404_1)",
                    content = @Content(
                            mediaType = "application/json",
                            examples = {
                                    @ExampleObject(
                                            name = "존재하지 않는 후기",
                                            value = """
                                                    {
                                                      "isSuccess": false,
                                                      "code": "REVIEW404_1",
                                                      "message": "해당 후기가 존재하지 않습니다.",
                                                      "result": null
                                                    }
                                                    """
                                    ),
                                    @ExampleObject(
                                            name = "업로드되지 않은 이미지",
                                            value = """
                                                    {
                                                      "isSuccess": false,
                                                      "code": "S3404_1",
                                                      "message": "업로드되지 않은 이미지입니다.",
                                                      "result": null
                                                    }
                                                    """
                                    )
                            }
                    )
            )
    })
    ApiResponse<ProductReviewResponse.ReviewUpdate> updateReview(
            @Parameter(hidden = true)
            Long memberId,
            @Parameter(
                    description = "수정할 후기 ID입니다.",
                    example = "1"
            )
            Long reviewId,
            @Valid ProductReviewRequest.ReviewUpdate reqDto
    );

    @Operation(
            summary = "상품 후기 삭제",
            description = "본인이 작성한 후기를 삭제합니다. "
                    + "삭제된 후기는 목록과 평점 집계에서 즉시 제외되며, 같은 상품에 후기를 다시 등록할 수 있습니다. "
                    + "이미 삭제된 후기를 다시 삭제하면 404를 반환합니다."
    )
    @SecurityRequirement(name = "JWT TOKEN")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "후기 삭제 성공",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "isSuccess": true,
                                              "code": "REVIEW200_4",
                                              "message": "후기 삭제에 성공했습니다.",
                                              "result": {
                                                "deleted": true,
                                                "reviewId": 1
                                              }
                                            }
                                            """
                            )
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "본인이 작성하지 않은 후기를 삭제하려는 경우",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "isSuccess": false,
                                              "code": "REVIEW403_2",
                                              "message": "본인이 작성한 후기만 삭제할 수 있습니다.",
                                              "result": null
                                            }
                                            """
                            )
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "존재하지 않거나 이미 삭제된 후기",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "isSuccess": false,
                                              "code": "REVIEW404_1",
                                              "message": "해당 후기가 존재하지 않습니다.",
                                              "result": null
                                            }
                                            """
                            )
                    )
            )
    })
    ApiResponse<ProductReviewResponse.ReviewDelete> deleteReview(
            @Parameter(hidden = true)
            Long memberId,
            @Parameter(
                    description = "삭제할 후기 ID입니다.",
                    example = "1"
            )
            Long reviewId
    );

    @Operation(
            summary = "작성한 후기 단건 조회",
            description = "본인이 작성한 후기를 조회합니다. 후기 수정 화면 진입 시 폼을 채우는 용도입니다. "
                    + "imageKeys는 후기 수정 요청에 그대로 전달하면 되는 값이고, "
                    + "imageUrls는 같은 순서의 표시용 URL입니다. "
                    + "이미지를 바꾸지 않을 때는 imageKeys를 그대로 되돌려 보내면 재업로드가 필요 없습니다."
    )
    @SecurityRequirement(name = "JWT TOKEN")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "후기 조회 성공",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "isSuccess": true,
                                              "code": "REVIEW200_5",
                                              "message": "작성한 후기 조회에 성공했습니다.",
                                              "result": {
                                                "reviewId": 1,
                                                "productId": 101,
                                                "rating": 5,
                                                "content": "먹고 나서 컨디션이 좋아졌어요.",
                                                "imageKeys": [
                                                  "reviews/3f2a9c1e-0b4d-4a2f-9c3e-1a2b3c4d5e6f.jpg"
                                                ],
                                                "imageUrls": [
                                                  "https://cdn.ipillgood.com/reviews/3f2a9c1e-0b4d-4a2f-9c3e-1a2b3c4d5e6f.jpg"
                                                ],
                                                "createdAt": "2026-07-20T15:00:00",
                                                "updatedAt": "2026-07-20T15:20:00"
                                              }
                                            }
                                            """
                            )
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "본인이 작성하지 않은 후기를 조회하려는 경우",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "isSuccess": false,
                                              "code": "REVIEW403_3",
                                              "message": "본인이 작성한 후기만 조회할 수 있습니다.",
                                              "result": null
                                            }
                                            """
                            )
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "존재하지 않거나 삭제된 후기",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "isSuccess": false,
                                              "code": "REVIEW404_1",
                                              "message": "해당 후기가 존재하지 않습니다.",
                                              "result": null
                                            }
                                            """
                            )
                    )
            )
    })
    ApiResponse<ProductReviewResponse.ReviewDetail> getMyReview(
            @Parameter(hidden = true)
            Long memberId,
            @Parameter(
                    description = "조회할 후기 ID입니다.",
                    example = "1"
            )
            Long reviewId
    );

    @Operation(
            summary = "후기 도움됨 등록",
            description = "다른 회원이 작성한 후기에 도움됨을 표시합니다. "
                    + "한 회원은 같은 후기에 도움됨을 한 번만 표시할 수 있으며, 이미 표시한 상태에서 다시 요청하면 409를 반환합니다. "
                    + "본인이 작성한 후기에는 표시할 수 없습니다. "
                    + "응답의 helpfulCount는 이번 요청이 반영된 뒤의 값이므로, 목록 화면의 도움됨 수를 이 값으로 갱신하면 됩니다."
    )
    @SecurityRequirement(name = "JWT TOKEN")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "201",
                    description = "도움됨 등록 성공",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "isSuccess": true,
                                              "code": "REVIEW201_2",
                                              "message": "해당 리뷰에 도움됨을 등록했습니다.",
                                              "result": {
                                                "helpful": true,
                                                "reviewId": 1,
                                                "helpfulCount": 13
                                              }
                                            }
                                            """
                            )
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "본인이 작성한 후기에 도움됨을 표시하려는 경우",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "isSuccess": false,
                                              "code": "REVIEW403_4",
                                              "message": "본인이 작성한 후기에 도움됨을 누를 수 없습니다.",
                                              "result": null
                                            }
                                            """
                            )
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "존재하지 않거나 삭제된 후기(REVIEW404_1) 또는 존재하지 않는 회원(MEMBER404_1)",
                    content = @Content(
                            mediaType = "application/json",
                            examples = {
                                    @ExampleObject(
                                            name = "존재하지 않는 후기",
                                            value = """
                                                    {
                                                      "isSuccess": false,
                                                      "code": "REVIEW404_1",
                                                      "message": "해당 후기가 존재하지 않습니다.",
                                                      "result": null
                                                    }
                                                    """
                                    ),
                                    @ExampleObject(
                                            name = "존재하지 않는 회원",
                                            value = """
                                                    {
                                                      "isSuccess": false,
                                                      "code": "MEMBER404_1",
                                                      "message": "회원을 찾을 수 없습니다.",
                                                      "result": null
                                                    }
                                                    """
                                    )
                            }
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "409",
                    description = "이미 해당 후기에 도움됨을 표시한 경우",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "isSuccess": false,
                                              "code": "REVIEW409_2",
                                              "message": "이미 도움됨 이력을 표시한 리뷰입니다.",
                                              "result": null
                                            }
                                            """
                            )
                    )
            )
    })
    ApiResponse<ProductReviewResponse.ReviewHelpful> createHelpful(
            @Parameter(hidden = true)
            Long memberId,
            @Parameter(
                    description = "도움됨을 표시할 후기 ID입니다.",
                    example = "1"
            )
            Long reviewId
    );

    @Operation(
            summary = "후기 도움됨 취소",
            description = "후기에 표시한 도움됨을 취소합니다. "
                    + "도움됨 이력이 없는 상태에서 취소를 요청하면 404(REVIEW404_2)를 반환합니다. "
                    + "취소 후에는 같은 후기에 도움됨을 다시 표시할 수 있습니다. "
                    + "응답의 helpfulCount는 이번 요청이 반영된 뒤의 값이므로, 목록 화면의 도움됨 수를 이 값으로 갱신하면 됩니다."
    )
    @SecurityRequirement(name = "JWT TOKEN")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "도움됨 취소 성공",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "isSuccess": true,
                                              "code": "REVIEW200_6",
                                              "message": "해당 리뷰에 도움됨을 취소했습니다.",
                                              "result": {
                                                "helpful": false,
                                                "reviewId": 1,
                                                "helpfulCount": 12
                                              }
                                            }
                                            """
                            )
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "존재하지 않거나 삭제된 후기(REVIEW404_1), 도움됨 이력이 없는 경우(REVIEW404_2), "
                            + "또는 존재하지 않는 회원(MEMBER404_1)",
                    content = @Content(
                            mediaType = "application/json",
                            examples = {
                                    @ExampleObject(
                                            name = "존재하지 않는 후기",
                                            value = """
                                                    {
                                                      "isSuccess": false,
                                                      "code": "REVIEW404_1",
                                                      "message": "해당 후기가 존재하지 않습니다.",
                                                      "result": null
                                                    }
                                                    """
                                    ),
                                    @ExampleObject(
                                            name = "도움됨 이력 없음",
                                            value = """
                                                    {
                                                      "isSuccess": false,
                                                      "code": "REVIEW404_2",
                                                      "message": "해당 후기에 도움됨 이력을 남긴 적이 없습니다.",
                                                      "result": null
                                                    }
                                                    """
                                    ),
                                    @ExampleObject(
                                            name = "존재하지 않는 회원",
                                            value = """
                                                    {
                                                      "isSuccess": false,
                                                      "code": "MEMBER404_1",
                                                      "message": "회원을 찾을 수 없습니다.",
                                                      "result": null
                                                    }
                                                    """
                                    )
                            }
                    )
            )
    })
    ApiResponse<ProductReviewResponse.ReviewHelpful> deleteHelpful(
            @Parameter(hidden = true)
            Long memberId,
            @Parameter(
                    description = "도움됨을 취소할 후기 ID입니다.",
                    example = "1"
            )
            Long reviewId
    );
}
