# 🎨 Visual Architecture & Flow Diagrams

## 1. Complete Request Flow Diagram

```
┌─────────────────────────────────────────────────────────────────────┐
│                           CLIENT SIDE                               │
└──────────────────────────────┬──────────────────────────────────────┘

                     Step 1: Registration
                            │
                            ▼
                  ┌──────────────────────┐
                  │  POST /register      │
                  │  { username, pwd }   │
                  └──────────────────────┘
                            │
                            ▼
────────────────────────────────────────────────────────────────────────
│                                                                       │
│                        SERVER SIDE                                   │
│                                                                       │
│  ┌─────────────────────────────────────────────────────────────┐   │
│  │  AuthController.register()                                  │   │
│  │  1. @Valid validates input                                  │   │
│  │  2. Calls authService.register()                            │   │
│  └────────────────────────┬────────────────────────────────────┘   │
│                           │                                          │
│                           ▼                                          │
│  ┌─────────────────────────────────────────────────────────────┐   │
│  │  AuthServiceImpl.register()                                  │   │
│  │  1. Check if username exists                                │   │
│  │     ├─ Query: SELECT * FROM users WHERE username = ?       │   │
│  │     ├─ If exists → throw UserAlreadyExistsException        │   │
│  │     └─ If not → continue                                    │   │
│  │  2. Hash password with BCrypt                               │   │
│  │     └─ "secret123" → "$2a$10$EixZaYVK..."                  │   │
│  │  3. Create User entity                                      │   │
│  │  4. Save to database                                        │   │
│  │  5. Return success response                                 │   │
│  └─────────────────────────────────────────────────────────────┘   │
│                                                                       │
│                            │                                          │
│                            ▼                                          │
│  ┌─────────────────────────────────────────────────────────────┐   │
│  │  UserRepository.save()                                      │   │
│  │  INSERT INTO users (username, password, role, enabled)      │   │
│  │  VALUES (?, ?, ?, ?)                                        │   │
│  └─────────────────────────────────────────────────────────────┘   │
│                                                                       │
└───────────────────────────────────────────────────────────────────────

                     ◄─ Response 201 Created ─────


                   Step 2: Login & JWT Generation
                            │
                            ▼
                  ┌──────────────────────┐
                  │  POST /login         │
                  │  { username, pwd }   │
                  └──────────────────────┘
                            │
                            ▼
────────────────────────────────────────────────────────────────────────
│                                                                       │
│  ┌─────────────────────────────────────────────────────────────┐   │
│  │  AuthController.login()                                     │   │
│  │  1. @Valid validates input                                  │   │
│  │  2. Calls authService.login()                               │   │
│  └────────────────────────┬────────────────────────────────────┘   │
│                           │                                          │
│                           ▼                                          │
│  ┌─────────────────────────────────────────────────────────────┐   │
│  │  AuthServiceImpl.login()                                     │   │
│  │  1. Create UsernamePasswordAuthenticationToken              │   │
│  │  2. Call authenticationManager.authenticate(token)          │   │
│  └────────────────────────┬────────────────────────────────────┘   │
│                           │                                          │
│                           ▼                                          │
│  ┌─────────────────────────────────────────────────────────────┐   │
│  │  AuthenticationManager                                      │   │
│  │  Looks for matching AuthenticationProvider                  │   │
│  │  Finds → DaoAuthenticationProvider                          │   │
│  └────────────────────────┬────────────────────────────────────┘   │
│                           │                                          │
│                           ▼                                          │
│  ┌─────────────────────────────────────────────────────────────┐   │
│  │  DaoAuthenticationProvider                                  │   │
│  │  1. Call CustomUserDetailsService.loadUserByUsername()      │   │
│  └────────────────────────┬────────────────────────────────────┘   │
│                           │                                          │
│                           ▼                                          │
│  ┌─────────────────────────────────────────────────────────────┐   │
│  │  CustomUserDetailsService                                   │   │
│  │  1. Query: SELECT * FROM users WHERE username = 'john_doe' │   │
│  │  2. Return UserDetails {username, password_hash, roles}    │   │
│  └────────────────────────┬────────────────────────────────────┘   │
│                           │                                          │
│    ◄────────────────────── UserDetails ────────────────────────     │
│                           │                                          │
│  ┌─────────────────────────────────────────────────────────────┐   │
│  │  DaoAuthenticationProvider (continued)                      │   │
│  │  2. Call PasswordEncoder.matches()                          │   │
│  │     Input: "secret123" (from login)                         │   │
│  │     Stored: "$2a$10$EixZaYVK..." (from DB)                 │   │
│  │     ├─ If match → Authentication successful ✓              │   │
│  │     └─ If no match → throw BadCredentialsException ✗       │   │
│  └────────────────────────┬────────────────────────────────────┘   │
│                           │                                          │
│    ◄────────────────────── Authentication ───────────────────────   │
│                           │                                          │
│  ┌─────────────────────────────────────────────────────────────┐   │
│  │  AuthServiceImpl.login() (continued)                         │   │
│  │  3. Call JwtUtil.generateToken(userDetails)                 │   │
│  └────────────────────────┬────────────────────────────────────┘   │
│                           │                                          │
│                           ▼                                          │
│  ┌─────────────────────────────────────────────────────────────┐   │
│  │  JwtUtil.generateToken()                                    │   │
│  │  1. Extract roles from userDetails                          │   │
│  │  2. Create Claims                                           │   │
│  │     {                                                        │   │
│  │       "sub": "john_doe",                                    │   │
│  │       "roles": ["USER"],                                    │   │
│  │       "iat": 1776496757,                                    │   │
│  │       "exp": 1776583157                                     │   │
│  │     }                                                        │   │
│  │  3. Sign with secret key (HMAC-SHA256)                      │   │
│  │  4. Return JWT token                                        │   │
│  │     eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJqb2huIi...xyz         │   │
│  └─────────────────────────────────────────────────────────────┘   │
│                                                                       │
│  ┌─────────────────────────────────────────────────────────────┐   │
│  │  AuthServiceImpl.login() - Build response                   │   │
│  │  {                                                           │   │
│  │    "token": "eyJhbGciOiJIUzI1NiJ9...",                      │   │
│  │    "username": "john_doe",                                  │   │
│  │    "role": "USER",                                          │   │
│  │    "message": "Login successful"                            │   │
│  │  }                                                           │   │
│  └─────────────────────────────────────────────────────────────┘   │
│                                                                       │
└───────────────────────────────────────────────────────────────────────

                 ◄─ Response 200 OK with JWT ─────


              Step 3: Access Protected Endpoint
                            │
                            ▼
                  ┌──────────────────────┐
                  │  GET /protected-test │
                  │  Authorization:      │
                  │  Bearer JWT_TOKEN    │
                  └──────────────────────┘
                            │
                            ▼
────────────────────────────────────────────────────────────────────────
│                                                                       │
│  ┌─────────────────────────────────────────────────────────────┐   │
│  │  JwtAuthenticationFilter (doFilterInternal)                 │   │
│  │  Runs for EVERY request                                     │   │
│  │                                                              │   │
│  │  1. Extract token from Authorization header                 │   │
│  │     Authorization: "Bearer eyJhbGciOiJIUzI1NiJ9..."        │   │
│  │     Remove "Bearer " prefix                                 │   │
│  │     Token: "eyJhbGciOiJIUzI1NiJ9..."                       │   │
│  │                                                              │   │
│  │  2. Call JwtUtil.validateToken(token)                       │   │
│  └────────────────────────┬────────────────────────────────────┘   │
│                           │                                          │
│                           ▼                                          │
│  ┌─────────────────────────────────────────────────────────────┐   │
│  │  JwtUtil.validateToken()                                    │   │
│  │  1. Parse token using signing key                           │   │
│  │  2. Verify signature:                                       │   │
│  │     ├─ Recalculate signature using secret key              │   │
│  │     ├─ Compare with token's signature                       │   │
│  │     ├─ If match → Token not tampered ✓                      │   │
│  │     └─ If mismatch → throw JwtException ✗                  │   │
│  │  3. Check expiration:                                       │   │
│  │     ├─ Get "exp" claim from token                           │   │
│  │     ├─ Compare with current time                            │   │
│  │     ├─ If not expired → Valid ✓                             │   │
│  │     └─ If expired → throw ExpiredJwtException ✗             │   │
│  │  4. Return true (if both checks pass)                       │   │
│  └────────────────────────┬────────────────────────────────────┘   │
│                           │                                          │
│                           ▼                                          │
│  ┌─────────────────────────────────────────────────────────────┐   │
│  │  JwtAuthenticationFilter (continued)                        │   │
│  │  3. Extract username from token                             │   │
│  │     username = "john_doe"                                   │   │
│  │  4. Extract roles from token                                │   │
│  │     roles = ["USER"]                                        │   │
│  │  5. Create Authentication object                            │   │
│  │     UsernamePasswordAuthenticationToken {                   │   │
│  │       principal: "john_doe",                                │   │
│  │       credentials: null,                                    │   │
│  │       authorities: [ROLE_USER]                              │   │
│  │     }                                                        │   │
│  │  6. Store in SecurityContext                                │   │
│  │     SecurityContextHolder.getContext()                      │   │
│  │       .setAuthentication(authenticationToken)               │   │
│  │  7. Continue with filter chain                              │   │
│  └─────────────────────────────────────────────────────────────┘   │
│                                                                       │
│                           │                                          │
│                           ▼                                          │
│  ┌─────────────────────────────────────────────────────────────┐   │
│  │  Spring Security Authorization Check                        │   │
│  │  1. Is endpoint protected? YES (/protected-test requires    │   │
│  │                                 authentication)             │   │
│  │  2. Is user authenticated? YES (stored in SecurityContext)  │   │
│  │  3. Does user have required role? N/A (any role OK)        │   │
│  │  → AUTHORIZATION PASSED ✓                                   │   │
│  └─────────────────────────────────────────────────────────────┘   │
│                                                                       │
│                           │                                          │
│                           ▼                                          │
│  ┌─────────────────────────────────────────────────────────────┐   │
│  │  AuthController.protectedTest(Authentication auth)          │   │
│  │  1. Get username from authentication                        │   │
│  │     username = auth.getName() → "john_doe"                 │   │
│  │  2. Build response                                          │   │
│  │  3. Return 200 OK with data                                 │   │
│  └─────────────────────────────────────────────────────────────┘   │
│                                                                       │
└───────────────────────────────────────────────────────────────────────

                   ◄─ Response 200 OK ─────
```

