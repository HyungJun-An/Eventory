package com.eventory.systemAdmin.service;

import java.security.SecureRandom;

public class RandomGenerator {
    private static final String ID_CHARS = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final String PW_CHARS = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789!@#$%^&*()";
    private static final SecureRandom random = new SecureRandom();

    public static String generateRandomId(int length) {
        return generateRandomString(ID_CHARS, length);
    }

    public static String generateRandomPassword(int length) {
        return generateRandomString(PW_CHARS, length);
    }

    private static String generateRandomString(String characters, int length) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) {
            sb.append(characters.charAt(random.nextInt(characters.length())));
        }
        return sb.toString();
    }
}
