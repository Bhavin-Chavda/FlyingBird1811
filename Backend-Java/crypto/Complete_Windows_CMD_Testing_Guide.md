# 🧪 Complete cURL Testing Guide - Spring Security API

## 📌 Prerequisites

- Application running on `http://localhost:8080`
- `curl` command-line tool installed
- (Optional) `jq` for JSON formatting

---

## 🚀 Quick Start Tests

### Test 1: Register a New User

```cmd
curl -X POST http://localhost:8080/api/auth/register ^
  -H "Content-Type: application/json" ^
  -d '{"username":"bhavin","password":"bhavin"}' ^
  -w "\nHTTP Status: %{http_code}\n"
```

**Expected Response (201 Created):**
```json
{
  "token": null,
  "username": "john_doe",
  "role": "USER",
  "message": "Registration successful"
}
```

---

### Test 2: Login User and Get JWT Token

```cmd
curl -X POST http://localhost:8080/api/auth/login ^
  -H "Content-Type: application/json" ^
  -d '{"username":"bhavin","password":"bhavin"}'
```

**Expected Response (200 OK):**
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9.eyJyb2xlcyI6WyJVU0VSIl0sInN1YiI6ImpvaG5fZG9lIiwiaWF0IjoxNzc2NDk2NzU3LCJleHAiOjE3NzY1ODMxNTd9.xyz...",
  "username": "john_doe",
  "role": "USER",
  "message": "Login successful"
}
```

**Save the token for further use:**
```cmd
set TOKEN=eyJhbGciOiJIUzI1NiJ9.eyJyb2xlcyI6WyJVU0VSIl0sInN1YiI6ImpvaG5fZG9lIiwiaWF0IjoxNzc2NTA2ODMxLCJleHAiOjE3NzY1OTMyMzF9.BQLImv7AFxkL-fodpTEsGzTyuFJAejQq5EcfYU8IX38
```

---

### Test 3: Access Protected Endpoint with Valid JWT

```cmd
curl -X GET http://localhost:8080/protected-test ^
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9.eyJyb2xlcyI6WyJVU0VSIl0sInN1YiI6ImpvaG5fZG9lIiwiaWF0IjoxNzc2NTA2ODMxLCJleHAiOjE3NzY1OTMyMzF9.BQLImv7AFxkL-fodpTEsGzTyuFJAejQq5EcfYU8IX38" ^
  -w "\nHTTP Status: %{http_code}\n"
```

**Expected Response (200 OK):**
```json
{
  "message": "You have access to protected resource",
  "username": "john_doe"
}
```

---

### Test 4: Try Protected Endpoint WITHOUT Token (Should Fail)

```cmd
curl -X GET http://localhost:8080/protected-test ^
  -w "\nHTTP Status: %{http_code}\n"
```

**Expected Response (401 Unauthorized with Proper JSON Error):**
```json
{
  "statusCode": 401,
  "error": "UNAUTHORIZED",
  "message": "Missing or invalid JWT token",
  "timestamp": "2026-04-18 14:21:41",
  "errorCode": "AUTH_003"
}
HTTP Status: 401
```

---

## 👨‍💼 Admin Endpoint Tests

### Test 5: Try Admin Endpoint as Regular User (Should Fail)

```cmd
curl -X GET http://localhost:8080/admin-data ^
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9.eyJyb2xlcyI6WyJVU0VSIl0sInN1YiI6ImpvaG5fZG9lIiwiaWF0IjoxNzc2NTA2ODMxLCJleHAiOjE3NzY1OTMyMzF9.BQLImv7AFxkL-fodpTEsGzTyuFJAejQq5EcfYU8IX38" ^
  -w "\nHTTP Status: %{http_code}\n"
```

**Expected Response (403 Forbidden):**
```json
{
  "statusCode": 403,
  "error": "FORBIDDEN",
  "message": "Access Denied - Insufficient permissions",
  "timestamp": "2026-04-18 13:30:45",
  "errorCode": "AUTH_002"
}
```

---

### Test 6: Convert User to Admin (Utility Endpoint)

```cmd
curl -X POST "http://localhost:8080/admin/update-role?username=john_doe&role=ADMIN" ^
  -w "\nHTTP Status: %{http_code}\n"
