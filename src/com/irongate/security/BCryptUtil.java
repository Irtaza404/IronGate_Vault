package com.irongate.security;

import java.security.SecureRandom;
import java.util.Base64;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

/**
 * Lightweight BCrypt-style password hashing using PBKDF2-HMAC-SHA256.
 * Drop-in for environments without the favre bcrypt jar.
 * Format: $pbkdf2$<base64-salt>$<base64-hash>
 */
public class BCryptUtil {

    private static final int ITERATIONS = 120_000;
    private static final int KEY_LENGTH  = 256; // bits
    private static final String PREFIX   = "$pbkdf2$";

    public static String hash(String password) {
        byte[] salt = new byte[16];
        new SecureRandom().nextBytes(salt);
        byte[] hash = pbkdf2(password.toCharArray(), salt);
        return PREFIX
                + Base64.getEncoder().encodeToString(salt) + "$"
                + Base64.getEncoder().encodeToString(hash);
    }

    public static boolean verify(String password, String stored) {
        if (!stored.startsWith(PREFIX)) return false;
        String[] parts = stored.substring(PREFIX.length()).split("\\$");
        if (parts.length != 2) return false;
        byte[] salt = Base64.getDecoder().decode(parts[0]);
        byte[] expected = Base64.getDecoder().decode(parts[1]);
        byte[] actual   = pbkdf2(password.toCharArray(), salt);
        return slowEquals(expected, actual);
    }

    private static byte[] pbkdf2(char[] password, byte[] salt) {
        try {
            PBEKeySpec spec = new PBEKeySpec(password, salt, ITERATIONS, KEY_LENGTH);
            SecretKeyFactory skf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
            return skf.generateSecret(spec).getEncoded();
        } catch (Exception e) {
            throw new RuntimeException("PBKDF2 error", e);
        }
    }

    /** Constant-time comparison to prevent timing attacks */
    private static boolean slowEquals(byte[] a, byte[] b) {
        int diff = a.length ^ b.length;
        for (int i = 0; i < a.length && i < b.length; i++)
            diff |= a[i] ^ b[i];
        return diff == 0;
    }
}
