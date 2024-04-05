package ru.halcyon.meetingease.util;

import io.jsonwebtoken.Claims;
import ru.halcyon.meetingease.security.JwtAuthentication;

public class JwtUtil {
    public static JwtAuthentication getAuthentication(Claims claims) {
        final JwtAuthentication jwtAuthInfo = new JwtAuthentication();
        jwtAuthInfo.setEmail(claims.getSubject());
        jwtAuthInfo.setClient((Boolean) claims.get("isClient"));

        return jwtAuthInfo;
    }
}
