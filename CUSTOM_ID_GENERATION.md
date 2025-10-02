# Custom ID Generation Strategies for User Entity

## Overview
This document explains different approaches to implement custom ID generation with the pattern: `PREFIX-TIMESTAMP-SUFFIX`

Example ID: `USR-20251003143025-A3B9`

## ✅ Implemented Solution: @PrePersist (Recommended)

### Why This Approach?
- ✅ Simple and straightforward
- ✅ No deprecated APIs
- ✅ Full control over ID generation logic
- ✅ Works with all JPA providers
- ✅ Easy to test and debug

### Implementation

**User.java:**
```java
@Entity
@Table(name = "users")
public class User {
    
    @Id
    @Column(length = 50)
    private String id;
    
    // ... other fields ...
    
    @PrePersist
    protected void onCreate() {
        if (id == null || id.isEmpty()) {
            id = generateCustomId();
        }
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    private String generateCustomId() {
        String prefix = "USR";
        String timestamp = LocalDateTime.now()
            .format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String suffix = generateRandomSuffix(4);
        return String.format("%s-%s-%s", prefix, timestamp, suffix);
    }
    
    private String generateRandomSuffix(int length) {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        StringBuilder suffix = new StringBuilder(length);
        ThreadLocalRandom random = ThreadLocalRandom.current();
        
        for (int i = 0; i < length; i++) {
            int index = random.nextInt(chars.length());
            suffix.append(chars.charAt(index));
        }
        
        return suffix.toString();
    }
}
```

### Key Changes Made:
1. **ID Type**: Changed from `Long` to `String`
2. **Generation Logic**: Implemented in `@PrePersist` method
3. **Format**: `PREFIX-TIMESTAMP-SUFFIX`
   - PREFIX: "USR" (User identifier)
   - TIMESTAMP: yyyyMMddHHmmss (14 digits)
   - SUFFIX: 4 random alphanumeric characters

### Updated Files:
- ✅ `User.java` - Changed ID type and added generation logic
- ✅ `UserDTO.java` - Changed ID from Long to String
- ✅ `UserRepository.java` - Changed JpaRepository<User, String>
- ✅ `UserService.java` - Updated all method signatures
- ✅ `UserController.java` - Updated all @PathVariable types

## Alternative Approaches

### Option 2: Custom Hibernate IdentifierGenerator

**Pros:**
- Separate ID generation logic
- Reusable across entities
- Called before @PrePersist

**Cons:**
- More complex setup
- Requires Hibernate-specific code
- Deprecated annotations in newer versions

**Implementation:**
```java
// UserIdGenerator.java
public class UserIdGenerator implements IdentifierGenerator {
    @Override
    public Serializable generate(SharedSessionContractImplementor session, Object object) {
        String prefix = "USR";
        String timestamp = LocalDateTime.now()
            .format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String suffix = generateRandomSuffix(4);
        return String.format("%s-%s-%s", prefix, timestamp, suffix);
    }
}

// User.java
@Entity
public class User {
    @Id
    @GeneratedValue(generator = "user-id-generator")
    @GenericGenerator(name = "user-id-generator", 
                      strategy = "com.bvs.user.generator.UserIdGenerator")
    private String id;
}
```

### Option 3: Service Layer ID Generation

**Pros:**
- Full control in service layer
- Easy to test
- No JPA/Hibernate specifics

**Cons:**
- Manual ID assignment required
- ID must be set before calling repository.save()

**Implementation:**
```java
@Service
public class UserService {
    
    @Transactional
    public UserDTO createUser(CreateUserRequest request) {
        User user = new User();
        user.setId(generateUserId()); // Generate ID manually
        user.setUsername(request.getUsername());
        // ... set other fields ...
        
        return convertToDTO(userRepository.save(user));
    }
    
    private String generateUserId() {
        String prefix = "USR";
        String timestamp = LocalDateTime.now()
            .format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String suffix = generateRandomSuffix(4);
        return String.format("%s-%s-%s", prefix, timestamp, suffix);
    }
}
```

### Option 4: UUID-based Approach

**Pros:**
- Guaranteed uniqueness
- No timestamp dependency
- Standard approach

**Cons:**
- Longer IDs
- Less readable
- No timestamp information

**Implementation:**
```java
@Entity
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
}

// Or with custom format:
@PrePersist
protected void onCreate() {
    if (id == null) {
        id = "USR-" + UUID.randomUUID().toString();
    }
}
```

### Option 5: Database Sequence/Trigger

**Pros:**
- Handled by database
- Consistent across applications
- Atomic generation

**Cons:**
- Database-specific
- Less portable
- Harder to customize

**PostgreSQL Example:**
```sql
CREATE SEQUENCE user_id_seq START 1;

CREATE OR REPLACE FUNCTION generate_user_id()
RETURNS TRIGGER AS $$
BEGIN
    NEW.id := 'USR-' || 
              TO_CHAR(NOW(), 'YYYYMMDDHH24MISS') || '-' ||
              LPAD(nextval('user_id_seq')::TEXT, 4, '0');
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER user_id_trigger
BEFORE INSERT ON users
FOR EACH ROW
EXECUTE FUNCTION generate_user_id();
```

