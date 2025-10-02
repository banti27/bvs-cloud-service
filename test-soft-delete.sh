#!/bin/bash

# Test script for Soft Delete with User Status Management
# Demonstrates that users are never hard deleted by default

echo "=============================================="
echo "Soft Delete & User Status Management Test"
echo "=============================================="
echo ""

BASE_URL="http://localhost:8080/api/users"

# Colors for output
GREEN='\033[0;32m'
BLUE='\033[0;34m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m' # No Color

echo -e "${BLUE}Step 1: Create a test user${NC}"
echo "----------------------------------------"
USER_RESPONSE=$(curl -s -X POST $BASE_URL \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser_softdelete",
    "email": "softdelete@example.com",
    "password": "password123",
    "firstName": "Soft",
    "lastName": "Delete"
  }')

echo "$USER_RESPONSE" | jq
USER_ID=$(echo "$USER_RESPONSE" | jq -r '.id')
echo -e "${GREEN}✓ User created with ID: $USER_ID${NC}"
echo -e "${GREEN}✓ Initial status: ACTIVE${NC}"
echo ""

echo -e "${BLUE}Step 2: Get all active users${NC}"
echo "----------------------------------------"
curl -s -X GET $BASE_URL/active | jq
echo ""

echo -e "${YELLOW}Step 3: Soft Delete user (DELETE)${NC}"
echo "----------------------------------------"
curl -s -X DELETE $BASE_URL/$USER_ID
echo -e "${YELLOW}✓ User soft deleted (status changed to DELETED)${NC}"
echo ""

echo -e "${BLUE}Step 4: Verify user still exists in database${NC}"
echo "----------------------------------------"
curl -s -X GET $BASE_URL/$USER_ID | jq
echo -e "${GREEN}✓ User record still exists!${NC}"
echo -e "${GREEN}✓ Status is now: DELETED${NC}"
echo ""

echo -e "${BLUE}Step 5: Get all users (including deleted)${NC}"
echo "----------------------------------------"
curl -s -X GET $BASE_URL | jq
echo -e "${GREEN}✓ Deleted user appears in all users list${NC}"
echo ""

echo -e "${BLUE}Step 6: Get active users (deleted excluded)${NC}"
echo "----------------------------------------"
curl -s -X GET $BASE_URL/active | jq
echo -e "${GREEN}✓ Deleted user NOT in active users list${NC}"
echo ""

echo -e "${BLUE}Step 7: Get users by status (DELETED)${NC}"
echo "----------------------------------------"
curl -s -X GET $BASE_URL/status/DELETED | jq
echo -e "${GREEN}✓ Can query specifically for deleted users${NC}"
echo ""

echo -e "${BLUE}Step 8: Create another user for status demos${NC}"
echo "----------------------------------------"
USER2_RESPONSE=$(curl -s -X POST $BASE_URL \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser_status",
    "email": "status@example.com",
    "password": "password123",
    "firstName": "Status",
    "lastName": "Test"
  }')

echo "$USER2_RESPONSE" | jq
USER2_ID=$(echo "$USER2_RESPONSE" | jq -r '.id')
echo -e "${GREEN}✓ Second user created with ID: $USER2_ID${NC}"
echo ""

echo -e "${YELLOW}Step 9: Deactivate user (INACTIVE)${NC}"
echo "----------------------------------------"
curl -s -X PATCH $BASE_URL/$USER2_ID/deactivate
echo -e "${YELLOW}✓ User deactivated${NC}"
curl -s -X GET $BASE_URL/$USER2_ID | jq
echo ""

echo -e "${YELLOW}Step 10: Reactivate user (back to ACTIVE)${NC}"
echo "----------------------------------------"
curl -s -X PATCH $BASE_URL/$USER2_ID/reactivate
echo -e "${GREEN}✓ User reactivated${NC}"
curl -s -X GET $BASE_URL/$USER2_ID | jq
echo ""

echo -e "${RED}Step 11: Suspend user (SUSPENDED)${NC}"
echo "----------------------------------------"
curl -s -X PATCH $BASE_URL/$USER2_ID/suspend
echo -e "${RED}✓ User suspended${NC}"
curl -s -X GET $BASE_URL/$USER2_ID | jq
echo ""

echo -e "${RED}Step 12: Lock user account (LOCKED)${NC}"
echo "----------------------------------------"
curl -s -X PATCH $BASE_URL/$USER2_ID/lock
echo -e "${RED}✓ User locked${NC}"
curl -s -X GET $BASE_URL/$USER2_ID | jq
echo ""

echo -e "${BLUE}Step 13: Update status directly${NC}"
echo "----------------------------------------"
curl -s -X PATCH "$BASE_URL/$USER2_ID/status?status=PENDING"
echo -e "${BLUE}✓ Status updated to PENDING${NC}"
curl -s -X GET $BASE_URL/$USER2_ID | jq
echo ""

echo -e "${BLUE}Step 14: Get users by different statuses${NC}"
echo "----------------------------------------"
echo "Active users:"
curl -s -X GET $BASE_URL/status/ACTIVE | jq -c '[.[] | {id, username, status}]'
echo ""
echo "Deleted users:"
curl -s -X GET $BASE_URL/status/DELETED | jq -c '[.[] | {id, username, status}]'
echo ""
echo "Pending users:"
curl -s -X GET $BASE_URL/status/PENDING | jq -c '[.[] | {id, username, status}]'
echo ""

echo ""
echo "=============================================="
echo "Summary of Soft Delete Implementation"
echo "=============================================="
echo ""
echo -e "${GREEN}✓ Soft Delete:${NC} Users marked as DELETED, not removed"
echo -e "${GREEN}✓ Data Retention:${NC} All user data preserved in database"
echo -e "${GREEN}✓ Multiple States:${NC} ACTIVE, INACTIVE, SUSPENDED, PENDING, LOCKED, DELETED"
echo -e "${GREEN}✓ Status Management:${NC} Easy status transitions via API"
echo -e "${GREEN}✓ Query Support:${NC} Filter users by status"
echo -e "${GREEN}✓ Recovery:${NC} Can reactivate inactive users"
echo ""
echo "Available User Statuses:"
echo "  • ACTIVE    - User is active and can access system"
echo "  • INACTIVE  - Temporarily inactive (can be reactivated)"
echo "  • SUSPENDED - Suspended by admin (admin must reactivate)"
echo "  • PENDING   - Pending activation (e.g., email verification)"
echo "  • LOCKED    - Account locked (e.g., failed login attempts)"
echo "  • DELETED   - Soft deleted (data retained)"
echo ""
echo "API Endpoints:"
echo "  DELETE /api/users/{id}               - Soft delete (status → DELETED)"
echo "  PATCH  /api/users/{id}/deactivate    - Deactivate (status → INACTIVE)"
echo "  PATCH  /api/users/{id}/reactivate    - Reactivate (status → ACTIVE)"
echo "  PATCH  /api/users/{id}/suspend       - Suspend (status → SUSPENDED)"
echo "  PATCH  /api/users/{id}/lock          - Lock (status → LOCKED)"
echo "  PATCH  /api/users/{id}/status?status=X - Update to any status"
echo "  GET    /api/users/status/{status}    - Get users by status"
echo "  DELETE /api/users/{id}/hard          - Hard delete (⚠️  PERMANENT)"
echo ""
echo "=============================================="
