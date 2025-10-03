package com.bvs.user.validation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

/**
 * Custom validation annotation for password validation
 * Password must:
 * - Be at least 12 characters long
 * - Contain at least one special character
 * - Contain at least one uppercase letter
 * - Contain at least one digit
 */
@Documented
@Constraint(validatedBy = PasswordValidator.class)
@Target({ ElementType.FIELD, ElementType.PARAMETER })
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidPassword {
    
    String message() default "Password must be at least 12 characters long and contain at least one special character, one uppercase letter, and one digit";
    
    Class<?>[] groups() default {};
    
    Class<? extends Payload>[] payload() default {};
}
