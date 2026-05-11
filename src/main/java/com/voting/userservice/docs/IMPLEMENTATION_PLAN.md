# 📋 User Service — Day-Wise Implementation Plan

> **Goal:** Build the identity and authentication layer for the Online Voting System.  
> **Status:** ✅ Day 15 — Completed (Testing & Cleanup)

---

## 📅 Day 6: Project Bootstrapping
- [x] 6.1 — Initialize Maven structure (`src/main/java`, `src/main/resources`)
- [x] 6.2 — Configure `pom.xml` with core dependencies:
    - `spring-boot-starter-web`
    - `spring-boot-starter-data-jpa`
    - `spring-boot-starter-security`
    - `postgresql`
    - `lombok`
- [x] 6.3 — Create `UserServiceApplication.java`
- [x] 6.4 — Create `application.yml` with basic PostgreSQL and Port (`8081`) config

## 📅 Day 7: Database Layer (Foundation)
- [x] 7.1 — Create `model/enums/Role.java` (VOTER, ADMIN)
- [x] 7.2 — Create `model/User.java` Entity (UUID ID, username, email, password, role, audit fields)
- [x] 7.3 — Implement `UserRepository.java` (existsBy methods, findByUsername)
- [x] 7.4 — Verify DB connection (Manual verification via startup logs)

## 📅 Day 8: DTOs & Exception Handling
- [x] 8.1 — Create `dto/RegisterRequest.java` & `dto/UserResponseDTO.java`
- [x] 8.2 — Create `dto/ApiResponse.java` (Generic wrapper)
- [x] 8.3 — Implement `exception/ResourceNotFoundException.java` & `DuplicateResourceException.java`
- [x] 8.4 — Create `exception/GlobalExceptionHandler.java` (@RestControllerAdvice)

## 📅 Day 9: User Registration Logic
- [x] 9.1 — Create `mapper/UserMapper.java`
- [x] 9.2 — Define `IUserService.java` interface
- [x] 9.3 — Implement `registerUser()` in `UserServiceImpl.java` (with BCrypt password hashing)
- [x] 9.4 — Implement registration validation (check for existing email/username)
- [x] 9.5 — Create `config/SecurityConfig.java` (temporary — BCryptPasswordEncoder bean + permitAll)

## 📅 Day 10: JWT Utility & Foundation
- [x] 10.1 — Add `jjwt-api`, `jjwt-impl`, `jjwt-jackson` dependencies to `pom.xml`
- [x] 10.2 — Create `security/JwtService.java`
- [x] 10.3 — Implement `generateToken()`, `extractUsername()`, and `isTokenValid()`
- [x] 10.4 — Secure secret keys via `application.yml` placeholders

## 📅 Day 11: Spring Security Configuration
- [x] 11.1 — Implement `security/CustomUserDetailsService.java`
- [x] 11.2 — Create `security/JwtAuthenticationFilter.java`
- [x] 11.3 — Configure `security/SecurityConfig.java`:
    - Disable CSRF
    - Set session policy to STATELESS
    - Configure PasswordEncoder bean
    - Setup FilterChain to authorize public endpoints (`/register`, `/login`)

## 📅 Day 12: Authentication APIs (Login)
- [x] 12.1 — Create `dto/LoginRequest.java` & `dto/AuthResponse.java`
- [x] 12.2 — Implement `login()` logic in `UserService` (AuthenticationManager)
- [x] 12.3 — Implement `AuthController.java` with `POST /register` and `POST /login`

## 📅 Day 13: Token Validation & Internal Endpoints
- [x] 13.1 — Implement `GET /api/v1/auth/validate` for API Gateway use
- [~] 13.2 — [CANCELED] Implement `GET /api/v1/users/{id}/role` for Voting Service use (using HTTP headers instead)
- [x] 13.3 — Wrap all responses in `ApiResponse<T>`

## 📅 Day 14: Unit Testing (Mocking)
- [x] 14.1 — Test `UserService.registerUser()` (Happy path + Conflict)
- [x] 14.2 — Test `UserService.login()` (Valid vs Invalid credentials)
- [x] 14.3 — Test `JwtService` token parsing logic

## 📅 Day 15: Integration Testing & Cleanup
- [x] 15.1 — Test Auth flow using `@WebMvcTest` and `MockMvc`
- [x] 15.2 — Verify full Register → Login → Validate loop
- [x] 15.3 — Cleanup imports, add Javadoc, and final code review

---

## 🏗️ Design Decisions
| Decision | Choice | Rationale |
|----------|--------|-----------|
| **Auth Type** | JWT (Stateless) | Necessary for microservices scalability |
| **Password Hashing** | BCrypt | Industry standard for secure storage |
| **User ID** | UUID | Prevents ID enumeration attacks |
| **Role Enforcement** | `hasRole('ADMIN')` | Standard Spring Security approach |
| **Token Validation** | Purely Stateless | `JwtAuthenticationFilter` reads claims directly; zero DB calls to avoid microservice bottlenecks |
| **Cross-Service Roles** | HTTP Headers | Gateway passes `X-User-Role` to avoid Feign network hop for role verification |
