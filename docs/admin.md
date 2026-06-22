# Admin API Documentation

Base URL:

```text
/api/admin
```

All admin endpoints require an authenticated user with the `ADMIN` role.

Use the access token from login/register:

```http
Authorization: Bearer <accessToken>
```

---

# Admin Users API

Base path:

```text
/api/admin/users
```

---

## Get All Users

```http
GET /api/admin/users
```

Returns a paginated list of users.

### Query Parameters

Spring pagination is supported.

| Parameter |    Type |     Default | Description              |
| --------- | ------: | ----------: | ------------------------ |
| `page`    | integer |         `0` | Page number              |
| `size`    | integer |        `20` | Number of users per page |
| `sort`    |  string | `createdAt` | Sort field               |

### Example Request

```http
GET /api/admin/users?page=0&size=20&sort=createdAt
Authorization: Bearer <adminAccessToken>
```

### Success Response

```http
200 OK
```

```json
{
  "content": [
    {
      "id": 1,
      "email": "user@cvapp.com",
      "role": "USER",
      "enabled": true,
      "provider": "LOCAL",
      "createdAt": "2026-06-22T14:30:00"
    }
  ],
  "pageable": {},
  "totalElements": 1,
  "totalPages": 1,
  "size": 20,
  "number": 0
}
```

### Errors

| Status             | Reason                   |
| ------------------ | ------------------------ |
| `401 Unauthorized` | Missing or invalid token |
| `403 Forbidden`    | User is not admin        |

---

## Get User By ID

```http
GET /api/admin/users/{id}
```

Returns a single user by ID.

### Path Parameters

| Parameter | Type | Description |
| --------- | ---: | ----------- |
| `id`      | long | User ID     |

### Example Request

```http
GET /api/admin/users/1
Authorization: Bearer <adminAccessToken>
```

### Success Response

```http
200 OK
```

```json
{
  "id": 1,
  "email": "user@cvapp.com",
  "role": "USER",
  "enabled": true,
  "provider": "LOCAL",
  "createdAt": "2026-06-22T14:30:00"
}
```

### Errors

| Status             | Reason                   |
| ------------------ | ------------------------ |
| `401 Unauthorized` | Missing or invalid token |
| `403 Forbidden`    | User is not admin        |
| `404 Not Found`    | User does not exist      |

---

## Toggle User Enabled Status

```http
PATCH /api/admin/users/{id}/toggle
```

Enables or disables a user account.

If the user is currently enabled, this endpoint disables them.
If the user is currently disabled, this endpoint enables them.

### Path Parameters

| Parameter | Type | Description |
| --------- | ---: | ----------- |
| `id`      | long | User ID     |

### Example Request

```http
PATCH /api/admin/users/1/toggle
Authorization: Bearer <adminAccessToken>
```

### Success Response

```http
200 OK
```

```json
{
  "id": 1,
  "email": "user@cvapp.com",
  "role": "USER",
  "enabled": false,
  "provider": "LOCAL",
  "createdAt": "2026-06-22T14:30:00"
}
```

### Errors

| Status             | Reason                   |
| ------------------ | ------------------------ |
| `401 Unauthorized` | Missing or invalid token |
| `403 Forbidden`    | User is not admin        |
| `404 Not Found`    | User does not exist      |

---

## Change User Role

```http
PATCH /api/admin/users/{id}/role
```

Changes the role of a user.

### Path Parameters

| Parameter | Type | Description |
| --------- | ---: | ----------- |
| `id`      | long | User ID     |

### Request Body

```json
{
  "role": "ADMIN"
}
```

### Available Roles

```text
USER
COMPANY
ADMIN
```

### Example Request

```http
PATCH /api/admin/users/1/role
Authorization: Bearer <adminAccessToken>
Content-Type: application/json
```

```json
{
  "role": "ADMIN"
}
```

### Success Response

```http
200 OK
```

