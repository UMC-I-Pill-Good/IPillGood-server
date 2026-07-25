package com.ipillgood.server.domain.search.controller.docs;

import com.ipillgood.server.domain.healthconcern.entity.enums.MajorCategory;
import com.ipillgood.server.domain.search.dto.ProductSearchRequest;
import com.ipillgood.server.domain.search.dto.ProductSearchResponse;
import com.ipillgood.server.domain.search.entity.enums.ProductSearchSort;
import com.ipillgood.server.global.enums.AgeGroup;
import com.ipillgood.server.global.enums.Gender;
import com.ipillgood.server.global.apiPayload.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

import java.util.List;

@Tag(name = "Search API", description = "영양제 상품 검색 관련 API")
public interface ProductSearchApi {

    @Operation(
            summary = "영양제 상품 목록 조회",
            description = "검색어, 필터, 정렬 조건에 맞는 영양제 상품 목록을 커서 페이징으로 조회합니다. "
                    + "검색어가 없으면 검색/랭킹 기본 목록으로 사용합니다."
    )
    @SecurityRequirement(name = "JWT TOKEN")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "영양제 상품 목록 조회 성공",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "isSuccess": true,
                                              "code": "SEARCH200_1",
                                              "message": "영양제 상품 목록 조회에 성공했습니다.",
                                              "result": {
                                                "keyword": "비타민",
                                                "products": [
                                                  {
                                                    "productId": 101,
                                                    "productName": "비타민D 1000IU",
                                                    "brand": "IPG",
                                                    "thumbnailImageUrl": "https://ipillgood-bucket.s3.ap-northeast-2.amazonaws.com/ingredients/vitamin-d.png",
                                                    "mfdsCertified": true,
                                                    "ingredientNames": ["비타민 D"],
                                                    "averageRating": 4.5,
                                                    "reviewCount": 12
                                                  }
                                                ],
                                                "size": 20,
                                                "totalCount": 42,
                                                "hasNext": true,
                                                "nextCursor": "101|12"
                                              }
                                            }
                                            """
                            )
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "파라미터 형식/제약 오류(COMMON400_1) 또는 커서 오류(SEARCH400_1)",
                    content = @Content(
                            mediaType = "application/json",
                            examples = {
                                    @ExampleObject(
                                            name = "파라미터 형식/제약 오류",
                                            value = """
                                                    {
                                                      "isSuccess": false,
                                                      "code": "COMMON400_1",
                                                      "message": "잘못된 요청입니다.",
                                                      "result": {
                                                        "size": "1에서 100 사이여야 합니다"
                                                      }
                                                    }
                                                    """
                                    ),
                                    @ExampleObject(
                                            name = "커서 오류",
                                            value = """
                                                    {
                                                      "isSuccess": false,
                                                      "code": "SEARCH400_1",
                                                      "message": "검색 커서가 올바르지 않습니다.",
                                                      "result": null
                                                    }
                                                    """
                                    )
                            }
                    )
            )
    })
    ApiResponse<ProductSearchResponse.ProductSearch> searchProducts(
            @Parameter(hidden = true)
            Long memberId,
            @Parameter(
                    description = "검색어입니다. trim 후 빈 문자열이면 적용하지 않으며, 최대 100자입니다.",
                    example = "비타민"
            )
            @Size(max = 100) String keyword,
            @Parameter(
                    description = "정렬 기준입니다. 기본값 REVIEW_COUNT.",
                    example = "REVIEW_COUNT"
            )
            ProductSearchSort sort,
            @Parameter(
                    description = "연령대 필터 목록(콤마 구분)입니다. ALL(전체)이 포함되거나 생략되면 연령 필터를 적용하지 않습니다.",
                    example = "TWENTIES,THIRTIES"
            )
            List<AgeGroup> ageGroups,
            @Parameter(
                    description = "성별 필터입니다.",
                    example = "FEMALE"
            )
            Gender gender,
            @Parameter(
                    description = "식약처 인증 제품만 조회 여부입니다. true만 필터를 적용하며, false·미지정은 필터를 적용하지 않습니다.",
                    example = "true"
            )
            Boolean mfdsCertified,
            @Parameter(
                    description = "건강 상태 대분류 필터 목록(콤마 구분)입니다.",
                    example = "NERVOUS_SYSTEM,IMMUNE_SYSTEM"
            )
            List<MajorCategory> healthConcernMajorCategories,
            @Parameter(
                    description = "페이지 크기입니다. 1~100, 기본값 20.",
                    example = "20"
            )
            @Min(1) @Max(100) Integer size,
            @Parameter(
                    description = "커서입니다. 이전 응답의 nextCursor 값({상품ID}|{정렬값})을 그대로 전달합니다. "
                            + "커서에는 정렬 기준이 없으므로 같은 sort와 함께 보내야 하며, sort를 바꾸면 커서 없이 처음부터 조회하세요.",
                    example = "101|12"
            )
            String cursor
    );

    @Operation(
            summary = "최근 검색어 조회",
            description = "로그인한 회원의 최근 검색어를 최신순으로 최대 10개 조회합니다."
    )
    @SecurityRequirement(name = "JWT TOKEN")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "최근 검색어 조회 성공",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "isSuccess": true,
                                              "code": "SEARCH200_2",
                                              "message": "최근 검색어 조회에 성공했습니다.",
                                              "result": {
                                                "keywords": [
                                                  {
                                                    "keywordId": 1,
                                                    "keyword": "비타민",
                                                    "searchedAt": "2026-07-20T15:00:00"
                                                  }
                                                ]
                                              }
                                            }
                                            """
                            )
                    )
            )
    })
    ApiResponse<ProductSearchResponse.RecentSearchKeywords> getRecentSearchKeywords(
            @Parameter(hidden = true)
            Long memberId
    );

    @Operation(
            summary = "최근 검색어 저장",
            description = "검색 실행 시 최근 검색어를 저장합니다. 이미 있는 검색어면 검색 일시만 최신화합니다."
    )
    @SecurityRequirement(name = "JWT TOKEN")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "201",
                    description = "최근 검색어 저장 성공",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "isSuccess": true,
                                              "code": "SEARCH201_1",
                                              "message": "최근 검색어 저장에 성공했습니다.",
                                              "result": {
                                                "keywordId": 1,
                                                "keyword": "비타민",
                                                "searchedAt": "2026-07-20T15:00:00"
                                              }
                                            }
                                            """
                            )
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "검색어 검증 실패(공백이거나 100자 초과)",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "isSuccess": false,
                                              "code": "COMMON400_1",
                                              "message": "잘못된 요청입니다.",
                                              "result": {
                                                "keyword": "공백일 수 없습니다"
                                              }
                                            }
                                            """
                            )
                    )
            )
    })
    ApiResponse<ProductSearchResponse.RecentSearchKeyword> storeRecentSearchKeyword(
            @Parameter(hidden = true)
            Long memberId,
            @Valid ProductSearchRequest.RecentSearchKeyword request
    );
}