```

**Expected Response (200 OK):**
```json
{
  "message": "User role updated",
  "username": "john_doe",
  "role": "ADMIN"
}
```

---

### Test 7: Login Again to Get New Token with ADMIN Role

```cmd
curl -X POST http://localhost:8080/api/auth/login ^
  -H "Content-Type: application/json" ^
  -d '{"username":"john_doe","password":"secret123"}'
```

**Save this new ADMIN token:**
```cmd
set ADMIN_TOKEN=your_admin_jwt_token_here
```

---

### Test 8: Access Admin Endpoint as Admin User (Should Succeed)

```cmd
curl -X GET http://localhost:8080/admin-data ^
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9.eyJyb2xlcyI6WyJBRE1JTiJdLCJzdWIiOiJqb2huX2RvZSIsImlhdCI6MTc3NjUwNjk5MywiZXhwIjoxNzc2NTkzMzkzfQ.-C67_sXO3M2nZHe8N3Vx4FLtqmK83cB1qYiGC0tB0Yw" ^
  -w "\nHTTP Status: %{http_code}\n"
```

**Expected Response (200 OK):**
```json
{
  "message": "Admin data - Sensitive information here",
  "username": "john_doe",
  "role": "ADMIN"
}
```

---

## ❌ Error Scenario Tests

### Test 9: Register with Duplicate Username (Should Fail)

```cmd
curl -X POST http://localhost:8080/api/auth/register ^
  -H "Content-Type: application/json" ^
  -d '{"username":"john_doe","password":"another_password"}' ^
  -w "\nHTTP Status: %{http_code}\n"
```

**Expected Response (409 Conflict):**
```json
{
  "statusCode": 409,
  "error": "CONFLICT",
  "message": "Username 'john_doe' already exists",
  "timestamp": "2026-04-18 13:30:45",
  "errorCode": "USER_001"
}
```

---

### Test 10: Login with Wrong Password (Should Fail)

```cmd
curl -X POST http://localhost:8080/api/auth/login ^
  -H "Content-Type: application/json" ^
  -d '{"username":"john_doe","password":"wrong_password"}' ^
  -w "\nHTTP Status: %{http_code}\n"
```

**Expected Response (401 Unauthorized):**
```json
{
  "statusCode": 401,
  "error": "UNAUTHORIZED",
  "message": "Invalid username or password",
  "timestamp": "2026-04-18 13:30:45",
  "errorCode": "AUTH_001"
}
```

---

### Test 11: Login with Non-Existent User (Should Fail)

```cmd
curl -X POST http://localhost:8080/api/auth/login ^
  -H "Content-Type: application/json" ^
  -d '{"username":"nonexistent_user","password":"password"}' ^
  -w "\nHTTP Status: %{http_code}\n"
```

**Expected Response (401 Unauthorized):**
```json
{
  "statusCode": 401,
  "error": "UNAUTHORIZED",
  "message": "Invalid username or password",
  "timestamp": "2026-04-18 13:30:45",
  "errorCode": "AUTH_001"
}
```

---

### Test 12: Register with Empty Username (Validation Error)

```cmd
curl -X POST http://localhost:8080/api/auth/register ^
  -H "Content-Type: application/json" ^
  -d '{"username":"","password":"password123"}' ^
  -w "\nHTTP Status: %{http_code}\n"
```

**Expected Response (400 Bad Request):**
```json
{
  "statusCode": 400,
  "error": "BAD_REQUEST",
  "message": "Validation failed: username must not be blank",
  "timestamp": "2026-04-18 13:30:45",
  "errorCode": "VAL_001"
}
```

---

### Test 13: Login with Empty Password (Validation Error)

```cmd
curl -X POST http://localhost:8080/api/auth/login ^
  -H "Content-Type: application/json" ^
  -d '{"username":"john_doe","password":""}' ^
  -w "\nHTTP Status: %{http_code}\n"
```

**Expected Response (400 Bad Request):**
```json
{
  "statusCode": 400,
  "error": "BAD_REQUEST",
  "message": "Validation failed: password must not be blank",
  "timestamp": "2026-04-18 13:30:45",
  "errorCode": "VAL_001"
}
```

---

## 📋 Advanced Testing Scenarios

### Scenario 1: Complete User Journey

```cmd
# Step 1: Register new user
echo "=== Step 1: Register ==="
curl -X POST http://localhost:8080/api/auth/register ^
  -H "Content-Type: application/json" ^
  -d '{"username":"testuser","password":"test123"}' | jq .

