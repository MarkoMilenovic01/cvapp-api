# Authentication API

Base path: `/api/auth`. These routes are public except sending a company invite, which requires `ADMIN`.

## Endpoints

| Method | Path | Body | Success |
| --- | --- | --- | --- |
| `POST` | `/api/auth/register` | `RegisterRequest` | `200` message; verification email sent |
| `POST` | `/api/auth/verify-email` | `{"token":"..."}` | `204` |
| `POST` | `/api/auth/resend-verification` | `{"email":"..."}` | `204` |
| `POST` | `/api/auth/login` | `LoginRequest` | `200 AuthResponse`; refresh-token cookie set |
| `POST` | `/api/auth/oauth/google` | `{"idToken":"..."}` | `200 AuthResponse`; refresh-token cookie set |
| `POST` | `/api/auth/refresh` | No body; refresh-token cookie required | `200 AuthResponse`; rotated cookie set |
| `POST` | `/api/auth/logout` | No body; refresh-token cookie required | `204`; cookie cleared |
| `POST` | `/api/auth/forgot-password` | `{"email":"..."}` | `204` |
| `POST` | `/api/auth/reset-password` | `PasswordResetRequest` | `204` |
| `POST` | `/api/auth/company-invites` | `InviteRequest`, admin token | `204` |
| `POST` | `/api/auth/company-invites/accept` | `AcceptInviteRequest` | `200 AuthResponse`; refresh-token cookie set |

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

If the verification token expires, request another one with `POST /api/auth/resend-verification`. The endpoint always returns `204` so it does not reveal whether an account exists. A new token is sent only for an existing disabled local account.

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
  "role": "USER"
}
```

The refresh token is never included in the JSON response. It is returned in a
`refresh_token` HTTP cookie instead.

Email is required, valid and at most 254 characters. Passwords are 8–128 characters when created or reset and must satisfy the custom password policy; login accepts a nonblank password up to 128 characters. Confirmation must match.

## Refresh and logout

The refresh token is transported only in the `refresh_token` cookie. The cookie
is set after successful credentials login, Google login, session refresh and
company-invite acceptance with these attributes:

- `HttpOnly`, so frontend JavaScript cannot read it
- `Secure` in production by default (`REFRESH_COOKIE_SECURE=true`)
- `SameSite=Lax` by default (configurable with `REFRESH_COOKIE_SAME_SITE`)
- `Path=/api/auth`, so it is sent only to authentication endpoints
- `Max-Age` equal to the configured refresh-token lifetime

Call `POST /api/auth/refresh` and `POST /api/auth/logout` without a JSON body.
The browser supplies the cookie automatically. For a frontend making a
cross-origin request, include credentials, for example:

```javascript
await fetch(`${apiUrl}/api/auth/refresh`, {
  method: "POST",
  credentials: "include"
});
```

The API CORS configuration allows credentials from the configured development
frontend origins. Production deployments must likewise allow the deployed
frontend origin. If the frontend and API are cross-site rather than merely
cross-origin, configure `SameSite=None` together with `Secure=true`.

Access tokens authenticate API calls through `Authorization: Bearer <accessToken>`.
Refresh tokens are stored server-side as hashes and rotate through the session
service. Refresh returns a new access token in JSON and replaces the cookie with
the rotated refresh token. Logout invalidates the server-side token and clears
the cookie with `Max-Age=0`. Disabling, deleting or changing a user's role
revokes stored refresh tokens.

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
