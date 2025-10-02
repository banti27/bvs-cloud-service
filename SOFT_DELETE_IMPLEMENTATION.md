# Soft Delete Implementation with User Status Management

## Overview
This implementation uses **soft delete** with an enum-based status system instead of hard delete. Users are never physically deleted from the database; instead, their status is changed to track their lifecycle.

## Why Soft Delete?

### Benefits:
1. **Data Retention**: Maintain historical records for auditing and compliance
2. **Recovery**: Ability to restore accidentally deleted users
3. **Referential Integrity**: No broken foreign key relationships
4. **Audit Trail**: Track user lifecycle and status changes
5. **Compliance**: GDPR, SOX, and other regulations often require data retention
6. **Analytics**: Historical data remains available for reporting

### Advantages Over Boolean Active Flag:
- ❌ Boolean `active` (true/false) is limited to only 2 states
- ✅ Enum `UserStatus` supports multiple states and is easily extensible

## UserStatus Enum

### Available Statuses

```java
public enum UserStatus {
    ACTIVE,      // User is active and can access the system
    INACTIVE,    // User is temporarily inactive (can be reactivated)
    SUSPENDED,   // User is suspended (requires admin intervention)
    PENDING,     // User account is pending activation (e.g., email verification)
    LOCKED,      // User account is locked (e.g., failed login attempts)
    DELETED      // User is soft deleted (data retained)
}
```

### Status Descriptions

| Status | Description | Can Login? | Can Reactivate? | Use Case |
|--------|-------------|------------|-----------------|----------|
| `ACTIVE` | Fully functional user | ✅ Yes | N/A | Normal user state |
| `INACTIVE` | Temporarily disabled | ❌ No | ✅ User/Admin | User requested deactivation |
| `SUSPENDED` | Administratively disabled | ❌ No | ⚠️ Admin only | Policy violation |
| `PENDING` | Awaiting activation | ❌ No | ✅ User | Email verification pending |
| `LOCKED` | Security lock | ❌ No | ⚠️ Admin only | Failed login attempts |
| `DELETED` | Soft deleted | ❌ No | ❌ No | User deletion requested |

## Implementation

### 1. Entity (`User.java`)

```java
@Entity
@Table(name = "users")
public class User {
    @Id
    private String id;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private UserStatus status = UserStatus.ACTIVE;
    
    // ... other fields
}
```

**Key Points:**
- ✅ Uses `@Enumerated(EnumType.STRING)` to store enum name (not ordinal)
- ✅ Default status is `ACTIVE`
- ✅ Column length 20 accommodates longest enum name
- ✅ NOT NULL constraint ensures status is always set

### 2. Repository Methods

```java
public interface UserRepository extends JpaRepository<User, String> {
    // Find users by status
    List<User> findByStatus(UserStatus status);
    
    // Find user excluding certain status (e.g., not deleted)
    Optional<User> findByUsernameAndStatusNot(String username, UserStatus status);
    
    // Count users by status
    long countByStatus(UserStatus status);
}
```

### 3. Service Layer

#### Soft Delete
```java
@Transactional
public void deleteUser(String id) {
    User user = userRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("User not found"));
    user.setStatus(UserStatus.DELETED);  // Soft delete
    userRepository.save(user);
}
```

#### Status Management Methods
- `deactivateUser(id)` - Set status to INACTIVE
- `reactivateUser(id)` - Set status back to ACTIVE
- `suspendUser(id)` - Set status to SUSPENDED
- `lockUser(id)` - Set status to LOCKED
- `updateUserStatus(id, status)` - Generic status update

### 4. Controller Endpoints

| Method | Endpoint | Description | Status Change |
|--------|----------|-------------|---------------|
| `DELETE` | `/api/users/{id}` | Soft delete user | → DELETED |
| `PATCH` | `/api/users/{id}/deactivate` | Deactivate user | → INACTIVE |
| `PATCH` | `/api/users/{id}/reactivate` | Reactivate user | → ACTIVE |
| `PATCH` | `/api/users/{id}/suspend` | Suspend user | → SUSPENDED |
| `PATCH` | `/api/users/{id}/lock` | Lock user | → LOCKED |
| `PATCH` | `/api/users/{id}/status?status=X` | Update status | → X |
| `DELETE` | `/api/users/{id}/hard` | **Hard delete** ⚠️ | Permanent |
| `GET` | `/api/users/status/{status}` | Get users by status | N/A |

## API Usage Examples

### 1. Create User (Default Status: ACTIVE)
```bash
curl -X POST http://localhost:8080/api/users \
  -H "Content-Type: application/json" \
  -d '{
    "username": "john.doe",
    "email": "john@example.com",
    "password": "password123",
    "firstName": "John",
    "lastName": "Doe"
  }'
```

