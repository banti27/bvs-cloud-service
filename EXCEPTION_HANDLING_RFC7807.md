# Exception Handling with ProblemDetail (RFC 7807)

## Overview
This implementation uses **Spring Framework 6's ProblemDetail** class for standardized error responses following RFC 7807. All exceptions are handled globally using `@RestControllerAdvice`.

## Why ProblemDetail?

### Benefits of RFC 7807 Standard
1. **Standardized Format**: Industry-standard error response format
2. **Machine-Readable**: Structured error information for clients
3. **Human-Friendly**: Clear error messages and descriptions
4. **Extensible**: Can add custom properties
5. **Type URIs**: Links to error documentation
6. **HTTP-Compliant**: Proper use of HTTP status codes

### Before vs After

#### ❌ Before (RuntimeException)
```java
throw new RuntimeException("User not found with id: 123");
```

**Response:** Inconsistent error format
```json
{
  "timestamp": "2025-10-03T10:00:00",
  "status": 500,
  "error": "Internal Server Error",
  "message": "User not found with id: 123",
  "path": "/api/users/123"
}
```

#### ✅ After (ProblemDetail with Custom Exceptions)
```java
throw new UserNotFoundException("id", "123");
```

**Response:** RFC 7807 compliant format
```json
{
  "type": "https://api.bvs.com/errors/user-not-found",
  "title": "User Not Found",
  "status": 404,
  "detail": "User not found with id: 123",
  "errorCode": "USER_NOT_FOUND",
  "timestamp": "2025-10-03T10:00:00Z"
}
```

---

## Architecture

### 1. Custom Exception Hierarchy

```
UserServiceException (Base)
    ├── UserNotFoundException
    ├── UserAlreadyExistsException
    ├── InvalidPasswordException
    └── InvalidStatusTransitionException
```

### 2. Global Exception Handler

```
@RestControllerAdvice
GlobalExceptionHandler
    ├── handleUserNotFoundException()
    ├── handleUserAlreadyExistsException()
    ├── handleInvalidPasswordException()
    ├── handleInvalidStatusTransitionException()
    ├── handleUserServiceException()
    ├── handleValidationException()
    └── handleGlobalException()
```

---

## Exception Classes

### 1. UserServiceException (Base)

**File:** `UserServiceException.java`

```java
public class UserServiceException extends RuntimeException {
    private final String errorCode;
    
    public UserServiceException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }
}
```

**Purpose:** Base exception for all user service exceptions

### 2. UserNotFoundException

**File:** `UserNotFoundException.java`

```java
public class UserNotFoundException extends UserServiceException {
    public UserNotFoundException(String field, String value) {
        super("USER_NOT_FOUND", 
              String.format("User not found with %s: %s", field, value));
    }
}
```

**Usage:**
```java
throw new UserNotFoundException("id", userId);
throw new UserNotFoundException("username", username);
throw new UserNotFoundException("email", email);
```

**HTTP Status:** 404 NOT FOUND

### 3. UserAlreadyExistsException

**File:** `UserAlreadyExistsException.java`

```java
public class UserAlreadyExistsException extends UserServiceException {
    public UserAlreadyExistsException(String field, String value) {
        super("USER_ALREADY_EXISTS", 
              String.format("User with %s '%s' already exists", field, value));
    }
}
```

**Usage:**
```java
throw new UserAlreadyExistsException("username", username);
throw new UserAlreadyExistsException("email", email);
```

**HTTP Status:** 409 CONFLICT

### 4. InvalidPasswordException

**File:** `InvalidPasswordException.java`

```java
public class InvalidPasswordException extends UserServiceException {
    public InvalidPasswordException(String message) {
        super("INVALID_PASSWORD", message);
    }
}
```

**Usage:**
```java
throw new InvalidPasswordException("Invalid old password");
throw new InvalidPasswordException("Password does not meet requirements");
```

**HTTP Status:** 401 UNAUTHORIZED

### 5. InvalidStatusTransitionException

**File:** `InvalidStatusTransitionException.java`

```java
public class InvalidStatusTransitionException extends UserServiceException {
    public InvalidStatusTransitionException(String message) {
        super("INVALID_STATUS_TRANSITION", message);
    }
}
```

