# 🚀 Quick Reference Guide - Spring Security Implementation

## Endpoints Summary

| Endpoint | Method | Auth | Purpose | Role |
|----------|--------|------|---------|------|
| `/register` | POST | ❌ | Create new user | None |
| `/login` | POST | ❌ | Get JWT token | None |
| `/protected-test` | GET | ✅ | Test authentication | Any |
| `/admin-data` | GET | ✅ | Admin-only data | ADMIN |
| `/swagger-ui.html` | GET | ❌ | API documentation | None |
| `/admin/update-role` | POST | ❌ | Update user role (testing only) | None |

---

## Testing Commands

### 1. Register User
```bash
curl -X POST http://localhost:8080/register \
  -H "Content-Type: application/json" \
  -d '{"username":"john_doe","password":"secret123"}'
```

### 2. Login
```bash
curl -X POST http://localhost:8080/login \
  -H "Content-Type: application/json" \
  -d '{"username":"john_doe","password":"secret123"}'
```
Save the token from response.

### 3. Use Protected Endpoint
```bash
TOKEN="eyJhbGciOiJIUzI1NiJ9..."
curl -X GET http://localhost:8080/protected-test \
  -H "Authorization: Bearer $TOKEN"
```

### 4. Create Admin User
```bash
# First update role
curl -X POST "http://localhost:8080/admin/update-role?username=admin_user&role=ADMIN"

# Then login to get new token with ADMIN role
curl -X POST http://localhost:8080/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin_user","password":"admin123"}'
```

### 5. Access Admin Endpoint
```bash
ADMIN_TOKEN="eyJhbGciOiJIUzI1NiJ9..."
curl -X GET http://localhost:8080/admin-data \
  -H "Authorization: Bearer $ADMIN_TOKEN"
```

---

## HTTP Status Codes

| Code | Meaning | When |
|------|---------|------|
| 200 | OK | Successful GET request |
| 201 | Created | Successful POST (registration) |
| 400 | Bad Request | Validation failed |
| 401 | Unauthorized | Invalid credentials or missing token |
| 403 | Forbidden | Valid token but insufficient permissions |
| 409 | Conflict | Username already exists |
| 500 | Server Error | Unexpected error |

---

## Security Classes & Their Roles

```
JwtUtil.java
├─ generateToken() → Creates JWT
├─ validateToken() → Verifies JWT
├─ extractUsername() → Gets username from JWT
└─ extractRoles() → Gets roles from JWT

JwtAuthenticationFilter.java
├─ Extract token from header
├─ Validate token
├─ Store user in SecurityContext
└─ Allow request to proceed

CustomUserDetailsService.java
├─ Load user from database
├─ Convert to UserDetails
└─ Return with authorities

SecurityConfig.java
├─ Define public/protected endpoints
├─ Configure password encoder
├─ Setup authentication provider
└─ Create filter chain

AuthServiceImpl.java
├─ Authenticate login request
├─ Generate JWT token
├─ Register new user
└─ Hash password
```

---

## Error Response Format

All errors return:
```json
{
  "statusCode": 401,
  "error": "UNAUTHORIZED",
  "message": "Invalid username or password",
  "errorCode": "AUTH_001",
  "timestamp": "2026-04-18 10:30:45"
}
```

---

## Database Schema

```sql
CREATE TABLE users (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  username VARCHAR(255) UNIQUE NOT NULL,
  password VARCHAR(255) NOT NULL,  -- Bcrypt hashed
  role VARCHAR(50) NOT NULL,       -- USER, ADMIN, etc.
  enabled BOOLEAN DEFAULT TRUE
);
```

---

## Token Structure

```
Authorization: Bearer <TOKEN>

TOKEN = HEADER.PAYLOAD.SIGNATURE

PAYLOAD:
{
  "sub": "username",
  "roles": ["ROLE_ADMIN"],
  "iat": 1234567890,
  "exp": 1234654290
}
```

---

## Common Error Codes

| Code | HTTP | Meaning | Solution |
|------|------|---------|----------|
| USER_001 | 409 | Username exists | Use different username |
| AUTH_001 | 401 | Invalid credentials | Check username/password |
| AUTH_002 | 403 | Insufficient permissions | Need ADMIN role |
| AUTH_003 | 401 | User not found | Register first |
| SYS_001 | 500 | Server error | Check logs |

