# Auth API Documentation

Base URL:

```text
/api/auth
```

Most auth endpoints are public.
Protected endpoints require:

```http
Authorization: Bearer <accessToken>
```

---

# Auth Response

Most successful authentication endpoints return:

```json
{
  "accessToken": "jwt-access-token",
  "refreshToken": "refresh-token",
  "role": "USER"
}
```

## Fields

| Field          | Type   | Description                                          |
| -------------- | ------ | ---------------------------------------------------- |
| `accessToken`  | string | JWT token used for authenticated requests            |
| `refreshToken` | string | Token used to generate a new access token            |
| `role`         | string | User role, for example `USER`, `COMPANY`, or `ADMIN` |

---

# Credentials Auth

Base path:

```text
/api/auth
```

---

## Register

```http
POST /api/auth/register
```

Creates a new regular user account.

### Request Body

```json
{
  "email": "test@best.com",
  "password": "Test@1234",
  "confirmPassword": "Test@1234"
}
```

### Validation

| Field             | Rules                                                            |
| ----------------- | ---------------------------------------------------------------- |
| `email`           | required, valid email format, max 254 characters                 |
| `password`        | required, 8–128 characters, must pass custom password validation |
| `confirmPassword` | required, 8–128 characters                                       |

### Success Response

```http
200 OK
```

```json
{
  "accessToken": "jwt-access-token",
  "refreshToken": "refresh-token",
  "role": "USER"
}
```

### Errors

| Status            | Reason                                    |
| ----------------- | ----------------------------------------- |
| `400 Bad Request` | Invalid request body or validation failed |
| `409 Conflict`    | Email already exists                      |
| `409 Conflict`    | Email is registered with Google OAuth     |

---

## Login

```http
POST /api/auth/login
```

Logs in an existing user.

### Request Body

```json
{
  "email": "test@best.com",
  "password": "Test@1234"
}
```

### Validation

| Field      | Rules                                            |
| ---------- | ------------------------------------------------ |
| `email`    | required, valid email format, max 254 characters |
| `password` | required, 8–128 characters                       |

### Success Response

```http
200 OK
```

```json
{
  "accessToken": "jwt-access-token",
  "refreshToken": "refresh-token",
  "role": "USER"
}
```

### Errors

| Status             | Reason                                    |
| ------------------ | ----------------------------------------- |
| `400 Bad Request`  | Invalid request body or validation failed |
| `401 Unauthorized` | Invalid email or password                 |

---

# Session API

Base path:

```text
/api/auth
```

---

## Refresh Token

```http
POST /api/auth/refresh
```

Generates a new access token using a valid refresh token.

### Request Body

```json
{
  "refreshToken": "refresh-token"
}
```

### Validation

| Field          | Rules    |
| -------------- | -------- |
| `refreshToken` | required |

### Success Response

```http
200 OK
```

```json
{
  "accessToken": "new-jwt-access-token",
  "refreshToken": "refresh-token",
  "role": "USER"
}
```

### Errors

| Status             | Reason                                        |
| ------------------ | --------------------------------------------- |
| `400 Bad Request`  | Refresh token is missing                      |
| `401 Unauthorized` | Refresh token is invalid, expired, or revoked |

---

## Logout

```http
POST /api/auth/logout
```

Logs out the user by invalidating the refresh token.

### Request Body

```json
{
  "refreshToken": "refresh-token"
}
```

### Validation

| Field          | Rules    |
| -------------- | -------- |
| `refreshToken` | required |

### Success Response

```http
204 No Content
```

### Errors

| Status             | Reason                   |
| ------------------ | ------------------------ |
| `400 Bad Request`  | Refresh token is missing |
| `401 Unauthorized` | Refresh token is invalid |

---

# Password Reset API

Base path:

```text
/api/auth
```

These endpoints are rate limited.

```text
3 requests per 60 seconds
```

---

## Forgot Password

```http
POST /api/auth/forgot-password
```

Sends a password reset email to an existing user.

### Request Body

