package com.ipillgood.server.domain.survey.code;

import com.ipillgood.server.global.apiPayload.code.BaseErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum SurveyErrorCode implements BaseErrorCode {

    NONE_AND_DETAIL_CONFLICT(HttpStatus.BAD_REQUEST, "SURVEY400_1", "없음 선택과 상세 선택을 동시에 요청할 수 없습니다."),
    MENSTRUAL_INFO_INCOMPLETE(HttpStatus.BAD_REQUEST, "SURVEY400_2", "마지막 생리 시작일과 주기를 모두 입력해 주세요."),
    CONCERN_CODE_COUNT_INVALID(HttpStatus.BAD_REQUEST, "SURVEY400_3", "건강 고민은 1~3개까지 선택할 수 있습니다."),
    CONTRAINDICATION_DETAIL_REQUIRED(HttpStatus.BAD_REQUEST, "SURVEY400_4", "기저질환, 복용약, 알러지는 없음 또는 상세 항목을 선택해야 합니다."),
    CURRENT_INGREDIENT_DETAIL_REQUIRED(HttpStatus.BAD_REQUEST, "SURVEY400_5", "현재 복용 성분은 없음 또는 성분을 선택해야 합니다."),
    PREGNANT_REQUIRED_FOR_FEMALE(HttpStatus.BAD_REQUEST, "SURVEY400_6", "여성 사용자는 임신 여부를 선택해야 합니다."),
    ;

    private final HttpStatus status;
    private final String code;
    private final String message;
}