# Step 2: Login
echo -e "\n=== Step 2: Login ==="
LOGIN_RESPONSE=$(curl -s -X POST http://localhost:8080/api/auth/login ^
  -H "Content-Type: application/json" ^
  -d '{"username":"testuser","password":"test123"}')
echo "$LOGIN_RESPONSE" | jq .
TOKEN=$(echo "$LOGIN_RESPONSE" | jq -r '.token')

# Step 3: Access protected resource
echo -e "\n=== Step 3: Protected Resource ==="
curl -s -X GET http://localhost:8080/protected-test ^
  -H "Authorization: Bearer $TOKEN" | jq .

# Step 4: Try admin endpoint (should fail)
echo -e "\n=== Step 4: Admin Resource (Should Fail) ==="
curl -s -X GET http://localhost:8080/admin-data ^
  -H "Authorization: Bearer $TOKEN" | jq .
```

---

### Scenario 2: Admin Role Testing

```cmd
# Step 1: Update user to admin
echo "=== Step 1: Update to Admin ==="
curl -s -X POST "http://localhost:8080/admin/update-role?username=testuser&role=ADMIN" | jq .

# Step 2: Login again to get new token with ADMIN role
echo -e "\n=== Step 2: Login (Get New Token) ==="
ADMIN_LOGIN=$(curl -s -X POST http://localhost:8080/api/auth/login ^
  -H "Content-Type: application/json" ^
  -d '{"username":"testuser","password":"test123"}')
echo "$ADMIN_LOGIN" | jq .
ADMIN_TOKEN=$(echo "$ADMIN_LOGIN" | jq -r '.token')

# Step 3: Access admin endpoint (should succeed)
echo -e "\n=== Step 3: Admin Resource (Should Succeed) ==="
curl -s -X GET http://localhost:8080/admin-data ^
  -H "Authorization: Bearer $ADMIN_TOKEN" | jq .
```

---

## 🔧 Helper Scripts

### Save Token to Variable

```cmd
# Get token and save to variable
TOKEN=$(curl -s -X POST http://localhost:8080/api/auth/login ^
  -H "Content-Type: application/json" ^
  -d '{"username":"john_doe","password":"secret123"}' | jq -r '.token')

# Verify token is saved
echo "Token: $TOKEN"
```

---

### Test All Endpoints in Sequence

```cmd
#!/bin/bash

echo "========================================="
echo "Testing All Endpoints"
echo "========================================="

# Register
echo -e "\n1. REGISTER"
REGISTER=$(curl -s -X POST http://localhost:8080/api/auth/register ^
  -H "Content-Type: application/json" ^
  -d '{"username":"fulltest","password":"test123"}')
echo "$REGISTER" | jq .

# Login
echo -e "\n2. LOGIN"
LOGIN=$(curl -s -X POST http://localhost:8080/api/auth/login ^
  -H "Content-Type: application/json" ^
  -d '{"username":"fulltest","password":"test123"}')
echo "$LOGIN" | jq .
TOKEN=$(echo "$LOGIN" | jq -r '.token')

# Protected endpoint
echo -e "\n3. PROTECTED ENDPOINT"
curl -s -X GET http://localhost:8080/protected-test ^
  -H "Authorization: Bearer $TOKEN" | jq .

# Admin endpoint (should fail)
echo -e "\n4. ADMIN ENDPOINT (should fail)"
curl -s -X GET http://localhost:8080/admin-data ^
  -H "Authorization: Bearer $TOKEN" | jq .

echo -e "\n========================================="
echo "Testing Complete"
echo "========================================="
```

---

### Pretty Print JSON Responses

```cmd
# Without jq (basic formatting)
curl -s -X POST http://localhost:8080/api/auth/login ^
  -H "Content-Type: application/json" ^
  -d '{"username":"john_doe","password":"secret123"}'

# With jq (pretty formatting)
curl -s -X POST http://localhost:8080/api/auth/login ^
  -H "Content-Type: application/json" ^
  -d '{"username":"john_doe","password":"secret123"}' | jq .

# With jq (colorized)
curl -s -X POST http://localhost:8080/api/auth/login ^
  -H "Content-Type: application/json" ^
  -d '{"username":"john_doe","password":"secret123"}' | jq -C .
```

---

### Get Full Response with Headers

```cmd
curl -i -X POST http://localhost:8080/api/auth/login ^
  -H "Content-Type: application/json" ^
  -d '{"username":"john_doe","password":"secret123"}'