```json
{
  "email": "test@best.com"
}
```

### Validation

| Field   | Rules                                            |
| ------- | ------------------------------------------------ |
| `email` | required, valid email format, max 254 characters |

### Success Response

```http
204 No Content
```

### Errors

| Status                  | Reason                              |
| ----------------------- | ----------------------------------- |
| `400 Bad Request`       | Email is blank or invalid           |
| `404 Not Found`         | User with this email does not exist |
| `429 Too Many Requests` | Too many reset requests             |

---

## Reset Password

```http
POST /api/auth/reset-password
```

Resets the user password using a valid reset token.

### Request Body

```json
{
  "token": "reset-token",
  "password": "NewPass@1234",
  "confirmPassword": "NewPass@1234"
}
```

### Validation

| Field             | Rules                      |
| ----------------- | -------------------------- |
| `token`           | required                   |
| `password`        | required, 6–128 characters |
| `confirmPassword` | required, 6–128 characters |

### Success Response

```http
204 No Content
```

### Errors

| Status                  | Reason                                      |
| ----------------------- | ------------------------------------------- |
| `400 Bad Request`       | Validation failed or passwords do not match |
| `404 Not Found`         | Reset token does not exist                  |
| `410 Gone`              | Reset token expired or was already used     |
| `429 Too Many Requests` | Too many reset attempts                     |

---

# Company Invite API

Base path:

```text
/api/auth/company-invites
```

---

## Send Company Invite

```http
POST /api/auth/company-invites
```

Sends an invitation email to a company.

Only users with the `ADMIN` role can use this endpoint.

This endpoint is rate limited.

```text
5 requests per 60 seconds
```

### Authorization

```http
Authorization: Bearer <adminAccessToken>
```

### Request Body

```json
{
  "email": "company@test.com",
  "companyName": "Test Company"
}
```

### Validation

| Field         | Rules                                            |
| ------------- | ------------------------------------------------ |
| `email`       | required, valid email format, max 254 characters |
| `companyName` | required, max 120 characters                     |

### Success Response

```http
204 No Content
```

### Errors

| Status                  | Reason                                    |
| ----------------------- | ----------------------------------------- |
| `400 Bad Request`       | Invalid request body or validation failed |
| `401 Unauthorized`      | Missing or invalid token                  |
| `403 Forbidden`         | User is not admin                         |
| `409 Conflict`          | Invite already exists for this email      |
| `409 Conflict`          | Email is already registered               |
| `429 Too Many Requests` | Too many invite requests                  |

---

## Accept Company Invite

```http
POST /api/auth/company-invites/accept
```

Accepts a company invite and creates a company user account.

This endpoint is public because the invite token is used as proof.

### Request Body

```json
{
  "token": "invite-token",
  "password": "Test@1234",
  "confirmPassword": "Test@1234"
}
```

### Validation

| Field             | Rules                       |
| ----------------- | --------------------------- |
| `token`           | required, max 36 characters |
| `password`        | required, 8–128 characters  |
| `confirmPassword` | required, 8–128 characters  |

### Success Response

```http
200 OK
```

```json
{
  "accessToken": "jwt-access-token",
  "refreshToken": "refresh-token",
  "role": "COMPANY"
}
```

### Errors

| Status            | Reason                 |
| ----------------- | ---------------------- |
| `400 Bad Request` | Invalid token          |
| `400 Bad Request` | Invite already used    |
| `400 Bad Request` | Invite expired         |
| `400 Bad Request` | Passwords do not match |
| `400 Bad Request` | Validation failed      |

---

# JWT

The application uses JWT access tokens.

After login, register, refresh, or accepting a company invite, the API returns an `accessToken`.

Use the access token like this:

```http
Authorization: Bearer <accessToken>
```

The JWT contains:

| Claim  | Description         |
| ------ | ------------------- |
| `sub`  | User email          |
| `role` | User role authority |
| `iat`  | Issued at           |
| `exp`  | Expiration time     |

Example protected request:

```http
GET /api/user/cv
Authorization: Bearer <accessToken>
```

