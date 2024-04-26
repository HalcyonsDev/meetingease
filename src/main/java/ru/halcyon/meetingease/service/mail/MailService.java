package ru.halcyon.meetingease.service.mail;

public interface MailService {
    void sendSimpleVerificationMailMessage(String name, String to, String token);
}
