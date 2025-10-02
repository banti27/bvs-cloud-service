# Password Security with BCrypt - Implementation Guide

## ✅ Implemented: BCryptPasswordEncoder

### Why BCrypt is the Best Choice

#### 1. **Industry Standard**
- Used by millions of applications worldwide
- Recommended by OWASP and security experts
- Default choice in Spring Security

#### 2. **Adaptive Hashing**
- Cost factor can be increased over time
- Keeps pace with hardware improvements
- Current strength: 12 (customizable)

#### 3. **Built-in Salt**
- Automatically generates random salt for each password
- Salt is stored within the hash
- Prevents rainbow table attacks

#### 4. **Slow by Design**
- Intentionally computationally expensive
- Protects against brute-force attacks
- ~100-200ms per hash (configurable)

#### 5. **Proven Security**
- Based on Blowfish cipher
- No known vulnerabilities
- Battle-tested for decades

## Implementation Details

### Dependencies Added
```gradle
implementation 'org.springframework.boot:spring-boot-starter-security'
```

### Security Configuration
**File**: `SecurityConfig.java`

```java
@Bean
public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder(12);
}
```

**Strength Levels:**
- **10** (default): ~65ms, good for most apps
- **12** (recommended): ~260ms, better security
- **14**: ~1 second, high security
- **15**: ~2 seconds, very high security

### Service Integration
**File**: `UserService.java`

```java
@Service
@RequiredArgsConstructor
public class UserService {
    private final PasswordEncoder passwordEncoder;
    
    // Hash password when creating user
    user.setPassword(passwordEncoder.encode(rawPassword));
    
    // Verify password for login
    passwordEncoder.matches(rawPassword, hashedPassword);
}
```

## How BCrypt Works

### Hash Format
```
$2a$12$R9h/cIPz0gi.URNNX3kh2OPST9/PgBkqquzi.Ss7KIUgO2t0jWMUW
\__/\/ \____________________/\_____________________________/
 │  │         │                          │
 │  │         │                          └─ Hash (31 chars)
 │  │         └──────────────────────────── Salt (22 chars)
 │  └────────────────────────────────────── Cost factor (10-31)
 └───────────────────────────────────────── Algorithm identifier
```

### Example
**Plain text**: `password123`
**BCrypt hash**: `$2a$12$R9h/cIPz0gi.URNNX3kh2OPST9/PgBkqquzi.Ss7KIUgO2t0jWMUW`

### Key Features
1. **Same password → Different hashes** (due to random salt)
2. **One-way function** (cannot decrypt)
3. **Deterministic verification** (same password always matches)
4. **Length**: Always 60 characters

## Usage Examples

### 1. Creating a User (Password Hashing)

**Request:**
```bash
curl -X POST http://localhost:8080/api/users \
  -H "Content-Type: application/json" \
  -d '{
    "username": "john_doe",
    "email": "john@example.com",
    "password": "MySecurePassword123!",
    "firstName": "John",
    "lastName": "Doe"
  }'
```

**What Happens:**
1. Plain password: `MySecurePassword123!`
2. BCrypt generates random salt
3. Hashes password with salt and cost factor 12
4. Stores: `$2a$12$xyz...` (60 characters)

**Database Storage:**
```sql
id: USR-20251003143025-A3B9
username: john_doe
password: $2a$12$R9h/cIPz0gi.URNNX3kh2OPST9/PgBkqquzi.Ss7KIUgO2t0jWMUW
```

### 2. Verifying Password (Login)

```java
// In service
public boolean verifyPassword(String username, String rawPassword) {
    User user = userRepository.findByUsername(username).orElseThrow();
    return passwordEncoder.matches(rawPassword, user.getPassword());
}
```

**Example:**
```java
// User enters: "MySecurePassword123!"
boolean isValid = userService.verifyPassword("john_doe", "MySecurePassword123!");
// Returns: true (password matches)

// User enters: "WrongPassword"
boolean isValid = userService.verifyPassword("john_doe", "WrongPassword");
// Returns: false (password doesn't match)
```

### 3. Changing Password

```java
public void changePassword(String userId, String oldPassword, String newPassword) {
    User user = userRepository.findById(userId).orElseThrow();
    
    // Verify old password
    if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
        throw new RuntimeException("Invalid old password");
    }
    
    // Hash and set new password
    user.setPassword(passwordEncoder.encode(newPassword));
    userRepository.save(user);
}
```

## Security Features Implemented

### ✅ Password Hashing
- All passwords hashed with BCrypt before storage
- No plain text passwords in database

### ✅ Salt per Password
- Each password gets unique salt
- Prevents rainbow table attacks

### ✅ Configurable Strength
- Cost factor: 12 (can be adjusted)
- Balance between security and performance

### ✅ Update Password Support
- Passwords rehashed when changed
- Old password verification required

### ✅ Verification Method
- Secure password comparison
- Timing-attack resistant

## Comparison: Plain Text vs BCrypt

### Before (Plain Text) ❌
```
User: john_doe
Password: password123

Database stores: "password123"
```
**Risks:**
- Anyone with DB access sees passwords
- Breach = all passwords exposed
- No protection

### After (BCrypt) ✅
```
User: john_doe
Password: password123

Database stores: "$2a$12$R9h/cIPz0gi..."
```
**Benefits:**
- Hash is useless without original password
- Each password has unique salt
- Brute force takes centuries

## Alternative Encoders

