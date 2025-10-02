# BVS Cloud Service

Multi-module Gradle project for BVS Cloud Services.

## Project Structure

- **bvs-user-service**: User management service
- **bvs-storage-service**: Storage service with AWS S3 integration

## Requirements

- Java 21
- Gradle 8.10.2 (via wrapper)

## Building the Project

```bash
./gradlew build
```

## Running Services

### User Service
```bash
./gradlew :bvs-user-service:bootRun
```

### Storage Service
```bash
./gradlew :bvs-storage-service:bootRun
```

## Module Details

### bvs-user-service
- Spring Boot Web application
- Runs on port 8080

### bvs-storage-service
- Spring Boot Web application with AWS S3 support
- Runs on port 8081
- Requires AWS credentials configuration

## Configuration

Configure AWS credentials for the storage service by setting environment variables:
- `AWS_ACCESS_KEY_ID`
- `AWS_SECRET_ACCESS_KEY`

Or update `bvs-storage-service/src/main/resources/application.properties`
