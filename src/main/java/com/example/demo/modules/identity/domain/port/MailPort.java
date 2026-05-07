package com.example.demo.modules.identity.domain.port;

public interface MailPort {
    void sendSimpleMail(String to, String subject, String text);
}
