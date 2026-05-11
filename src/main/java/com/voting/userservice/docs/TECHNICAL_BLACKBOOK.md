# 📓 User Service — Technical Blackbook

This document dives into the internal design decisions and security mechanics of the User Service.

## 🔐 Security Deep Dive

### 1. Password Protection
We use **BCryptPasswordEncoder** with a default strength of 10. 
- **Mechanism**: Salted hashing. Every password gets a unique salt automatically, preventing rainbow table attacks.
- **Decision**: BCrypt is computationally expensive, which protects against brute-force attacks even if the database is leaked.

### 2. JWT Implementation (Stateless Auth)
The system uses the `io.jsonwebtoken` (JJWT) library.
- **Structure**:
  - **Header**: Algorithm (HS256) and type (JWT).
  - **Payload (Claims)**: `sub` (username), `userId`, `role`, `iat` (issued at), `exp` (expiration).
  - **Signature**: Generated using a secret key stored in environment variables.
- **Validation**: Every request passing through the `JwtAuthenticationFilter` is checked for:
  1.  Expiration time.
  2.  Signature integrity.
  3.  Presence of mandatory claims.

### 3. Stateless vs. Stateful
- **Choice**: Stateless (JWT).
- **Rationale**: In a microservices architecture, session affinity (sticky sessions) is a bottleneck. Using JWT allows the **API Gateway** or **Voting Service** to verify a user's identity without asking the User Service DB every time. This scales horizontally with ease.

## ⚙️ Design Patterns

### 1. Global Exception Handling
We use `@RestControllerAdvice` to intercept exceptions across the entire application.
- **Benefit**: Ensures a consistent API response format (`ApiResponse<T>`) for errors, making it easier for frontend developers to handle failures.

### 2. UUID as Primary Key
- **Decision**: Use `java.util.UUID` instead of `Long` for User IDs.
- **Rationale**: 
  - **Security**: Prevents ID enumeration (an attacker guessing `user/1`, `user/2`).
  - **Distributed Systems**: Allows ID generation in the application layer without database round-trips for sequences.

### 3. Idempotent Validations
The `/validate` endpoint is built to be extremely lightweight. It does **not** query the database for every validation call; it only verifies the cryptographic signature of the JWT. This makes it highly performant for the API Gateway.

## 🚀 Performance Considerations
- **BCrypt Cost**: If authentication becomes a bottleneck, we can scale the User Service instances independently.
- **JWT Expiration**: Currently set to 24 hours. For high-security phases (like actual election day), this can be reduced via the `JWT_EXPIRATION` environment variable.

---
> **Back to [Service Overview](SERVICE_OVERVIEW.md)**
