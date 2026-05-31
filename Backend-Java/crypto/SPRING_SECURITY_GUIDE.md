# 🔐 Spring Security Implementation Guide - Complete Flow Explanation

## 📋 Overview
This document explains how Spring Security is implemented in this crypto microservice. The implementation follows industry best practices and enterprise patterns.

---

## 🏗️ Architecture & Components

### 1. **Layered Architecture**
```
User Request
    ↓
Controller (API Layer) - AuthController.java
    ↓
Service (Business Logic) - AuthService/AuthServiceImpl.java
    ↓
Repository (Data Access) - UserRepository.java
    ↓
Database (MySQL)
```

Each layer has a single responsibility:
- **Controller**: HTTP request/response handling
- **Service**: Business logic and security verification
- **Repository**: Database queries
- **Entity**: Data model

---

## 🔑 Key Components Explained

### **1. User Entity** (`User.java`)
```
@Entity
public class User {
    - id: Unique identifier
    - username: Login name (unique)
    - password: Hashed password (NEVER plain text)
    - role: User permission level (e.g., "ADMIN", "USER")
    - enabled: Account status
}
```

### **2. DTOs (Data Transfer Objects)** - Used for communication
- **LoginRequestDto**: Client sends username + password
- **RegisterRequestDto**: Client sends username + password
- **AuthResponseDto**: Server returns JWT token
- **ErrorResponse**: Standardized error responses

### **3. JWT Token Utility** (`JwtUtil.java`)
```
JWT Token Structure: HEADER.PAYLOAD.SIGNATURE

Example: eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJqb2huIiwicm9sZXMiOlsiQURNSU4iXX0.xyz

HEADER: Algorithm (HS256)
PAYLOAD: User info (username, roles) + timestamp
SIGNATURE: Cryptographic signature (prevents tampering)
```

Key methods:
- `generateToken()`: Create JWT after login
- `validateToken()`: Verify JWT is not expired/tampered
- `extractUsername()`: Extract username from token
- `extractRoles()`: Extract user roles from token

### **4. Security Configuration** (`SecurityConfig.java`)
Configures Spring Security globally:
- CSRF disabled (not needed for stateless API)
- Stateless session management (each request is independent)
- Public vs protected endpoints
- Password encoding (BCrypt)
- Authentication provider

### **5. JWT Authentication Filter** (`JwtAuthenticationFilter.java`)
Runs for EVERY request:
1. Extract JWT from Authorization header
2. Validate JWT signature and expiration
3. Extract username and roles
4. Store user in SecurityContext
5. Allow request to proceed

### **6. Custom UserDetailsService** (`CustomUserDetailsService.java`)
Loads user from database:
1. Called during login to verify credentials
2. Called by JWT filter to load user details
3. Returns Spring's UserDetails object

### **7. Global Exception Handler** (`GlobalExceptionHandler.java`)
Centralizes error handling:
- Catches all exceptions
- Returns structured error responses
- Never exposes internal errors to client

---

## 🔄 Complete Security Flow

### **Scenario 1: User Registration**

```
1. Client sends POST /register
   {
     "username": "john_doe",
     "password": "secret123"
   }

2. AuthController.register() receives request
   ↓
3. @Valid annotation validates input
   - @NotBlank ensures username and password are not blank
   ↓
4. AuthServiceImpl.register() executes:
   a) Check if username already exists
      - SELECT * FROM users WHERE username = 'john_doe'
      - If exists → throw UserAlreadyExistsException (409 Conflict)
   
   b) Hash password using BCryptPasswordEncoder
      - Plain: "secret123"
      - Hashed: "$2a$10$EixZaYVK1fsbw1ZfbX3OXePaWxn96p36WQoeG6Lruj3njPHga/iguEn"
      - Each hash is unique due to random salt
   
   c) Create User entity
      User {
        username: "john_doe"
        password: "$2a$10$..." (hashed)
        role: "USER"
        enabled: true
      }
   
   d) Save to database
      - INSERT INTO users (username, password, role, enabled)
      - VALUES ('john_doe', '$2a$10$...', 'USER', true)

5. Return success response (201 Created)
   {
     "username": "john_doe",
     "role": "USER",
     "message": "Registration successful"
   }
```

---

### **Scenario 2: User Login & JWT Generation**

