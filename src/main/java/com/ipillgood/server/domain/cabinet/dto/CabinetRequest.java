package com.ipillgood.server.domain.cabinet.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

public class CabinetRequest {

    @Schema(description = "캐비닛 영양제 추가 요청")
    public record AddProducts(
            @Schema(description = "캐비닛에 추가할 영양제 상품 ID 목록", example = "[118, 124]")
            List<Long> productIds
    ) {
    }
}
