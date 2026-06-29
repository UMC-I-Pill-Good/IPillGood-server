package com.ipillgood.server.global.apiPayload.handler;

import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import com.ipillgood.server.global.apiPayload.ApiResponse;
import com.ipillgood.server.global.apiPayload.code.BaseErrorCode;
import com.ipillgood.server.global.apiPayload.code.GeneralErrorCode;
import com.ipillgood.server.global.apiPayload.exception.GeneralException;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GeneralExceptionAdvice extends ResponseEntityExceptionHandler {

    // 프로젝트에서 발생한 예외 처리
    @ExceptionHandler(GeneralException.class)
    public ResponseEntity<ApiResponse<Void>> handleGeneralException(GeneralException e
    ) {
        BaseErrorCode errorCode = e.getCode();
        return ResponseEntity.status(errorCode.getStatus())
                .body(ApiResponse.onFailure(errorCode, null));
    }

    // 그 외의 정의되지 않은 모든 예외 처리
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<String>> handleException(
            Exception ex
    ) {
        BaseErrorCode code = GeneralErrorCode.INTERNAL_SERVER_ERROR;
        return ResponseEntity.status(code.getStatus())
                .body(ApiResponse.onFailure(
                                code,
                                ex.getMessage()
                        )
                );
    }

    // @Valid 어노테이션 검증 실패 예외
    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException e, HttpHeaders headers,
            HttpStatusCode status, WebRequest request
    ) {
        Map<String, String> errors = new HashMap<>();
        e.getBindingResult().getAllErrors().forEach(error -> {
            String field = (error instanceof org.springframework.validation.FieldError fe)
                    ? fe.getField() : error.getObjectName();
            errors.put(field, error.getDefaultMessage());
        });
        BaseErrorCode code = GeneralErrorCode.BAD_REQUEST;
        return ResponseEntity.status(code.getStatus())
                .body(ApiResponse.onFailure(code, errors));
    }

    // 컨트롤러 @RequestParam, @PathVariable 등 검증 실패 예외
    @Override
    protected ResponseEntity<Object> handleHandlerMethodValidationException(
            HandlerMethodValidationException e, HttpHeaders headers,
            HttpStatusCode status, WebRequest request
    ) {
        Map<String, String> errors = new HashMap<>();
        e.getValueResults().forEach(result ->
                result.getResolvableErrors().forEach(error ->
                        errors.put(result.getMethodParameter().getParameterName(), error.getDefaultMessage())
                )
        );
        e.getBeanResults().forEach(result ->
                result.getFieldErrors().forEach(error ->
                        errors.put(error.getField(), error.getDefaultMessage())
                )
        );

        BaseErrorCode code = GeneralErrorCode.BAD_REQUEST;
        return ResponseEntity.status(code.getStatus())
                .body(ApiResponse.onFailure(code, errors));
    }

    // @Validated + @RequestParam, @PathVariable 등 검증 실패 예외
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiResponse<Map<String, String>>> handleConstraintViolationException(
            ConstraintViolationException e
    ) {
        Map<String, String> errors = new HashMap<>();
        e.getConstraintViolations().forEach(violation ->
                errors.put(violation.getPropertyPath().toString(), violation.getMessage())
        );

        BaseErrorCode code = GeneralErrorCode.BAD_REQUEST;
        return ResponseEntity.status(code.getStatus())
                .body(ApiResponse.onFailure(code, errors));
    }

    // 스프링 표준 예외(잘못된 JSON·없는 URL·메서드 미지원 등)를 원래 status 유지하며 ApiResponse로 변환
    @Override
    protected ResponseEntity<Object> handleExceptionInternal(
            Exception ex, Object body, HttpHeaders headers,
            HttpStatusCode statusCode, WebRequest request
    ) {
        HttpStatus status = HttpStatus.valueOf(statusCode.value());
        return new ResponseEntity<>(toApiResponse(status), headers, statusCode);
    }

    // status에 해당하는 에러 코드로 변환 (매핑이 없으면 서버 에러로 처리)
    private ApiResponse<Void> toApiResponse(HttpStatus status) {
        BaseErrorCode code = switch (status) {
            case BAD_REQUEST -> GeneralErrorCode.BAD_REQUEST;
            case UNAUTHORIZED -> GeneralErrorCode.UNAUTHORIZED;
            case FORBIDDEN -> GeneralErrorCode.FORBIDDEN;
            case NOT_FOUND -> GeneralErrorCode.NOT_FOUND;
            case METHOD_NOT_ALLOWED -> GeneralErrorCode.METHOD_NOT_ALLOWED;
            case NOT_ACCEPTABLE -> GeneralErrorCode.NOT_ACCEPTABLE;
            case UNSUPPORTED_MEDIA_TYPE -> GeneralErrorCode.UNSUPPORTED_MEDIA_TYPE;
            default -> GeneralErrorCode.INTERNAL_SERVER_ERROR;
        };
        return ApiResponse.onFailure(code, null);
    }
}
