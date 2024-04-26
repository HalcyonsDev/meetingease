package ru.halcyon.meetingease.util;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Encoders;

public class GenerateSecret {
    public static void main(String[] args) {
        System.out.println(generateToken());
        System.out.println(generateToken());
    }

    private static String generateToken() {
        return Encoders.BASE64.encode(Jwts.SIG.HS512.key().build().getEncoded());
    }
}
