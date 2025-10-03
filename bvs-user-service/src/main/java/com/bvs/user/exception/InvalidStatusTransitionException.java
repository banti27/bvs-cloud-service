package com.bvs.user.exception;

import com.bvs.user.entity.UserStatus;

/**
 * Exception thrown when an invalid status operation is attempted
 */
public class InvalidStatusTransitionException extends UserServiceException {

    public InvalidStatusTransitionException(UserStatus from, UserStatus to) {
        super("INVALID_STATUS_TRANSITION", 
              String.format("Cannot transition user status from %s to %s", from, to));
    }

    public InvalidStatusTransitionException(String message) {
        super("INVALID_STATUS_TRANSITION", message);
    }
}
