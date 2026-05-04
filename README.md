# 👤 User Service

> Part of the **Blockchain-Inspired Online Voting System** — a production-grade, scalable microservices platform.

[![Java](https://img.shields.io/badge/Java-17-orange.svg)](https://openjdk.org/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.3.6-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Spring Security](https://img.shields.io/badge/Spring%20Security-6.x-blue.svg)](https://spring.io/projects/spring-security)
[![JWT](https://img.shields.io/badge/JWT-JJWT-black.svg)](https://github.com/jwtk/jjwt)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-blue.svg)](https://www.postgresql.org/)
[![License](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)

---

## 📌 Overview

The **User Service** is the central identity provider for the voting system. It handles user registration, authentication via JWT tokens, and manages roles (`ROLE_VOTER`, `ROLE_ADMIN`). It ensures that only registered voters can cast votes and only authorized admins can manage candidates.

### Feature Status
- 🟡 Project bootstrapping (Day 6 Complete)
- 🔜 User registration with BCrypt hashing
- 🔜 JWT token generation and validation
- 🔜 Role-based Access Control (RBAC)
- 🔜 Token validation endpoint for API Gateway
- 🔜 Global Exception Handling
- 🔜 Swagger/OpenAPI documentation
- 🔜 Unit + Integration tests

---

## 🏗️ Architecture

```
                    ┌──────────────────┐
                    │   API Gateway    │
                    │ (Spring Cloud)   │
                    └────────┬─────────┘
                             │
              ┌──────────────┼──────────────┐
              │              │              │
    ┌─────────▼──┐  ┌───────▼────┐  ┌──────▼──────┐
    │ ★USER★     │  │  Candidate │  │  Voting     │
    │  SERVICE   │  │  Service   │  │  Service    │
    │  (8081)    │  │  (8082)    │  │  (8083)     │
    └──────┬─────┘  └────────────┘  └─────────────┘
           │
     ┌─────▼──────┐
     │ PostgreSQL │
     │ user       │
     │ service_db │
     └────────────┘
```

### Cross-Service Communication

| Consumer | Protocol | Endpoints Called | Purpose |
|----------|----------|--------------------|---------|
| **API Gateway** | REST (sync) | `GET /api/v1/auth/validate` | Authenticate every incoming request |
| **Voting Service** | OpenFeign (sync) | `GET /api/v1/users/{id}/role` | Ensure user has `ROLE_VOTER` before casting |

---

## 🛠️ Tech Stack

| Component | Technology | Version |
|-----------|-----------|---------|
| Language | Java | 17 |
| Framework | Spring Boot | 3.3.6 |
| Security | Spring Security | 6.x |
| Auth Token | JJWT (Java JWT) | 0.12.x |
| Database | PostgreSQL (dedicated) | 16 |
| ORM | Spring Data JPA / Hibernate | — |
| API Docs | Swagger UI / OpenAPI 3.0 | springdoc 2.5.0 |
| Build Tool | Maven | 3.8+ |

---

## 🔌 API Reference (Planned)

### Base URL
```
http://localhost:8081/api/v1/auth
```

### Authentication Endpoints

| Method | Endpoint | Description | Auth |
|--------|----------|-------------|------|
| `POST` | `/api/v1/auth/register` | Create a new user account | PUBLIC |
| `POST` | `/api/v1/auth/login` | Authenticate and get JWT | PUBLIC |
| `GET` | `/api/v1/auth/validate` | Validate token integrity | INTERNAL |

### User Endpoints

| Method | Endpoint | Description | Auth |
|--------|----------|-------------|------|
| `GET` | `/api/v1/users/{id}` | Get user profile | USER/ADMIN |
| `GET` | `/api/v1/users/{id}/role` | Get user role | INTERNAL |

---

## 🗄️ Database Schema

**Database:** `user_service_db`

```sql
CREATE TABLE users (
    id              UUID PRIMARY KEY,
    username        VARCHAR(50) UNIQUE NOT NULL,
    email           VARCHAR(100) UNIQUE NOT NULL,
    password        VARCHAR(255) NOT NULL,
    role            VARCHAR(20) NOT NULL,
    created_at      TIMESTAMP NOT NULL,
    updated_at      TIMESTAMP NOT NULL
);
```

---

## 📦 Project Structure

```
user-service/
├── src/
│   ├── main/
│   │   ├── java/com/voting/userservice/
│   │   │   ├── UserServiceApplication.java
│   │   │   ├── config/              ← Security & JWT Config
│   │   │   ├── controller/          ← Auth & User Controllers
│   │   │   ├── dto/                 ← Requests & Responses
│   │   │   ├── exception/           ← Global Exception Handler
│   │   │   ├── model/               ← User Entity & Role Enum
│   │   │   ├── repository/          ← Spring Data JPA
│   │   │   ├── security/            ← JWT Service & Filters
│   │   │   └── service/             ← Business Logic
│   │   └── resources/
│   │       └── application.yml
├── docs/
│   └── IMPLEMENTATION_PLAN.md      ← Day-by-day checklist
├── README.md                       ← This file
├── pom.xml
└── .gitignore
```

---

## 🚀 Getting Started

### Prerequisites
- Java 17+
- Maven 3.8+
- PostgreSQL 14+

### Setup

1. **Create PostgreSQL database**
   ```sql
   CREATE DATABASE user_service_db;
   ```

2. **Build the project**
   ```bash
   mvn clean install
   ```

3. **Run the service**
   ```bash
   mvn spring-boot:run
   ```

---

## 📋 Implementation Progress

> Detailed checklist: [IMPLEMENTATION_PLAN.md](src/main/java/com/voting/userservice/docs/IMPLEMENTATION_PLAN.md)

| Step | Description | Status |
|------|-------------|--------|
| 6 | Project Bootstrapping | ✅ Done |
| 7 | Database Layer (User Entity, Repository) | 🔜 Next |
| 8 | DTOs & Exception Handling | 🔜 Planned |
| 9 | User Registration Logic | 🔜 Planned |
| 10 | JWT Foundation | 🔜 Planned |
| 11 | Spring Security Configuration | 🔜 Planned |
| 12 | Authentication APIs (Login) | 🔜 Planned |
| 13 | Token Validation & Internal Endpoints | 🔜 Planned |
| 14 | Unit Testing | 🔜 Planned |
| 15 | Integration Testing | 🔜 Planned |

---

## 🔗 Related Services

| Service | Port | Description | Status |
|---------|------|-------------|--------|
| **User Service** | **8081** | **Authentication & Roles** | **🟡 In Progress** |
| Candidate Service | 8082 | Candidate lifecycle | ✅ Complete |
| Voting Service | 8083 | Vote casting | 🔜 Planned |
| Result Service | 8084 | Live aggregation | 🔜 Planned |

---

## 📝 License

This project is licensed under the MIT License.

---

> **Maintainer:** Vaibhav Jain  
> **Last Updated:** April 24, 2026