---

# Google OAuth

The auth package includes Google OAuth support.

When a user logs in with Google:

* If the email does not exist, a new user is created.
* The new user gets the `USER` role.
* The auth provider is set to `GOOGLE`.
* The user is enabled by default.

If the email already exists as a normal password-based account, the request fails with:

```http
409 Conflict
```

Reason:

```text
This email is already registered with a password. Please login normally.
```

No separate REST controller endpoint was provided for Google OAuth in this package. The actual OAuth login URL is usually handled by Spring Security configuration.

---

# DTO Reference

## RegisterRequest

```json
{
  "email": "test@best.com",
  "password": "Test@1234",
  "confirmPassword": "Test@1234"
}
```

| Field             | Type   | Required | Validation                                   |
| ----------------- | ------ | -------: | -------------------------------------------- |
| `email`           | string |      yes | valid email, max 254 characters              |
| `password`        | string |      yes | 8–128 characters, custom password validation |
| `confirmPassword` | string |      yes | 8–128 characters                             |

---

## LoginRequest

```json
{
  "email": "test@best.com",
  "password": "Test@1234"
}
```

| Field      | Type   | Required | Validation                      |
| ---------- | ------ | -------: | ------------------------------- |
| `email`    | string |      yes | valid email, max 254 characters |
| `password` | string |      yes | 8–128 characters                |

---

## RefreshTokenRequest

```json
{
  "refreshToken": "refresh-token"
}
```

| Field          | Type   | Required | Validation |
| -------------- | ------ | -------: | ---------- |
| `refreshToken` | string |      yes | not blank  |

---

## ForgotPasswordRequest

```json
{
  "email": "test@best.com"
}
```

| Field   | Type   | Required | Validation                      |
| ------- | ------ | -------: | ------------------------------- |
| `email` | string |      yes | valid email, max 254 characters |

---

## PasswordResetRequest

```json
{
  "token": "reset-token",
  "password": "NewPass@1234",
  "confirmPassword": "NewPass@1234"
}
```

| Field             | Type   | Required | Validation       |
| ----------------- | ------ | -------: | ---------------- |
| `token`           | string |      yes | not blank        |
| `password`        | string |      yes | 6–128 characters |
| `confirmPassword` | string |      yes | 6–128 characters |

---

## InviteRequest

```json
{
  "email": "company@test.com",
  "companyName": "Test Company"
}
```

| Field         | Type   | Required | Validation                      |
| ------------- | ------ | -------: | ------------------------------- |
| `email`       | string |      yes | valid email, max 254 characters |
| `companyName` | string |      yes | max 120 characters              |

---

## AcceptInviteRequest

```json
{
  "token": "invite-token",
  "password": "Test@1234",
  "confirmPassword": "Test@1234"
}
```

| Field             | Type   | Required | Validation        |
| ----------------- | ------ | -------: | ----------------- |
| `token`           | string |      yes | max 36 characters |
| `password`        | string |      yes | 8–128 characters  |
| `confirmPassword` | string |      yes | 8–128 characters  |

---

## AuthResponse

```json
{
  "accessToken": "jwt-access-token",
  "refreshToken": "refresh-token",
  "role": "USER"
}
```

| Field          | Type   |
| -------------- | ------ |
| `accessToken`  | string |
| `refreshToken` | string |
| `role`         | string |

---

# Rate Limits

| Endpoint                         | Limit                     |
| -------------------------------- | ------------------------- |
| `POST /api/auth/forgot-password` | 3 requests per 60 seconds |
| `POST /api/auth/reset-password`  | 3 requests per 60 seconds |
| `POST /api/auth/company-invites` | 5 requests per 60 seconds |

---

# Notes

`POST /api/auth/register` creates a regular user with the `USER` role.

`POST /api/auth/company-invites/accept` creates a company user with the `COMPANY` role.

Admin-only endpoints require the user to have the `ADMIN` role.

Refresh tokens are used only for session renewal and logout.

Access tokens are used for protected API requests.
