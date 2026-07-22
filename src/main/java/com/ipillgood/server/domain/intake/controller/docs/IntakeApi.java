package com.ipillgood.server.domain.intake.controller.docs;

import com.ipillgood.server.domain.intake.dto.IntakeRequest;
import com.ipillgood.server.domain.intake.dto.IntakeResponse;
import com.ipillgood.server.global.apiPayload.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
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

    @Operation(
            summary = "섭취 중 등록 전 병용 금기 확인",
            description = "새로 등록하려는 영양제와 현재 섭취 중 영양제 간 주의 또는 금기 성분 조합을 확인합니다."
    )
    @SecurityRequirement(name = "JWT TOKEN")
    @RequestBody(
            required = true,
            description = "섭취 중으로 등록하려는 회원 캐비닛 상품 ID를 전달합니다.",
            content = @Content(
                    schema = @Schema(implementation = IntakeRequest.CompatibilityCheck.class),
                    examples = @ExampleObject(
                            value = """
                                    {
                                      "memberProductId": 16
                                    }
                                    """
                    )
            )
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "병용 금기 확인 성공",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "isSuccess": true,
                                              "code": "SUCCESS200_1",
                                              "message": "요청이 성공적으로 처리되었습니다.",
                                              "result": {
                                                "hasConflicts": true,
                                                "conflicts": [
                                                  {
                                                    "combinationType": "CAUTION",
                                                    "currentIngredientId": 10,
                                                    "currentIngredientName": "칼슘",
                                                    "targetIngredientId": 18,
                                                    "targetIngredientName": "철",
                                                    "reason": "체내 흡수 경로가 겹쳐 동시 복용 시 서로의 흡수를 방해할 수 있어요."
                                                  }
                                                ]
                                              }
                                            }
                                            """
                            )
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "등록/병용 확인 요청 오류",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "isSuccess": false,
                                              "code": "INTAKE400_2",
                                              "message": "등록/병용 확인 요청이 올바르지 않습니다.",
                                              "result": null
                                            }
                                            """
                            )
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "등록 대상 캐비닛 상품 없음",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "isSuccess": false,
                                              "code": "INTAKE404_1",
                                              "message": "섭취 중으로 등록할 캐비닛 상품을 찾을 수 없습니다.",
                                              "result": null
                                            }
                                            """
                            )
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "409",
                    description = "이미 섭취 중인 영양제",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "isSuccess": false,
                                              "code": "INTAKE409_1",
                                              "message": "이미 섭취 중인 영양제입니다.",
                                              "result": null
                                            }
                                            """
                            )
                    )
            )
    })
    ApiResponse<IntakeResponse.CompatibilityCheck> checkCompatibility(
            @Parameter(hidden = true)
            Long memberId,
            IntakeRequest.CompatibilityCheck request
    );
}
