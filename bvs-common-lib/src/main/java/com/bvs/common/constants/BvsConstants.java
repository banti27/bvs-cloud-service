package com.bvs.common.constants;

/**
 * Common constants for all BVS services
 */
public final class BvsConstants {
    
    private BvsConstants() {
        // Private constructor to prevent instantiation
    }
    
    /**
     * API version
     */
    public static final String API_VERSION = "v1";
    
    /**
     * Default page size for pagination
     */
    public static final int DEFAULT_PAGE_SIZE = 20;
    
    /**
     * Maximum page size for pagination
     */
    public static final int MAX_PAGE_SIZE = 100;
    
    /**
     * Default sort direction
     */
    public static final String DEFAULT_SORT_DIRECTION = "ASC";
    
    /**
     * Date format pattern
     */
    public static final String DATE_FORMAT = "yyyy-MM-dd";
    
    /**
     * Date-time format pattern
     */
    public static final String DATETIME_FORMAT = "yyyy-MM-dd'T'HH:mm:ss";
    
    /**
     * Timezone
     */
    public static final String DEFAULT_TIMEZONE = "UTC";
}
