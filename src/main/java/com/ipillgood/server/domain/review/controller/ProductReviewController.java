package com.ipillgood.server.domain.review.controller;

import com.ipillgood.server.domain.review.code.ProductReviewSuccessCode;
import com.ipillgood.server.domain.review.controller.docs.ProductReviewApi;
import com.ipillgood.server.domain.review.dto.ProductReviewRequest;
import com.ipillgood.server.domain.review.dto.ProductReviewResponse;
import com.ipillgood.server.domain.review.service.ProductReviewService;
import com.ipillgood.server.global.apiPayload.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}
