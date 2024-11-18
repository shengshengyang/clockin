package com.example.clockin.dto;

import java.io.Serializable;

public class ClockInResult implements Serializable {

    private boolean success;
    private String message;

    public ClockInResult(boolean success, String message) {
        this.success = success;
        this.message = message;
    }

    public ClockInResult() {
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
