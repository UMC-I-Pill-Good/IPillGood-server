package com.ipillgood.server.domain.cabinet.controller.docs;

import com.ipillgood.server.domain.cabinet.dto.CabinetRequest;
import com.ipillgood.server.domain.cabinet.dto.CabinetResponse;
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
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "캐비닛 후보 검색 조건 오류",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "isSuccess": false,
                                              "code": "CABINET400_1",
                                              "message": "캐비닛 검색 조건이 올바르지 않습니다.",
                                              "result": null
                                            }
                                            """
                            )
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "초기 설문 미완료",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "isSuccess": false,
                                              "code": "CABINET403_1",
                                              "message": "초기 설문을 완료해야 이용할 수 있습니다.",
                                              "result": null
                                            }
                                            """
                            )
                    )
            )
    })
    ApiResponse<CabinetResponse.ProductCandidates> getProductCandidates(
            @Parameter(hidden = true)
            Long memberId,
            @Parameter(
                    description = "검색어입니다.",
                    schema = @Schema(type = "string", example = "비타민D")
            )
            String keyword,
            @Parameter(
                    description = "정렬 기준입니다.",
                    schema = @Schema(
                            type = "string",
                            allowableValues = {"REVIEW_COUNT_DESC", "RATING_DESC"},
                            defaultValue = "REVIEW_COUNT_DESC",
                            example = "REVIEW_COUNT_DESC"
                    )
            )
            String sort,
            @Parameter(
                    description = "페이지 번호입니다.",
                    schema = @Schema(type = "integer", defaultValue = "0", example = "0")
            )
            String page,
            @Parameter(
                    description = "페이지 크기입니다.",
                    schema = @Schema(type = "integer", defaultValue = "20", example = "20")
            )
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
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "초기 설문 미완료",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "isSuccess": false,
                                              "code": "CABINET403_1",
                                              "message": "초기 설문을 완료해야 이용할 수 있습니다.",
                                              "result": null
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
            summary = "후기 작성 유도 대상 조회",
            description = "섭취 중으로 등록한 지 30일이 지났고 아직 후기를 작성하지 않은 영양제를 조회합니다. 닫힘 처리된 배너 대상은 제외합니다."
    )
    @SecurityRequirement(name = "JWT TOKEN")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "후기 작성 유도 대상 조회 성공",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "isSuccess": true,
                                              "code": "SUCCESS200_1",
                                              "message": "요청이 성공적으로 처리되었습니다.",
                                              "result": {
                                                "duePrompts": [
                                                  {
                                                    "activeProductId": 7,
                                                    "productId": 112,
                                                    "productName": "뉴트리코어 유기농 비타민D 1000IU"
                                                  }
                                                ]
                                              }
                                            }
                                            """
                            )
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "초기 설문 미완료",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "isSuccess": false,
                                              "code": "CABINET403_1",
                                              "message": "초기 설문을 완료해야 이용할 수 있습니다.",
                                              "result": null
                                            }
                                            """
                            )
                    )
            )
    })
    ApiResponse<CabinetResponse.ReviewPrompts> getDueReviewPrompts(
            @Parameter(hidden = true)
            Long memberId
    );

    @Operation(
            summary = "후기 작성 유도 배너 닫힘 기록",
            description = "후기 작성 유도 인앱 배너를 닫은 상태로 저장합니다. 이미 닫힌 대상은 기존 닫힘 일시를 반환합니다."
    )
    @SecurityRequirement(name = "JWT TOKEN")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "후기 작성 유도 배너 닫힘 기록 성공",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "isSuccess": true,
                                              "code": "SUCCESS200_1",
                                              "message": "요청이 성공적으로 처리되었습니다.",
                                              "result": {
                                                "activeProductId": 7,
                                                "dismissedAt": "2026-07-21T11:30:00"
                                              }
                                            }
                                            """
                            )
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "후기 작성 유도 대상 ID 오류",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "isSuccess": false,
                                              "code": "CABINET400_4",
                                              "message": "후기 작성 유도 대상 ID가 올바르지 않습니다.",
                                              "result": null
                                            }
                                            """
                            )
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "초기 설문 미완료",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "isSuccess": false,
                                              "code": "CABINET403_1",
                                              "message": "초기 설문을 완료해야 이용할 수 있습니다.",
                                              "result": null
                                            }
                                            """
                            )
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "후기 작성 유도 대상 없음",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "isSuccess": false,
                                              "code": "CABINET404_3",
                                              "message": "후기 작성 유도 대상을 찾을 수 없습니다.",
                                              "result": null
                                            }
                                            """
                            )
                    )
            )
    })
    ApiResponse<CabinetResponse.ReviewPromptDismissed> dismissReviewPrompt(
            @Parameter(hidden = true)
            Long memberId,
            @Parameter(
                    description = "닫힘 처리할 활성 섭취 중 상품 ID",
                    schema = @Schema(type = "integer", format = "int64", example = "7")
            )
            Long activeProductId
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
                                                    "description": "칼슘 흡수를 도와 뼈와 치아를 튼튼하게 하고 면역 체계를 강화합니다.",
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
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "캐비닛 보유 상품 ID 오류",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "isSuccess": false,
                                              "code": "CABINET400_3",
                                              "message": "캐비닛 보유 상품 ID가 올바르지 않습니다.",
                                              "result": null
                                            }
                                            """
                            )
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "초기 설문 미완료",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "isSuccess": false,
                                              "code": "CABINET403_1",
                                              "message": "초기 설문을 완료해야 이용할 수 있습니다.",
                                              "result": null
                                            }
                                            """
                            )
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "캐비닛 보유 상품 없음",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "isSuccess": false,
                                              "code": "CABINET404_2",
                                              "message": "캐비닛 보유 상품을 찾을 수 없습니다.",
                                              "result": null
                                            }
                                            """
                            )
                    )
            )
    })
    ApiResponse<CabinetResponse.ProductDetail> getProduct(
            @Parameter(hidden = true)
            Long memberId,
            @Parameter(
                    description = "조회할 회원 캐비닛 상품 ID",
                    schema = @Schema(type = "integer", format = "int64", example = "15")
            )
            Long memberProductId
    );

    @Operation(
            summary = "캐비닛 영양제 추가",
            description = "선택한 영양제를 내 캐비닛 보유 목록에 추가합니다. 복용 설정과 병용 금기 판단은 처리하지 않습니다."
    )
    @SecurityRequirement(name = "JWT TOKEN")
    @RequestBody(
            required = true,
            description = "캐비닛에 추가할 영양제 상품 ID 목록을 전달합니다.",
            content = @Content(
                    schema = @Schema(implementation = CabinetRequest.AddProducts.class),
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
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "캐비닛 추가 상품 목록 오류",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "isSuccess": false,
                                              "code": "CABINET400_2",
                                              "message": "캐비닛에 추가할 상품 목록이 올바르지 않습니다.",
                                              "result": null
                                            }
                                            """
                            )
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "초기 설문 미완료",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "isSuccess": false,
                                              "code": "CABINET403_1",
                                              "message": "초기 설문을 완료해야 이용할 수 있습니다.",
                                              "result": null
                                            }
                                            """
                            )
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "캐비닛 추가 대상 상품 없음",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "isSuccess": false,
                                              "code": "CABINET404_1",
                                              "message": "캐비닛에 추가할 상품을 찾을 수 없습니다.",
                                              "result": null
                                            }
                                            """
                            )
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "409",
                    description = "이미 보유 중인 영양제",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "isSuccess": false,
                                              "code": "CABINET409_1",
                                              "message": "이미 캐비닛에 등록된 영양제입니다.",
                                              "result": null
                                            }
                                            """
                            )
                    )
            )
    })
    ApiResponse<CabinetResponse.AddProducts> addProducts(
            @Parameter(hidden = true)
            Long memberId,
            CabinetRequest.AddProducts request
    );

    @Operation(
            summary = "캐비닛 영양제 복수 삭제",
            description = "선택한 캐비닛 보유 영양제를 삭제합니다. 섭취 중인 영양제는 함께 중단 처리하고 과거 복용 기록은 유지합니다."
    )
    @SecurityRequirement(name = "JWT TOKEN")
    @RequestBody(
            required = true,
            description = "삭제할 회원 캐비닛 상품 ID 목록을 전달합니다.",
            content = @Content(
                    schema = @Schema(implementation = CabinetRequest.DeleteProducts.class),
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
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "캐비닛 보유 상품 ID 오류",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "isSuccess": false,
                                              "code": "CABINET400_3",
                                              "message": "캐비닛 보유 상품 ID가 올바르지 않습니다.",
                                              "result": null
                                            }
                                            """
                            )
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "초기 설문 미완료",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "isSuccess": false,
                                              "code": "CABINET403_1",
                                              "message": "초기 설문을 완료해야 이용할 수 있습니다.",
                                              "result": null
                                            }
                                            """
                            )
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "캐비닛 보유 상품 없음",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "isSuccess": false,
                                              "code": "CABINET404_2",
                                              "message": "캐비닛 보유 상품을 찾을 수 없습니다.",
                                              "result": null
                                            }
                                            """
                            )
                    )
            )
    })
    ApiResponse<CabinetResponse.DeleteProducts> deleteProducts(
            @Parameter(hidden = true)
            Long memberId,
            CabinetRequest.DeleteProducts request
    );
}
