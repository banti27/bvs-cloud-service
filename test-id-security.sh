#!/bin/bash

# Test script to demonstrate that User ID cannot be overridden
# This script tests the ID generation security

echo "=============================================="
echo "User ID Generation Security Test"
echo "=============================================="
echo ""

BASE_URL="http://localhost:8080/api/users"

echo "Test 1: Create user WITHOUT providing ID"
echo "------------------------------------------"
curl -X POST $BASE_URL \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser1",
    "email": "test1@example.com",
    "password": "password123",
    "firstName": "Test",
    "lastName": "User"
  }' | jq

echo ""
echo ""
echo "Test 2: Attempt to create user WITH a custom ID"
echo "-------------------------------------------------"
echo "Note: Even though we send 'id' field, it will be IGNORED"
curl -X POST $BASE_URL \
  -H "Content-Type: application/json" \
  -d '{
    "id": "USR-MALICIOUS-HACK",
    "username": "testuser2",
    "email": "test2@example.com",
    "password": "password123",
    "firstName": "Hacker",
    "lastName": "Attempt"
  }' | jq

echo ""
echo ""
echo "Test 3: Verify all users have system-generated IDs"
echo "-----------------------------------------------------"
curl -X GET $BASE_URL | jq

echo ""
echo ""
echo "=============================================="
echo "Expected Results:"
echo "1. All IDs follow pattern: USR-TIMESTAMP-SUFFIX"
echo "2. The 'id' field in Test 2 was IGNORED"
echo "3. Each user has a unique system-generated ID"
echo "=============================================="
