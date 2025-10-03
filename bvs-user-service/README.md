# BVS User Service

## Overview
The **BVS User Service** is a microservice responsible for user management and authentication within the BVS (Batch Video Streaming) Cloud Service platform. This service provides RESTful APIs for user registration, profile management, and user-related operations.

> **Note:** This README will be updated with extensive documentation upon completion of all features and enhancements.

## Current Features
✅ **User CRUD Operations** - Complete user lifecycle management
✅ **Database Independent** - Support for H2, PostgreSQL, MySQL via Spring Data JPA
✅ **RESTful API** - Standard HTTP endpoints for user management
✅ **Soft Delete** - Preserve user data with deactivation option
✅ **Health Check** - Service monitoring endpoint

## Technology Stack
- **Framework:** Spring Boot 3.x
- **ORM:** Spring Data JPA / Hibernate
- **Database:** H2 (development), PostgreSQL/MySQL (production-ready)
- **Build Tool:** Gradle
- **Java Version:** 17+

## Quick Start

### Prerequisites
- Java 17 or higher
- Gradle 7.x or higher

### Running the Service
```bash
# Build and run
./gradlew :bvs-user-service:bootRun

# Service will start on http://localhost:8080
```

