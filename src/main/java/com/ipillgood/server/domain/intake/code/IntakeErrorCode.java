package com.ipillgood.server.domain.intake.code;

import com.ipillgood.server.global.apiPayload.code.BaseErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum IntakeErrorCode implements BaseErrorCode {

    // 400 BAD_REQUEST - 섭취 중 상품 ID/요청 값 검증
    ACTIVE_PRODUCT_ID_INVALID(HttpStatus.BAD_REQUEST, "INTAKE400_1", "활성 섭취 상품 ID가 올바르지 않습니다."),
    REGISTRATION_REQUEST_INVALID(HttpStatus.BAD_REQUEST, "INTAKE400_2", "등록/병용 확인 요청이 올바르지 않습니다."),
    SETTINGS_UPDATE_REQUEST_INVALID(HttpStatus.BAD_REQUEST, "INTAKE400_3", "섭취 중 영양제 설정 변경 요청이 올바르지 않습니다."),
    TODAY_RECORD_REQUEST_INVALID(HttpStatus.BAD_REQUEST, "INTAKE400_4", "오늘 복용 체크 요청이 올바르지 않습니다."),

    // 403 FORBIDDEN - 복용 루틴 접근 조건
    ONBOARDING_NOT_COMPLETED(HttpStatus.FORBIDDEN, "INTAKE403_1", "초기 설문을 완료해야 이용할 수 있습니다."),

    // 404 NOT_FOUND - 섭취 중 등록 대상 조회 실패
    REGISTRATION_TARGET_NOT_FOUND(HttpStatus.NOT_FOUND, "INTAKE404_1", "섭취 중으로 등록할 캐비닛 상품을 찾을 수 없습니다."),
    ACTIVE_PRODUCT_NOT_FOUND(HttpStatus.NOT_FOUND, "INTAKE404_2", "활성 섭취 중 상품을 찾을 수 없습니다."),

    // 409 CONFLICT - 섭취 중 상품 중복 등록/복용 팝업 노출 대상 오류
    ACTIVE_PRODUCT_ALREADY_EXISTS(HttpStatus.CONFLICT, "INTAKE409_1", "이미 섭취 중인 영양제입니다."),
    TODAY_POPUP_NOT_TARGET(HttpStatus.CONFLICT, "INTAKE409_2", "오늘 복용 팝업 노출 대상이 아닙니다."),
    ;

    private final HttpStatus status;
    private final String code;
    private final String message;
}
