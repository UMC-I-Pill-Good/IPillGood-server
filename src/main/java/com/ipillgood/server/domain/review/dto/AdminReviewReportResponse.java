package com.ipillgood.server.domain.review.dto;

import com.ipillgood.server.domain.review.entity.enums.ProductReviewReportReason;
import com.ipillgood.server.domain.review.entity.enums.ReviewReportStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.List;

public class AdminReviewReportResponse {

    @Schema(description = "관리자 신고 목록 조회 응답")
    @Builder
    public record ReportList(
            @Schema(description = "신고 목록")
            List<ReportSummary> reports,

            @Schema(description = "검색 조건에 맞는 전체 신고 수", example = "52")
            Long totalCount,

            @Schema(description = "전체 페이지 수", example = "6")
            Integer totalPages,

            @Schema(description = "현재 페이지 번호", example = "0")
            Integer currentPage
    ) {
    }

    @Schema(description = "관리자 신고 목록 항목")
    @Builder
    public record ReportSummary(
            @Schema(description = "신고 번호", example = "12")
            Long reportId,

            @Schema(description = "신고된 후기 내용", example = "이 제품 먹고 효과가 좋았어요.")
            String reviewContent,

            @Schema(description = "신고 사유")
            ReasonInfo reason,

            @Schema(description = "신고일", example = "2026-07-06T10:00:00")
            LocalDateTime reportedAt,

            @Schema(description = "처리 상태")
            StatusInfo status
    ) {
    }

    @Schema(description = "관리자 신고 상세 조회 응답")
    @Builder
    public record ReportDetail(
            @Schema(description = "신고 번호", example = "12")
            Long reportId,

            @Schema(description = "신고 사유")
            ReasonInfo reason,

            @Schema(description = "후기 작성자 정보")
            Writer writer,

            @Schema(description = "후기 작성일", example = "2026-07-07T03:37:00")
            LocalDateTime writtenAt,

            @Schema(description = "후기 내용", example = "이 제품 먹었는데 효과 개좋고...")
            String content,

            @Schema(description = "처리 상태")
            StatusInfo status,

            @Schema(description = "처리 사유. 처리 전에는 null", nullable = true)
            String processReason
    ) {
    }

    @Schema(description = "후기 작성자 정보")
    @Builder
    public record Writer(
            @Schema(description = "작성자 닉네임", example = "주니")
            String nickname,

            @Schema(description = "작성자 아이디", example = "junny0207")
            String username
    ) {
    }

    @Schema(description = "신고 처리 응답")
    @Builder
    public record ReportProcessed(
            @Schema(description = "신고 번호", example = "12")
            Long reportId,

            @Schema(description = "처리 상태")
            StatusInfo status,

            @Schema(description = "처리 사유", nullable = true, example = "광고성 후기로 확인되어 삭제 처리")
            String processReason,

            @Schema(description = "처리일시", example = "2026-08-03T14:00:00")
            LocalDateTime processedAt
    ) {
    }

    @Schema(description = "신고 사유 코드/라벨")
    @Builder
    public record ReasonInfo(
            @Schema(description = "신고 사유 코드", example = "AD_PROMOTION")
            ProductReviewReportReason type,

            @Schema(description = "신고 사유 한글명", example = "광고·홍보")
            String label
    ) {
    }

    @Schema(description = "처리 상태 코드/라벨")
    @Builder
    public record StatusInfo(
            @Schema(description = "처리 상태 코드", example = "PENDING")
            ReviewReportStatus type,

            @Schema(description = "처리 상태 한글명", example = "처리 대기")
            String label
    ) {
    }
}
