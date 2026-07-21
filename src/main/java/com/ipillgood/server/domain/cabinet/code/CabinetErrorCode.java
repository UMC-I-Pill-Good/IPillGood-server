package com.ipillgood.server.domain.cabinet.code;

import com.ipillgood.server.global.apiPayload.code.BaseErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum CabinetErrorCode implements BaseErrorCode {

    // 400 BAD_REQUEST - 캐비닛 요청 값 검증
    PRODUCT_CANDIDATE_SEARCH_CONDITION_INVALID(HttpStatus.BAD_REQUEST, "CABINET400_1", "캐비닛 검색 조건이 올바르지 않습니다."),
    ADD_PRODUCT_LIST_INVALID(HttpStatus.BAD_REQUEST, "CABINET400_2", "캐비닛에 추가할 상품 목록이 올바르지 않습니다."),
    MEMBER_PRODUCT_ID_INVALID(HttpStatus.BAD_REQUEST, "CABINET400_3", "캐비닛 보유 상품 ID가 올바르지 않습니다."),

    // 403 FORBIDDEN - 캐비닛 접근 조건
    ONBOARDING_NOT_COMPLETED(HttpStatus.FORBIDDEN, "CABINET403_1", "초기 설문을 완료해야 이용할 수 있습니다."),

    // 404 NOT_FOUND - 캐비닛 추가 대상 조회 실패
    ADD_TARGET_PRODUCT_NOT_FOUND(HttpStatus.NOT_FOUND, "CABINET404_1", "캐비닛에 추가할 상품을 찾을 수 없습니다."),
    MEMBER_PRODUCT_NOT_FOUND(HttpStatus.NOT_FOUND, "CABINET404_2", "캐비닛 보유 상품을 찾을 수 없습니다."),

    // 409 CONFLICT - 캐비닛 중복 보유
    PRODUCT_ALREADY_OWNED(HttpStatus.CONFLICT, "CABINET409_1", "이미 캐비닛에 등록된 영양제입니다."),
    ;

    private final HttpStatus status;
    private final String code;
    private final String message;
}
