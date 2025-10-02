package com.bvs.user.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Security Configuration for BVS User Service
 * 
 * Configures:
 * - BCrypt password encoder with strength 12
 * - Disables default Spring Security (for now)
 * - Can be extended for authentication/authorization
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    /**
     * Password Encoder Bean - BCrypt with strength 12
     * 
     * Strength levels:
     * - 10 (default): Good for most applications
     * - 12: Better security, slightly slower (recommended for user passwords)
     * - 14-15: High security, noticeably slower (for sensitive data)
     * 
     * @return BCryptPasswordEncoder instance
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }

    /**
     * Security Filter Chain
     * 
     * Currently permits all requests (no authentication required).
     * Enable authentication when ready for production.
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())  // Disable CSRF for REST API
            .authorizeHttpRequests(auth -> auth
                .anyRequest().permitAll()  // Allow all requests without authentication
            );
        
        return http.build();
    }
    
    /**
     * Alternative: Enable authentication (uncomment when ready)
     */
    /*
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/users/login", "/api/users/register").permitAll()
                .requestMatchers("/h2-console/**").permitAll()  // H2 console access
                .anyRequest().authenticated()
            )
            .httpBasic(Customizer.withDefaults());  // Basic auth for testing
        
        return http.build();
    }
    */
}
