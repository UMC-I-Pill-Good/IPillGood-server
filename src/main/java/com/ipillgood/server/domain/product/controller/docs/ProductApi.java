package com.ipillgood.server.domain.product.controller.docs;

import com.ipillgood.server.domain.product.dto.ProductResponse;
import com.ipillgood.server.global.apiPayload.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Product API", description = "영양제 상품 관련 API")
public interface ProductApi {

    @Operation(
            summary = "상품 상세 조회",
            description = "상품 상세 화면에 필요한 기본 정보, 대표 이미지, 후기 요약(평점·개수), "
                    + "과대광고 위험 성분 여부 및 목록을 조회합니다. 대표 이미지는 성분이 1개인 경우 "
                    + "해당 성분 이미지를, 2개 이상인 경우 기타 대표 이미지를 사용합니다."
    )
    @SecurityRequirement(name = "JWT TOKEN")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "상품 상세 조회 성공",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "isSuccess": true,
                                              "code": "PRODUCT200_1",
                                              "message": "상품 상세 정보를 성공적으로 조회했습니다.",
                                              "result": {
                                                "productId": 1,
                                                "productName": "메가도스 비타민C 1000",
                                                "brand": "아이필굿",
                                                "imageUrl": "https://ipillgood-bucket.s3.ap-northeast-2.amazonaws.com/ingredients/1.png",
                                                "description": "고함량 비타민C로 항산화와 면역에 도움을 주는 영양제입니다.",
                                                "purchaseUrl": "https://smartstore.naver.com/ipillgood/products/123456",
                                                "mfdsCertified": true,
                                                "ratingAverage": 4.5,
                                                "reviewCount": 128,
                                                "adClaimRisk": true,
                                                "adClaimRiskIngredients": ["비타민C"]
                                              }
                                            }
                                            """
                            )
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "상품 없음",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "isSuccess": false,
                                              "code": "PRODUCT404_1",
                                              "message": "해당 상품은 존재하지 않습니다.",
                                              "result": null
                                            }
                                            """
                            )
                    )
            )
    })
    ApiResponse<ProductResponse.ProductInfo> getProductInfo(
            @Parameter(
                    description = "조회할 상품 ID",
                    schema = @Schema(type = "integer", format = "int64", example = "1")
            )
            Long productId
    );

    @Operation(
            summary = "상품 성분 정보 조회",
            description = "상품에 포함된 성분 목록과 각 성분의 이름·설명·이미지·효능 키워드를 조회합니다. "
                    + "효능 키워드가 없는 성분은 빈 배열로 반환됩니다."
    )
    @SecurityRequirement(name = "JWT TOKEN")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "상품 성분 정보 조회 성공",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "isSuccess": true,
                                              "code": "PRODUCT200_2",
                                              "message": "상품의 성분 정보를 성공적으로 조회했습니다.",
                                              "result": {
                                                "productId": 1,
                                                "ingredientCount": 2,
                                                "ingredientInfos": [
                                                  {
                                                    "ingredientId": 2,
                                                    "name": "비타민 D",
                                                    "description": "칼슘 흡수와 뼈 건강에 도움을 주는 성분입니다.",
                                                    "imageUrl": "https://ipillgood-bucket.s3.ap-northeast-2.amazonaws.com/ingredients/2.png",
                                                    "effectKeywords": ["뼈 건강", "면역"]
                                                  },
                                                  {
                                                    "ingredientId": 5,
                                                    "name": "비타민 C",
                                                    "description": "항산화와 면역에 도움을 주는 성분입니다.",
                                                    "imageUrl": "https://ipillgood-bucket.s3.ap-northeast-2.amazonaws.com/ingredients/5.png",
                                                    "effectKeywords": []
                                                  }
                                                ]
                                              }
                                            }
                                            """
                            )
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "상품 없음",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "isSuccess": false,
                                              "code": "PRODUCT404_1",
                                              "message": "해당 상품은 존재하지 않습니다.",
                                              "result": null
                                            }
                                            """
                            )
                    )
            )
    })
    ApiResponse<ProductResponse.ProductIngredientsInfo> getProductIngredients(
            @Parameter(
                    description = "조회할 상품 ID",
                    schema = @Schema(type = "integer", format = "int64", example = "1")
            )
            Long productId
    );

    @Operation(
            summary = "상품 성분 궁합 조회 (캐비닛 기준)",
            description = "로그인 사용자가 캐비닛에 보유한 영양제 성분 중, 조회 상품의 성분과 함께 섭취하면 "
                    + "좋은 조합(GOOD)·주의가 필요한 조합(CAUTION)에 해당하는 성분을 반환합니다. "
                    + "상품 자체에 이미 포함된 성분은 제외되며, ownedProductCount는 '보유 중인 영양제 N개 기준' "
                    + "문구에 사용됩니다."
    )
    @SecurityRequirement(name = "JWT TOKEN")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "상품 성분 궁합 조회 성공",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "isSuccess": true,
                                              "code": "PRODUCT200_3",
                                              "message": "상품의 성분 궁합 정보를 성공적으로 조회했습니다.",
                                              "result": {
                                                "productId": 1,
                                                "ownedProductCount": 6,
                                                "goodCombinations": [
                                                  {
                                                    "targetIngredientId": 2,
                                                    "targetIngredientName": "비타민 D"
                                                  }
                                                ],
                                                "cautionCombinations": [
                                                  {
                                                    "targetIngredientId": 5,
                                                    "targetIngredientName": "칼슘"
                                                  }
                                                ]
                                              }
                                            }
                                            """
                            )
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "상품 없음",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "isSuccess": false,
                                              "code": "PRODUCT404_1",
                                              "message": "해당 상품은 존재하지 않습니다.",
                                              "result": null
                                            }
                                            """
                            )
                    )
            )
    })
    ApiResponse<ProductResponse.ProductCombinations> getProductCombinations(
            @Parameter(hidden = true)
            Long memberId,
            @Parameter(
                    description = "조회할 상품 ID",
                    schema = @Schema(type = "integer", format = "int64", example = "1")
            )
            Long productId
    );
}