**Usage:**
```java
throw new InvalidStatusTransitionException("Cannot reactivate deleted user");
throw new InvalidStatusTransitionException(UserStatus.DELETED, UserStatus.ACTIVE);
```

**HTTP Status:** 400 BAD REQUEST

---

## Global Exception Handler

**File:** `GlobalExceptionHandler.java`

### Features

1. **@RestControllerAdvice**: Applies to all REST controllers
2. **ProblemDetail**: RFC 7807 compliant responses
3. **Custom Properties**: Add errorCode, timestamp, etc.
4. **Type URIs**: Links to error documentation
5. **Proper HTTP Status Codes**: Semantically correct status codes

### Exception Mapping

| Exception | HTTP Status | Error Code | Title |
|-----------|-------------|------------|-------|
| `UserNotFoundException` | 404 | USER_NOT_FOUND | User Not Found |
| `UserAlreadyExistsException` | 409 | USER_ALREADY_EXISTS | User Already Exists |
| `InvalidPasswordException` | 401 | INVALID_PASSWORD | Invalid Password |
| `InvalidStatusTransitionException` | 400 | INVALID_STATUS_TRANSITION | Invalid Status Transition |
| `UserServiceException` | 400 | USER_SERVICE_ERROR | User Service Error |
| `MethodArgumentNotValidException` | 400 | VALIDATION_ERROR | Validation Error |
| `Exception` | 500 | INTERNAL_ERROR | Internal Server Error |

### Handler Example

```java
@ExceptionHandler(UserNotFoundException.class)
public ProblemDetail handleUserNotFoundException(
        UserNotFoundException ex, WebRequest request) {
    
    ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
        HttpStatus.NOT_FOUND, 
        ex.getMessage()
    );
    
    problemDetail.setTitle("User Not Found");
    problemDetail.setType(URI.create("https://api.bvs.com/errors/user-not-found"));
    problemDetail.setProperty("errorCode", ex.getErrorCode());
    problemDetail.setProperty("timestamp", Instant.now());
    
    return problemDetail;
}
```

---

## ProblemDetail Response Format

### Structure

```json
{
  "type": "URI",           // Link to error documentation
  "title": "string",       // Short, human-readable summary
  "status": 404,           // HTTP status code
  "detail": "string",      // Detailed error description
  "errorCode": "string",   // Machine-readable error code
  "timestamp": "ISO8601"   // When the error occurred
}
```

### Example Responses

#### 1. User Not Found (404)

**Request:**
```bash
GET /api/users/USR-INVALID-ID
```

**Response:**
```json
{
  "type": "https://api.bvs.com/errors/user-not-found",
  "title": "User Not Found",
  "status": 404,
  "detail": "User not found with id: USR-INVALID-ID",
  "errorCode": "USER_NOT_FOUND",
  "timestamp": "2025-10-03T14:30:25Z"
}
```

#### 2. User Already Exists (409)

**Request:**
```bash
POST /api/users
{
  "username": "existing_user",
  "email": "test@example.com"
}
```

**Response:**
```json
{
  "type": "https://api.bvs.com/errors/user-already-exists",
  "title": "User Already Exists",
  "status": 409,
  "detail": "User with username 'existing_user' already exists",
  "errorCode": "USER_ALREADY_EXISTS",
  "timestamp": "2025-10-03T14:30:25Z"
}
```

#### 3. Invalid Password (401)

**Request:**
```bash
POST /api/users/USR-123/change-password
{
  "oldPassword": "wrong_password",
  "newPassword": "new_password"
}
```

**Response:**
```json
{
  "type": "https://api.bvs.com/errors/invalid-password",
  "title": "Invalid Password",
  "status": 401,
  "detail": "Invalid old password",
  "errorCode": "INVALID_PASSWORD",
  "timestamp": "2025-10-03T14:30:25Z"
}
```

#### 4. Invalid Status Transition (400)

**Request:**
```bash
PATCH /api/users/USR-123/reactivate
# User status is DELETED
```

**Response:**
```json
{
  "type": "https://api.bvs.com/errors/invalid-status-transition",
  "title": "Invalid Status Transition",
  "status": 400,
  "detail": "Cannot reactivate deleted user",
  "errorCode": "INVALID_STATUS_TRANSITION",
  "timestamp": "2025-10-03T14:30:25Z"
}
```

