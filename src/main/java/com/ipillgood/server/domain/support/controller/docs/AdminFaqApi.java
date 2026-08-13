package com.ipillgood.server.domain.support.controller.docs;

import com.ipillgood.server.domain.support.dto.AdminFaqRequest;
import com.ipillgood.server.domain.support.dto.AdminFaqResponse;
import com.ipillgood.server.global.apiPayload.ApiResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

/**
 * 관리자 FAQ 관리 API 문서
 */
@Tag(name = "Admin FAQ API", description = "관리자 FAQ 관리 API")
public interface AdminFaqApi {

    @Operation(summary = "FAQ 목록 조회",
            description = "관리자 페이지에서 FAQ를 제목과 카테고리 조건으로 조회합니다.")
    @SecurityRequirement(name = "JWT TOKEN")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "FAQ 목록 조회 성공",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "isSuccess": true,
                                              "code": "SUPPORT200_3",
                                              "message": "FAQ 목록 조회에 성공했습니다.",
                                              "result": {
                                                "faqs": [
                                                  {
                                                    "faqId": 12,
                                                    "question": "회원 탈퇴는 어떻게 하나요?",
                                                    "answer": "마이페이지 > 회원 정보 > 탈퇴하기에서 진행할 수 있습니다.",
                                                    "category": "ETC",
                                                    "updatedAt": "2026-07-06T10:00:00"
                                                  }
                                                ],
                                                "totalCount": 12,
                                                "totalPages": 1,
                                                "currentPage": 0
                                              }
                                            }
                                            """
                            )
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "category가 허용된 값이 아니거나 page/size 값이 올바르지 않음",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "isSuccess": false,
                                              "code": "COMMON400_2",
                                              "message": "검증에 실패했습니다.",
                                              "result": null
                                            }
                                            """
                            )
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "ADMIN 권한이 없음",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "isSuccess": false,
                                              "code": "COMMON403_1",
                                              "message": "요청이 거부되었습니다.",
                                              "result": null
                                            }
                                            """
                            )
                    )
            )
    })
    ApiResponse<AdminFaqResponse.FaqList> getFaqs(
            @Parameter(description = "질문 검색어", required = false, example = "탈퇴")
            String keyword,

            @Parameter(description = "카테고리 필터", required = false, example = "ETC")
            String category,

            @Parameter(description = "페이지 번호 (기본값 0)", required = false, example = "0")
            String page,

            @Parameter(description = "페이지 크기 (기본값 20)", required = false, example = "20")
            String size
    );

    @Operation(summary = "FAQ 추가",
            description = "관리자가 새 FAQ를 추가합니다.")
    @SecurityRequirement(name = "JWT TOKEN")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "201",
                    description = "FAQ 등록 성공",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "isSuccess": true,
                                              "code": "SUPPORT201_1",
                                              "message": "FAQ 등록에 성공했습니다.",
                                              "result": {
                                                "faqId": 13,
                                                "question": "복용 알림은 어떻게 설정하나요?",
                                                "answer": "마이페이지 > 설정 > 복용 시간 알림에서 설정할 수 있습니다.",
                                                "category": "NOTIFICATION",
                                                "createdAt": "2026-07-22T18:00:00"
                                              }
                                            }
                                            """
                            )
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "필수값 누락, 길이 초과, 허용되지 않은 카테고리",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "isSuccess": false,
                                              "code": "COMMON400_1",
                                              "message": "잘못된 요청입니다.",
                                              "result": null
                                            }
                                            """
                            )
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "ADMIN 권한이 없음",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "isSuccess": false,
                                              "code": "COMMON403_1",
                                              "message": "요청이 거부되었습니다.",
                                              "result": null
                                            }
                                            """
                            )
                    )
            )
    })
    ApiResponse<AdminFaqResponse.FaqCreated> createFaq(@Valid AdminFaqRequest.Upsert request);

    @Operation(summary = "FAQ 수정",
            description = "관리자가 기존 FAQ의 질문, 답변, 카테고리를 수정합니다.")
    @SecurityRequirement(name = "JWT TOKEN")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "FAQ 수정 성공",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "isSuccess": true,
                                              "code": "SUPPORT200_4",
                                              "message": "FAQ 수정에 성공했습니다.",
                                              "result": {
                                                "faqId": 13,
                                                "question": "복용 알림은 어떻게 설정하나요?",
                                                "answer": "마이페이지 > 설정 > 복용 시간 알림에서 설정할 수 있습니다.",
                                                "category": "NOTIFICATION",
                                                "updatedAt": "2026-07-22T18:10:00"
                                              }
                                            }
                                            """
                            )
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "필수값 누락, 길이 초과, 허용되지 않은 카테고리",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "isSuccess": false,
                                              "code": "COMMON400_1",
                                              "message": "잘못된 요청입니다.",
                                              "result": null
                                            }
                                            """
                            )
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "ADMIN 권한이 없음",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "isSuccess": false,
                                              "code": "COMMON403_1",
                                              "message": "요청이 거부되었습니다.",
                                              "result": null
                                            }
                                            """
                            )
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "해당 faqId의 FAQ가 없음",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "isSuccess": false,
                                              "code": "COMMON404_1",
                                              "message": "요청한 리소스를 찾을 수 없습니다.",
                                              "result": null
                                            }
                                            """
                            )
                    )
            )
    })
    ApiResponse<AdminFaqResponse.FaqUpdated> updateFaq(
            @Parameter(description = "수정할 FAQ ID", required = true, example = "13")
            Long faqId,

            @Valid AdminFaqRequest.Upsert request
    );

    @Operation(summary = "FAQ 삭제",
            description = "관리자가 FAQ를 영구 삭제합니다.")
    @SecurityRequirement(name = "JWT TOKEN")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "FAQ 삭제 성공",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "isSuccess": true,
                                              "code": "SUPPORT200_5",
                                              "message": "삭제 처리되었습니다.",
                                              "result": null
                                            }
                                            """
                            )
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "ADMIN 권한이 없음",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "isSuccess": false,
                                              "code": "COMMON403_1",
                                              "message": "요청이 거부되었습니다.",
                                              "result": null
                                            }
                                            """
                            )
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "해당 faqId의 FAQ가 없음",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "isSuccess": false,
                                              "code": "COMMON404_1",
                                              "message": "요청한 리소스를 찾을 수 없습니다.",
                                              "result": null
                                            }
                                            """
                            )
                    )
            )
    })
    ApiResponse<Void> deleteFaq(
            @Parameter(description = "삭제할 FAQ ID", required = true, example = "13")
            Long faqId
    );
}
