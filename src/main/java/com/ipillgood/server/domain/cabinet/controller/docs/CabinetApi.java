package com.ipillgood.server.domain.cabinet.controller.docs;

import com.ipillgood.server.domain.cabinet.dto.CabinetRequest;
import com.ipillgood.server.domain.cabinet.dto.CabinetResponse;
import com.ipillgood.server.global.apiPayload.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Cabinet API", description = "캐비닛 관련 API")
public interface CabinetApi {

    @Operation(
            summary = "캐비닛 추가 후보 검색",
            description = "캐비닛 추가 화면에서 상품 후보를 검색합니다. 브랜드명, 상품명, 포함 성분명을 대상으로 조회하고 보유 여부를 함께 반환합니다."
    )
    @SecurityRequirement(name = "JWT TOKEN")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "캐비닛 추가 후보 검색 성공",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "isSuccess": true,
                                              "code": "SUCCESS200_1",
                                              "message": "요청이 성공적으로 처리되었습니다.",
                                              "result": {
                                                "keyword": "비타민D",
                                                "sort": "REVIEW_COUNT_DESC",
                                                "page": 0,
                                                "size": 20,
                                                "totalCount": 42,
                                                "hasNext": true,
                                                "products": [
                                                  {
                                                    "productId": 112,
                                                    "brand": "뉴트리코어",
                                                    "productName": "뉴트리코어 유기농 비타민D 1000IU",
                                                    "thumbnailImageUrl": "https://ipillgood-bucket.s3.ap-northeast-2.amazonaws.com/ingredients/2.png",
                                                    "averageRating": 4.7,
                                                    "reviewCount": 128,
                                                    "ingredientTags": [
                                                      "뼈 건강",
                                                      "면역"
                                                    ],
                                                    "isOwned": true,
                                                    "isSelectable": false
                                                  }
                                                ]
                                              }
                                            }
                                            """
                            )
                    )
            )
    })
    ApiResponse<CabinetResponse.ProductCandidates> getProductCandidates(
            @Parameter(hidden = true)
            Long memberId,
            @Parameter(description = "검색어입니다.")
            String keyword,
            @Parameter(
                    description = "정렬 기준입니다.",
                    schema = @Schema(
                            allowableValues = {"REVIEW_COUNT_DESC", "RATING_DESC"},
                            defaultValue = "REVIEW_COUNT_DESC"
                    )
            )
            String sort,
            @Parameter(description = "페이지 번호입니다.", schema = @Schema(defaultValue = "0"))
            String page,
            @Parameter(description = "페이지 크기입니다.", schema = @Schema(defaultValue = "20"))
            String size
    );

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
            summary = "캐비닛 개별 영양제 조회",
            description = "캐비닛 개별 영양제 상세 모달에 필요한 상품 요약, 포함 성분, 섭취 중 설정 정보를 조회합니다. 섭취 중이 아니면 activeProduct는 null로 반환합니다."
    )
    @SecurityRequirement(name = "JWT TOKEN")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "캐비닛 개별 영양제 조회 성공",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "isSuccess": true,
                                              "code": "SUCCESS200_1",
                                              "message": "요청이 성공적으로 처리되었습니다.",
                                              "result": {
                                                "memberProductId": 15,
                                                "productId": 112,
                                                "brand": "뉴트리코어",
                                                "productName": "뉴트리코어 유기농 비타민D 1000IU",
                                                "thumbnailImageUrl": "https://ipillgood-bucket.s3.ap-northeast-2.amazonaws.com/ingredients/2.png",
                                                "isActiveIntake": true,
                                                "hasMyReview": false,
                                                "ingredients": [
                                                  {
                                                    "ingredientId": 2,
                                                    "name": "비타민 D",
                                                    "imageUrl": "https://ipillgood-bucket.s3.ap-northeast-2.amazonaws.com/ingredients/2.png",
                                                    "description": "칼슘 흡수와 뼈 건강에 도움을 주는 성분입니다.",
                                                    "effectTags": [
                                                      "뼈 건강"
                                                    ]
                                                  }
                                                ],
                                                "activeProduct": {
                                                  "activeProductId": 7,
                                                  "startedOn": "2026-07-01",
                                                  "intakeDayCount": 21,
                                                  "notificationEnabled": true,
                                                  "intakeTime": "08:30",
                                                  "frequency": "EVERY_DAY",
                                                  "frequencyLabel": "매일",
                                                  "frequencyIntervalDays": 1,
                                                  "scheduleAnchorOn": "2026-07-01"
                                                }
                                              }
                                            }
                                            """
                            )
                    )
            )
    })
    ApiResponse<CabinetResponse.ProductDetail> getProduct(
            @Parameter(hidden = true)
            Long memberId,
            @Parameter(description = "조회할 회원 캐비닛 상품 ID")
            Long memberProductId
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

    @Operation(
            summary = "캐비닛 영양제 복수 삭제",
            description = "선택한 캐비닛 보유 영양제를 삭제합니다. 섭취 중인 영양제는 함께 중단 처리하고 과거 복용 기록은 유지합니다."
    )
    @SecurityRequirement(name = "JWT TOKEN")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "캐비닛 영양제 복수 삭제 성공",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "isSuccess": true,
                                              "code": "SUCCESS200_1",
                                              "message": "요청이 성공적으로 처리되었습니다.",
                                              "result": {
                                                "deletedCount": 2,
                                                "deletedProducts": [
                                                  {
                                                    "memberProductId": 15,
                                                    "productId": 112,
                                                    "productName": "뉴트리코어 유기농 비타민D 1000IU",
                                                    "wasActiveIntake": true,
                                                    "stoppedActiveProductId": 7
                                                  },
                                                  {
                                                    "memberProductId": 16,
                                                    "productId": 124,
                                                    "productName": "헬로바이오 맥스 비타민C 3000",
                                                    "wasActiveIntake": false,
                                                    "stoppedActiveProductId": null
                                                  }
                                                ]
                                              }
                                            }
                                            """
                            )
                    )
            )
    })
    ApiResponse<CabinetResponse.DeleteProducts> deleteProducts(
            @Parameter(hidden = true)
            Long memberId,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "삭제할 회원 캐비닛 상품 ID 목록입니다.",
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "memberProductIds": [
                                                15,
                                                16
                                              ]
                                            }
                                            """
                            )
                    )
            )
            CabinetRequest.DeleteProducts request
    );
}