---

## 2. Password Hashing Flow

```
Plain Password: "secret123"
        │
        ▼
┌──────────────────────────────────┐
│  BCryptPasswordEncoder.encode()   │
│                                  │
│  1. Generate random salt         │
│     Salt: random(16 bytes)       │
│                                  │
│  2. Hash password with salt      │
│     Using bcrypt algorithm       │
│     (CPU-intensive, slow)        │
│                                  │
│  3. Combine hash + salt + rounds │
│     Format: $2a$10$SALT$HASH     │
│                                  │
│  4. Return combined result       │
└──────────────────────────────────┘
        │
        ▼
Hashed Password: "$2a$10$EixZaYVK1fsbw1ZfbX3OXePaWxn96p36WQoeG6Lruj3njPHga/iguEn"

Store in DB ✓


─────────────────────────────────────────────────────────

Login Later: User enters "secret123"
        │
        ▼
┌──────────────────────────────────────────────┐
│  BCryptPasswordEncoder.matches()              │
│                                              │
│  Input:  "secret123" (plain password)        │
│  Stored: "$2a$10$..." (from database)        │
│                                              │
│  1. Extract salt from stored hash            │
│     Salt: EixZaYVK1fsbw1ZfbX3OX              │
│                                              │
│  2. Hash input password with extracted salt  │
│     Plain: "secret123"                       │
│     Salt: EixZaYVK1fsbw1ZfbX3OX              │
│     Result: "$2a$10$EixZaYVK1f..."          │
│                                              │
│  3. Compare with stored hash                 │
│     Result == Stored? YES ✓                  │
│                                              │
│  4. Return true/false                        │
└──────────────────────────────────────────────┘
        │
        ▼
Authentication Successful!
```

