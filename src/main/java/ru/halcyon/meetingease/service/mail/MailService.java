package ru.halcyon.meetingease.service.mail;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import ru.halcyon.meetingease.util.EmailUtil;

@Service
@RequiredArgsConstructor
public class MailService {
    @Value("${spring.mail.username}")
    private String fromEmail;

    private final JavaMailSender mailSender;

    public void sendSimpleVerificationMailMessage(String name, String to, String token) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setSubject("New User Account Verification");
        message.setFrom(fromEmail);
        message.setTo(to);
        message.setText(EmailUtil.getVerificationEmailMessage(name, "http://localhost:8080", token));

        mailSender.send(message);
    }
}
