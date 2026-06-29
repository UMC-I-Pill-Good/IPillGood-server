package com.ipillgood.server.global.apiPayload;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;
import com.ipillgood.server.global.apiPayload.code.BaseErrorCode;
import com.ipillgood.server.global.apiPayload.code.BaseSuccessCode;

@Getter
@AllArgsConstructor
@JsonPropertyOrder({"isSuccess", "code", "message", "result"})
public class ApiResponse<T> {

    // 응답 바디에는 포함하지 않고, HTTP 상태 코드를 세팅하는 용도로만 사용
    @JsonIgnore
    private final HttpStatus httpStatus;

    @JsonProperty
    private final Boolean isSuccess;

    @JsonProperty
    private final String code;

    @JsonProperty
    private final String message;

    @JsonProperty
    private final T result;

    public static <T> ApiResponse<T> onSuccess(BaseSuccessCode code, T result) {
        return new ApiResponse<>(code.getStatus(), true, code.getCode(), code.getMessage(), result);
    }

    public static <T> ApiResponse<T> onFailure(BaseErrorCode code, T result) {
        return new ApiResponse<>(code.getStatus(), false, code.getCode(), code.getMessage(), result);
    }
}
