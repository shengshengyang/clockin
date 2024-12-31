package com.example.clockin.service.factory;

import com.example.clockin.dto.EmailType;
import com.example.clockin.exception.ApiException;
import com.example.clockin.exception.SysCode;
import com.example.clockin.service.ResetPasswordEmailSender;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class EmailSenderFactory {

    private final ResetPasswordEmailSender resetPasswordEmailSender;

    @Autowired
    public EmailSenderFactory(ResetPasswordEmailSender resetPasswordEmailSender) {
        this.resetPasswordEmailSender = resetPasswordEmailSender;
    }

    public EmailSender getEmailSender(EmailType emailType) {
        switch (emailType) {
            case PASSWORD_RESET:
                return resetPasswordEmailSender;
            default:
                throw new ApiException(SysCode.INVALID_ARGUMENT_VALUE);
        }
    }
}
