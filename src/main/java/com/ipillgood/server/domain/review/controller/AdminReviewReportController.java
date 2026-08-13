package com.ipillgood.server.domain.review.controller;

import com.ipillgood.server.domain.review.code.ProductReviewSuccessCode;
import com.ipillgood.server.domain.review.controller.docs.AdminReviewReportApi;
import com.ipillgood.server.domain.review.dto.AdminReviewReportRequest;
import com.ipillgood.server.domain.review.dto.AdminReviewReportResponse;
import com.ipillgood.server.domain.review.service.AdminReviewReportService;
import com.ipillgood.server.global.apiPayload.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/admin/review-reports")
public class AdminReviewReportController implements AdminReviewReportApi {

    private final AdminReviewReportService adminReviewReportService;

    // 관리자 신고된 후기 목록 조회
    @Override
    @GetMapping
    public ApiResponse<AdminReviewReportResponse.ReportList> getReports(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String page,
            @RequestParam(required = false) String size
    ) {
        AdminReviewReportResponse.ReportList response =
                adminReviewReportService.getReports(keyword, status, page, size);
        return ApiResponse.onSuccess(ProductReviewSuccessCode.ADMIN_REVIEW_REPORT_LIST_SUCCESS, response);
    }

    // 관리자 신고 상세 조회
    @Override
    @GetMapping("/{reportId}")
    public ApiResponse<AdminReviewReportResponse.ReportDetail> getReport(
            @PathVariable Long reportId
    ) {
        AdminReviewReportResponse.ReportDetail response = adminReviewReportService.getReport(reportId);
        return ApiResponse.onSuccess(ProductReviewSuccessCode.ADMIN_REVIEW_REPORT_DETAIL_SUCCESS, response);
    }

    // 관리자 신고 후기 처리
    @Override
    @PatchMapping("/{reportId}")
    public ApiResponse<AdminReviewReportResponse.ReportProcessed> processReport(
            @PathVariable Long reportId,
            @RequestBody @Valid AdminReviewReportRequest.Process request
    ) {
        AdminReviewReportResponse.ReportProcessed response =
                adminReviewReportService.processReport(reportId, request);
        return ApiResponse.onSuccess(ProductReviewSuccessCode.ADMIN_REVIEW_REPORT_PROCESS_SUCCESS, response);
    }
}
