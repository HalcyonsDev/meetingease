package ru.halcyon.meetingease.security;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class AuthenticatedDataProvider {
    public String getEmail() {
        return Optional.ofNullable((JwtAuthentication) SecurityContextHolder.getContext().getAuthentication())
                .map(JwtAuthentication::getEmail)
                .orElseThrow(IllegalStateException::new);
    }

    public boolean getIsClient() {
        return Optional.ofNullable((JwtAuthentication) SecurityContextHolder.getContext().getAuthentication())
                .map(JwtAuthentication::isClient)
                .orElseThrow(IllegalStateException::new);
    }
}
