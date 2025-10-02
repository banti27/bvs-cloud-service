# BVS Common Library (`bvs-common-lib`)

## Overview
`bvs-common-lib` is a shared library module containing common utilities, DTOs, exceptions, and constants used across all BVS services.

## Purpose
- **Code Reusability**: Share common code across services
- **Consistency**: Maintain consistent patterns and structures
- **Maintainability**: Update shared logic in one place
- **Type Safety**: Common data structures and utilities

## Module Structure

```
bvs-common-lib/
├── build.gradle
└── src/main/java/com/bvs/common/
    ├── constants/
    │   ├── BvsConstants.java        # Common constants
    │   └── ErrorCodes.java          # Standard error codes
    ├── dto/
    │   ├── ApiResponse.java         # Standard API response wrapper
    │   ├── ErrorDetails.java        # Error details structure
    │   └── PageResponse.java        # Pagination response
    ├── exception/
    │   ├── BvsException.java                      # Base exception
    │   ├── ResourceNotFoundException.java         # 404 errors
    │   ├── ResourceAlreadyExistsException.java    # Duplicate errors
    │   └── ValidationException.java               # Validation errors
    └── util/
        ├── IdGenerator.java         # Custom ID generation
        ├── JsonUtil.java            # JSON utilities
        └── SecurityUtil.java        # Security utilities
```

## Components

### 1. DTOs (Data Transfer Objects)

#### ApiResponse<T>
Standard wrapper for all API responses:
```java
// Success response
ApiResponse<UserDTO> response = ApiResponse.success(userData);

// Success with custom message
ApiResponse<UserDTO> response = ApiResponse.success("User created", userData);

// Error response
ApiResponse<Void> response = ApiResponse.error("User not found");

// Error with details
ApiResponse<Void> response = ApiResponse.error("Validation failed", errorDetails);
```

**Response Format:**
```json
{
  "success": true,
  "message": "Success",
  "data": { /* your data */ },
  "error": null,
  "timestamp": "2025-10-03T14:30:25"
}
```

#### ErrorDetails
Detailed error information:
```java
ErrorDetails error = ErrorDetails.builder()
    .code("VALIDATION_ERROR")
    .details("Invalid input")
    .fieldErrors(Map.of("email", "Invalid email format"))
    .build();
```

#### PageResponse<T>
Pagination response:
```java
PageResponse<UserDTO> page = PageResponse.<UserDTO>builder()
    .content(users)
    .pageNumber(0)
    .pageSize(20)
    .totalElements(100)
    .totalPages(5)
    .first(true)
    .last(false)
    .hasNext(true)
    .hasPrevious(false)
    .build();
```

### 2. Exceptions

#### BvsException (Base)
```java
throw new BvsException("Something went wrong");
throw new BvsException("ERROR_CODE", "Message");
```

#### ResourceNotFoundException
```java
throw new ResourceNotFoundException("User not found");
throw new ResourceNotFoundException("User", "id", userId);
```

#### ResourceAlreadyExistsException
```java
throw new ResourceAlreadyExistsException("User already exists");
throw new ResourceAlreadyExistsException("User", "email", email);
```

#### ValidationException
```java
throw new ValidationException("Invalid input");
throw new ValidationException("email", "Invalid email format");
```

### 3. Utilities

#### IdGenerator
Generate custom IDs for any entity:
```java
// Generate with default suffix length (4)
String id = IdGenerator.generate("USR");
// Result: USR-20251003143025-A3B9

// Generate with custom suffix length
String id = IdGenerator.generate("ORD", 6);
// Result: ORD-20251003143025-A3B9XY

// Validate ID format
boolean valid = IdGenerator.isValid(id, "USR");
```

**Use Cases:**
- User IDs: `USR-20251003143025-A3B9`
- Order IDs: `ORD-20251003143025-X7M4`
- Storage IDs: `STR-20251003143025-K9P2`
- Any entity requiring custom IDs

