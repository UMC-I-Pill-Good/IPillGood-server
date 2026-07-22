package com.ipillgood.server.domain.review.controller;

import com.ipillgood.server.domain.review.code.ReviewSuccessCode;
import com.ipillgood.server.domain.review.controller.docs.ReviewApi;
import com.ipillgood.server.domain.review.dto.ReviewRequest;
import com.ipillgood.server.domain.review.dto.ReviewResponse;
import com.ipillgood.server.domain.review.service.ReviewService;
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
public class ReviewController implements ReviewApi {

    private final ReviewService reviewService;

    @Override
    @PostMapping("/images/presign")
    public ApiResponse<ReviewResponse.ImagePresigns> createImageUploadUrls(
            @AuthenticationPrincipal Long memberId,
            @RequestBody @Valid ReviewRequest.ImagePresign request
    ) {
        ReviewResponse.ImagePresigns response = reviewService.createImageUploadUrls(request);
        return ApiResponse.onSuccess(ReviewSuccessCode.REVIEW_IMAGE_UPLOAD_URL_SUCCESS, response);
    }
}
