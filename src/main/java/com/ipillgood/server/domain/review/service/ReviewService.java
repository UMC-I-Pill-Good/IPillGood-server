package com.ipillgood.server.domain.review.service;

import com.ipillgood.server.domain.review.dto.ReviewRequest;
import com.ipillgood.server.domain.review.dto.ReviewResponse;
import com.ipillgood.server.global.s3.ImageDirectory;
import com.ipillgood.server.global.s3.S3Service;
import com.ipillgood.server.global.s3.dto.PresignedUpload;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final S3Service s3Service;

    public ReviewResponse.ImagePresigns createImageUploadUrls(ReviewRequest.ImagePresign request) {
        List<PresignedUpload> images =
                s3Service.createUploadUrls(ImageDirectory.REVIEW, request.contentTypes());
        return new ReviewResponse.ImagePresigns(images);
    }
}