#### JsonUtil
JSON serialization/deserialization:
```java
// Object to JSON
String json = JsonUtil.toJson(object);

// Object to pretty JSON
String prettyJson = JsonUtil.toPrettyJson(object);

// JSON to object
UserDTO user = JsonUtil.fromJson(json, UserDTO.class);

// Get ObjectMapper
ObjectMapper mapper = JsonUtil.getObjectMapper();
```

#### SecurityUtil
Security-related utilities:
```java
// Generate random token
String token = SecurityUtil.generateToken(32);

// SHA-256 hash
String hash = SecurityUtil.sha256("input");

// MD5 hash (for ETags, not passwords!)
String etag = SecurityUtil.md5("content");

// Mask sensitive data
String masked = SecurityUtil.mask("password123", 2);
// Result: pa*********

// Mask email
String maskedEmail = SecurityUtil.maskEmail("john.doe@example.com");
// Result: jo***@example.com
```

### 4. Constants

#### BvsConstants
```java
BvsConstants.API_VERSION;           // "v1"
BvsConstants.DEFAULT_PAGE_SIZE;     // 20
BvsConstants.MAX_PAGE_SIZE;         // 100
BvsConstants.DEFAULT_SORT_DIRECTION; // "ASC"
BvsConstants.DATE_FORMAT;           // "yyyy-MM-dd"
BvsConstants.DATETIME_FORMAT;       // "yyyy-MM-dd'T'HH:mm:ss"
BvsConstants.DEFAULT_TIMEZONE;      // "UTC"
```

#### ErrorCodes
```java
ErrorCodes.INTERNAL_ERROR;
ErrorCodes.VALIDATION_ERROR;
ErrorCodes.UNAUTHORIZED;
ErrorCodes.FORBIDDEN;
ErrorCodes.NOT_FOUND;
ErrorCodes.BAD_REQUEST;
ErrorCodes.RESOURCE_NOT_FOUND;
ErrorCodes.RESOURCE_ALREADY_EXISTS;
ErrorCodes.USER_NOT_FOUND;
ErrorCodes.USER_ALREADY_EXISTS;
ErrorCodes.FILE_NOT_FOUND;
ErrorCodes.FILE_UPLOAD_FAILED;
// ... and more
```

## Usage in Services

### Adding Dependency

In any service's `build.gradle`:
```gradle
dependencies {
    // BVS Common Library
    implementation project(':bvs-common-lib')
    
    // ... other dependencies
}
```

### Example: Using in User Service

#### 1. Using IdGenerator
```java
@Entity
public class User {
    @Id
    private String id;
    
    @PrePersist
    protected void onCreate() {
        if (id == null || id.isEmpty()) {
            id = IdGenerator.generate("USR");
        }
    }
}
```

#### 2. Using ApiResponse
```java
@RestController
@RequestMapping("/api/users")
public class UserController {
    
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<UserDTO>> getUser(@PathVariable String id) {
        UserDTO user = userService.getUserById(id);
        return ResponseEntity.ok(ApiResponse.success(user));
    }
    
    @PostMapping
    public ResponseEntity<ApiResponse<UserDTO>> createUser(@RequestBody CreateUserRequest request) {
        UserDTO user = userService.createUser(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("User created successfully", user));
    }
}
```

#### 3. Using Exceptions
```java
@Service
public class UserService {
    
    public UserDTO getUserById(String id) {
        return userRepository.findById(id)
                .map(this::convertToDTO)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));
    }
    
    public UserDTO createUser(CreateUserRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new ResourceAlreadyExistsException("User", "username", request.getUsername());
        }
        // ... create user
    }
}
```

#### 4. Using Constants
```java
@Service
public class UserService {
    
    public PageResponse<UserDTO> getUsers(int page, int size) {
        // Use constants for defaults
        size = Math.min(size, BvsConstants.MAX_PAGE_SIZE);
        // ... fetch data
    }
}
```

### Example: Using in Storage Service

```java
@Service
public class StorageService {
    
    public String generateFileId() {
        return IdGenerator.generate("FILE", 6);
        // Result: FILE-20251003143025-A3B9XY
    }
    
    public void uploadFile(MultipartFile file) {
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new BvsException(ErrorCodes.FILE_TOO_LARGE, 
                                  "File size exceeds maximum allowed size");
        }
        // ... upload logic
    }
}
```

