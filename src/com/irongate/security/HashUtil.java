package com.irongate.security;

import java.io.InputStream;
import java.security.MessageDigest;

public class HashUtil {

    /** Compute SHA-256 hex digest of raw bytes. */
    public static String sha256(byte[] data) throws Exception {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        byte[] digest = md.digest(data);
        StringBuilder sb = new StringBuilder();
        for (byte b : digest) sb.append(String.format("%02x", b));
        return sb.toString();
    }

    /** Compute SHA-256 hex digest from an InputStream (streams large files without full load). */
    public static String sha256(InputStream in) throws Exception {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        byte[] buf = new byte[8192];
        int n;
        while ((n = in.read(buf)) != -1) md.update(buf, 0, n);
        byte[] digest = md.digest();
        StringBuilder sb = new StringBuilder();
        for (byte b : digest) sb.append(String.format("%02x", b));
        return sb.toString();
    }
}
