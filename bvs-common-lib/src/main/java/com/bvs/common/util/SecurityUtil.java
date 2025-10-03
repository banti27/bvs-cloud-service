package com.bvs.common.util;

import java.security.SecureRandom;
import java.util.Base64;

import org.apache.commons.codec.digest.DigestUtils;

/**
 * Security utility class
 */
public class SecurityUtil {

    private static final SecureRandom secureRandom = new SecureRandom();

    /**
     * Generate random token
     * 
     * @param length Length of token in bytes
     * @return Base64 encoded token
     */
    public static String generateToken(int length) {
        byte[] bytes = new byte[length];
        secureRandom.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    /**
     * Generate SHA-256 hash
     * 
     * @param input Input string
     * @return Hex-encoded hash
     */
    public static String sha256(String input) {
        return DigestUtils.sha256Hex(input);
    }

    /**
     * Generate SHA-256 hash (recommended over MD5, even for non-security purposes
     * like ETags)
     * 
     * @param input Input string
     * @return Hex-encoded hash
     */
    public static String sha256ForETag(String input) {
        return DigestUtils.sha256Hex(input);
    }

    /**
     * Mask sensitive data (e.g., email, phone)
     * 
     * @param data         Data to mask
     * @param visibleChars Number of visible characters at start
     * @return Masked data
     */
    public static String mask(String data, int visibleChars) {
        if (data == null || data.length() <= visibleChars) {
            return data;
        }

        String visible = data.substring(0, visibleChars);
        String masked = "*".repeat(data.length() - visibleChars);
        return visible + masked;
    }

    /**
     * Mask email (show first 2 chars and domain)
     * 
     * @param email Email to mask
     * @return Masked email (e.g., jo***@example.com)
     */
    public static String maskEmail(String email) {
        if (email == null || !email.contains("@")) {
            return email;
        }

        String[] parts = email.split("@");
        String local = parts[0];
        String domain = parts[1];

        if (local.length() <= 2) {
            return local + "@" + domain;
        }

        return local.substring(0, 2) + "***@" + domain;
    }
}
