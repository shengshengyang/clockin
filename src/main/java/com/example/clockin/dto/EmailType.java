package com.example.clockin.dto;

import lombok.Getter;

@Getter
public enum EmailType {
    PASSWORD_RESET("打卡系統密碼重設信件"),
    ACCOUNT_ACTIVATION("帳號啟用信件");

    private final String subject;

    EmailType(String subject) {
        this.subject = subject;
    }

}