---

## 3. JWT Token Anatomy

```
┌────────────────────────────────────────────────────────────────┐
│                    JWT Token Structure                         │
└────────────────────────────────────────────────────────────────┘

Token: eyJhbGciOiJIUzI1NiJ9.eyJyb2xlcyI6WyJVU0VSIl0sInN1YiI6ImpvaG4iLCJpYXQiOjE3NzY0OTY3NTcsImV4cCI6MTc3NjU4MzE1N30.xyz...

                    │                         │                        │
                    ▼                         ▼                        ▼
              ┌───────────┐          ┌────────────────┐       ┌──────────────┐
              │  HEADER   │          │    PAYLOAD     │       │  SIGNATURE   │
              └───────────┘          └────────────────┘       └──────────────┘


HEADER (Base64 Decoded):
{
  "alg": "HS256",      ← Algorithm (HMAC with SHA256)
  "typ": "JWT"         ← Type (JSON Web Token)
}


PAYLOAD (Base64 Decoded):
{
  "roles": ["USER"],   ← User's roles/authorities
  "sub": "john",       ← Subject (username)
  "iat": 1776496757,   ← Issued At (timestamp)
  "exp": 1776583157    ← Expiration (timestamp + 24 hours)
}


SIGNATURE:
- Secret Key: "mySecretKeyForJWT..."
- Data to Sign: HEADER.PAYLOAD
- Algorithm: HMAC-SHA256
- Result: xyz... (computed signature)
- Verification: Recalculate signature and compare
  ├─ If match → Token is valid ✓
  └─ If no match → Token is tampered ✗
```

