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

### Q7: How is the User Service registered with Eureka?
> **Answer**: It uses the `@EnableDiscoveryClient` annotation and configuration in `application.yml` to register itself with the **Eureka Server** under the logical name `user-service`. This allows the API Gateway to find it without knowing its IP address.

### Q8: What is the difference between an Access Token and a Refresh Token?
> **Answer**: 
> - **Access Token**: Short-lived (e.g., 15 mins), sent with every request to authorize the user.
> - **Refresh Token**: Long-lived (e.g., 7 days), stored securely (like an HttpOnly cookie) and used only to request a new Access Token. This provides a better balance between security and user experience.

### Q9: How do you handle sensitive data (like passwords) in logs?
> **Answer**: We use log masking techniques (like Logback's `CompositeConverter`) to ensure that passwords, tokens, or PII (Personally Identifiable Information) are never printed to the logs. We also avoid logging full request/response bodies in production for the same reason.

### Q10: How would you implement "Social Login" (Google/GitHub)?
> **Answer**: We would use **Spring Security OAuth2 Client**. The User Service redirects the user to the provider (Google). Upon successful login, Google sends an Authorization Code back to our service, which we exchange for an ID Token. We then create/update a local user record and issue our own JWT.

---
## 🚀 Advanced Discussion Topics
- **Token Revocation**: Using Redis to blacklist tokens upon logout.
- **Multi-Factor Authentication (MFA)**: Adding a second layer of security via TOTP.
- **Distributed Tracing**: Using Sleuth/Zipkin to track a login request across the Gateway and User Service.

---
## 🏗️ Architecture & Modern Java

### Q11: Why did we choose a Microservices architecture for this system?
> **Answer**: It provides **Scalability** (we can scale the auth service independently), **Fault Isolation** (a crash in the candidate service doesn't stop people from logging in), and **Independent Deployment** (we can update auth logic without touching other services).

### Q12: What are the benefits of using Java 17 and Spring Boot 3 in this service?
> **Answer**: 
> 1. **Records**: We use Records for our DTOs because they are immutable and require zero boilerplate.
> 2. **Virtual Threads**: Allows us to handle thousands of concurrent auth requests with very low memory overhead.
> 3. **Native Support**: We can use GraalVM to compile this service into a native binary for near-instant startup times.

---
> **Back to [Service Overview](SERVICE_OVERVIEW.md)**