### 1. Argon2 (Modern Choice)
```java
@Bean
public PasswordEncoder passwordEncoder() {
    return new Argon2PasswordEncoder(16, 32, 1, 4096, 3);
}
```
- **Pros**: Winner of Password Hashing Competition 2015, memory-hard
- **Cons**: Newer (less adoption), more complex configuration

### 2. SCrypt
```java
@Bean
public PasswordEncoder passwordEncoder() {
    return new SCryptPasswordEncoder(16384, 8, 1, 32, 64);
}
```
- **Pros**: Memory-hard, good security
- **Cons**: Slower than BCrypt

### 3. PBKDF2
```java
@Bean
public PasswordEncoder passwordEncoder() {
    return new Pbkdf2PasswordEncoder("secret", 310000, 256);
}
```
- **Pros**: NIST-approved standard
- **Cons**: Less secure than BCrypt for same iterations

### Recommendation Matrix

| Use Case | Best Choice | Why |
|----------|-------------|-----|
| **General web apps** | BCrypt | Industry standard, well-tested |
| **High security** | Argon2 | Most modern, memory-hard |
| **Legacy systems** | PBKDF2 | Standards compliance |
| **Mobile apps** | BCrypt | Good balance |
| **Microservices** | BCrypt | Fast enough, secure |

## Performance Considerations

### BCrypt Timing (Strength 12)
```
Hash:   ~260ms
Verify: ~260ms
```

### Impact on API
- **User Creation**: +260ms
- **Login**: +260ms
- **Password Change**: +520ms (verify old + hash new)

### Is 260ms acceptable?
✅ **Yes!** Because:
1. Only happens during user operations (not every request)
2. Protects against attacks worth millions
3. Can be done async if needed
4. Users won't notice

### Optimization Tips
```java
// For less sensitive operations, reduce strength
@Bean("fastEncoder")
public PasswordEncoder fastPasswordEncoder() {
    return new BCryptPasswordEncoder(10);  // ~65ms
}

// For sensitive operations, increase strength
@Bean("secureEncoder")
public PasswordEncoder securePasswordEncoder() {
    return new BCryptPasswordEncoder(14);  // ~1s
}
```

## Testing Password Encoding

### Unit Test Example
```java
@Test
void testPasswordEncoding() {
    PasswordEncoder encoder = new BCryptPasswordEncoder(12);
    
    String rawPassword = "MyPassword123!";
    String encoded = encoder.encode(rawPassword);
    
    // Verify format
    assertTrue(encoded.startsWith("$2a$12$"));
    assertEquals(60, encoded.length());
    
    // Verify matching
    assertTrue(encoder.matches(rawPassword, encoded));
    assertFalse(encoder.matches("WrongPassword", encoded));
    
    // Verify different salts
    String encoded2 = encoder.encode(rawPassword);
    assertNotEquals(encoded, encoded2);  // Different hashes
    assertTrue(encoder.matches(rawPassword, encoded2));  // Both valid
}
```

### Manual Testing
```bash
# Create user
curl -X POST http://localhost:8080/api/users \
  -H "Content-Type: application/json" \
  -d '{
    "username": "test",
    "email": "test@example.com",
    "password": "SecurePass123!",
    "firstName": "Test",
    "lastName": "User"
  }'

# Check H2 Console
# Visit: http://localhost:8080/h2-console
# Query: SELECT id, username, password FROM users;
# You'll see password is hashed: $2a$12$...
```

## Security Best Practices

### ✅ Implemented
- [x] BCrypt password hashing
- [x] Cost factor 12
- [x] Automatic salt generation
- [x] Password verification method
- [x] Change password with verification

### 🔜 Recommended Additions
- [ ] Password complexity requirements
- [ ] Password history (prevent reuse)
- [ ] Account lockout after failed attempts
- [ ] Two-factor authentication
- [ ] Password reset via email
- [ ] Session management
- [ ] Rate limiting on login
- [ ] Security audit logging

## Password Validation (Next Step)

### Add to CreateUserRequest.java
```java
@Pattern(
    regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$",
    message = "Password must contain at least 8 characters, one uppercase, one lowercase, one number and one special character"
)
private String password;
```

### Validation Rules
- Minimum 8 characters
- At least one uppercase letter
- At least one lowercase letter
- At least one number
- At least one special character

## Migration from Plain Text

If you have existing plain text passwords:

```java
@Service
public class PasswordMigrationService {
    
    @Transactional
    public void migratePasswords() {
        List<User> users = userRepository.findAll();
        
        for (User user : users) {
            String password = user.getPassword();
            
            // Check if already hashed
            if (!password.startsWith("$2a$")) {
                // Hash plain text password
                user.setPassword(passwordEncoder.encode(password));
                userRepository.save(user);
            }
        }
    }
}
```

## Summary

### ✅ What Was Implemented
1. **BCryptPasswordEncoder** - Strength 12
2. **SecurityConfig** - Bean configuration
3. **Password Hashing** - On user creation
4. **Password Verification** - Login support
5. **Change Password** - With old password verification
6. **Update Password** - On user updates (optional)

### 🔒 Security Level
- **Before**: ⚠️ Plain text (zero security)
- **After**: ✅ BCrypt (industry-standard security)

### 📊 Performance Impact
- **Hash time**: ~260ms (acceptable)
- **User experience**: No noticeable impact

### ✅ Build Status
```
BUILD SUCCESSFUL ✅
```

---

**Your passwords are now secure with BCrypt! 🔐**
