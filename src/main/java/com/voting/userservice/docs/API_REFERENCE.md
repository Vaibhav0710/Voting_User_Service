# 📖 User Service — API Reference

All requests to the User Service should use the base path: `/api/v1/auth`.

## 1. Register User
Registers a new voter or administrator in the system.

- **URL**: `POST /api/v1/auth/register`
- **Auth Required**: None (Public)
- **Request Body**:
```json
{
  "username": "john_voter",
  "email": "john@example.com",
  "password": "securePassword123",
  "role": "ROLE_VOTER"
}
```
- **Success Response (201 Created)**:
```json
{
  "success": true,
  "message": "User registered successfully",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiJ9..."
  }
}
```
- **Error Response (409 Conflict)**: Username or Email already exists.

---

## 2. Login
Authenticates a user and issues a JWT token.

- **URL**: `POST /api/v1/auth/login`
- **Auth Required**: None (Public)
- **Request Body**:
```json
{
  "username": "john_voter",
  "password": "securePassword123"
}
```
- **Success Response (200 OK)**:
```json
{
  "success": true,
  "message": "Login successful",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiJ9..."
  }
}
```
- **Error Response (401 Unauthorized)**: Invalid credentials.

---

## 3. Validate Token
Internal endpoint used by the **API Gateway** to verify a token and extract claims.

- **URL**: `GET /api/v1/auth/validate`
- **Auth Required**: Bearer Token
- **Headers**: `Authorization: Bearer <token>`
- **Success Response (200 OK)**:
```json
{
  "valid": true,
  "userId": "d1f50619-35f4-4447-98e2-23d926c43331",
  "username": "john_voter",
  "role": "ROLE_VOTER"
}
```
- **Error Response (401 Unauthorized)**: Token is expired, malformed, or missing.

---

## 🛑 Common Error Codes

| Status Code | Code | Description |
|-------------|------|-------------|
| `400` | `BAD_REQUEST` | Missing required fields or validation failure. |
| `401` | `UNAUTHORIZED` | Invalid credentials or expired token. |
| `404` | `NOT_FOUND` | User does not exist. |
| `409` | `CONFLICT` | Username or Email is already taken. |
| `500` | `INTERNAL_SERVER_ERROR` | Unexpected server-side error. |

---
> **Back to [Service Overview](SERVICE_OVERVIEW.md)**
