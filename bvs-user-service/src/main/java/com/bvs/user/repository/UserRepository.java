package com.bvs.user.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.bvs.user.entity.User;
import com.bvs.user.entity.UserStatus;

@Repository
public interface UserRepository extends JpaRepository<User, String> {

    /**
     * Find user by username
     */
    Optional<User> findByUsername(String username);

    /**
     * Find user by email
     */
    Optional<User> findByEmail(String email);

    /**
     * Check if username exists
     */
    boolean existsByUsername(String username);

    /**
     * Check if email exists
     */
    boolean existsByEmail(String email);

    /**
     * Find all users by status
     */
    List<User> findByStatus(UserStatus status);

    // To find all active users, use: findByStatus(UserStatus.ACTIVE)

    /**
     * Find user by username and not deleted
     */
    Optional<User> findByUsernameAndStatusNot(String username, UserStatus status);

    /**
     * Find user by email and not deleted
     */
    Optional<User> findByEmailAndStatusNot(String email, UserStatus status);

    /**
     * Count users by status
     */
    long countByStatus(UserStatus status);
}
