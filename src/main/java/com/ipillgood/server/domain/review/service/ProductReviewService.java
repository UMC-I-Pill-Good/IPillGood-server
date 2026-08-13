package com.ipillgood.server.domain.review.service;

import com.ipillgood.server.domain.member.code.MemberErrorCode;
import com.ipillgood.server.domain.member.entity.Member;
import com.ipillgood.server.domain.member.exception.MemberException;
import com.ipillgood.server.domain.member.repository.MemberRepository;
import com.ipillgood.server.domain.product.code.ProductErrorCode;
import com.ipillgood.server.domain.product.entity.Product;
import com.ipillgood.server.domain.product.exception.ProductException;
import com.ipillgood.server.domain.product.repository.ProductRepository;
import com.ipillgood.server.domain.review.code.ProductReviewErrorCode;
import com.ipillgood.server.domain.review.converter.ProductReviewConverter;
import com.ipillgood.server.domain.review.dto.ProductReviewRequest;
import com.ipillgood.server.domain.review.dto.ProductReviewResponse;
import com.ipillgood.server.domain.review.entity.ProductReview;
import com.ipillgood.server.domain.review.entity.ProductReviewHelpful;
import com.ipillgood.server.domain.review.entity.ProductReviewReport;
import com.ipillgood.server.domain.review.entity.enums.ProductReviewSort;
import com.ipillgood.server.domain.review.exception.ProductReviewException;
import com.ipillgood.server.domain.review.repository.*;
import com.ipillgood.server.domain.survey.repository.SurveyProjection;
import com.ipillgood.server.domain.survey.service.SurveyService;
import com.ipillgood.server.global.pagination.CursorPage;
import com.ipillgood.server.global.s3.ImageDirectory;
import com.ipillgood.server.global.s3.S3Service;
import com.ipillgood.server.global.s3.dto.PresignedUpload;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductReviewService {

    private static final int DEFAULT_REVIEW_PAGE_SIZE = 20;

    private final ProductReviewRepository productReviewRepository;
    private final ProductRepository productRepository;
    private final MemberRepository memberRepository;
    private final ProductReviewHelpfulRepository productReviewHelpfulRepository;
    private final ProductReviewReportRepository productReviewReportRepository;
    private final SurveyService surveyService;
    private final S3Service s3Service;

    public ProductReviewResponse.ImagePresigns createImageUploadUrls(ProductReviewRequest.ImagePresign request) {
        List<PresignedUpload> images =
                s3Service.createUploadUrls(ImageDirectory.REVIEW, request.contentTypes());
        return new ProductReviewResponse.ImagePresigns(images);
    }

    public ProductReviewResponse.ReviewSummary getReviewSummary(Product product){
        ProductReviewProjection.ReviewSummary summary = productReviewRepository.findActiveSummaryByProduct(product);
        return new ProductReviewResponse.ReviewSummary(
                toRoundedAverageRating(summary.ratingAverage()),
                summary.reviewCount().intValue());
    }

    public ProductReviewResponse.ProductReviews getReviews(
            Long memberId,
            Long productId,
            ProductReviewSort sort,
            Integer size,
            String cursor
    ) {
        Product product = getActiveProduct(productId);
        ProductReviewCondition condition = toReviewCondition(product, sort, size, cursor);

        List<ProductReviewProjection.Review> rows = productReviewRepository.findReviews(condition);
        CursorPage<ProductReviewProjection.Review> page = CursorPage.of(
                rows,
                condition.size(),
                lastRow -> ProductReviewCursorCodec.encode(condition.sort(), lastRow));

        List<Long> reviewIds = page.content().stream()
                .map(ProductReviewProjection.Review::reviewId)
                .toList();
        List<Long> reviewerIds = page.content().stream()
                .map(ProductReviewProjection.Review::memberId)
                .toList();

        List<ProductReviewProjection.ReviewImage> imageRows =
                productReviewRepository.findImagesByReviewIds(reviewIds);
        Set<Long> helpfulReviewIds =
                Set.copyOf(productReviewRepository.findHelpfulReviewIds(memberId, reviewIds));
        Map<Long, SurveyProjection.MemberProfile> profilesByMemberId =
                surveyService.getLatestProfiles(reviewerIds);

        return ProductReviewConverter.toProductReviews(
                productId,
                memberId,
                condition.sort(),
                condition.size(),
                getReviewSummary(product),
                page,
                imageRows,
                helpfulReviewIds,
                profilesByMemberId,
                s3Service::getPublicUrl
        );
    }

    public ProductReviewResponse.ReviewDetail getMyReview(Long memberId, Long reviewId) {
        ProductReview review = productReviewRepository.findWithImagesByIdAndDeletedAtIsNullAndHiddenFalse(reviewId)
                .orElseThrow(() -> new ProductReviewException(ProductReviewErrorCode.REVIEW_NOT_FOUND));

        if (!review.getMember().getId().equals(memberId)) {
            throw new ProductReviewException(ProductReviewErrorCode.REVIEW_VIEW_FORBIDDEN);
        }

        return ProductReviewConverter.toReviewDetail(review, s3Service::getPublicUrl);
    }

    @Transactional
    public ProductReviewResponse.ReviewCreate createReview(
            Long memberId,
            Long productId,
            ProductReviewRequest.Review reqDto
    ) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new MemberException(MemberErrorCode.MEMBER_NOT_FOUND));
        Product product = getActiveProduct(productId);

        if(productReviewRepository.existsByMemberAndProductAndDeletedAtIsNull(member, product)){
            throw new ProductReviewException(ProductReviewErrorCode.REVIEW_ALREADY_EXISTS);
        }

        ProductReview newReview = ProductReviewConverter.toProductReview(member, product, reqDto);
        List<String> imageKeys = reqDto.imageKeys();
        if(imageKeys != null){
            imageKeys.forEach(key -> s3Service.validateUploadedKey(ImageDirectory.REVIEW, key));
            imageKeys.forEach(newReview::addPhoto);
        }

        productReviewRepository.save(newReview);
        return ProductReviewConverter.toReviewCreate(newReview, s3Service::getPublicUrl);
    }

    @Transactional
    public ProductReviewResponse.ReviewUpdate updateReview(
            Long memberId,
            Long reviewId,
            ProductReviewRequest.ReviewUpdate reqDto
    ) {
        ProductReview review = productReviewRepository.findByIdAndDeletedAtIsNullAndHiddenFalse(reviewId)
                .orElseThrow(() -> new ProductReviewException(ProductReviewErrorCode.REVIEW_NOT_FOUND));

        if (!review.getMember().getId().equals(memberId)) {
            throw new ProductReviewException(ProductReviewErrorCode.REVIEW_UPDATE_FORBIDDEN);
        }

        review.updateContent(reqDto.rating(), reqDto.content());
        review.clearPhotos();
        productReviewRepository.flush();

        List<String> imageKeys = reqDto.imageKeys();
        if(imageKeys != null){
            imageKeys.forEach(key -> s3Service.validateUploadedKey(ImageDirectory.REVIEW, key));
            imageKeys.forEach(review::addPhoto);
        }

        productReviewRepository.flush();
        return ProductReviewConverter.toReviewUpdate(review, s3Service::getPublicUrl);
    }

    @Transactional
    public ProductReviewResponse.ReviewDelete deleteReview(Long memberId, Long reviewId) {
        ProductReview review = productReviewRepository.findByIdAndDeletedAtIsNullAndHiddenFalse(reviewId)
                .orElseThrow(() -> new ProductReviewException(ProductReviewErrorCode.REVIEW_NOT_FOUND));

        if (!review.getMember().getId().equals(memberId)) {
            throw new ProductReviewException(ProductReviewErrorCode.REVIEW_DELETE_FORBIDDEN);
        }

        review.markDeleted(LocalDateTime.now());
        return new ProductReviewResponse.ReviewDelete(true, reviewId);
    }

    @Transactional
    public ProductReviewResponse.ReviewHelpful createHelpful(Long memberId, Long reviewId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new MemberException(MemberErrorCode.MEMBER_NOT_FOUND));
        ProductReview review = productReviewRepository.findByIdAndDeletedAtIsNullAndHiddenFalse(reviewId)
                .orElseThrow(() -> new ProductReviewException(ProductReviewErrorCode.REVIEW_NOT_FOUND));

        if (review.getMember().getId().equals(memberId)) {
            throw new ProductReviewException(ProductReviewErrorCode.REVIEW_HELPFUL_FORBIDDEN);
        }

        if (productReviewHelpfulRepository.existsByMemberAndReview(member, review)) {
            throw new ProductReviewException(ProductReviewErrorCode.HELPFUL_ALREADY_EXISTS);
        }

        ProductReviewHelpful helpful = ProductReviewHelpful.builder()
                .review(review)
                .member(member)
                .build();

        review.increaseHelpfulCount();
        productReviewHelpfulRepository.save(helpful);
        return ProductReviewConverter.toReviewHelpful(true, review);
    }

    @Transactional
    public ProductReviewResponse.ReviewHelpful deleteHelpful(Long memberId, Long reviewId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new MemberException(MemberErrorCode.MEMBER_NOT_FOUND));
        ProductReview review = productReviewRepository.findByIdAndDeletedAtIsNullAndHiddenFalse(reviewId)
                .orElseThrow(() -> new ProductReviewException(ProductReviewErrorCode.REVIEW_NOT_FOUND));

        ProductReviewHelpful helpful = productReviewHelpfulRepository.findByMemberAndReview(member, review)
                .orElseThrow(() -> new ProductReviewException(ProductReviewErrorCode.REVIEW_HELPFUL_NOT_FOUND));

        productReviewHelpfulRepository.delete(helpful);
        review.decreaseHelpfulCount();

        return ProductReviewConverter.toReviewHelpful(false, review);
    }

    @Transactional
    public ProductReviewResponse.ReviewReport createReviewReport(
            Long memberId,
            Long reviewId,
            ProductReviewRequest.ReviewReport reqDto
    ) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new MemberException(MemberErrorCode.MEMBER_NOT_FOUND));
        ProductReview review = productReviewRepository.findByIdAndDeletedAtIsNullAndHiddenFalse(reviewId)
                .orElseThrow(() -> new ProductReviewException(ProductReviewErrorCode.REVIEW_NOT_FOUND));

        if (review.getMember().getId().equals(memberId)) {
            throw new ProductReviewException(ProductReviewErrorCode.REVIEW_REPORT_FORBIDDEN);
        }
        if (productReviewReportRepository.existsByReporterMemberAndReview(member, review)){
            throw new ProductReviewException(ProductReviewErrorCode.REVIEW_REPORT_EXISTS);
        }

        ProductReviewReport report = ProductReviewConverter.toProductReviewReport(member, review, reqDto);
        productReviewReportRepository.save(report);
        return ProductReviewConverter.toReviewReport(report);
    }

    private ProductReviewCondition toReviewCondition(
            Product product,
            ProductReviewSort sort,
            Integer size,
            String cursor
    ) {
        ProductReviewSort reviewSort = (sort == null) ? ProductReviewSort.LATEST : sort;

        return new ProductReviewCondition(
                product,
                reviewSort,
                size == null ? DEFAULT_REVIEW_PAGE_SIZE : size,
                ProductReviewCursorCodec.decode(cursor, reviewSort)
        );
    }

    private Product getActiveProduct(Long productId) {
        return productRepository.findActiveById(productId)
                .orElseThrow(() -> new ProductException(ProductErrorCode.PRODUCT_NOT_FOUND));
    }

    private Double toRoundedAverageRating(Double averageRating) {
        if (averageRating == null) {
            return null;
        }
        return Math.round(averageRating * 10) / 10.0;
    }
}