## Benefits

### 1. Code Reusability
- Write once, use everywhere
- No code duplication
- Consistent implementations

### 2. Maintainability
- Update in one place
- All services get the update
- Easier refactoring

### 3. Consistency
- Same error handling across services
- Uniform API responses
- Consistent ID generation

### 4. Type Safety
- Shared data structures
- Compile-time checking
- IDE autocomplete

### 5. Testing
- Test common utilities once
- Reuse test utilities
- Shared test fixtures

## Extending the Library

### Adding New Utilities

1. Create new utility class:
```java
package com.bvs.common.util;

public class DateUtil {
    public static String formatDate(LocalDate date) {
        // ...
    }
}
```

2. Use in any service:
```java
String formatted = DateUtil.formatDate(LocalDate.now());
```

### Adding New Exceptions

1. Create exception:
```java
package com.bvs.common.exception;

public class PaymentException extends BvsException {
    public PaymentException(String message) {
        super("PAYMENT_ERROR", message);
    }
}
```

2. Use in payment service:
```java
throw new PaymentException("Payment failed");
```

### Adding New DTOs

1. Create DTO:
```java
package com.bvs.common.dto;

@Data
@Builder
public class AuditInfo {
    private String createdBy;
    private LocalDateTime createdAt;
    private String updatedBy;
    private LocalDateTime updatedAt;
}
```

2. Use in entities:
```java
@Embedded
private AuditInfo auditInfo;
```

## Dependencies

The common library includes:
- Spring Boot Starter
- Spring Boot Starter Web
- Spring Boot Starter Validation
- Lombok
- Jackson (JSON)
- Apache Commons Lang3
- Commons Codec

## Build Information

### Build the Library
```bash
./gradlew :bvs-common-lib:build
```

### Publish to Local Maven (Optional)
```bash
./gradlew :bvs-common-lib:publishToMavenLocal
```

### Check Dependencies
```bash
./gradlew :bvs-common-lib:dependencies
```

## Best Practices

### 1. Keep It Lightweight
- Only add truly common code
- Don't add service-specific logic
- Keep dependencies minimal

### 2. Documentation
- Document all public APIs
- Provide usage examples
- Keep README updated

### 3. Versioning
- Follow semantic versioning
- Maintain backward compatibility
- Communicate breaking changes

### 4. Testing
- Write unit tests for utilities
- Test exceptions
- Validate constants

## Current Services Using Common Library

✅ **bvs-user-service**
- Uses IdGenerator for user IDs
- Can use ApiResponse for responses
- Can use exceptions for error handling

✅ **bvs-storage-service**
- Uses common DTOs
- Can use IdGenerator for file IDs
- Can use SecurityUtil for tokens

## Future Enhancements

### Planned Additions
- [ ] Logging utilities
- [ ] Caching utilities
- [ ] Validation annotations
- [ ] Audit DTOs
- [ ] Event DTOs (for messaging)
- [ ] Metrics utilities
- [ ] Health check utilities
- [ ] Date/Time utilities

### Potential Features
- [ ] Common Spring configurations
- [ ] Database utilities
- [ ] API documentation annotations
- [ ] Testing utilities
- [ ] Mock data generators

## Summary

The `bvs-common-lib` provides:

✅ **Standard API Response** format
✅ **Custom ID Generation** utility
✅ **Common Exceptions** for error handling
✅ **Error Codes** constants
✅ **JSON Utilities** for serialization
✅ **Security Utilities** for tokens/hashing
✅ **Pagination** support
✅ **Shared Constants** across services

**Benefits:**
- 📦 Code reusability
- 🔄 Consistency across services
- 🛠️ Easy maintenance
- 🎯 Single source of truth
- ⚡ Faster development

---

**Module Status**: ✅ Built and integrated with all services
**Build Status**: ✅ BUILD SUCCESSFUL
**Ready to Use**: ✅ Yes