---

## 4. Role-Based Access Control (RBAC) Flow

```
Request: GET /admin-data
Header: Authorization: Bearer JWT_TOKEN

                │
                ▼
┌─────────────────────────────────────────────────────┐
│  JwtAuthenticationFilter                            │
│  Extract & validate token                           │
│  Store user in SecurityContext                      │
│  Roles: ["ROLE_USER"] or ["ROLE_ADMIN"]            │
└──────────────────────┬────────────────────────────┘
                       │
                       ▼
┌─────────────────────────────────────────────────────┐
│  Spring Security @PreAuthorize Processing           │
│  (Declarative security at method level)             │
│                                                     │
│  @GetMapping("/admin-data")                         │
│  @PreAuthorize("hasRole('ADMIN')")                 │
│                                                     │
│  1. Check @PreAuthorize expression BEFORE method   │
│  2. Extract user authorities from SecurityContext  │
│  3. Evaluate: Does user have ROLE_ADMIN?           │
│  4. Authorization decision made automatically       │
└──────────────────────┬────────────────────────────┘
                       │
        ┌──────────────┴──────────────┐
        │                             │
        ▼ Authorization DENIED        ▼ Authorization ALLOWED
    ┌─────────────────────┐       ┌─────────────────────┐
    │ Spring Security     │       │ Method executes     │
    │ Returns 403         │       │ with full user      │
    │ Forbidden           │       │ context             │
    └─────────────────────┘       └────────┬────────────┘
                                         │
                                         ▼
                                  ┌────────────────┐
                                  │ Return 200 OK  │
                                  │ with admin data│
                                  └────────────────┘
```

---

## 5. Dependency Injection Pattern - @RequiredArgsConstructor

```
BEFORE: Manual Constructor Injection (Verbose)
┌──────────────────────────────────────────────┐
│ public class AuthServiceImpl {               │
│   private final UserRepository userRepo;    │
│   private final PasswordEncoder encoder;    │
│   private final JwtUtil jwtUtil;            │
│                                             │
│   // Boilerplate constructor code          │
│   public AuthServiceImpl(                    │
│       UserRepository userRepo,              │
│       PasswordEncoder encoder,              │
│       JwtUtil jwtUtil                       │
│   ) {                                       │
│       this.userRepo = userRepo;             │
│       this.encoder = encoder;               │
│       this.jwtUtil = jwtUtil;               │
│   }                                         │
│ }                                           │
└──────────────────────────────────────────────┘

AFTER: Lombok @RequiredArgsConstructor (Clean)
┌──────────────────────────────────────────────┐
│ @RequiredArgsConstructor                     │
│ public class AuthServiceImpl {               │
│   private final UserRepository userRepo;    │
│   private final PasswordEncoder encoder;    │
│   private final JwtUtil jwtUtil;            │
│   // Constructor auto-generated by Lombok   │
│ }                                           │
└──────────────────────────────────────────────┘

BENEFITS of @RequiredArgsConstructor:
✓ Less boilerplate code
✓ Constructor auto-generated for final fields
✓ No field injection (safer)
✓ Immutable dependencies
✓ Easy to test (explicit dependencies)
✓ Easier to refactor
✓ Clear dependency visibility
✓ Recommended by Spring & industry best practices

APPLIED IN:
├─ AuthServiceImpl (@RequiredArgsConstructor)
├─ AuthController (@RequiredArgsConstructor)
├─ JwtAuthenticationFilter (@RequiredArgsConstructor)
├─ CustomUserDetailsService (@RequiredArgsConstructor)
├─ SecurityConfig (@RequiredArgsConstructor)
└─ AdminUtilityController (@RequiredArgsConstructor)
```

---

## 6. Request Authentication Paths