---

## Environment Variables (Recommended for Production)

```bash
export JWT_SECRET="your-256-char-secret-key-here"
export JWT_EXPIRATION="86400000"
export DB_URL="jdbc:mysql://localhost:3306/fly_db"
export DB_USER="fly_user"
export DB_PASSWORD="your-password"
```

Then use in application.yml:
```yaml
spring:
  jwt:
    secret: ${JWT_SECRET}
    expiration: ${JWT_EXPIRATION}
  datasource:
    url: ${DB_URL}
    username: ${DB_USER}
    password: ${DB_PASSWORD}
```

---

## Debug Tips

### 1. Check if JWT is valid
Go to https://jwt.io and paste your token

### 2. Enable SQL logging
In application.yml:
```yaml
spring:
  jpa:
    show-sql: true
```

### 3. Enable security debug
In application.yml:
```yaml
logging:
  level:
    org.springframework.security: DEBUG
```

### 4. Check database
```sql
SELECT username, role, enabled FROM users;
```

### 5. Test without JWT (should fail)
```bash
curl -X GET http://localhost:8080/protected-test
# Should return 403 Forbidden
```

---

## Performance Considerations

- **JWT is stateless**: Good for scaling
- **BCrypt is slow**: By design (prevents brute force)
- **No database query on every request**: Token validates offline
- **Enable query caching**: Use @Cacheable for user lookups

---

## Security Checklist

- ✅ Passwords are hashed (BCrypt)
- ✅ Tokens are signed (HMAC-SHA256)
- ✅ Tokens are verified before use
- ✅ Tokens expire after 24 hours
- ✅ Role-based access control
- ✅ Input validation (@Valid)
- ✅ Error messages don't leak info
- ✅ Stateless architecture
- ✅ No hardcoded secrets

---

## Migration to Production

1. **Move secrets to vault**
   - Use AWS Secrets Manager, HashiCorp Vault, etc.
   - Don't store in application.yml

2. **Enable HTTPS**
   - Configure SSL/TLS certificates
   - Use environment-based configuration

3. **Add audit logging**
   - Log all login attempts
   - Log all failed authentications
   - Log role changes

4. **Implement token refresh**
   - Add refresh token mechanism
   - Auto-refresh before expiration

5. **Rate limiting**
   - Limit login attempts
   - Prevent brute force attacks

6. **Database backups**
   - Regular backups
   - Test restore procedures

7. **Monitoring**
   - Alert on failed authentications
   - Monitor token generation rate
   - Alert on errors

---

## File Structure

```
crypto/
├── src/main/java/com/flyingbird/crypto/
│   ├── controller/
│   │   ├── AuthController.java
│   │   └── AdminUtilityController.java
│   ├── service/
│   │   ├── AuthService.java
│   │   └── AuthServiceImpl.java
│   ├── repository/
│   │   └── UserRepository.java
│   ├── entity/
│   │   └── User.java
│   ├── dto/
│   │   ├── LoginRequestDto.java
│   │   ├── RegisterRequestDto.java
│   │   ├── AuthResponseDto.java
│   │   └── ErrorResponse.java
│   ├── exception/
│   │   ├── UserAlreadyExistsException.java
│   │   ├── InvalidCredentialsException.java
│   │   ├── ForbiddenAccessException.java
│   │   └── GlobalExceptionHandler.java
│   ├── config/
│   │   ├── SecurityConfig.java
│   │   ├── JwtAuthenticationFilter.java
│   │   └── CustomUserDetailsService.java
│   ├── util/
│   │   └── JwtUtil.java
│   └── CryptoApplication.java
└── resources/
    └── application.yml
```

---

## Glossary

- **JWT**: JSON Web Token - stateless token format
- **BCrypt**: Password hashing algorithm
- **HMAC**: Keyed-hash message authentication code
- **RBAC**: Role-based access control
- **Authentication**: Verifying who you are
- **Authorization**: Verifying what you can do
- **Filter**: Intercepts requests before reaching controller
- **Claims**: Data stored in JWT payload
- **Signature**: Cryptographic proof token isn't tampered

---

**Quick Start: Register → Login → Use Token → Access Protected Resources**

