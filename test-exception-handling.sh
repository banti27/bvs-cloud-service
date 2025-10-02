#!/bin/bash

# Test script for ProblemDetail Exception Handling (RFC 7807)
# Demonstrates standardized error responses

echo "=============================================="
echo "ProblemDetail Exception Handling Test"
echo "RFC 7807 Compliant Error Responses"
echo "=============================================="
echo ""

BASE_URL="http://localhost:8080/api/users"

# Colors
GREEN='\033[0;32m'
BLUE='\033[0;34m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m'

echo -e "${BLUE}Test 1: User Not Found (404)${NC}"
echo "----------------------------------------"
echo "GET /api/users/INVALID-ID"
curl -s -X GET $BASE_URL/INVALID-ID | jq
echo ""
echo -e "${YELLOW}Expected: 404 NOT FOUND with ProblemDetail${NC}"
echo -e "${YELLOW}Error Code: USER_NOT_FOUND${NC}"
echo ""

echo -e "${BLUE}Test 2: Create User Successfully${NC}"
echo "----------------------------------------"
RANDOM_NUM=$RANDOM
USER_RESPONSE=$(curl -s -X POST $BASE_URL \
  -H "Content-Type: application/json" \
  -d "{
    \"username\": \"testuser_$RANDOM_NUM\",
    \"email\": \"test_$RANDOM_NUM@example.com\",
    \"password\": \"password123\",
    \"firstName\": \"Test\",
    \"lastName\": \"User\"
  }")

echo "$USER_RESPONSE" | jq
USER_ID=$(echo "$USER_RESPONSE" | jq -r '.id')
USERNAME=$(echo "$USER_RESPONSE" | jq -r '.username')
EMAIL=$(echo "$USER_RESPONSE" | jq -r '.email')
echo -e "${GREEN}✓ User created: $USER_ID${NC}"
echo ""

echo -e "${RED}Test 3: User Already Exists - Duplicate Username (409)${NC}"
echo "----------------------------------------"
echo "POST /api/users (with existing username)"
curl -s -X POST $BASE_URL \
  -H "Content-Type: application/json" \
  -d "{
    \"username\": \"$USERNAME\",
    \"email\": \"different_$RANDOM_NUM@example.com\",
    \"password\": \"password123\",
    \"firstName\": \"Test\",
    \"lastName\": \"User\"
  }" | jq

echo ""
echo -e "${YELLOW}Expected: 409 CONFLICT with ProblemDetail${NC}"
echo -e "${YELLOW}Error Code: USER_ALREADY_EXISTS${NC}"
echo ""

echo -e "${RED}Test 4: User Already Exists - Duplicate Email (409)${NC}"
echo "----------------------------------------"
echo "POST /api/users (with existing email)"
curl -s -X POST $BASE_URL \
  -H "Content-Type: application/json" \
  -d "{
    \"username\": \"different_user_$RANDOM_NUM\",
    \"email\": \"$EMAIL\",
    \"password\": \"password123\",
    \"firstName\": \"Test\",
    \"lastName\": \"User\"
  }" | jq

echo ""
echo -e "${YELLOW}Expected: 409 CONFLICT with ProblemDetail${NC}"
echo -e "${YELLOW}Error Code: USER_ALREADY_EXISTS${NC}"
echo ""

echo -e "${RED}Test 5: Invalid Status Transition (400)${NC}"
echo "----------------------------------------"
echo "Step 1: Soft delete the user"
curl -s -X DELETE $BASE_URL/$USER_ID
echo -e "${YELLOW}User deleted (status → DELETED)${NC}"
echo ""
echo "Step 2: Try to reactivate deleted user"
curl -s -X PATCH $BASE_URL/$USER_ID/reactivate | jq
echo ""
echo -e "${YELLOW}Expected: 400 BAD REQUEST with ProblemDetail${NC}"
echo -e "${YELLOW}Error Code: INVALID_STATUS_TRANSITION${NC}"
echo ""

echo -e "${BLUE}Test 6: User Not Found by Username (404)${NC}"
echo "----------------------------------------"
echo "GET /api/users/username/nonexistent_user"
curl -s -X GET $BASE_URL/username/nonexistent_user_12345 | jq
echo ""
echo -e "${YELLOW}Expected: 404 NOT FOUND with ProblemDetail${NC}"
echo -e "${YELLOW}Error Code: USER_NOT_FOUND${NC}"
echo ""

echo ""
echo "=============================================="
echo "ProblemDetail Response Format (RFC 7807)"
echo "=============================================="
echo ""
echo "All error responses follow this structure:"
echo ""
cat << 'EOF'
{
  "type": "https://api.bvs.com/errors/{error-type}",
  "title": "Human-Readable Title",
  "status": 400,
  "detail": "Detailed error description",
  "errorCode": "MACHINE_READABLE_CODE",
  "timestamp": "2025-10-03T14:30:25Z"
}
EOF
echo ""
echo ""
echo "=============================================="
echo "Exception to HTTP Status Mapping"
echo "=============================================="
echo ""
echo "  UserNotFoundException           → 404 NOT FOUND"
echo "  UserAlreadyExistsException      → 409 CONFLICT"
echo "  InvalidPasswordException        → 401 UNAUTHORIZED"
echo "  InvalidStatusTransitionException → 400 BAD REQUEST"
echo "  UserServiceException            → 400 BAD REQUEST"
echo "  MethodArgumentNotValidException → 400 BAD REQUEST"
echo "  Exception (catch-all)           → 500 INTERNAL SERVER ERROR"
echo ""
echo "=============================================="
echo "Error Codes"
echo "=============================================="
echo ""
echo "  USER_NOT_FOUND              - User doesn't exist"
echo "  USER_ALREADY_EXISTS         - Duplicate username/email"
echo "  INVALID_PASSWORD            - Password validation failed"
echo "  INVALID_STATUS_TRANSITION   - Invalid state change"
echo "  USER_SERVICE_ERROR          - Generic user service error"
echo "  VALIDATION_ERROR            - Bean validation failed"
echo "  INTERNAL_ERROR              - Unexpected server error"
echo ""
echo "=============================================="
echo "Benefits of ProblemDetail (RFC 7807)"
echo "=============================================="
echo ""
echo "  ✓ Standardized error format"
echo "  ✓ Machine-readable error codes"
echo "  ✓ Human-friendly messages"
echo "  ✓ Type URIs for documentation"
echo "  ✓ Proper HTTP status codes"
echo "  ✓ Extensible with custom properties"
echo "  ✓ Spring Framework 6+ native support"
echo ""
echo "=============================================="