```

---

### Measure Response Time

```cmd
curl -w "\nTime: %{time_total}s\nStatus: %{http_code}\n" ^
  -X GET http://localhost:8080/protected-test ^
  -H "Authorization: Bearer YOUR_TOKEN"
```

---

## 📊 Test Summary Table

| # | Test | Method | Endpoint | Auth Required | Expected Status |
|---|------|--------|----------|--|---|
| 1 | Register User | POST | `/register` | ❌ | 201 |
| 2 | Login | POST | `/login` | ❌ | 200 |
| 3 | Protected (Valid JWT) | GET | `/protected-test` | ✅ | 200 |
| 4 | Protected (No JWT) | GET | `/protected-test` | ✅ | 403 |
| 5 | Admin (User Role) | GET | `/admin-data` | ✅ | 403 |
| 6 | Update Role | POST | `/admin/update-role` | ❌ | 200 |
| 7 | Login (New Token) | POST | `/login` | ❌ | 200 |
| 8 | Admin (Admin Role) | GET | `/admin-data` | ✅ | 200 |
| 9 | Duplicate Register | POST | `/register` | ❌ | 409 |
| 10 | Wrong Password | POST | `/login` | ❌ | 401 |
| 11 | Non-existent User | POST | `/login` | ❌ | 401 |
| 12 | Empty Username | POST | `/register` | ❌ | 400 |
| 13 | Empty Password | POST | `/login` | ❌ | 400 |

---

## 🎯 Testing Checklist

- [ ] Test 1: Register new user
- [ ] Test 2: Login and get JWT
- [ ] Test 3: Access protected endpoint with JWT
- [ ] Test 4: Access protected endpoint without JWT
- [ ] Test 5: Access admin endpoint as regular user
- [ ] Test 6: Update user to admin
- [ ] Test 7: Login again to get new token
- [ ] Test 8: Access admin endpoint as admin user
- [ ] Test 9: Register with duplicate username
- [ ] Test 10: Login with wrong password
- [ ] Test 11: Login with non-existent user
- [ ] Test 12: Register with empty username
- [ ] Test 13: Login with empty password
- [ ] Scenario 1: Complete user journey
- [ ] Scenario 2: Admin role testing

---

## 💡 Tips & Tricks

### Use Environment Variables for Multiple Tests

```cmd
# Set base URL
BASE_URL="http://localhost:8080"

# Use in requests
curl -X POST "$BASE_URL/register" ^
  -H "Content-Type: application/json" ^
  -d '{"username":"user1","password":"pass123"}'
```

---

### Store Response Parts

```cmd
# Extract just the token
TOKEN=$(curl -s -X POST http://localhost:8080/api/auth/login ^
  -H "Content-Type: application/json" ^
  -d '{"username":"john_doe","password":"secret123"}' | jq -r '.token')

# Use token in next request
curl -X GET http://localhost:8080/protected-test ^
  -H "Authorization: Bearer $TOKEN"
```

---

### Test with Different Users

```cmd
# Create multiple users
for i in {1..5}; do
  curl -s -X POST http://localhost:8080/api/auth/register ^
    -H "Content-Type: application/json" ^
    -d "{\"username\":\"user$i\",\"password\":\"pass$i\"}" | jq .
done
```

---

### Loop Through Tests

```cmd
# Test multiple endpoints
for endpoint in protected-test admin-data; do
  echo "Testing: $endpoint"
  curl -s -X GET "http://localhost:8080/$endpoint" ^
    -H "Authorization: Bearer $TOKEN" | jq .
done
```

---

## 🚀 Quick Copy-Paste Commands

### Register Multiple Test Users

```cmd
curl -X POST http://localhost:8080/api/auth/register -H "Content-Type: application/json" -d '{"username":"user1","password":"pass1"}' && ^
curl -X POST http://localhost:8080/api/auth/register -H "Content-Type: application/json" -d '{"username":"user2","password":"pass2"}' && ^
curl -X POST http://localhost:8080/api/auth/register -H "Content-Type: application/json" -d '{"username":"user3","password":"pass3"}'
```

---

## 📝 Notes

- Replace `YOUR_JWT_TOKEN_HERE` with actual JWT token from login response
- All timestamps are in format: `YYYY-MM-DD HH:mm:ss`
- Error codes help identify the specific error type
- Check application logs for detailed error information
- Tokens expire after 24 hours

---

**Last Updated:** April 18, 2026  
**Status:** ✅ Complete & Ready for Testing