```
1. Client sends POST /login
   {
     "username": "john_doe",
     "password": "secret123"
   }

2. AuthController.login() receives request
   ↓
3. @Valid annotation validates input
   ↓
4. AuthServiceImpl.login() executes:
   
   a) Create authentication token
      UsernamePasswordAuthenticationToken token
        = new UsernamePasswordAuthenticationToken(
            "john_doe",        // username
            "secret123"        // password (plain)
          )
   
   b) Call authenticationManager.authenticate(token)
      ↓ AuthenticationManager looks for DaoAuthenticationProvider
      ↓ DaoAuthenticationProvider calls:
        - CustomUserDetailsService.loadUserByUsername("john_doe")
          * Query: SELECT * FROM users WHERE username = 'john_doe'
          * Returns: UserDetails object with username, password hash, authorities
        
        - PasswordEncoder.matches(provided_password, stored_hash)
          * Provided: "secret123"
          * Stored: "$2a$10$..."
          * BCrypt compares them:
            - Takes provided password
            - Extracts salt from stored hash
            - Hashes provided password with salt
            - Compares with stored hash
          * If match ✓ → Authentication successful
          * If no match ✗ → throw BadCredentialsException (401 Unauthorized)

5. If authentication successful:
   a) Get authenticated user details
   
   b) Generate JWT token:
      Claims:
      {
        "sub": "john_doe",              // Subject = username
        "roles": ["ROLE_USER"],         // Authorities
        "iat": 1671234567,              // Issued at time
        "exp": 1671320967               // Expiration time (24 hours later)
      }
      
      Token signed with secret key: "$2a$10$..."
      ↓
      Final JWT: "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJqb2huIiwicm9sZXMiOlsiUk9MRV9VU0VSIl19.xyz..."

6. Return success response (200 OK)
   {
     "token": "eyJhbGciOiJIUzI1NiJ9...",
     "username": "john_doe",
     "role": "USER",
     "message": "Login successful"
   }

7. Client stores token (in localStorage, sessionStorage, or memory)
   → Will use for next requests
```

---

### **Scenario 3: Accessing Protected Endpoint with JWT**

```
Client wants to access: GET /protected-test

1. Client sends request with JWT in Authorization header:
   GET /protected-test
   Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...

2. Request enters JwtAuthenticationFilter
   ↓
3. Extract JWT from Authorization header:
   - Get header: "Bearer eyJhbGciOiJIUzI1NiJ9..."
   - Remove "Bearer " prefix
   - Token: "eyJhbGciOiJIUzI1NiJ9..."

4. Validate JWT token (jwtUtil.validateToken(token)):
   a) Parse token using signing key
   b) Verify signature:
      - Extract signature from token
      - Recalculate signature using secret key
      - Compare: stored_signature == calculated_signature?
      - If match ✓ → Signature valid (not tampered)
      - If no match ✗ → throw JwtException
   
   c) Check expiration:
      - Extract "exp" claim
      - Compare with current time
      - If token_exp > now ✓ → Token not expired
      - If token_exp < now ✗ → throw ExpiredJwtException

5. If validation successful:
   a) Extract username: "john_doe"
   
   b) Extract roles: ["ROLE_USER"]
   
   c) Create Authentication object:
      UsernamePasswordAuthenticationToken {
        principal: "john_doe"
        credentials: null (not needed for token auth)
        authorities: ["ROLE_USER"]
        authenticated: true
      }
   
   d) Store in SecurityContextHolder:
      SecurityContext.authentication = authenticationToken
      ↓ Now Spring Security knows user is authenticated

6. Request proceeds to controller
   ↓
7. AuthController.protectedTest(Authentication authentication) executes:
   - authentication.getName() → "john_doe"
   
   Return response (200 OK):
   {
     "message": "You have access to protected resource",
     "username": "john_doe"
   }

What if token is invalid or missing?
- Filter doesn't authenticate user
- SecurityContext remains empty
- Spring Security checks: isAuthenticated? NO
- Request is rejected (403 Forbidden)
- AccessDeniedException is thrown
```

---

### **Scenario 4: Admin-Only Endpoint Access**

```
Non-Admin User tries to access: GET /admin-data
Authorization: Bearer [valid-jwt-with-role-user]

1. Request enters JwtAuthenticationFilter
   ↓
2. Token is valid and not expired ✓
   ↓
3. Extract username: "john_doe"
   Extract roles: ["USER"]
   ↓
4. Create and store Authentication in SecurityContext:
   authorities: ["ROLE_USER"]
   ↓
5. Request reaches controller: AuthController.adminData()
   ↓
6. Manual role check in controller:
   boolean isAdmin = authentication.getAuthorities()
     .stream()
     .map(GrantedAuthority::getAuthority)
     .anyMatch(auth -> auth.equals("ROLE_ADMIN"));
   
   isAdmin = false ✗
   ↓
7. Throw ForbiddenAccessException("Only administrators can access this resource")
   ↓
8. GlobalExceptionHandler catches exception
   ↓
9. Return error response (403 Forbidden):
   {
     "statusCode": 403,
     "error": "FORBIDDEN",
     "message": "Only administrators can access this resource",
     "errorCode": "AUTH_002"
   }

---

Admin User tries to access: GET /admin-data
Authorization: Bearer [valid-jwt-with-role-admin]

1. Request enters JwtAuthenticationFilter
   ↓
2. Token is valid ✓
   ↓
3. Extract username: "admin_user"
   Extract roles: ["ADMIN"]
   ↓
4. Create and store Authentication in SecurityContext:
   authorities: ["ROLE_ADMIN"]
   ↓
5. Request reaches controller: AuthController.adminData()
   ↓
6. Role check:
   isAdmin = true ✓
   ↓
7. Return response (200 OK):
   {
     "message": "Admin data - Sensitive information here",
     "username": "admin_user",
     "role": "ADMIN"
   }
```

---

## 🔐 Security Best Practices Implemented

