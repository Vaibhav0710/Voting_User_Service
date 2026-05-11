# 🛡️ User Service — Service Overview

The **User Service** is the foundation of the Online Voting System's security. It serves as the **Identity Provider (IdP)** for the entire microservices cluster, ensuring that only authenticated and authorized users can interact with the platform.

## 🎯 Purpose
In a voting system, trust is paramount. The User Service ensures that:
1.  **Identity is Verified**: Only registered citizens (voters) or officials (admins) can access the system.
2.  **Roles are Enforced**: Prevents voters from accessing administrative functions and ensures only admins can manage candidates or elections.
3.  **Stateless Security**: Uses JWT (JSON Web Tokens) to allow other services to verify identity without constant database lookups.

## 🏗️ Core Responsibilities
- **User Management**: Registration of new voters and administrators with secure password hashing (BCrypt).
- **Authentication**: Validating credentials and issuing signed JWT access tokens.
- **Token Validation**: Providing an internal endpoint for the **API Gateway** to verify tokens before routing requests.
- **Role Management**: Assigning and maintaining user roles (`ROLE_VOTER`, `ROLE_ADMIN`).

## 🛠️ Tech Stack
- **Framework**: Spring Boot 3.3.x
- **Security**: Spring Security 6 (Stateless/Sessionless)
- **Authentication**: JWT (JSON Web Token) via JJWT library
- **Persistence**: PostgreSQL (via Spring Data JPA)
- **Language**: Java 17
- **Utilities**: Lombok (Boilerplate reduction), MapStruct (DTO Mapping)

## 📐 Architecture
The service follows a clean, layered architecture:
- **Controller Layer**: Exposes REST endpoints and handles HTTP status codes.
- **Service Layer**: Contains business logic (Password hashing, JWT generation).
- **Security Layer**: Intercepts requests, validates tokens, and sets the Security Context.
- **Repository Layer**: Interface with the PostgreSQL database.

---
> **Related Documents:**
> - [API Reference](API_REFERENCE.md)
> - [Technical Blackbook](TECHNICAL_BLACKBOOK.md)
> - [Interview Guide](INTERVIEW_GUIDE.md)