```json
{
  "id": 1,
  "email": "user@cvapp.com",
  "role": "ADMIN",
  "enabled": true,
  "provider": "LOCAL",
  "createdAt": "2026-06-22T14:30:00"
}
```

### Errors

| Status             | Reason                   |
| ------------------ | ------------------------ |
| `400 Bad Request`  | Invalid role             |
| `401 Unauthorized` | Missing or invalid token |
| `403 Forbidden`    | User is not admin        |
| `404 Not Found`    | User does not exist      |

---

## Delete User

```http
DELETE /api/admin/users/{id}
```

Deletes a user by ID.

### Path Parameters

| Parameter | Type | Description |
| --------- | ---: | ----------- |
| `id`      | long | User ID     |

### Example Request

```http
DELETE /api/admin/users/1
Authorization: Bearer <adminAccessToken>
```

### Success Response

```http
204 No Content
```

### Errors

| Status             | Reason                   |
| ------------------ | ------------------------ |
| `401 Unauthorized` | Missing or invalid token |
| `403 Forbidden`    | User is not admin        |
| `404 Not Found`    | User does not exist      |

---

# Admin Companies API

Base path:

```text
/api/admin/companies
```

---

## Get All Companies

```http
GET /api/admin/companies
```

Returns a paginated list of companies.

### Query Parameters

| Parameter |    Type |     Default | Description                  |
| --------- | ------: | ----------: | ---------------------------- |
| `page`    | integer |         `0` | Page number                  |
| `size`    | integer |        `20` | Number of companies per page |
| `sort`    |  string | `createdAt` | Sort field                   |

### Example Request

```http
GET /api/admin/companies?page=0&size=20&sort=createdAt
Authorization: Bearer <adminAccessToken>
```

### Success Response

```http
200 OK
```

```json
{
  "content": [
    {
      "id": 1,
      "userId": 2,
      "email": "company@cvapp.com",
      "name": "BEST Nis",
      "description": "Tech company",
      "website": "https://best.eu.org",
      "industry": "Education",
      "photoUrl": "https://example.com/company-photo.jpg",
      "createdAt": "2026-06-22T14:30:00"
    }
  ],
  "pageable": {},
  "totalElements": 1,
  "totalPages": 1,
  "size": 20,
  "number": 0
}
```

### Errors

| Status             | Reason                   |
| ------------------ | ------------------------ |
| `401 Unauthorized` | Missing or invalid token |
| `403 Forbidden`    | User is not admin        |

---

## Get Company By ID

```http
GET /api/admin/companies/{id}
```

Returns a single company by ID.

### Path Parameters

| Parameter | Type | Description |
| --------- | ---: | ----------- |
| `id`      | long | Company ID  |

### Example Request

```http
GET /api/admin/companies/1
Authorization: Bearer <adminAccessToken>
```

### Success Response

```http
200 OK
```

```json
{
  "id": 1,
  "userId": 2,
  "email": "company@cvapp.com",
  "name": "BEST Nis",
  "description": "Tech company",
  "website": "https://best.eu.org",
  "industry": "Education",
  "photoUrl": "https://example.com/company-photo.jpg",
  "createdAt": "2026-06-22T14:30:00"
}
```

### Errors

| Status             | Reason                   |
| ------------------ | ------------------------ |
| `401 Unauthorized` | Missing or invalid token |
| `403 Forbidden`    | User is not admin        |
| `404 Not Found`    | Company does not exist   |

---

## Delete Company

```http
DELETE /api/admin/companies/{id}
```

Deletes a company by ID.

### Path Parameters

| Parameter | Type | Description |
| --------- | ---: | ----------- |
| `id`      | long | Company ID  |

### Example Request

```http
DELETE /api/admin/companies/1
Authorization: Bearer <adminAccessToken>
```

### Success Response

```http
204 No Content
```

### Errors

| Status             | Reason                   |
| ------------------ | ------------------------ |
| `401 Unauthorized` | Missing or invalid token |
| `403 Forbidden`    | User is not admin        |
| `404 Not Found`    | Company does not exist   |

