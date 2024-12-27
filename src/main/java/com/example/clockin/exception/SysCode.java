package com.example.clockin.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum SysCode implements ErrorCode{

    OK(200, "Success"),
    USER_NOT_FOUND(1001, "User not found"),
    UNEXPECTED_RESPONSE_TYPE(1002, "unexpected response type"),
    CLOCK_IN_FAILED(1003, "unexpected response type"),
    AUTHENTICATION_FAILED(1004, "Authentication failed"),
    SHIFT_NOT_FOUND(1005, "Shift not found"),
    UPDATE_FAILED(1006, "Failed to update user");

    private final Integer code;
    private final String message;
}
