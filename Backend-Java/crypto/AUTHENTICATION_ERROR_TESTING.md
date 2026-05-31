# 🧪 Authentication Error Response Testing - Quick Reference

## ✅ Verification Tests (Copy-Paste Ready)

### Test 1: Protected Endpoint WITHOUT JWT Token
```bash
curl -X GET http://localhost:8080/protected-test \
  -w "\nHTTP Status: %{http_code}\n"
```

**Expected Response (401 Unauthorized):**
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

### Test 2: Protected Endpoint WITH Invalid Token
```bash
curl -X GET http://localhost:8080/protected-test \
  -H "Authorization: Bearer invalid_token_xyz" \
  -w "\nHTTP Status: %{http_code}\n"
```

**Expected Response (401 Unauthorized):**
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

### Test 3: Protected Endpoint WITH Valid Token (Success)
```bash
# Step 1: Register user
curl -X POST http://localhost:8080/register \
  -H "Content-Type: application/json" \
  -d '{"username":"testuser","password":"testpass123"}'

# Step 2: Login to get token
TOKEN=$(curl -s -X POST http://localhost:8080/login \
  -H "Content-Type: application/json" \
  -d '{"username":"testuser","password":"testpass123"}' | jq -r '.token')

# Step 3: Access protected endpoint with valid token
curl -X GET http://localhost:8080/protected-test \
  -H "Authorization: Bearer $TOKEN" \
  -w "\nHTTP Status: %{http_code}\n"
```

**Expected Response (200 OK):**
```json
{
  "message": "You have access to protected resource",
  "username": "testuser"
}
HTTP Status: 200
```

---

## 📊 Comparison: All Authentication Scenarios

| Scenario | Endpoint | Auth Header | Status | Error Code | Message |
|----------|----------|-------------|--------|-----------|---------|
| No token | /protected-test | None | 401 | AUTH_003 | Missing or invalid JWT token |
| Invalid token | /protected-test | Bearer invalid | 401 | AUTH_003 | Missing or invalid JWT token |
| Valid token | /protected-test | Bearer valid_jwt | 200 | - | Success response |
| Wrong credentials | /login | - | 401 | AUTH_001 | Invalid username or password |
| No permission (user) | /admin-data | Bearer user_jwt | 403 | AUTH_002 | Access Denied - Insufficient permissions |
| Correct permission (admin) | /admin-data | Bearer admin_jwt | 200 | - | Admin data response |

---

## 🔑 Key Points

✅ **All authentication failures return JSON responses** - No more blank responses
✅ **401 Unauthorized status** - For missing/invalid JWT
✅ **ERROR CODE AUTH_003** - Specifically identifies authentication token issues
✅ **Consistent format** - Matches all other application error responses
✅ **Helpful messages** - Clients know exactly what's wrong
✅ **Timestamps** - For debugging and audit trails

---

## 🚀 Full Workflow Test

```bash
#!/bin/bash

echo "=== FULL AUTHENTICATION WORKFLOW TEST ==="

# 1. Register
echo -e "\n1. Register new user:"
REG=$(curl -s -X POST http://localhost:8080/register \
  -H "Content-Type: application/json" \
  -d '{"username":"workflow_user","password":"workflow_pass"}')
echo "$REG" | jq '.'

# 2. Login
echo -e "\n2. Login and get token:"
LOGIN=$(curl -s -X POST http://localhost:8080/login \
  -H "Content-Type: application/json" \
  -d '{"username":"workflow_user","password":"workflow_pass"}')
echo "$LOGIN" | jq '.'
TOKEN=$(echo "$LOGIN" | jq -r '.token')

# 3. Test protected - with valid token (should succeed)
echo -e "\n3. Protected endpoint WITH valid token (200 OK):"
curl -s -X GET http://localhost:8080/protected-test \
  -H "Authorization: Bearer $TOKEN" | jq '.'

# 4. Test protected - without token (should fail)
echo -e "\n4. Protected endpoint WITHOUT token (401):"
curl -s -X GET http://localhost:8080/protected-test | jq '.'

# 5. Test protected - with invalid token (should fail)
echo -e "\n5. Protected endpoint WITH invalid token (401):"
curl -s -X GET http://localhost:8080/protected-test \
  -H "Authorization: Bearer invalid_xyz" | jq '.'

# 6. Test admin - as regular user (should fail)
echo -e "\n6. Admin endpoint as regular user (403):"
curl -s -X GET http://localhost:8080/admin-data \
  -H "Authorization: Bearer $TOKEN" | jq '.'

echo -e "\n=== WORKFLOW TEST COMPLETE ==="
```

---

## 💾 Installation Instructions

If you want to recreate this functionality:

1. **Created File**: `CustomAuthenticationEntryPoint.java`
   - Location: `src/main/java/com/flyingbird/crypto/config/`
   - Implements Spring Security's AuthenticationEntryPoint
   - Returns JSON error responses for auth failures

2. **Updated File**: `SecurityConfig.java`
   - Added CustomAuthenticationEntryPoint injection
   - Registered in security exception handling

3. **Effect**: All authentication errors now return proper JSON responses

---

## 🎯 What Changed

### Before
```
GET /protected-test (no token)
→ Response: Blank page or HTML error
→ Status: 403 Forbidden (incorrect)
```

### After
```
GET /protected-test (no token)
→ Response: JSON error
→ Status: 401 Unauthorized (correct)
→ Error Code: AUTH_003
→ Message: "Missing or invalid JWT token"
```

---

## ✨ Benefits

✓ **Consistent** - All errors follow same format
✓ **Clear** - Clients know what went wrong
✓ **Professional** - Proper HTTP status codes
✓ **Debuggable** - Error codes and timestamps
✓ **API-Friendly** - Easy to parse and handle

---

**Status**: ✅ PRODUCTION READY
**Last Updated**: April 18, 2026