```
                    ┌─────────────────────────────┐
                    │  Incoming HTTP Request      │
                    └───────────────┬─────────────┘
                                    │
                                    ▼
                    ┌─────────────────────────────┐
                    │  Has Authorization Header?  │
                    └───────────────┬─────────────┘
                                    │
                ┌───────────────────┴──────────────────┐
                │                                      │
                ▼ YES                                  ▼ NO
    ┌──────────────────────┐              ┌──────────────────────┐
    │ Extract JWT Token    │              │ Endpoint Public?     │
    └──────────┬───────────┘              └──────────┬───────────┘
               │                                     │
               ▼                                     │ YES
    ┌──────────────────────┐                ┌───────▼─────────┐
    │ Valid Token?         │                │ Allow Request   │
    └──────────┬───────────┘                └─────────────────┘
               │
        ┌──────┴──────┐
        │             │
        ▼ YES         ▼ NO
    ┌────────┐    ┌──────────────┐
    │Authenticate │ Return 403   │
    │ Request     │ Unauthorized │
    └───┬────┘    └──────────────┘
        │
        ▼
    ┌─────────────────────┐
    │ Check Endpoint      │
    │ Authorization       │
    │ (Role required?)    │
    └──────────┬──────────┘
               │
        ┌──────┴──────┐
        │             │
        ▼ ALLOWED     ▼ DENIED
    ┌────────┐    ┌──────────┐
    │ Allow  │    │ Return   │
    │ Request│    │ 403      │
    │        │    │ Forbidden│
    └────┬───┘    └──────────┘
         │
         ▼
    ┌─────────────────────┐
    │ Pass to Controller  │
    └─────────────────────┘
```

---

## 7. Error Handling Flow

```
┌─────────────────────────────────────────────────────────┐
│  Exception Thrown in Controller/Service                 │
└───────────────────────┬─────────────────────────────────┘
                        │
                        ▼
        ┌───────────────────────────────────┐
        │ What type of exception?           │
        └───────┬───────┬──────┬────────┬───┘
                │       │      │        │
        ┌───────┘       │      │        │
        │     ┌────────┘      │        │
        │     │       ┌───────┘        │
        │     │       │        ┌───────┘
        │     │       │        │
        ▼     ▼       ▼        ▼
    ┌──────────────────────────────────────────────────────┐
    │  GlobalExceptionHandler                              │
    │  @RestControllerAdvice                              │
    │  @ExceptionHandler(SpecificException.class)         │
    └──────────────┬────────────────────────────────────┘
                   │
                   ▼
        ┌──────────────────────────┐
        │ Build ErrorResponse      │
        │ - statusCode             │
        │ - error                  │
        │ - message                │
        │ - timestamp              │
        │ - errorCode              │
        └──────────────┬───────────┘
                       │
                       ▼
        ┌──────────────────────────┐
        │ Return ResponseEntity    │
        │ (with appropriate HTTP   │
        │  status code)            │
        └──────────────┬───────────┘
                       │
                       ▼
        ┌──────────────────────────┐
        │ JSON Response to Client  │
        │ {                        │
        │   "statusCode": 401,     │
        │   "error": "UNAUTHORIZED",
        │   "message": "...",      │
        │   "errorCode": "AUTH_001"│
        │ }                        │
        └──────────────────────────┘

Exception Types & Responses:
┌──────────────────────────────────────────┐
│ Exception                    │ HTTP Code  │
├──────────────────────────────┼────────────┤
│ UserAlreadyExistsException   │ 409        │
│ InvalidCredentialsException  │ 401        │
│ ForbiddenAccessException     │ 403        │
│ UsernameNotFoundException     │ 401        │
│ BadCredentialsException      │ 401        │
│ MethodArgumentNotValidException│ 400       │
│ Generic Exception            │ 500        │
└──────────────────────────────────────────┘
```

---

## 8. Deployment Architecture (Recommended)

```
┌─────────────────────────────────────────────────────────┐
│                    Internet / Load Balancer             │
└──────────────────────┬──────────────────────────────────┘
                       │
            ┌──────────┴──────────┐
            │                     │
            ▼                     ▼
    ┌───────────────┐     ┌───────────────┐
    │  Instance 1   │     │  Instance 2   │
    │  Port 8080    │     │  Port 8080    │
    └───┬───────────┘     └───┬───────────┘
        │                     │
        ├─────┬───────────────┤
        │     │               │
        ▼     ▼               ▼
    ┌──────────────────────────────┐
    │   MySQL Database             │
    │   (Shared persistence)       │
    │   - users table              │
    │   - Connection pool (Hikari) │
    └────────────────┬─────────────┘
                     │
        ┌────────────┴────────────┐
        ▼                         ▼
    ┌──────────────┐       ┌──────────────┐
    │ Backups      │       │ Monitoring   │
    │ (Automated)  │       │ (Metrics)    │
    └──────────────┘       └──────────────┘

Key Points:
- Stateless application (can have multiple instances)
- JWT tokens work across instances (no session sync needed)
- Database is single point of persistence
- Load balancer distributes requests
- Each instance can validate tokens independently
```

---

**All flows documented and production-ready! 🎉**

