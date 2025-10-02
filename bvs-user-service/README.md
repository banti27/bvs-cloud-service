# BVS User Service - Database Integration

## Overview
The `bvs-user-service` now includes Spring Data JPA for database-independent ORM functionality.

## Features
✅ **Database Independent** - Switch between H2, PostgreSQL, MySQL without code changes
✅ **JPA/Hibernate ORM** - Entity mapping and relationship management
✅ **Spring Data Repositories** - Auto-generated CRUD operations
✅ **RESTful API** - Complete user management endpoints
✅ **Soft Delete** - Mark users as inactive instead of removing
✅ **Validation** - Username and email uniqueness checks

## Dependencies Added
- `spring-boot-starter-data-jpa` - Spring Data JPA
- `h2` - H2 in-memory database (default for development)
- `postgresql` - PostgreSQL driver
- `mysql-connector-j` - MySQL driver

## Database Configuration

### H2 (Default - In-Memory)
By default, the service uses H2 in-memory database for development:
```properties
spring.datasource.url=jdbc:h2:mem:bvs_user_db
spring.h2.console.enabled=true
spring.h2.console.path=/h2-console
```

Access H2 Console at: `http://localhost:8080/h2-console`
- JDBC URL: `jdbc:h2:mem:bvs_user_db`
- Username: `sa`
- Password: _(leave empty)_

### PostgreSQL
Uncomment in `application.properties`:
```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/bvs_user_db
spring.datasource.username=postgres
spring.datasource.password=postgres
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect
```

### MySQL
Uncomment in `application.properties`:
```properties
spring.datasource.url=jdbc:mysql://localhost:3306/bvs_user_db
spring.datasource.username=root
spring.datasource.password=root
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.MySQLDialect
```

## Project Structure
```
bvs-user-service/
└── src/main/java/com/bvs/user/
    ├── BvsUserServiceApplication.java    # Main application
    ├── controller/
    │   ├── HealthController.java         # Health check endpoint
    │   └── UserController.java           # User REST API
    ├── dto/
    │   ├── CreateUserRequest.java        # Request DTO
    │   └── UserDTO.java                  # Response DTO
    ├── entity/
    │   └── User.java                     # JPA Entity
    ├── repository/
    │   └── UserRepository.java           # Spring Data Repository
    └── service/
        └── UserService.java              # Business logic
```

## API Endpoints

### Health Check
```bash
GET /api/health
```

### User Management

#### Create User
```bash
POST /api/users
Content-Type: application/json

{
  "username": "john_doe",
  "email": "john@example.com",
  "password": "password123",
  "firstName": "John",
  "lastName": "Doe"
}
```

#### Get User by ID
```bash
GET /api/users/{id}
```

#### Get User by Username
```bash
GET /api/users/username/{username}
```

#### Get All Users
```bash
GET /api/users
```

#### Get Active Users Only
```bash
GET /api/users/active
```

#### Update User
```bash
PUT /api/users/{id}
Content-Type: application/json

{
  "username": "john_doe_updated",
  "email": "john.updated@example.com",
  "password": "newpassword",
  "firstName": "John",
  "lastName": "Doe"
}
```

#### Delete User (Soft Delete)
```bash
DELETE /api/users/{id}
```
Sets `active = false`, user data is retained.

#### Hard Delete User
```bash
DELETE /api/users/{id}/hard
```
Permanently removes user from database.

## Testing with cURL

### Start the service
```bash
./gradlew :bvs-user-service:bootRun
```

### Test the API
```bash
# Health check
curl http://localhost:8080/api/health

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

# Get all users
curl http://localhost:8080/api/users

# Get user by ID
curl http://localhost:8080/api/users/1

# Get user by username
curl http://localhost:8080/api/users/username/testuser

# Update user
curl -X PUT http://localhost:8080/api/users/1 \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser_updated",
    "email": "test.updated@example.com",
    "password": "password123",
    "firstName": "Test",
    "lastName": "User Updated"
  }'

# Soft delete user
curl -X DELETE http://localhost:8080/api/users/1

# Get active users (won't include deleted user)
curl http://localhost:8080/api/users/active
```

## User Entity Schema

| Column      | Type         | Constraints                    |
|-------------|--------------|--------------------------------|
| id          | BIGINT       | PRIMARY KEY, AUTO_INCREMENT    |
| username    | VARCHAR(100) | NOT NULL, UNIQUE               |
| email       | VARCHAR(100) | NOT NULL, UNIQUE               |
| password    | VARCHAR(255) | NOT NULL                       |
| first_name  | VARCHAR(50)  |                                |
| last_name   | VARCHAR(50)  |                                |
| active      | BOOLEAN      | NOT NULL, DEFAULT TRUE         |
| created_at  | TIMESTAMP    | NOT NULL, AUTO                 |
| updated_at  | TIMESTAMP    | AUTO                           |

## Security Notes

⚠️ **Important for Production:**
1. **Password Hashing**: Currently passwords are stored in plain text. Implement password hashing (BCrypt) before production.
2. **Validation**: Add input validation using `@Valid` and Bean Validation annotations.
3. **Exception Handling**: Implement global exception handler with `@ControllerAdvice`.
4. **Authentication**: Add Spring Security for authentication and authorization.
5. **H2 Console**: Disable H2 console in production (`spring.h2.console.enabled=false`).

## Next Steps

### Recommended Enhancements:
1. Add password encryption (Spring Security BCrypt)
2. Add input validation annotations
3. Implement pagination for list endpoints
4. Add global exception handler
5. Add API documentation (Swagger/OpenAPI)
6. Add database migration tool (Flyway/Liquibase)
7. Add unit and integration tests
8. Add logging with SLF4J

### Example: Add Validation
```java
// In CreateUserRequest.java
@NotBlank(message = "Username is required")
@Size(min = 3, max = 50)
private String username;

@NotBlank(message = "Email is required")
@Email(message = "Invalid email format")
private String email;
```

## Troubleshooting

### Issue: "Table 'USERS' not found"
**Solution**: Ensure `spring.jpa.hibernate.ddl-auto=update` is set in application.properties

### Issue: Database connection failed
**Solution**: 
- For PostgreSQL/MySQL: Ensure database is running and credentials are correct
- For H2: Should work out of the box (in-memory)

### Issue: Duplicate username/email
**Solution**: The service will return an error. Check if the username/email already exists.

## Build and Run

```bash
# Build
./gradlew :bvs-user-service:build

# Run
./gradlew :bvs-user-service:bootRun

# Build JAR
./gradlew :bvs-user-service:bootJar

# Run JAR
java -jar bvs-user-service/build/libs/bvs-user-service-1.0.0-SNAPSHOT.jar
```

## Status
✅ Database-independent ORM configured
✅ Complete CRUD operations
✅ RESTful API implemented
✅ Build successful
✅ Ready for development
