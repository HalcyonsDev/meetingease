package ru.halcyon.meetingease.security;

import lombok.SneakyThrows;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.UUID;

@Component
public class RefreshTokenGenerator {
    private static final String ALGORITHM = "SHA256";

    @SneakyThrows
    public String generate() {
        String randomIdentifier = String.valueOf(UUID.randomUUID());
        MessageDigest messageDigest = MessageDigest.getInstance(ALGORITHM);
        byte[] hash = messageDigest.digest(randomIdentifier.getBytes(StandardCharsets.UTF_8));

        return convertBytesToString(hash);
    }

    private String convertBytesToString(byte[] bytes) {
        StringBuilder hexStringBuilder = new StringBuilder();
        for (byte currentByte: bytes) {
            String hexValue = String.format("%02x", currentByte);
            hexStringBuilder.append(hexValue);
        }
        return hexStringBuilder.toString();
    }
}
