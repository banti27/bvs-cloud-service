package com.bvs.user.entity;

/**
 * User status enum for soft delete and user lifecycle management
 * This allows for flexible user state management without hard deletes
 */
public enum UserStatus {
    /**
     * User is active and can access the system
     */
    ACTIVE("Active", "User is active and can access the system"),
    
    /**
     * User is temporarily inactive (can be reactivated)
     */
    INACTIVE("Inactive", "User is temporarily inactive"),
    
    /**
     * User is suspended (requires admin intervention to reactivate)
     */
    SUSPENDED("Suspended", "User account is suspended"),
    
    /**
     * User is pending activation (e.g., email verification)
     */
    PENDING("Pending", "User account is pending activation"),
    
    /**
     * User account is locked (e.g., too many failed login attempts)
     */
    LOCKED("Locked", "User account is locked"),
    
    /**
     * User is soft deleted (marked for deletion but data retained)
     */
    DELETED("Deleted", "User account is marked as deleted");

    private final String displayName;
    private final String description;

    UserStatus(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }

    /**
     * Check if user can login
     */
    public boolean canLogin() {
        return this == ACTIVE;
    }

    /**
     * Check if user is considered deleted
     */
    public boolean isDeleted() {
        return this == DELETED;
    }

    /**
     * Check if user can be reactivated without admin
     */
    public boolean canSelfReactivate() {
        return this == INACTIVE || this == PENDING;
    }
}
