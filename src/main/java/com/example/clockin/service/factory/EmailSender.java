package com.example.clockin.service.factory;

import java.util.Collection;

public interface EmailSender {
    void sendEmail(Collection<String> receivers, String subject, String link);
}
