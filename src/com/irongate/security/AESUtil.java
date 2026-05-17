package com.irongate.security;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * AES-256-GCM encryption / decryption.
 * Cipher format: base64(IV [12 bytes] || ciphertext+tag [N+16 bytes])
 */
public class AESUtil {

    private static final String ALGORITHM   = "AES/GCM/NoPadding";
    private static final int    GCM_IV_LEN  = 12;  // 96 bits
    private static final int    GCM_TAG_LEN = 128; // bits

    /** Generate a fresh 256-bit AES key, returned as Base64 string. */
    public static String generateKey() throws Exception {
        KeyGenerator kg = KeyGenerator.getInstance("AES");
        kg.init(256, new SecureRandom());
        return Base64.getEncoder().encodeToString(kg.generateKey().getEncoded());
    }

    /** Encrypt raw bytes. Returns Base64-encoded IV+ciphertext. */
    public static byte[] encrypt(byte[] plaintext, String base64Key) throws Exception {
        byte[] keyBytes = Base64.getDecoder().decode(base64Key);
        SecretKey key   = new SecretKeySpec(keyBytes, "AES");

        byte[] iv = new byte[GCM_IV_LEN];
        new SecureRandom().nextBytes(iv);

        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.ENCRYPT_MODE, key, new GCMParameterSpec(GCM_TAG_LEN, iv));
        byte[] ciphertext = cipher.doFinal(plaintext);

        // Prepend IV
        byte[] result = new byte[GCM_IV_LEN + ciphertext.length];
        System.arraycopy(iv, 0, result, 0, GCM_IV_LEN);
        System.arraycopy(ciphertext, 0, result, GCM_IV_LEN, ciphertext.length);
        return result;
    }

    /** Decrypt bytes produced by encrypt(). Returns original plaintext. */
    public static byte[] decrypt(byte[] ivAndCiphertext, String base64Key) throws Exception {
        byte[] keyBytes = Base64.getDecoder().decode(base64Key);
        SecretKey key   = new SecretKeySpec(keyBytes, "AES");

        byte[] iv         = new byte[GCM_IV_LEN];
        byte[] ciphertext = new byte[ivAndCiphertext.length - GCM_IV_LEN];
        System.arraycopy(ivAndCiphertext, 0, iv, 0, GCM_IV_LEN);
        System.arraycopy(ivAndCiphertext, GCM_IV_LEN, ciphertext, 0, ciphertext.length);

        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.DECRYPT_MODE, key, new GCMParameterSpec(GCM_TAG_LEN, iv));
        return cipher.doFinal(ciphertext);
    }
}
