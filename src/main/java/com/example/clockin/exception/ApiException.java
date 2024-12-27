package com.example.clockin.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class ApiException extends RuntimeException {
    private final transient ErrorCode errorCode;
    private final transient Object data;
    private final HttpStatus httpStatus;

    public ApiException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
        this.data = null;
        this.httpStatus = HttpStatus.EXPECTATION_FAILED;
    }

    public ApiException(ErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
        this.data = null;
        this.httpStatus = HttpStatus.EXPECTATION_FAILED;
    }

    public ApiException(ErrorCode errorCode, Object data) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
        this.data = data;
        this.httpStatus = HttpStatus.EXPECTATION_FAILED;
    }

    public ApiException(ErrorCode errorCode, HttpStatus httpStatus) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
        this.data = null;
        this.httpStatus = httpStatus;
    }

    public ApiException(ErrorCode errorCode, HttpStatus httpStatus, Object data) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
        this.data = data;
        this.httpStatus = httpStatus;
    }

    public ApiException(ErrorCode errorCode, String message, Object data) {
        super(message);
        this.errorCode = errorCode;
        this.data = data;
        this.httpStatus = HttpStatus.EXPECTATION_FAILED;
    }
}
