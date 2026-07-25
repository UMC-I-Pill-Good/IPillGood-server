package com.ipillgood.server.domain.search.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class ProductSearchRequest {

    @Schema(description = "최근 검색어 저장 요청")
    public record RecentSearchKeyword(
            @Schema(description = "저장할 검색어 (1~100자)", example = "비타민")
            @NotBlank
            @Size(max = 100)
            String keyword
    ) {}
}
