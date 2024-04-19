package ru.halcyon.meetingease.util;

public class EmailUtil {
    public static String getVerificationEmailMessage(String name, String host, String token) {
        return "Hello, " + name + "! \n\n" +
                "Your new account has been created. Please click the link below to verify your account. \n\n" +
                getVerificationUrl(host, token) + "\n\n The Meetingease support Team";
    }

    private static String getVerificationUrl(String host, String token) {
        return host + "/api/v1/clients/auth?token=" + token;
    }
}