---

# Admin Jobs API

Base path:

```text
/api/admin/jobs
```

---

## Get All Jobs

```http
GET /api/admin/jobs
```

Returns a paginated list of jobs.

### Query Parameters

| Parameter |    Type |     Default | Description             |
| --------- | ------: | ----------: | ----------------------- |
| `page`    | integer |         `0` | Page number             |
| `size`    | integer |        `20` | Number of jobs per page |
| `sort`    |  string | `createdAt` | Sort field              |

### Example Request

```http
GET /api/admin/jobs?page=0&size=20&sort=createdAt
Authorization: Bearer <adminAccessToken>
```

### Success Response

```http
200 OK
```

```json
{
  "content": [
    {
      "id": 1,
      "companyId": 1,
      "companyName": "BEST Nis",
      "title": "Backend Intern",
      "location": "Remote",
      "employmentType": "INTERNSHIP",
      "workMode": "REMOTE",
      "deadline": "2026-07-20",
      "active": true,
      "createdAt": "2026-06-22T14:30:00"
    }
  ],
  "pageable": {},
  "totalElements": 1,
  "totalPages": 1,
  "size": 20,
  "number": 0
}
```

### Errors

| Status             | Reason                   |
| ------------------ | ------------------------ |
| `401 Unauthorized` | Missing or invalid token |
| `403 Forbidden`    | User is not admin        |

---

## Get Job By ID

```http
GET /api/admin/jobs/{id}
```

Returns a single job by ID.

### Path Parameters

| Parameter | Type | Description |
| --------- | ---: | ----------- |
| `id`      | long | Job ID      |

### Example Request

```http
GET /api/admin/jobs/1
Authorization: Bearer <adminAccessToken>
```

### Success Response

```http
200 OK
```

```json
{
  "id": 1,
  "companyId": 1,
  "companyName": "BEST Nis",
  "title": "Backend Intern",
  "location": "Remote",
  "employmentType": "INTERNSHIP",
  "workMode": "REMOTE",
  "deadline": "2026-07-20",
  "active": true,
  "createdAt": "2026-06-22T14:30:00"
}
```

### Errors

| Status             | Reason                   |
| ------------------ | ------------------------ |
| `401 Unauthorized` | Missing or invalid token |
| `403 Forbidden`    | User is not admin        |
| `404 Not Found`    | Job does not exist       |

---

## Toggle Job Active Status

```http
PATCH /api/admin/jobs/{id}/toggle
```

Activates or deactivates a job.

If the job is currently active, this endpoint makes it inactive.
If the job is currently inactive, this endpoint makes it active.

### Path Parameters

| Parameter | Type | Description |
| --------- | ---: | ----------- |
| `id`      | long | Job ID      |

### Example Request

```http
PATCH /api/admin/jobs/1/toggle
Authorization: Bearer <adminAccessToken>
```

### Success Response

```http
200 OK
```

```json
{
  "id": 1,
  "companyId": 1,
  "companyName": "BEST Nis",
  "title": "Backend Intern",
  "location": "Remote",
  "employmentType": "INTERNSHIP",
  "workMode": "REMOTE",
  "deadline": "2026-07-20",
  "active": false,
  "createdAt": "2026-06-22T14:30:00"
}
```

### Errors

| Status             | Reason                   |
| ------------------ | ------------------------ |
| `401 Unauthorized` | Missing or invalid token |
| `403 Forbidden`    | User is not admin        |
| `404 Not Found`    | Job does not exist       |

---

## Delete Job

```http
DELETE /api/admin/jobs/{id}
```

Deletes a job by ID.

### Path Parameters

| Parameter | Type | Description |
| --------- | ---: | ----------- |
| `id`      | long | Job ID      |

### Example Request

```http
DELETE /api/admin/jobs/1
Authorization: Bearer <adminAccessToken>
```

### Success Response

```http
204 No Content
```

