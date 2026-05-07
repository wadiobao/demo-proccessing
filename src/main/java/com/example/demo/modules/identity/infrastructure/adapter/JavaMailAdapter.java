package com.example.demo.modules.identity.infrastructure.adapter;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

import com.example.demo.modules.identity.domain.port.MailPort;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class JavaMailAdapter implements MailPort {

    private final JavaMailSender javaMailSender;

    @Override
    public void sendSimpleMail(String to, String subject, String text) {
        SimpleMailMessage mailMessage = new SimpleMailMessage();
        mailMessage.setTo(to);
        mailMessage.setSubject(subject);
        mailMessage.setText(text);
        javaMailSender.send(mailMessage);
    }
}