#### 5. Validation Error (400)

**Request:**
```bash
POST /api/users
{
  "username": "",
  "email": "invalid-email"
}
```

**Response:**
```json
{
  "type": "https://api.bvs.com/errors/validation-error",
  "title": "Validation Error",
  "status": 400,
  "detail": "Validation failed",
  "errorCode": "VALIDATION_ERROR",
  "timestamp": "2025-10-03T14:30:25Z",
  "errors": [
    "username: must not be blank",
    "email: must be a valid email address"
  ]
}
```

#### 6. Internal Server Error (500)

**Request:**
```bash
GET /api/users/causesUnexpectedError
```

**Response:**
```json
{
  "type": "https://api.bvs.com/errors/internal-error",
  "title": "Internal Server Error",
  "status": 500,
  "detail": "An unexpected error occurred",
  "errorCode": "INTERNAL_ERROR",
  "timestamp": "2025-10-03T14:30:25Z"
}
```

---

## Usage in UserService

### Before (RuntimeException)

```java
@Transactional
public UserDTO createUser(CreateUserRequest request) {
    if (userRepository.existsByUsername(request.getUsername())) {
        throw new RuntimeException("Username already exists");  // ❌
    }
    // ...
}
```

### After (Custom Exceptions)

```java
@Transactional
public UserDTO createUser(CreateUserRequest request) {
    if (userRepository.existsByUsername(request.getUsername())) {
        throw new UserAlreadyExistsException("username", request.getUsername());  // ✅
    }
    // ...
}
```

### All Exception Usage Examples

```java
// User not found
throw new UserNotFoundException("id", userId);
throw new UserNotFoundException("username", username);

// User already exists
throw new UserAlreadyExistsException("username", username);
throw new UserAlreadyExistsException("email", email);

// Invalid password
throw new InvalidPasswordException("Invalid old password");

// Invalid status transition
throw new InvalidStatusTransitionException("Cannot reactivate deleted user");
```

---

## Testing Exception Handling

### 1. Test User Not Found

```bash
curl -i http://localhost:8080/api/users/INVALID-ID
```

**Expected:**
- Status: 404
- Body: ProblemDetail with USER_NOT_FOUND

### 2. Test User Already Exists

```bash
# Create user first
curl -X POST http://localhost:8080/api/users \
  -H "Content-Type: application/json" \
  -d '{"username":"testuser","email":"test@example.com","password":"pass"}'

# Try to create same user again
curl -i -X POST http://localhost:8080/api/users \
  -H "Content-Type: application/json" \
  -d '{"username":"testuser","email":"test@example.com","password":"pass"}'
```

**Expected:**
- Status: 409
- Body: ProblemDetail with USER_ALREADY_EXISTS

### 3. Test Invalid Password

```bash
curl -i -X POST http://localhost:8080/api/users/USR-123/change-password \
  -H "Content-Type: application/json" \
  -d '{
    "oldPassword": "wrong_password",
    "newPassword": "new_password"
  }'
```

**Expected:**
- Status: 401
- Body: ProblemDetail with INVALID_PASSWORD

### 4. Test Invalid Status Transition

```bash
# Soft delete user first
curl -X DELETE http://localhost:8080/api/users/USR-123

# Try to reactivate
curl -i -X PATCH http://localhost:8080/api/users/USR-123/reactivate
```

**Expected:**
- Status: 400
- Body: ProblemDetail with INVALID_STATUS_TRANSITION

---

## Client Integration

### JavaScript/TypeScript

```typescript
interface ProblemDetail {
  type: string;
  title: string;
  status: number;
  detail: string;
  errorCode: string;
  timestamp: string;
  errors?: string[];
}

async function createUser(userData: CreateUserRequest) {
  try {
    const response = await fetch('/api/users', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(userData)
    });
    
    if (!response.ok) {
      const problem: ProblemDetail = await response.json();
      
      switch (problem.errorCode) {
        case 'USER_ALREADY_EXISTS':
          alert('Username or email already exists');
          break;
        case 'VALIDATION_ERROR':
          alert('Validation failed: ' + problem.errors?.join(', '));
          break;
        default:
          alert(problem.detail);
      }
      
      throw new Error(problem.detail);
    }
    
    return await response.json();
  } catch (error) {
    console.error('Failed to create user:', error);
    throw error;
  }
}
```

