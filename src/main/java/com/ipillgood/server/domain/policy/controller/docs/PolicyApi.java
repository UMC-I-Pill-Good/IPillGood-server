package com.ipillgood.server.domain.policy.controller.docs;

import com.ipillgood.server.domain.policy.dto.PolicyResponse;
import com.ipillgood.server.domain.policy.entity.enums.PolicyDocumentType;
import com.ipillgood.server.global.apiPayload.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * 약관/정책 관련 API 문서
 * 최신 약관/정책 조회 (마이페이지)
 */
@Tag(name = "Policy API", description = "약관/정책 조회 관련 API")
public interface PolicyApi {

    @Operation(
            summary = "최신 약관/정책 조회",
            description = """
                    문서 유형 기준 최신 약관/정책을 본문과 함께 조회합니다. 로그인 없이 호출할 수 있습니다.
                    해당 유형의 활성 문서 중 effectiveAt이 가장 늦은 1건만 담기며, 없으면 빈 배열로 200입니다.
                    회원가입 약관 동의 화면과 마이페이지 > 설정의 약관 전문 화면에서 사용합니다."""
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "최신 약관/정책 조회 성공",
                    content = @Content(
                            mediaType = "application/json",
                            examples = {
                                    @ExampleObject(
                                            name = "조회 성공",
                                            value = """
                                                    {
                                                      "isSuccess": true,
                                                      "code": "POLICY200_2",
                                                      "message": "최신 약관/정책 조회에 성공했습니다.",
                                                      "result": {
                                                        "documents": [
                                                          {
                                                            "policyDocumentId": 1,
                                                            "documentType": "SERVICE_TERMS",
                                                            "title": "서비스 이용약관",
                                                            "content": "아필굿 서비스 이용약관\\n제1조(목적) ...",
                                                            "required": true,
                                                            "version": "v1.0",
                                                            "effectiveAt": "2026-08-01T00:00:00"
                                                          }
                                                        ]
                                                      }
                                                    }
                                                    """
                                    ),
                                    @ExampleObject(
                                            name = "해당 유형의 활성 문서 없음",
                                            value = """
                                                    {
                                                      "isSuccess": true,
                                                      "code": "POLICY200_2",
                                                      "message": "최신 약관/정책 조회에 성공했습니다.",
                                                      "result": {
                                                        "documents": []
                                                      }
                                                    }
                                                    """
                                    )
                            }
                    )
            )
    })
    ApiResponse<PolicyResponse.LatestDocuments> getLatest(
            @Parameter(
                    description = """
                            조회할 약관 유형입니다. 필수 값입니다.
                            SERVICE_TERMS(서비스 이용약관), PRIVACY_COLLECTION(개인정보 수집 및 이용),
                            HEALTH_INFO_COLLECTION(건강 정보 수집 및 이용), MARKETING(마케팅 정보 수신)
                            """,
                    example = "SERVICE_TERMS"
            )
            PolicyDocumentType documentType);
}
