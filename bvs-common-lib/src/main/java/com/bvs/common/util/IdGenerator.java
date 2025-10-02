package com.bvs.common.util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Utility class for generating custom IDs
 * Can be used across all BVS services
 */
public class IdGenerator {
    
    private static final String CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final DateTimeFormatter TIMESTAMP_FORMAT = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
    
    /**
     * Generate custom ID with format: PREFIX-yyyyMMddHHmmss-XXXX
     * 
     * @param prefix Prefix for the ID (e.g., "USR", "STR", "ORD")
     * @param suffixLength Length of random suffix
     * @return Generated ID
     */
    public static String generate(String prefix, int suffixLength) {
        String timestamp = LocalDateTime.now().format(TIMESTAMP_FORMAT);
        String suffix = generateRandomSuffix(suffixLength);
        return String.format("%s-%s-%s", prefix, timestamp, suffix);
    }
    
    /**
     * Generate custom ID with default suffix length of 4
     * 
     * @param prefix Prefix for the ID
     * @return Generated ID
     */
    public static String generate(String prefix) {
        return generate(prefix, 4);
    }
    
    /**
     * Generate random alphanumeric suffix
     * 
     * @param length Length of suffix
     * @return Random suffix
     */
    private static String generateRandomSuffix(int length) {
        StringBuilder suffix = new StringBuilder(length);
        ThreadLocalRandom random = ThreadLocalRandom.current();
        
        for (int i = 0; i < length; i++) {
            int index = random.nextInt(CHARS.length());
            suffix.append(CHARS.charAt(index));
        }
        
        return suffix.toString();
    }
    
    /**
     * Validate ID format
     * 
     * @param id ID to validate
     * @param prefix Expected prefix
     * @return true if valid, false otherwise
     */
    public static boolean isValid(String id, String prefix) {
        if (id == null || id.isEmpty()) {
            return false;
        }
        
        String pattern = String.format("^%s-\\d{14}-[A-Z0-9]+$", prefix);
        return id.matches(pattern);
    }
}
