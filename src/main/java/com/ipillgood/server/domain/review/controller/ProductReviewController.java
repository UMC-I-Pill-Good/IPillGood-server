package com.ipillgood.server.domain.review.controller;

import com.ipillgood.server.domain.review.code.ProductReviewSuccessCode;
import com.ipillgood.server.domain.review.controller.docs.ProductReviewApi;
import com.ipillgood.server.domain.review.dto.ProductReviewRequest;
import com.ipillgood.server.domain.review.dto.ProductReviewResponse;
import com.ipillgood.server.domain.review.entity.enums.ProductReviewSort;
import com.ipillgood.server.domain.review.service.ProductReviewService;
import com.ipillgood.server.global.apiPayload.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/reviews")
public class ProductReviewController implements ProductReviewApi {

    private final ProductReviewService reviewService;

    @Override
    @PostMapping("/images/presign")
    public ApiResponse<ProductReviewResponse.ImagePresigns> createImageUploadUrls(
            @AuthenticationPrincipal Long memberId,
            @RequestBody @Valid ProductReviewRequest.ImagePresign request
    ) {
        ProductReviewResponse.ImagePresigns response = reviewService.createImageUploadUrls(request);
        return ApiResponse.onSuccess(ProductReviewSuccessCode.REVIEW_IMAGE_UPLOAD_URL_SUCCESS, response);
    }

    @Override
    @GetMapping("/{productId}")
    public ApiResponse<ProductReviewResponse.ProductReviews> getReviews(
            @AuthenticationPrincipal Long memberId,
            @PathVariable Long productId,
            @RequestParam(required = false) ProductReviewSort sort,
            @RequestParam(required = false) Integer size,
            @RequestParam(required = false) String cursor
    ) {
        ProductReviewResponse.ProductReviews resDto =
                reviewService.getReviews(memberId, productId, sort, size, cursor);
        return ApiResponse.onSuccess(ProductReviewSuccessCode.VIEW_REVIEWS_SUCCESS, resDto);
    }

    @Override
    @GetMapping("/me/{reviewId}")
    public ApiResponse<ProductReviewResponse.ReviewDetail> getMyReview(
            @AuthenticationPrincipal Long memberId,
            @PathVariable Long reviewId
    ) {
        ProductReviewResponse.ReviewDetail resDto = reviewService.getMyReview(memberId, reviewId);
        return ApiResponse.onSuccess(ProductReviewSuccessCode.VIEW_MY_REVIEW_SUCCESS, resDto);
    }

    @Override
    @PostMapping("/{productId}")
    public ApiResponse<ProductReviewResponse.ReviewCreate> createReview(
            @AuthenticationPrincipal Long memberId,
            @PathVariable Long productId,
            @RequestBody @Valid ProductReviewRequest.Review reqDto
    ) {
        ProductReviewResponse.ReviewCreate resDto = reviewService.createReview(memberId, productId, reqDto);
        return ApiResponse.onSuccess(ProductReviewSuccessCode.REVIEW_CREATE_SUCCESS, resDto);
    }

    @Override
    @PatchMapping("/{reviewId}")
    public ApiResponse<ProductReviewResponse.ReviewUpdate> updateReview(
            @AuthenticationPrincipal Long memberId,
            @PathVariable Long reviewId,
            @RequestBody @Valid ProductReviewRequest.ReviewUpdate reqDto
    ) {
        ProductReviewResponse.ReviewUpdate resDto = reviewService.updateReview(memberId, reviewId, reqDto);
        return ApiResponse.onSuccess(ProductReviewSuccessCode.REVIEW_UPDATE_SUCCESS, resDto);
    }

    @Override
    @DeleteMapping("/{reviewId}")
    public ApiResponse<ProductReviewResponse.ReviewDelete> deleteReview(
            @AuthenticationPrincipal Long memberId,
            @PathVariable Long reviewId
    ) {
        ProductReviewResponse.ReviewDelete resDto = reviewService.deleteReview(memberId, reviewId);
        return ApiResponse.onSuccess(ProductReviewSuccessCode.REVIEW_DELETE_SUCCESS, resDto);
    }
}
