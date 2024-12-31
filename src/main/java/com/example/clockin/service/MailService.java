package com.example.clockin.service;

import com.example.clockin.dto.EmailType;
import com.example.clockin.service.factory.EmailSender;
import com.example.clockin.service.factory.EmailSenderFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collection;

@Service
public class MailService {

    private final EmailSenderFactory emailSenderFactory;

    @Autowired
    public MailService(EmailSenderFactory emailSenderFactory) {
        this.emailSenderFactory = emailSenderFactory;
    }

    public void sendEmail(EmailType emailType, Collection<String> receivers, String link) {
        EmailSender emailSender = emailSenderFactory.getEmailSender(emailType);
        emailSender.sendEmail(receivers, emailType.getSubject(), link);
    }
}
