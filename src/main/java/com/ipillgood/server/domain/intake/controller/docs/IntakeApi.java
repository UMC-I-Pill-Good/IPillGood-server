package com.ipillgood.server.domain.intake.controller.docs;

import com.ipillgood.server.domain.intake.dto.IntakeResponse;
import com.ipillgood.server.global.apiPayload.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Intake API", description = "복용 루틴 관련 API")
public interface IntakeApi {

    @Operation(
            summary = "섭취 중 영양제 목록 조회",
            description = "홈에서 표시할 현재 섭취 중 영양제 카드 목록을 활성 등록 순서로 조회합니다."
    )
    @SecurityRequirement(name = "JWT TOKEN")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "섭취 중 영양제 목록 조회 성공",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "isSuccess": true,
                                              "code": "SUCCESS200_1",
                                              "message": "요청이 성공적으로 처리되었습니다.",
                                              "result": {
                                                "totalCount": 2,
                                                "activeProducts": [
                                                  {
                                                    "activeProductId": 7,
                                                    "memberProductId": 15,
                                                    "productId": 112,
                                                    "productName": "뉴트리코어 유기농 비타민D 1000IU",
                                                    "thumbnailImageUrl": "https://ipillgood-bucket.s3.ap-northeast-2.amazonaws.com/ingredients/2.png"
                                                  }
                                                ]
                                              }
                                            }
                                            """
                            )
                    )
            )
    })
    ApiResponse<IntakeResponse.ActiveProducts> getActiveProducts(
            @Parameter(hidden = true)
            Long memberId
    );
}

