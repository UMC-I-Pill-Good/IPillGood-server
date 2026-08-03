package com.ipillgood.server.domain.review.service;

import com.ipillgood.server.domain.review.code.ProductReviewErrorCode;
import com.ipillgood.server.domain.review.converter.AdminReviewReportConverter;
import com.ipillgood.server.domain.review.dto.AdminReviewReportRequest;
import com.ipillgood.server.domain.review.dto.AdminReviewReportResponse;
import com.ipillgood.server.domain.review.entity.ProductReview;
import com.ipillgood.server.domain.review.entity.ProductReviewReport;
import com.ipillgood.server.domain.review.entity.enums.ReviewReportStatus;
import com.ipillgood.server.domain.review.exception.ProductReviewException;
import com.ipillgood.server.domain.review.repository.ProductReviewReportRepository;
import com.ipillgood.server.global.apiPayload.code.GeneralErrorCode;
import com.ipillgood.server.global.apiPayload.exception.GeneralException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminReviewReportService {

    private static final int DEFAULT_PAGE = 0;
    private static final int DEFAULT_SIZE = 20;
    private static final int MAX_SIZE = 100;

    private final ProductReviewReportRepository productReviewReportRepository;

    /**
     * 관리자 신고 목록 조회
     * 후기 내용 키워드, 처리 상태(ALL/PENDING/COMPLETED) 조건으로 페이지네이션 조회
     */
    public AdminReviewReportResponse.ReportList getReports(
            String keyword, String statusValue, String pageValue, String sizeValue
    ) {
        List<ReviewReportStatus> statuses = parseStatusFilter(statusValue);
        int page = parsePage(pageValue);
        int size = parseSize(sizeValue);

        Page<ProductReviewReport> reportPage = productReviewReportRepository.searchForAdmin(
                normalizeKeyword(keyword), statuses, PageRequest.of(page, size));

        return AdminReviewReportConverter.toReportList(reportPage);
    }

    /**
     * 관리자 신고 상세 조회
     */
    public AdminReviewReportResponse.ReportDetail getReport(Long reportId) {
        ProductReviewReport report = getReportOrThrow(reportId);

        return AdminReviewReportConverter.toReportDetail(report);
    }

    /**
     * 관리자 신고 처리
     * 신고 건(reportId) 단위로 처리하며, 같은 후기를 참조하는 다른 신고 건의 상태는 변경하지 않는다.
     * DELETED/HIDDEN 처리 시 해당 후기 자체의 노출 상태에도 반영한다.
     */
    @Transactional
    public AdminReviewReportResponse.ReportProcessed processReport(
            Long reportId, AdminReviewReportRequest.Process request
    ) {
        ProductReviewReport report = getReportOrThrow(reportId);
        ReviewReportStatus status = parseStatus(request.status());

        applyReviewSideEffect(report.getReview(), status);
        report.process(status, request.processReason(), LocalDateTime.now());

        return AdminReviewReportConverter.toReportProcessed(report);
    }

    private void applyReviewSideEffect(ProductReview review, ReviewReportStatus status) {
        switch (status) {
            case DELETED -> review.markDeleted(LocalDateTime.now());
            case HIDDEN -> review.hide();
            case MAINTAINED, PENDING -> review.unhide();
        }
    }

    private ProductReviewReport getReportOrThrow(Long reportId) {
        return productReviewReportRepository.findWithDetailsById(reportId)
                .orElseThrow(() -> new ProductReviewException(ProductReviewErrorCode.REVIEW_REPORT_NOT_FOUND));
    }

    private List<ReviewReportStatus> parseStatusFilter(String value) {
        if (value == null || value.isBlank() || value.equalsIgnoreCase("ALL")) {
            return null;
        }
        if (value.equalsIgnoreCase("PENDING")) {
            return List.of(ReviewReportStatus.PENDING);
        }
        if (value.equalsIgnoreCase("COMPLETED")) {
            return List.of(ReviewReportStatus.DELETED, ReviewReportStatus.MAINTAINED, ReviewReportStatus.HIDDEN);
        }
        throw new GeneralException(GeneralErrorCode.VALID_FAIL);
    }

    private ReviewReportStatus parseStatus(String value) {
        if (value == null || value.isBlank()) {
            throw new GeneralException(GeneralErrorCode.VALID_FAIL);
        }
        try {
            return ReviewReportStatus.valueOf(value.trim());
        } catch (IllegalArgumentException e) {
            throw new GeneralException(GeneralErrorCode.VALID_FAIL);
        }
    }

    private String normalizeKeyword(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return null;
        }
        return keyword.trim().toLowerCase(Locale.ROOT);
    }

    private int parsePage(String value) {
        if (value == null || value.isBlank()) {
            return DEFAULT_PAGE;
        }
        int page = parsePageInteger(value);
        if (page < 0) {
            throw new GeneralException(GeneralErrorCode.INVALID_PAGE);
        }
        return page;
    }

    private int parseSize(String value) {
        if (value == null || value.isBlank()) {
            return DEFAULT_SIZE;
        }
        int size = parsePageInteger(value);
        if (size < 1 || size > MAX_SIZE) {
            throw new GeneralException(GeneralErrorCode.INVALID_PAGE);
        }
        return size;
    }

    private int parsePageInteger(String value) {
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            throw new GeneralException(GeneralErrorCode.INVALID_PAGE);
        }
    }
}
