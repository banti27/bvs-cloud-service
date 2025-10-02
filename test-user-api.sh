#!/bin/bash

# BVS User Service - API Testing Script

BASE_URL="http://localhost:8080"

echo "================================"
echo "BVS User Service - API Tests"
echo "================================"
echo ""

# Health Check
echo "1. Health Check"
curl -s $BASE_URL/api/health | json_pp
echo -e "\n"

# Create User 1
echo "2. Creating User 1 (John Doe)"
curl -s -X POST $BASE_URL/api/users \
  -H "Content-Type: application/json" \
  -d '{
    "username": "john_doe",
    "email": "john@example.com",
    "password": "password123",
    "firstName": "John",
    "lastName": "Doe"
  }' | json_pp
echo -e "\n"

# Create User 2
echo "3. Creating User 2 (Jane Smith)"
curl -s -X POST $BASE_URL/api/users \
  -H "Content-Type: application/json" \
  -d '{
    "username": "jane_smith",
    "email": "jane@example.com",
    "password": "password456",
    "firstName": "Jane",
    "lastName": "Smith"
  }' | json_pp
echo -e "\n"

# Get All Users
echo "4. Getting All Users"
curl -s $BASE_URL/api/users | json_pp
echo -e "\n"

# Get User by ID
echo "5. Getting User by ID (1)"
curl -s $BASE_URL/api/users/1 | json_pp
echo -e "\n"

# Get User by Username
echo "6. Getting User by Username (john_doe)"
curl -s $BASE_URL/api/users/username/john_doe | json_pp
echo -e "\n"

# Update User
echo "7. Updating User 1"
curl -s -X PUT $BASE_URL/api/users/1 \
  -H "Content-Type: application/json" \
  -d '{
    "username": "john_doe_updated",
    "email": "john.updated@example.com",
    "password": "password123",
    "firstName": "John",
    "lastName": "Doe Updated"
  }' | json_pp
echo -e "\n"

# Get Active Users
echo "8. Getting Active Users"
curl -s $BASE_URL/api/users/active | json_pp
echo -e "\n"

# Soft Delete User
echo "9. Soft Deleting User 2"
curl -s -X DELETE $BASE_URL/api/users/2
echo "User 2 deleted (soft)"
echo -e "\n"

# Get Active Users After Delete
echo "10. Getting Active Users After Delete"
curl -s $BASE_URL/api/users/active | json_pp
echo -e "\n"

# Get All Users (includes inactive)
echo "11. Getting All Users (includes inactive)"
curl -s $BASE_URL/api/users | json_pp
echo -e "\n"

echo "================================"
echo "All tests completed!"
echo "================================"
