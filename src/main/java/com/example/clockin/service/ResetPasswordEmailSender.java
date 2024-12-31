package com.example.clockin.service;


import com.example.clockin.exception.ApiException;
import com.example.clockin.exception.SysCode;
import com.example.clockin.service.factory.EmailSender;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Collection;

@Service
public class ResetPasswordEmailSender implements EmailSender {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String mail;

    @Autowired
    public ResetPasswordEmailSender(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    @Override
    public void sendEmail(Collection<String> receivers, String subject, String resetPasswordLink) {
        MimeMessage message = mailSender.createMimeMessage();
        try {
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setTo(receivers.toArray(new String[0]));
            helper.setSubject(subject);

            // Load the email template
            String content = loadEmailTemplate();

            String formattedContent = content.replace("{{reset_password_link}}", resetPasswordLink);

            helper.setText(formattedContent, true); // true indicates HTML
            helper.setFrom("打卡系統客服<" + mail + ">");
            mailSender.send(message);
        } catch (MessagingException e) {
            throw new ApiException(SysCode.EMAIL_SEND_FAILED);
        }
    }

    private String loadEmailTemplate() {
        try (InputStream inputStream = getClass().getResourceAsStream("/templates/email-template.html")) {
            assert inputStream != null;
            return new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new ApiException(SysCode.EMAIL_SEND_FAILED, "Failed to load email template", e);
        }
    }
}
