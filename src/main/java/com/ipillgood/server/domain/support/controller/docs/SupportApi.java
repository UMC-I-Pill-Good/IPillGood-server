package com.ipillgood.server.domain.support.controller.docs;

import com.ipillgood.server.domain.support.dto.SupportResponse;
import com.ipillgood.server.domain.support.entity.enums.FaqCategory;
import com.ipillgood.server.global.apiPayload.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * 고객지원 관련 API 문서
 */
@Tag(name = "Support API", description = "FAQ/문의·고객센터 관련 API")
public interface SupportApi {

    @Operation(
            summary = "FAQ 목록 조회",
            description = """
                    활성 FAQ 목록을 조회합니다. category와 keyword는 모두 선택 값이고, 둘 다 주면 함께 만족하는 FAQ만 반환합니다.
                    조건에 맞는 FAQ가 없으면 빈 배열입니다.
                    """
    )
    @SecurityRequirement(name = "JWT TOKEN")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "FAQ 목록 조회 성공",
                    content = @Content(
                            mediaType = "application/json",
                            examples = {
                                    @ExampleObject(
                                            name = "조회 성공",
                                            value = """
                                                    {
                                                      "isSuccess": true,
                                                      "code": "SUPPORT200_1",
                                                      "message": "FAQ 목록 조회에 성공했습니다.",
                                                      "result": {
                                                        "faqs": [
                                                          {
                                                            "faqId": 1,
                                                            "category": "RECOMMENDATION_INGREDIENT",
                                                            "question": "영양제 추천은 어떤 기준으로 이루어지나요?",
                                                            "answer": "아필굿은 설문 조사 결과와 개인의 건강 정보, 섭취 이력 등을 종합적으로 분석해 추천해 드려요."
                                                          },
                                                          {
                                                            "faqId": 2,
                                                            "category": "INTAKE",
                                                            "question": "영양제를 언제 먹는 게 좋나요?",
                                                            "answer": "성분에 따라 달라요. 지용성 비타민은 식후에 드시는 것을 권장해요."
                                                          }
                                                        ]
                                                      }
                                                    }
                                                    """
                                    ),
                                    @ExampleObject(
                                            name = "조건에 맞는 FAQ 없음",
                                            value = """
                                                    {
                                                      "isSuccess": true,
                                                      "code": "SUPPORT200_1",
                                                      "message": "FAQ 목록 조회에 성공했습니다.",
                                                      "result": {
                                                        "faqs": []
                                                      }
                                                    }
                                                    """
                                    )
                            }
                    )
            )
    })
    ApiResponse<SupportResponse.FaqList> getFaqs(
            @Parameter(
                    description = """
                            조회할 FAQ 카테고리입니다. 생략하면 전체 카테고리를 조회합니다.
                            RECOMMENDATION_INGREDIENT(추천/성분), INTAKE(복용/섭취), NOTIFICATION(알림), ETC(기타)
                            """,
                    example = "INTAKE"
            )
            FaqCategory category,
            @Parameter(
                    description = "질문에 포함된 검색어입니다. 생략하면 키워드 필터를 적용하지 않습니다.",
                    example = "영양제"
            )
            String keyword);

    @Operation(
            summary = "문의/고객센터 조회",
            description = """
                    고객센터 화면에 필요한 정보를 한 번에 조회합니다.
                    faqs는 displayOrder 기준 상위 3개 미리보기이며(없으면 빈 배열), 전체 목록은 FAQ 목록 조회 API를 씁니다.
                    """
    )
    @SecurityRequirement(name = "JWT TOKEN")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "문의/고객센터 조회 성공",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "isSuccess": true,
                                              "code": "SUPPORT200_2",
                                              "message": "문의/고객센터 조회에 성공했습니다.",
                                              "result": {
                                                "faqs": [
                                                  {
                                                    "faqId": 1,
                                                    "category": "RECOMMENDATION_INGREDIENT",
                                                    "question": "영양제 추천은 어떤 기준으로 이루어지나요?",
                                                    "answer": "아필굿은 설문 조사 결과와 개인의 건강 정보, 섭취 이력 등을 종합적으로 분석해 추천해 드려요."
                                                  },
                                                  {
                                                    "faqId": 2,
                                                    "category": "INTAKE",
                                                    "question": "영양제를 언제 먹는 게 좋나요?",
                                                    "answer": "성분에 따라 달라요. 지용성 비타민은 식후에 드시는 것을 권장해요."
                                                  },
                                                  {
                                                    "faqId": 3,
                                                    "category": "NOTIFICATION",
                                                    "question": "복용 알림은 어떻게 설정하나요?",
                                                    "answer": "마이페이지 > 알림 설정에서 복용 시간대별로 알림을 켤 수 있어요."
                                                  }
                                                ],
                                                "contactEmail": "ipillgood.official@gmail.com",
                                                "operatingHours": "평일 09:00 ~ 18:00",
                                                "closedDays": "주말 및 공휴일 휴무"
                                              }
                                            }
                                            """
                            )
                    )
            )
    })
    ApiResponse<SupportResponse.Info> getSupportInfo();
}
