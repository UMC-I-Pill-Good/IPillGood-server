package com.ipillgood.server.domain.review.controller.docs;

import com.ipillgood.server.domain.review.dto.AdminReviewReportRequest;
import com.ipillgood.server.domain.review.dto.AdminReviewReportResponse;
import com.ipillgood.server.global.apiPayload.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

/**
 * 관리자 후기 신고 관리 API 문서
 */
@Tag(name = "Admin Review Report API", description = "관리자 후기 신고 관리 API")
public interface AdminReviewReportApi {

    @Operation(summary = "신고된 후기 목록 조회",
            description = "관리자 페이지에서 신고된 후기를 후기 내용 검색어와 처리 상태(ALL/PENDING/COMPLETED) 조건으로 조회합니다.")
    @SecurityRequirement(name = "JWT TOKEN")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "목록 조회 성공",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "isSuccess": true,
                                              "code": "REVIEW200_7",
                                              "message": "신고된 후기 목록 조회에 성공했습니다.",
                                              "result": {
                                                "reports": [
                                                  {
                                                    "reportId": 12,
                                                    "reviewContent": "이 제품 먹고 효과가 좋았어요.",
                                                    "reason": { "type": "AD_PROMOTION", "label": "광고·홍보" },
                                                    "reportedAt": "2026-07-06T10:00:00",
                                                    "status": { "type": "PENDING", "label": "처리 대기" }
                                                  }
                                                ],
                                                "totalCount": 52,
                                                "totalPages": 6,
                                                "currentPage": 0
                                              }
                                            }
                                            """
                            )
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "status가 허용된 값이 아니거나 page/size 값이 올바르지 않음",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "isSuccess": false,
                                              "code": "COMMON400_2",
                                              "message": "검증에 실패했습니다.",
                                              "result": null
                                            }
                                            """
                            )
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "인증이 필요합니다.",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "isSuccess": false,
                                              "code": "COMMON401_1",
                                              "message": "인증이 필요합니다.",
                                              "result": null
                                            }
                                            """
                            )
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "ADMIN 권한이 없음",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "isSuccess": false,
                                              "code": "COMMON403_1",
                                              "message": "요청이 거부되었습니다.",
                                              "result": null
                                            }
                                            """
                            )
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "500",
                    description = "서버 내부 오류",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "isSuccess": false,
                                              "code": "COMMON500_1",
                                              "message": "예기치 않은 서버 에러가 발생했습니다.",
                                              "result": null
                                            }
                                            """
                            )
                    )
            )
    })
    ApiResponse<AdminReviewReportResponse.ReportList> getReports(
            @Parameter(description = "후기 내용 검색어", required = false, example = "효과")
            String keyword,

            @Parameter(description = "처리 상태 필터 (ALL/PENDING/COMPLETED, 기본값 ALL)", required = false, example = "PENDING")
            String status,

            @Parameter(description = "페이지 번호 (기본값 0)", required = false, example = "0")
            String page,

            @Parameter(description = "페이지 크기 (기본값 20)", required = false, example = "20")
            String size
    );

    @Operation(summary = "신고 상세 조회",
            description = "신고 상세 모달에 표시할 후기 내용, 작성자 정보, 신고 사유, 처리 상태를 조회합니다.")
    @SecurityRequirement(name = "JWT TOKEN")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "상세 조회 성공",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "isSuccess": true,
                                              "code": "REVIEW200_8",
                                              "message": "신고 상세 조회에 성공했습니다.",
                                              "result": {
                                                "reportId": 12,
                                                "reason": { "type": "FALSE_INFO", "label": "허위 정보" },
                                                "writer": {
                                                  "nickname": "주니",
                                                  "username": "junny0207"
                                                },
                                                "writtenAt": "2026-07-07T03:37:00",
                                                "content": "이 제품 먹었는데 효과 개좋고...",
                                                "status": { "type": "PENDING", "label": "처리 대기" },
                                                "processReason": null
                                              }
                                            }
                                            """
                            )
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "인증이 필요합니다.",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "isSuccess": false,
                                              "code": "COMMON401_1",
                                              "message": "인증이 필요합니다.",
                                              "result": null
                                            }
                                            """
                            )
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "ADMIN 권한이 없음",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "isSuccess": false,
                                              "code": "COMMON403_1",
                                              "message": "요청이 거부되었습니다.",
                                              "result": null
                                            }
                                            """
                            )
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "해당 신고 내역이 존재하지 않음",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "isSuccess": false,
                                              "code": "REVIEW404_3",
                                              "message": "해당 신고 내역이 존재하지 않습니다.",
                                              "result": null
                                            }
                                            """
                            )
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "500",
                    description = "서버 내부 오류",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "isSuccess": false,
                                              "code": "COMMON500_1",
                                              "message": "예기치 않은 서버 에러가 발생했습니다.",
                                              "result": null
                                            }
                                            """
                            )
                    )
            )
    })
    ApiResponse<AdminReviewReportResponse.ReportDetail> getReport(
            @Parameter(description = "신고 번호", required = true, example = "12")
            Long reportId
    );

    @Operation(summary = "신고 후기 처리",
            description = "관리자가 신고된 후기의 처리 상태(삭제/유지/숨김)를 결정하고 처리 사유를 남깁니다. "
                    + "처리는 신고 건 단위로 이루어지며, 같은 후기를 참조하는 다른 신고 건의 상태는 변경되지 않습니다.")
    @SecurityRequirement(name = "JWT TOKEN")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "처리 성공",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "isSuccess": true,
                                              "code": "REVIEW200_9",
                                              "message": "후기 신고 처리에 성공했습니다.",
                                              "result": {
                                                "reportId": 12,
                                                "status": { "type": "DELETED", "label": "삭제 처리" },
                                                "processReason": "광고성 후기로 확인되어 삭제 처리",
                                                "processedAt": "2026-08-03T14:00:00"
                                              }
                                            }
                                            """
                            )
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "status가 DELETED/MAINTAINED/HIDDEN 외의 값(PENDING 포함)이거나 누락된 경우",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "isSuccess": false,
                                              "code": "COMMON400_2",
                                              "message": "검증에 실패했습니다.",
                                              "result": null
                                            }
                                            """
                            )
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "인증이 필요합니다.",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "isSuccess": false,
                                              "code": "COMMON401_1",
                                              "message": "인증이 필요합니다.",
                                              "result": null
                                            }
                                            """
                            )
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "ADMIN 권한이 없음",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "isSuccess": false,
                                              "code": "COMMON403_1",
                                              "message": "요청이 거부되었습니다.",
                                              "result": null
                                            }
                                            """
                            )
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "해당 신고 내역이 존재하지 않음",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "isSuccess": false,
                                              "code": "REVIEW404_3",
                                              "message": "해당 신고 내역이 존재하지 않습니다.",
                                              "result": null
                                            }
                                            """
                            )
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "500",
                    description = "서버 내부 오류",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "isSuccess": false,
                                              "code": "COMMON500_1",
                                              "message": "예기치 않은 서버 에러가 발생했습니다.",
                                              "result": null
                                            }
                                            """
                            )
                    )
            )
    })
    ApiResponse<AdminReviewReportResponse.ReportProcessed> processReport(
            @Parameter(description = "신고 번호", required = true, example = "12")
            Long reportId,

            @Valid AdminReviewReportRequest.Process request
    );
}