**Response:**
```json
{
  "id": "USR-20251003143025-A3B9",
  "username": "john.doe",
  "email": "john@example.com",
  "firstName": "John",
  "lastName": "Doe",
  "status": "ACTIVE"
}
```

### 2. Soft Delete User
```bash
curl -X DELETE http://localhost:8080/api/users/USR-20251003143025-A3B9
```

**Result:** User status changed to `DELETED`, data still in database

### 3. Deactivate User
```bash
curl -X PATCH http://localhost:8080/api/users/USR-20251003143025-A3B9/deactivate
```

**Result:** User status changed to `INACTIVE`

### 4. Reactivate User
```bash
curl -X PATCH http://localhost:8080/api/users/USR-20251003143025-A3B9/reactivate
```

**Result:** User status changed back to `ACTIVE`

### 5. Suspend User (Admin Action)
```bash
curl -X PATCH http://localhost:8080/api/users/USR-20251003143025-A3B9/suspend
```

**Result:** User status changed to `SUSPENDED`

### 6. Get All Active Users
```bash
curl http://localhost:8080/api/users/active
```

### 7. Get Users by Status
```bash
curl http://localhost:8080/api/users/status/DELETED
curl http://localhost:8080/api/users/status/SUSPENDED
curl http://localhost:8080/api/users/status/INACTIVE
```

### 8. Update Status Directly
```bash
curl -X PATCH "http://localhost:8080/api/users/USR-20251003143025-A3B9/status?status=LOCKED"
```

### 9. Hard Delete (DANGEROUS - Admin Only)
```bash
curl -X DELETE http://localhost:8080/api/users/USR-20251003143025-A3B9/hard
```

**⚠️ Warning:** This permanently removes the user from the database!

## Database Schema

### Before (Boolean)
```sql
CREATE TABLE users (
    id VARCHAR(50) PRIMARY KEY,
    username VARCHAR(100) NOT NULL UNIQUE,
    email VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    first_name VARCHAR(50),
    last_name VARCHAR(50),
    active BOOLEAN NOT NULL DEFAULT true,  -- Limited to 2 states
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP
);
```

### After (Enum)
```sql
CREATE TABLE users (
    id VARCHAR(50) PRIMARY KEY,
    username VARCHAR(100) NOT NULL UNIQUE,
    email VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    first_name VARCHAR(50),
    last_name VARCHAR(50),
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',  -- Supports multiple states
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP
);
```

## Query Examples

### Get Active Users Only
```java
List<User> activeUsers = userRepository.findByStatus(UserStatus.ACTIVE);
```

### Exclude Deleted Users
```java
Optional<User> user = userRepository.findByUsernameAndStatusNot("john", UserStatus.DELETED);
```

### Count by Status
```java
long deletedCount = userRepository.countByStatus(UserStatus.DELETED);
long activeCount = userRepository.countByStatus(UserStatus.ACTIVE);
```

## Business Logic Examples

### Login Check
```java
public boolean canUserLogin(String username) {
    User user = userRepository.findByUsername(username)
        .orElseThrow(() -> new RuntimeException("User not found"));
    
    return user.getStatus().canLogin();  // Only ACTIVE users can login
}
```

### Reactivation Logic
```java
@Transactional
public void reactivateUser(String id) {
    User user = userRepository.findById(id)
        .orElseThrow(() -> new RuntimeException("User not found"));
    
    if (user.getStatus() == UserStatus.DELETED) {
        throw new RuntimeException("Cannot reactivate deleted user");
    }
    
    if (user.getStatus() == UserStatus.SUSPENDED || 
        user.getStatus() == UserStatus.LOCKED) {
        throw new RuntimeException("Admin approval required to reactivate");
    }
    
    user.setStatus(UserStatus.ACTIVE);
    userRepository.save(user);
}
```

## Migration from Boolean to Enum

If migrating from existing `active` boolean field:

### Step 1: Add Status Column
```sql
ALTER TABLE users ADD COLUMN status VARCHAR(20);
```

### Step 2: Migrate Data
```sql
UPDATE users SET status = 'ACTIVE' WHERE active = true;
UPDATE users SET status = 'INACTIVE' WHERE active = false;
```

### Step 3: Set Default and NOT NULL
```sql
ALTER TABLE users ALTER COLUMN status SET DEFAULT 'ACTIVE';
ALTER TABLE users ALTER COLUMN status SET NOT NULL;
```

### Step 4: Drop Old Column
```sql
ALTER TABLE users DROP COLUMN active;
```

## Extensibility

### Adding New Statuses

Easy to add new statuses without database migration:

