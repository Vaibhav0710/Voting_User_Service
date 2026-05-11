# 🎯 User Service — Interview Guide

Use these questions to prepare for technical reviews or system design interviews regarding this microservice.

## 🧠 Technical Questions & Answers

### Q1: Why did you choose JWT over Session-based authentication?
> **Answer**: In a microservices architecture, session-based auth requires either shared session storage (like Redis) or session stickiness at the load balancer. JWT is stateless, meaning the token itself contains all necessary information. This allows individual services (like Voting or Result) to verify identity independently, leading to better scalability and zero-dependency authentication.

### Q2: How do you handle JWT security if a token is stolen?
> **Answer**: JWTs are inherently difficult to revoke before they expire. In our system, we mitigate this by:
> 1.  Using a short-lived expiration time (configurable via env vars).
> 2.  Using HTTPS only (SSL/TLS) to prevent interception.
> 3.  The next evolution would be implementing a **Token Blacklist** in Redis for logout scenarios.

### Q3: What is the purpose of the `JwtAuthenticationFilter`?
> **Answer**: It's a "OncePerRequestFilter" that intercepts every incoming HTTP request. It extracts the Bearer token, validates its signature and expiration, and if valid, populates the `SecurityContextHolder` with an `Authentication` object. This allows us to use `@PreAuthorize` on our controller methods to restrict access by role.

### Q4: Why use BCrypt instead of SHA-256 or MD5 for passwords?
> **Answer**: SHA-256 and MD5 are fast hashing algorithms designed for data integrity, not password storage. They are vulnerable to GPU-accelerated brute force and rainbow tables. BCrypt is a **slow hashing algorithm** with an integrated salt. Its "work factor" can be increased over time, making it much more resistant to modern hardware-based attacks.

### Q5: How does the API Gateway interact with this service?
> **Answer**: The Gateway acts as a "Gatekeeper." Before routing a request to any downstream service (like Voting), it calls the `/api/v1/auth/validate` endpoint of the User Service. If the User Service says the token is valid, the Gateway forwards the request; otherwise, it returns a 401 Unauthorized immediately, saving the downstream services from processing invalid requests.

### Q6: How did you handle exceptions to ensure a clean API?
> **Answer**: I implemented a `@RestControllerAdvice` class (GlobalExceptionHandler). This catches specific exceptions like `UserAlreadyExistsException` or `BadCredentialsException` and wraps them in a standardized `ApiResponse` object. This ensures that the frontend always receives a consistent JSON structure, whether the request succeeds or fails.

## 🚀 Advanced Discussion Topics
- **Refresh Tokens**: How to implement them to keep users logged in securely.
- **Distributed Tracing**: Using Sleuth/Zipkin to track a login request across the Gateway and User Service.
- **Rate Limiting**: Preventing brute force attacks at the Gateway level using Redis.

---
> **Back to [Service Overview](SERVICE_OVERVIEW.md)**
