package com.ipillgood.server.domain.review.service;

import com.ipillgood.server.domain.product.entity.Product;
import com.ipillgood.server.domain.review.dto.ProductReviewRequest;
import com.ipillgood.server.domain.review.dto.ProductReviewResponse;
import com.ipillgood.server.domain.review.repository.ProductReviewProjection;
import com.ipillgood.server.domain.review.repository.ProductReviewRepository;
import com.ipillgood.server.global.s3.ImageDirectory;
import com.ipillgood.server.global.s3.S3Service;
import com.ipillgood.server.global.s3.dto.PresignedUpload;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ProductReviewService {

    private final ProductReviewRepository productReviewRepository;
    private final S3Service s3Service;

    public ProductReviewResponse.ImagePresigns createImageUploadUrls(ProductReviewRequest.ImagePresign request) {
        List<PresignedUpload> images =
                s3Service.createUploadUrls(ImageDirectory.REVIEW, request.contentTypes());
        return new ProductReviewResponse.ImagePresigns(images);
    }

    public ProductReviewResponse.ReviewSummary getReviewSummary(Product product){
        ProductReviewProjection.ReviewSummary summary = productReviewRepository.findActiveSummaryByProduct(product);
        return new ProductReviewResponse.ReviewSummary(summary.ratingAverage(), summary.reviewCount().intValue());
    }
}
