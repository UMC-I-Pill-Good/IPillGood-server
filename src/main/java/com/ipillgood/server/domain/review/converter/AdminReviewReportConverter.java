package com.ipillgood.server.domain.review.converter;

import com.ipillgood.server.domain.review.dto.AdminReviewReportResponse;
import com.ipillgood.server.domain.review.entity.ProductReviewReport;
import com.ipillgood.server.domain.review.entity.enums.ProductReviewReportReason;
import com.ipillgood.server.domain.review.entity.enums.ReviewReportStatus;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Page;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class AdminReviewReportConverter {

    public static AdminReviewReportResponse.ReportList toReportList(Page<ProductReviewReport> page) {
        return AdminReviewReportResponse.ReportList.builder()
                .reports(page.getContent().stream()
                        .map(AdminReviewReportConverter::toReportSummary)
                        .toList())
                .totalCount(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .currentPage(page.getNumber())
                .build();
    }

    public static AdminReviewReportResponse.ReportSummary toReportSummary(ProductReviewReport report) {
        return AdminReviewReportResponse.ReportSummary.builder()
                .reportId(report.getId())
                .reviewContent(report.getReview().getContent())
                .reason(toReasonInfo(report.getReason()))
                .reportedAt(report.getCreatedAt())
                .status(toStatusInfo(report.getStatus()))
                .build();
    }

    public static AdminReviewReportResponse.ReportDetail toReportDetail(ProductReviewReport report) {
        return AdminReviewReportResponse.ReportDetail.builder()
                .reportId(report.getId())
                .reason(toReasonInfo(report.getReason()))
                .writer(AdminReviewReportResponse.Writer.builder()
                        .nickname(report.getReview().getMember().getNickname())
                        .username(report.getReview().getMember().getUsername())
                        .build())
                .writtenAt(report.getReview().getCreatedAt())
                .content(report.getReview().getContent())
                .status(toStatusInfo(report.getStatus()))
                .processReason(report.getProcessReason())
                .build();
    }

    public static AdminReviewReportResponse.ReportProcessed toReportProcessed(ProductReviewReport report) {
        return AdminReviewReportResponse.ReportProcessed.builder()
                .reportId(report.getId())
                .status(toStatusInfo(report.getStatus()))
                .processReason(report.getProcessReason())
                .processedAt(report.getProcessedAt())
                .build();
    }

    private static AdminReviewReportResponse.ReasonInfo toReasonInfo(ProductReviewReportReason reason) {
        return AdminReviewReportResponse.ReasonInfo.builder()
                .type(reason)
                .label(reason.getLabel())
                .build();
    }

    private static AdminReviewReportResponse.StatusInfo toStatusInfo(ReviewReportStatus status) {
        return AdminReviewReportResponse.StatusInfo.builder()
                .type(status)
                .label(status.getLabel())
                .build();
    }

}
