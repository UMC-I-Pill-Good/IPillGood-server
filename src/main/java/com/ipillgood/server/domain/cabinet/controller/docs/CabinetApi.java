package com.ipillgood.server.domain.cabinet.controller.docs;

import com.ipillgood.server.domain.cabinet.dto.CabinetRequest;
import com.ipillgood.server.domain.cabinet.dto.CabinetResponse;
import com.ipillgood.server.global.apiPayload.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Cabinet API", description = "캐비닛 관련 API")
public interface CabinetApi {

    @Operation(
            summary = "캐비닛 보유 영양제 목록 조회",
            description = "캐비닛 기본 화면과 섭취 중인 영양제 추가 화면에서 사용할 보유 영양제 목록을 조회합니다. 삭제되지 않은 보유 상품만 추가일 내림차순으로 반환합니다."
    )
    @SecurityRequirement(name = "JWT TOKEN")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "캐비닛 보유 영양제 목록 조회 성공",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "isSuccess": true,
                                              "code": "SUCCESS200_1",
                                              "message": "요청이 성공적으로 처리되었습니다.",
                                              "result": {
                                                "memberNickname": "필굿",
                                                "totalCount": 2,
                                                "products": [
                                                  {
                                                    "memberProductId": 15,
                                                    "productId": 112,
                                                    "productName": "뉴트리코어 유기농 비타민D 1000IU",
                                                    "thumbnailImageUrl": "https://ipillgood-bucket.s3.ap-northeast-2.amazonaws.com/ingredients/2.png",
                                                    "isActiveIntake": true,
                                                    "activeProductId": 7,
                                                    "addedAt": "2026-07-01T10:20:00"
                                                  }
                                                ]
                                              }
                                            }
                                            """
                            )
                    )
            )
    })
    ApiResponse<CabinetResponse.ProductList> getProducts(
            @Parameter(hidden = true)
            Long memberId
    );

    @Operation(
            summary = "캐비닛 영양제 추가",
            description = "선택한 영양제를 내 캐비닛 보유 목록에 추가합니다. 복용 설정과 병용 금기 판단은 처리하지 않습니다."
    )
    @SecurityRequirement(name = "JWT TOKEN")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "201",
                    description = "캐비닛 영양제 추가 성공",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "isSuccess": true,
                                              "code": "SUCCESS201_1",
                                              "message": "리소스가 성공적으로 생성되었습니다.",
                                              "result": {
                                                "addedCount": 2,
                                                "addedProducts": [
                                                  {
                                                    "memberProductId": 21,
                                                    "productId": 118,
                                                    "brand": "솔가",
                                                    "productName": "솔가 비타민D3 1000IU",
                                                    "thumbnailImageUrl": "https://ipillgood-bucket.s3.ap-northeast-2.amazonaws.com/ingredients/2.png",
                                                    "addedAt": "2026-07-21T11:30:00"
                                                  }
                                                ]
                                              }
                                            }
                                            """
                            )
                    )
            )
    })
    ApiResponse<CabinetResponse.AddProducts> addProducts(
            @Parameter(hidden = true)
            Long memberId,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "캐비닛에 추가할 영양제 상품 ID 목록입니다.",
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "productIds": [
                                                118,
                                                124
                                              ]
                                            }
                                            """
                            )
                    )
            )
            CabinetRequest.AddProducts request
    );
}