### Java Client

```java
@Service
public class UserClient {
    
    private final RestTemplate restTemplate;
    
    public UserDTO getUser(String id) {
        try {
            return restTemplate.getForObject(
                "http://api.bvs.com/users/" + id, 
                UserDTO.class
            );
        } catch (HttpClientErrorException.NotFound ex) {
            ProblemDetail problem = ex.getResponseBodyAs(ProblemDetail.class);
            if ("USER_NOT_FOUND".equals(problem.getProperties().get("errorCode"))) {
                throw new UserNotFoundException("User not found: " + id);
            }
            throw ex;
        }
    }
}
```

---

## Benefits Summary

| Benefit | Description |
|---------|-------------|
| 📋 **Standardized** | RFC 7807 compliant format |
| 🎯 **Type Safe** | Custom exception hierarchy |
| 🔍 **Discoverable** | Type URIs link to documentation |
| 🤖 **Machine Readable** | Error codes for programmatic handling |
| 👤 **Human Friendly** | Clear titles and details |
| 🔧 **Extensible** | Custom properties support |
| 🌐 **HTTP Compliant** | Proper status codes |
| 📊 **Consistent** | Same format across all errors |
| 🔒 **Secure** | No stack traces in production |
| 🧪 **Testable** | Easy to test error scenarios |

---

## Comparison

### ❌ Old Approach (RuntimeException)

**Problems:**
- Generic error messages
- Inconsistent response format
- No error codes
- Wrong HTTP status (500 for everything)
- Stack traces exposed
- No type information
- Hard to handle on client side

### ✅ New Approach (ProblemDetail + Custom Exceptions)

**Advantages:**
- Specific exception types
- RFC 7807 compliant format
- Machine-readable error codes
- Correct HTTP status codes
- No stack traces
- Type URIs for documentation
- Easy client-side handling
- Extensible with custom properties

---

## Best Practices

### 1. Use Specific Exceptions
```java
// ❌ Don't
throw new RuntimeException("User not found");

// ✅ Do
throw new UserNotFoundException("id", userId);
```

### 2. Provide Context
```java
// ❌ Don't
throw new UserAlreadyExistsException("User exists");

// ✅ Do
throw new UserAlreadyExistsException("username", username);
```

### 3. Use Appropriate HTTP Status
- 400: Client error (bad request, validation)
- 401: Authentication failure
- 403: Authorization failure
- 404: Resource not found
- 409: Conflict (duplicate)
- 500: Server error

### 4. Add Custom Properties
```java
problemDetail.setProperty("userId", userId);
problemDetail.setProperty("attemptedAction", "reactivate");
```

### 5. Document Error Types
Create documentation at the type URIs:
- `https://api.bvs.com/errors/user-not-found`
- `https://api.bvs.com/errors/user-already-exists`
- etc.

---

## Files Created

| File | Purpose |
|------|---------|
| `UserServiceException.java` | Base exception class |
| `UserNotFoundException.java` | User not found (404) |
| `UserAlreadyExistsException.java` | Duplicate user (409) |
| `InvalidPasswordException.java` | Password validation (401) |
| `InvalidStatusTransitionException.java` | Invalid state change (400) |
| `GlobalExceptionHandler.java` | @RestControllerAdvice handler |

---

## Summary

✅ **Implemented:**
- Custom exception hierarchy
- ProblemDetail responses (RFC 7807)
- Global exception handler
- Proper HTTP status codes
- Machine-readable error codes
- No more RuntimeException

✅ **Benefits:**
- Standardized error format
- Better client experience
- Type-safe error handling
- Production-ready
- RFC 7807 compliant

✅ **Build Status:**
```
BUILD SUCCESSFUL in 881ms
```

---

**Status:** ✅ Complete
**Standard:** RFC 7807 (ProblemDetail)
**Spring Version:** Spring Framework 6+
**Production Ready:** Yes
