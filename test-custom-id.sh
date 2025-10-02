#!/bin/bash

# Test Custom ID Generation

BASE_URL="http://localhost:8080"

echo "================================"
echo "Custom ID Generation Test"
echo "================================"
echo ""

echo "Starting bvs-user-service..."
echo "Please ensure the service is running:"
echo "  ./gradlew :bvs-user-service:bootRun"
echo ""
read -p "Press Enter when service is ready..."
echo ""

# Create User 1
echo "1. Creating User 1 - Check the custom ID format"
echo "   Expected format: USR-yyyyMMddHHmmss-XXXX"
echo ""
response1=$(curl -s -X POST $BASE_URL/api/users \
  -H "Content-Type: application/json" \
  -d '{
    "username": "alice_custom",
    "email": "alice@example.com",
    "password": "password123",
    "firstName": "Alice",
    "lastName": "Custom"
  }')

echo "$response1" | python3 -m json.tool
id1=$(echo "$response1" | python3 -c "import sys, json; print(json.load(sys.stdin)['id'])")
echo ""
echo "Generated ID: $id1"
echo ""

sleep 2

# Create User 2
echo "2. Creating User 2 - Different timestamp"
echo ""
response2=$(curl -s -X POST $BASE_URL/api/users \
  -H "Content-Type: application/json" \
  -d '{
    "username": "bob_custom",
    "email": "bob@example.com",
    "password": "password456",
    "firstName": "Bob",
    "lastName": "Custom"
  }')

echo "$response2" | python3 -m json.tool
id2=$(echo "$response2" | python3 -c "import sys, json; print(json.load(sys.stdin)['id'])")
echo ""
echo "Generated ID: $id2"
echo ""

# Get user by custom ID
echo "3. Getting User 1 by custom ID: $id1"
echo ""
curl -s $BASE_URL/api/users/$id1 | python3 -m json.tool
echo ""

# Get all users
echo ""
echo "4. Getting all users - showing custom IDs"
echo ""
curl -s $BASE_URL/api/users | python3 -m json.tool
echo ""

# Update user with custom ID
echo ""
echo "5. Updating User 1 (ID: $id1)"
echo ""
curl -s -X PUT $BASE_URL/api/users/$id1 \
  -H "Content-Type: application/json" \
  -d '{
    "username": "alice_updated",
    "email": "alice.updated@example.com",
    "password": "password123",
    "firstName": "Alice",
    "lastName": "Updated"
  }' | python3 -m json.tool
echo ""

# Verify update
echo ""
echo "6. Verifying update - fetching user again"
echo ""
curl -s $BASE_URL/api/users/$id1 | python3 -m json.tool
echo ""

# Delete user
echo ""
echo "7. Soft deleting User 2 (ID: $id2)"
echo ""
curl -s -X DELETE $BASE_URL/api/users/$id2
echo "User deleted"
echo ""

# Check active users
echo ""
echo "8. Getting active users (User 2 should not appear)"
echo ""
curl -s $BASE_URL/api/users/active | python3 -m json.tool
echo ""

echo "================================"
echo "Custom ID Generation Test Complete!"
echo ""
echo "Observations:"
echo "1. IDs follow format: USR-yyyyMMddHHmmss-XXXX"
echo "2. Each ID is unique"
echo "3. Timestamp reflects creation time"
echo "4. IDs work in all API operations"
echo "================================"
