# Authentication API

Base path: `/api/auth`. These routes are public except sending a company invite, which requires `ADMIN`.

## Endpoints

| Method | Path | Body | Success |
| --- | --- | --- | --- |
| `POST` | `/api/auth/register` | `RegisterRequest` | `200` message; verification email sent |
| `POST` | `/api/auth/verify-email` | `{"token":"..."}` | `204` |
| `POST` | `/api/auth/login` | `LoginRequest` | `200 AuthResponse` |
| `POST` | `/api/auth/oauth/google` | `{"idToken":"..."}` | `200 AuthResponse` |
| `POST` | `/api/auth/refresh` | `{"refreshToken":"..."}` | `200 AuthResponse` |
| `POST` | `/api/auth/logout` | `{"refreshToken":"..."}` | `204` |
| `POST` | `/api/auth/forgot-password` | `{"email":"..."}` | `204` |
| `POST` | `/api/auth/reset-password` | `PasswordResetRequest` | `204` |
| `POST` | `/api/auth/company-invites` | `InviteRequest`, admin token | `204` |
| `POST` | `/api/auth/company-invites/accept` | `AcceptInviteRequest` | `200 AuthResponse` |

## Credentials

Registration:

```json
{
  "email": "user@example.com",
  "password": "TestPassword123!",
  "confirmPassword": "TestPassword123!"
}
```

Registration creates a disabled `USER` account and returns a message. It does not return tokens until email verification is completed.

Login:

```json
{
  "email": "user@example.com",
  "password": "TestPassword123!"
}
```

Successful login, Google login, refresh and invite acceptance return:

```json
{
  "accessToken": "<jwt>",
  "refreshToken": "<opaque-refresh-token>",
  "role": "USER"
}
```

Email is required, valid and at most 254 characters. Passwords are 8–128 characters when created or reset and must satisfy the custom password policy; login accepts a nonblank password up to 128 characters. Confirmation must match.

## Refresh and logout

```json
{"refreshToken":"<refresh-token>"}
```

Access tokens authenticate API calls. Refresh tokens are stored server-side, rotate through the session service, and are removed on logout. Disabling, deleting or changing a user's role revokes stored refresh tokens.

## Password reset

Requesting a reset always returns `204` to avoid exposing account existence.

```json
{"email":"user@example.com"}
```

Complete it with the emailed token:

```json
{
  "token": "<reset-token>",
  "password": "NewPassword123!",
  "confirmPassword": "NewPassword123!"
}
```

## Company invites

Only administrators can send an invite:

```json
{
  "email": "company@example.com",
  "companyName": "Example Ltd"
}
```

`companyName` is required and limited to 120 characters. Acceptance creates a `COMPANY` account and profile:

```json
{
  "token": "<invite-token>",
  "password": "CompanyPassword123!",
  "confirmPassword": "CompanyPassword123!"
}
```

## Common errors

| Status | Meaning |
| --- | --- |
| `400` | Malformed body, validation failure, expired/invalid request token |
| `401` | Invalid credentials, disabled account, invalid refresh token |
| `403` | Non-admin attempts to send an invite |
| `409` | Existing account/invite or provider conflict |

Use `Authorization: Bearer <accessToken>` on protected routes. Opening `/login` in a browser sends an unrelated `GET /login` and returns `401`; the API login route is `POST /api/auth/login`.