### **1. Password Security**
✅ Passwords are hashed using BCrypt
✅ Each hash is unique (random salt)
✅ One-way function (can't decrypt)
✅ Plain password never stored or logged

### **2. JWT Security**
✅ Token is signed (tamper detection)
✅ Token expires after 24 hours
✅ Credentials verified before token generation
✅ Token format verified on every request

### **3. Input Validation**
✅ @Valid annotation on DTOs
✅ @NotBlank for required fields
✅ Custom validation for business rules (username unique)

### **4. Authorization**
✅ Authentication required for protected endpoints
✅ Role-based access control
✅ Admin-only endpoints protected

### **5. Error Handling**
✅ No internal errors exposed to client
✅ Structured error responses
✅ Sensitive info not logged
✅ Clear error codes for debugging

### **6. Stateless Architecture**
✅ No sessions stored on server
✅ JWT contains all user info
✅ Scalable for microservices
✅ Works with load balancers

### **7. Configuration Management**
✅ Secret key in application.yml
✅ Token expiration configurable
✅ No hardcoded values in code

---

## 📊 Request/Response Examples

### **1. Registration - Success**
```
Request:
POST /register
Content-Type: application/json

{
  "username": "john_doe",
  "password": "secret123"
}

Response: 201 Created
{
  "username": "john_doe",
  "role": "USER",
  "message": "Registration successful"
}
```

### **2. Registration - Username Exists**
```
Request:
POST /register
{
  "username": "existing_user",
  "password": "secret123"
}

Response: 409 Conflict
{
  "statusCode": 409,
  "error": "CONFLICT",
  "message": "Username 'existing_user' already exists",
  "errorCode": "USER_001",
  "timestamp": "2026-04-18 10:30:45"
}
```

### **3. Login - Success**
```
Request:
POST /login
{
  "username": "john_doe",
  "password": "secret123"
}

Response: 200 OK
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "username": "john_doe",
  "role": "USER",
  "message": "Login successful"
}
```

### **4. Login - Invalid Credentials**
```
Request:
POST /login
{
  "username": "john_doe",
  "password": "wrong_password"
}

Response: 401 Unauthorized
{
  "statusCode": 401,
  "error": "UNAUTHORIZED",
  "message": "Invalid username or password",
  "errorCode": "AUTH_001",
  "timestamp": "2026-04-18 10:30:45"
}
```

### **5. Protected Endpoint - With Valid JWT**
```
Request:
GET /protected-test
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...

Response: 200 OK
{
  "message": "You have access to protected resource",
  "username": "john_doe"
}
```

### **6. Protected Endpoint - Without JWT**
```
Request:
GET /protected-test

Response: 403 Forbidden
{
  "statusCode": 403,
  "error": "FORBIDDEN",
  "message": "Access Denied",
  "errorCode": "AUTH_002",
  "timestamp": "2026-04-18 10:30:45"
}
```

### **7. Admin Endpoint - Non-Admin User**
```
Request:
GET /admin-data
Authorization: Bearer [jwt-with-role-user]

Response: 403 Forbidden
{
  "statusCode": 403,
  "error": "FORBIDDEN",
  "message": "Only administrators can access this resource",
  "errorCode": "AUTH_002",
  "timestamp": "2026-04-18 10:30:45"
}
```

### **8. Admin Endpoint - Admin User**
```
Request:
GET /admin-data
Authorization: Bearer [jwt-with-role-admin]

Response: 200 OK
{
  "message": "Admin data - Sensitive information here",
  "username": "admin_user",
  "role": "ADMIN"
}
```

---

## 🧪 Testing with Swagger UI

Swagger UI is publicly accessible at:
```
http://localhost:8080/swagger-ui.html
```

Steps to test:
1. **Register**: Click POST /register, enter username/password, execute
2. **Login**: Click POST /login, enter username/password, execute → Copy token
3. **Try Protected**: Click GET /protected-test, paste token in "Authorization: Bearer" field, execute
4. **Admin Access**: Click GET /admin-data, paste token, execute

---

## 🛠️ How to Debug

### **1. Enable SQL Logging**
In application.yml:
```yaml
spring:
  jpa:
    show-sql: true
```

### **2. Enable JWT Debug Logging**
Check logs for:
```
INFO: Generating JWT token for user: john_doe
INFO: JWT token generated | username=john_doe
DEBUG: JWT token validated successfully
```

### **3. Test Token Manually**
Decode JWT at: https://jwt.io
- Paste token
- View claims (username, roles, expiration)

### **4. Check User in Database**
```sql
SELECT * FROM users WHERE username = 'john_doe';
```

---

## 📚 Key Takeaways

1. **Security is Layered**: Auth → JWT → Filter → Controller
2. **Passwords are Hashed**: Never stored plain, never logged
3. **Tokens are Stateless**: No session storage needed
4. **Roles are Flexible**: Added during registration, checked on access
5. **Errors are Structured**: Consistent responses, no info leakage
6. **Config is External**: Secrets in yml, not in code
7. **Testing is Easy**: Use Swagger to test all endpoints

---

**Happy and Secure Coding! 🚀**