## ID Format Customization

### Current Format
```
USR-20251003143025-A3B9
│   │              │
│   │              └─ Random suffix (4 chars)
│   └──────────────── Timestamp (yyyyMMddHHmmss)
└──────────────────── Prefix (entity type)
```

### Customization Options

#### 1. Change Prefix
```java
private String generateCustomId() {
    String prefix = "USER";  // More descriptive
    // ... rest of code
}
```

#### 2. Different Timestamp Format
```java
// Shorter: yyyyMMddHHmm
String timestamp = LocalDateTime.now()
    .format(DateTimeFormatter.ofPattern("yyyyMMddHHmm"));
// Example: USR-202510031430-A3B9

// With milliseconds: yyyyMMddHHmmssSSS
String timestamp = LocalDateTime.now()
    .format(DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS"));
// Example: USR-20251003143025123-A3B9
```

#### 3. Longer/Shorter Suffix
```java
String suffix = generateRandomSuffix(6);  // 6 characters
// Example: USR-20251003143025-A3B9XY
```

#### 4. Sequential Instead of Random
```java
// Use AtomicLong for sequence
private static final AtomicLong sequence = new AtomicLong(0);

private String generateCustomId() {
    String prefix = "USR";
    String timestamp = LocalDateTime.now()
        .format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
    String suffix = String.format("%04d", sequence.incrementAndGet() % 10000);
    return String.format("%s-%s-%s", prefix, timestamp, suffix);
}
```

#### 5. Include Environment
```java
@Value("${spring.profiles.active:dev}")
private String environment;

private String generateCustomId() {
    String prefix = "USR";
    String env = environment.substring(0, 1).toUpperCase(); // D, P, T
    String timestamp = LocalDateTime.now()
        .format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
    String suffix = generateRandomSuffix(4);
    return String.format("%s-%s-%s-%s", prefix, env, timestamp, suffix);
}
// Example: USR-P-20251003143025-A3B9 (Production)
```

## Testing the Custom ID Generation

### Test Script
```bash
# Create a user
curl -X POST http://localhost:8080/api/users \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "email": "test@example.com",
    "password": "password123",
    "firstName": "Test",
    "lastName": "User"
  }'

# Response will include generated ID:
{
  "id": "USR-20251003143025-A3B9",
  "username": "testuser",
  "email": "test@example.com",
  "firstName": "Test",
  "lastName": "User",
  "active": true
}

# Get user by custom ID
curl http://localhost:8080/api/users/USR-20251003143025-A3B9
```

## Advantages of Custom IDs

### 1. Readability
- ✅ Easy to identify entity type (USR prefix)
- ✅ Timestamp embedded (useful for debugging)
- ✅ Human-readable format

### 2. No Auto-increment Issues
- ✅ No database sequence conflicts in distributed systems
- ✅ IDs generated before database insert
- ✅ Can be used in URLs safely

### 3. Sorting
- ✅ IDs naturally sort by creation time (due to timestamp)
- ✅ Easy to filter by time ranges

### 4. Security
- ✅ Non-sequential (harder to guess)
- ✅ No information leakage about total records

## Considerations

### Uniqueness
- **Current implementation**: Random 4-char suffix provides 1,679,616 combinations
- **Collision risk**: Very low within same second
- **If needed**: Increase suffix length or add uniqueness check

### Performance
- **Impact**: Minimal (ID generation is in-memory)
- **Database**: String IDs slightly larger than numeric, but negligible

### Length
- Current: ~30 characters (USR-20251003143025-A3B9)
- Storage: VARCHAR(50) is more than sufficient
- URLs: Works fine in REST APIs

## Migration from Auto-increment

If migrating from Long ID to String ID:

1. Create new column for string ID
2. Generate IDs for existing records
3. Update foreign keys
4. Switch primary key
5. Remove old numeric ID column

**SQL Example:**
```sql
-- 1. Add new column
ALTER TABLE users ADD COLUMN new_id VARCHAR(50);

-- 2. Generate IDs for existing records
UPDATE users 
SET new_id = CONCAT('USR-', 
                    TO_CHAR(created_at, 'YYYYMMDDHH24MISS'), 
                    '-', 
                    LPAD(id::TEXT, 4, '0'))
WHERE new_id IS NULL;

-- 3. Make new_id primary key (after updating references)
ALTER TABLE users DROP CONSTRAINT users_pkey;
ALTER TABLE users ADD PRIMARY KEY (new_id);
ALTER TABLE users DROP COLUMN id;
ALTER TABLE users RENAME COLUMN new_id TO id;
```

## Summary

✅ **Implemented**: @PrePersist approach
✅ **ID Format**: USR-yyyyMMddHHmmss-XXXX
✅ **Type**: String (VARCHAR(50))
✅ **Build Status**: Successful
✅ **Ready to use**: Yes

The custom ID generation is now active and will automatically generate unique IDs for all new users!
