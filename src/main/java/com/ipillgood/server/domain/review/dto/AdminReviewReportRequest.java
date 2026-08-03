package com.ipillgood.server.domain.review.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;

public class AdminReviewReportRequest {

    @Schema(description = "후기 신고 처리 요청")
    public record Process(
            @Schema(description = "처리 상태. PENDING(처리 대기), DELETED(삭제 처리), "
                    + "MAINTAINED(유지 처리), HIDDEN(숨김 처리)",
                    example = "DELETED")
            String status,

            @Schema(description = "처리 사유 (최대 200자). 선택 항목이며 생략할 수 있습니다.",
                    nullable = true, example = "광고성 후기로 확인되어 삭제 처리")
            @Size(max = 200, message = "처리 사유는 200자를 넘을 수 없습니다.")
            String processReason
    ) {
    }
}