```java
public enum UserStatus {
    ACTIVE,
    INACTIVE,
    SUSPENDED,
    PENDING,
    LOCKED,
    DELETED,
    ARCHIVED,        // NEW: Long-term inactive users
    PROBATION,       // NEW: Under review
    BANNED           // NEW: Permanently banned
}
```

Just add to enum - no database schema changes needed!

## Best Practices

### ✅ DO:
- Use soft delete for user records
- Log status changes for audit trail
- Validate status transitions (e.g., can't reactivate deleted users)
- Exclude DELETED users from normal queries
- Use meaningful status names
- Document status meanings clearly

### ❌ DON'T:
- Use hard delete unless absolutely necessary
- Allow direct status manipulation without validation
- Expose hard delete endpoint to regular users
- Forget to handle status in authentication/authorization
- Use ordinal values for enum (always use STRING)

## Security Considerations

### 1. Access Control
```java
// Only admin can suspend users
@PreAuthorize("hasRole('ADMIN')")
@PatchMapping("/{id}/suspend")
public ResponseEntity<Void> suspendUser(@PathVariable String id) {
    userService.suspendUser(id);
    return ResponseEntity.ok().build();
}
```

### 2. Prevent Deleted User Login
```java
public UserDetails loadUserByUsername(String username) {
    User user = userRepository.findByUsernameAndStatusNot(username, UserStatus.DELETED)
        .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    
    if (user.getStatus() != UserStatus.ACTIVE) {
        throw new DisabledException("User account is " + user.getStatus());
    }
    
    return user;
}
```

### 3. Audit Logging
```java
@Transactional
public void deleteUser(String id) {
    User user = userRepository.findById(id)
        .orElseThrow(() -> new RuntimeException("User not found"));
    
    UserStatus oldStatus = user.getStatus();
    user.setStatus(UserStatus.DELETED);
    userRepository.save(user);
    
    // Log the change
    auditLog.log("User {} status changed from {} to {}", 
                 id, oldStatus, UserStatus.DELETED);
}
```

## Testing

### Unit Test Example
```java
@Test
void testSoftDelete() {
    // Create user
    User user = new User();
    user.setUsername("test");
    user.setStatus(UserStatus.ACTIVE);
    userRepository.save(user);
    
    // Soft delete
    userService.deleteUser(user.getId());
    
    // Verify still exists in DB
    Optional<User> found = userRepository.findById(user.getId());
    assertTrue(found.isPresent());
    
    // Verify status changed
    assertEquals(UserStatus.DELETED, found.get().getStatus());
}

@Test
void testCannotReactivateDeletedUser() {
    User user = createDeletedUser();
    
    assertThrows(RuntimeException.class, 
                () -> userService.reactivateUser(user.getId()));
}
```

## Compliance & Regulations

### GDPR Considerations
- Soft delete allows data retention for legal requirements
- Implement hard delete for "right to be forgotten" requests
- Add `deletedAt` timestamp field for retention policy enforcement

```java
@Column(name = "deleted_at")
private LocalDateTime deletedAt;

@PrePersist
@PreUpdate
protected void onStatusChange() {
    if (status == UserStatus.DELETED && deletedAt == null) {
        deletedAt = LocalDateTime.now();
    }
}
```

### Data Retention Policy
```java
// Delete users marked as DELETED for more than 30 days
@Scheduled(cron = "0 0 2 * * *")  // Run at 2 AM daily
public void cleanupDeletedUsers() {
    LocalDateTime cutoffDate = LocalDateTime.now().minusDays(30);
    List<User> usersToHardDelete = userRepository
        .findByStatusAndDeletedAtBefore(UserStatus.DELETED, cutoffDate);
    
    usersToHardDelete.forEach(user -> {
        auditLog.log("Hard deleting user {} (deleted on {})", 
                     user.getId(), user.getDeletedAt());
        userRepository.delete(user);
    });
}
```

## Summary

| Feature | Implementation | Benefit |
|---------|---------------|---------|
| Soft Delete | `status = DELETED` | Data retention |
| Multiple States | Enum with 6 states | Flexible lifecycle |
| Extensibility | Easy to add statuses | Future-proof |
| Query Support | Repository methods | Easy filtering |
| API Endpoints | RESTful status management | User-friendly |
| Security | Status-based access control | Secure |
| Audit Trail | Status change logging | Compliance |
| Recovery | Reactivation support | User-friendly |

**Status:** ✅ Implemented and tested
**Build:** ✅ BUILD SUCCESSFUL
**Database:** PostgreSQL/MySQL/H2 compatible

---

**Migration Path:** Boolean active → Enum status
**Breaking Changes:** API responses now return `status` instead of `active`
**Backward Compatibility:** N/A (new implementation)