### Errors

| Status             | Reason                   |
| ------------------ | ------------------------ |
| `401 Unauthorized` | Missing or invalid token |
| `403 Forbidden`    | User is not admin        |
| `404 Not Found`    | Job does not exist       |

---

# Admin Stats API

Base path:

```text
/api/admin
```

---

## Get Admin Stats

```http
GET /api/admin/stats
```

Returns general platform statistics.

### Example Request

```http
GET /api/admin/stats
Authorization: Bearer <adminAccessToken>
```

### Success Response

```http
200 OK
```

```json
{
  "totalUsers": 3,
  "totalCompanies": 1,
  "totalCVs": 1,
  "totalJobs": 2,
  "activeJobs": 1,
  "inactiveJobs": 1,
  "totalApplications": 0
}
```

### Errors

| Status             | Reason                   |
| ------------------ | ------------------------ |
| `401 Unauthorized` | Missing or invalid token |
| `403 Forbidden`    | User is not admin        |

---

# DTO Reference

## AdminUserResponse

```json
{
  "id": 1,
  "email": "user@cvapp.com",
  "role": "USER",
  "enabled": true,
  "provider": "LOCAL",
  "createdAt": "2026-06-22T14:30:00"
}
```

Fields:

| Field       | Type     |
| ----------- | -------- |
| `id`        | long     |
| `email`     | string   |
| `role`      | enum     |
| `enabled`   | boolean  |
| `provider`  | enum     |
| `createdAt` | datetime |

---

## ChangeRoleRequest

```json
{
  "role": "ADMIN"
}
```

Fields:

| Field  | Type |
| ------ | ---- |
| `role` | enum |

---

## AdminCompanyResponse

```json
{
  "id": 1,
  "userId": 2,
  "email": "company@cvapp.com",
  "name": "BEST Nis",
  "description": "Tech company",
  "website": "https://best.eu.org",
  "industry": "Education",
  "photoUrl": "https://example.com/company-photo.jpg",
  "createdAt": "2026-06-22T14:30:00"
}
```

Fields:

| Field         | Type     |
| ------------- | -------- |
| `id`          | long     |
| `userId`      | long     |
| `email`       | string   |
| `name`        | string   |
| `description` | string   |
| `website`     | string   |
| `industry`    | string   |
| `photoUrl`    | string   |
| `createdAt`   | datetime |

---

## AdminJobResponse

```json
{
  "id": 1,
  "companyId": 1,
  "companyName": "BEST Nis",
  "title": "Backend Intern",
  "location": "Remote",
  "employmentType": "INTERNSHIP",
  "workMode": "REMOTE",
  "deadline": "2026-07-20",
  "active": true,
  "createdAt": "2026-06-22T14:30:00"
}
```

Fields:

| Field            | Type     |
| ---------------- | -------- |
| `id`             | long     |
| `companyId`      | long     |
| `companyName`    | string   |
| `title`          | string   |
| `location`       | string   |
| `employmentType` | enum     |
| `workMode`       | enum     |
| `deadline`       | date     |
| `active`         | boolean  |
| `createdAt`      | datetime |

---

## AdminStatsResponse

```json
{
  "totalUsers": 3,
  "totalCompanies": 1,
  "totalCVs": 1,
  "totalJobs": 2,
  "activeJobs": 1,
  "inactiveJobs": 1,
  "totalApplications": 0
}
```

Fields:

| Field               | Type |
| ------------------- | ---- |
| `totalUsers`        | long |
| `totalCompanies`    | long |
| `totalCVs`          | long |
| `totalJobs`         | long |
| `activeJobs`        | long |
| `inactiveJobs`      | long |
| `totalApplications` | long |

---

# Notes

All list endpoints return a Spring `Page<T>` response.

The important field in paginated responses is:

```json
{
  "content": []
}
```

Default pagination:

```text
size = 20
sort = createdAt
```

Admin endpoints should only be accessible by users with the `ADMIN` role.
