# Jobs API Documentation

This document covers job listings, job search, user applications, company job management, and application review.

---

# Job Roles

The job system has two main access levels:

| Role      | Description                                                                                          |
| --------- | ---------------------------------------------------------------------------------------------------- |
| `USER`    | Can browse active jobs, search jobs, apply to jobs, view own applications, and withdraw applications |
| `COMPANY` | Can create, update, delete, and review applications for its own jobs                                 |

Use JWT authentication:

```http id="c3vsg1"
Authorization: Bearer <accessToken>
```

---

# Job Listing API

Base path:

```text id="ji0q9n"
/api/jobs
```

These endpoints require the `USER` role.

---

## Get All Active Jobs

```http id="91qvfx"
GET /api/jobs
```

Returns a paginated list of active jobs.

### Query Parameters

Spring pagination is supported.

| Parameter |    Type |     Default | Description             |
| --------- | ------: | ----------: | ----------------------- |
| `page`    | integer |         `0` | Page number             |
| `size`    | integer |        `10` | Number of jobs per page |
| `sort`    |  string | `createdAt` | Sort field              |

### Example Request

```http id="axwt83"
GET /api/jobs?page=0&size=10&sort=createdAt
Authorization: Bearer <userAccessToken>
```

### Success Response

```http id="pkzrx9"
200 OK
```

```json id="dc5nkt"
{
  "content": [
    {
      "id": 1,
      "companyId": 1,
      "companyName": "BEST Nis",
      "title": "Backend Intern",
      "description": "Build REST APIs with Spring Boot.",
      "requirements": "Java, Spring Boot, PostgreSQL",
      "location": "Maribor",
      "employmentType": "INTERNSHIP",
      "workMode": "HYBRID",
      "deadline": "2026-07-20",
      "active": true,
      "createdAt": "2026-06-22T14:30:00",
      "updatedAt": "2026-06-22T14:30:00"
    }
  ],
  "pageable": {},
  "totalElements": 1,
  "totalPages": 1,
  "size": 10,
  "number": 0
}
```

### Errors

| Status             | Reason                         |
| ------------------ | ------------------------------ |
| `401 Unauthorized` | Missing or invalid token       |
| `403 Forbidden`    | User does not have `USER` role |

---

## Get Active Job By ID

```http id="bwa3ou"
GET /api/jobs/{id}
```

Returns a single active job by ID.

### Path Parameters

| Parameter | Type | Description |
| --------- | ---: | ----------- |
| `id`      | long | Job ID      |

### Example Request

```http id="hou2d4"
GET /api/jobs/1
Authorization: Bearer <userAccessToken>
```

### Success Response

```http id="fczyn2"
200 OK
```

```json id="ca11o1"
{
  "id": 1,
  "companyId": 1,
  "companyName": "BEST Nis",
  "title": "Backend Intern",
  "description": "Build REST APIs with Spring Boot.",
  "requirements": "Java, Spring Boot, PostgreSQL",
  "location": "Maribor",
  "employmentType": "INTERNSHIP",
  "workMode": "HYBRID",
  "deadline": "2026-07-20",
  "active": true,
  "createdAt": "2026-06-22T14:30:00",
  "updatedAt": "2026-06-22T14:30:00"
}
```

### Errors

| Status             | Reason                              |
| ------------------ | ----------------------------------- |
| `401 Unauthorized` | Missing or invalid token            |
| `403 Forbidden`    | User does not have `USER` role      |
| `404 Not Found`    | Job does not exist or is not active |

---

# Job Search API

Base path:

```text id="u05lbp"
/api/jobs
```

These endpoints require the `USER` role.

---

## Search Jobs

```http id="eh7v4b"
GET /api/jobs/search
```

Searches active jobs using optional filters.

### Query Parameters

| Parameter        | Type    | Required | Description               |
| ---------------- | ------- | -------: | ------------------------- |
| `keyword`        | string  |       no | Search by general keyword |
| `location`       | string  |       no | Filter by location        |
| `workMode`       | enum    |       no | Filter by work mode       |
| `employmentType` | enum    |       no | Filter by employment type |
| `companyName`    | string  |       no | Filter by company name    |
| `page`           | integer |       no | Page number               |
| `size`           | integer |       no | Page size                 |
| `sort`           | string  |       no | Sort field                |

Default pagination:

```text id="swxn0f"
size = 10
sort = createdAt
```

### Available Work Modes

```text id="i9l2cy"
ONSITE
REMOTE
HYBRID
```

### Available Employment Types

```text id="dt1rfz"
INTERNSHIP
STUDENT_WORK
PART_TIME
FULL_TIME
```

### Example Request

```http id="u7mv2p"
GET /api/jobs/search?keyword=backend&location=Maribor&workMode=HYBRID&employmentType=INTERNSHIP&companyName=BEST&page=0&size=10
Authorization: Bearer <userAccessToken>
```

### Success Response

```http id="bybr3o"
200 OK
```

```json id="x3ps3h"
{
  "content": [
    {
      "id": 1,
      "companyId": 1,
      "companyName": "BEST Nis",
      "title": "Backend Intern",
      "description": "Build REST APIs with Spring Boot.",
      "requirements": "Java, Spring Boot, PostgreSQL",
      "location": "Maribor",
      "employmentType": "INTERNSHIP",
      "workMode": "HYBRID",
      "deadline": "2026-07-20",
      "active": true,
      "createdAt": "2026-06-22T14:30:00",
      "updatedAt": "2026-06-22T14:30:00"
    }
  ],
  "pageable": {},
  "totalElements": 1,
  "totalPages": 1,
  "size": 10,
  "number": 0
}
```

### Errors

| Status             | Reason                         |
| ------------------ | ------------------------------ |
| `400 Bad Request`  | Invalid enum value             |
| `401 Unauthorized` | Missing or invalid token       |
| `403 Forbidden`    | User does not have `USER` role |

---

# User Job Applications API

Base path:

```text id="xz7rw8"
/api/user/applications
```

These endpoints require the `USER` role.

---

## Apply To Job

```http id="qx58dn"
POST /api/user/applications/jobs/{jobId}/apply
```

Applies the current user to a job.

### Path Parameters

| Parameter | Type | Description |
| --------- | ---: | ----------- |
| `jobId`   | long | Job ID      |

### Example Request

```http id="8ib7lw"
POST /api/user/applications/jobs/1/apply
Authorization: Bearer <userAccessToken>
```

### Success Response

```http id="y4oj7c"
201 Created
```

```json id="g0kt5u"
{
  "id": 1,
  "jobId": 1,
  "jobTitle": "Backend Intern",
  "companyId": 1,
  "companyName": "BEST Nis",
  "userId": 2,
  "cvId": 1,
  "cvFirstName": "Marko",
  "cvLastName": "Milenovic",
  "status": "APPLIED",
  "appliedAt": "2026-06-22T14:30:00",
  "updatedAt": "2026-06-22T14:30:00"
}
```

### Errors

| Status             | Reason                           |
| ------------------ | -------------------------------- |
| `400 Bad Request`  | User cannot apply                |
| `401 Unauthorized` | Missing or invalid token         |
| `403 Forbidden`    | User does not have `USER` role   |
| `404 Not Found`    | Job or CV does not exist         |
| `409 Conflict`     | User already applied to this job |

---

## Get My Applications

```http id="umkgx2"
GET /api/user/applications
```

Returns all applications submitted by the current user.

### Example Request

```http id="k5idsy"
GET /api/user/applications
Authorization: Bearer <userAccessToken>
```

### Success Response

```http id="zvu8kk"
200 OK
```

```json id="5vlvba"
[
  {
    "id": 1,
    "jobId": 1,
    "jobTitle": "Backend Intern",
    "companyId": 1,
    "companyName": "BEST Nis",
    "userId": 2,
    "cvId": 1,
    "cvFirstName": "Marko",
    "cvLastName": "Milenovic",
    "status": "APPLIED",
    "appliedAt": "2026-06-22T14:30:00",
    "updatedAt": "2026-06-22T14:30:00"
  }
]
```

### Errors

| Status             | Reason                         |
| ------------------ | ------------------------------ |
| `401 Unauthorized` | Missing or invalid token       |
| `403 Forbidden`    | User does not have `USER` role |

---

## Withdraw Application

```http id="vbmxp0"
DELETE /api/user/applications/{applicationId}
```

Withdraws or deletes an application submitted by the current user.

### Path Parameters

| Parameter       | Type | Description    |
| --------------- | ---: | -------------- |
| `applicationId` | long | Application ID |

### Example Request

```http id="vn6mjr"
DELETE /api/user/applications/1
Authorization: Bearer <userAccessToken>
```

### Success Response

```http id="4snrq5"
204 No Content
```

### Errors

| Status             | Reason                         |
| ------------------ | ------------------------------ |
| `401 Unauthorized` | Missing or invalid token       |
| `403 Forbidden`    | User does not have `USER` role |
| `404 Not Found`    | Application does not exist     |

---

# Company Job Management API

Base path:

```text id="lc6g0c"
/api/company/jobs
```

These endpoints require the `COMPANY` role.

---

## Create Job

```http id="fza44s"
POST /api/company/jobs
```

Creates a new job for the current company.

### Request Body

```json id="514xzt"
{
  "title": "Backend Intern",
  "description": "Build REST APIs with Spring Boot.",
  "requirements": "Java, Spring Boot, PostgreSQL",
  "location": "Maribor",
  "employmentType": "INTERNSHIP",
  "workMode": "HYBRID",
  "deadline": "2026-07-20"
}
```

### Success Response

```http id="ua1rlv"
201 Created
```

```json id="qf43qs"
{
  "id": 1,
  "companyId": 1,
  "companyName": "BEST Nis",
  "title": "Backend Intern",
  "description": "Build REST APIs with Spring Boot.",
  "requirements": "Java, Spring Boot, PostgreSQL",
  "location": "Maribor",
  "employmentType": "INTERNSHIP",
  "workMode": "HYBRID",
  "deadline": "2026-07-20",
  "active": true,
  "createdAt": "2026-06-22T14:30:00",
  "updatedAt": "2026-06-22T14:30:00"
}
```

### Errors

| Status             | Reason                            |
| ------------------ | --------------------------------- |
| `400 Bad Request`  | Invalid request body              |
| `401 Unauthorized` | Missing or invalid token          |
| `403 Forbidden`    | User does not have `COMPANY` role |

---

## Get My Jobs

```http id="pkrf8j"
GET /api/company/jobs
```

Returns a paginated list of jobs created by the current company.

### Query Parameters

| Parameter |    Type |     Default | Description             |
| --------- | ------: | ----------: | ----------------------- |
| `page`    | integer |         `0` | Page number             |
| `size`    | integer |        `10` | Number of jobs per page |
| `sort`    |  string | `createdAt` | Sort field              |

### Example Request

```http id="rvtmxy"
GET /api/company/jobs?page=0&size=10&sort=createdAt
Authorization: Bearer <companyAccessToken>
```

### Success Response

```http id="f743wi"
200 OK
```

```json id="52fcfg"
{
  "content": [
    {
      "id": 1,
      "companyId": 1,
      "companyName": "BEST Nis",
      "title": "Backend Intern",
      "description": "Build REST APIs with Spring Boot.",
      "requirements": "Java, Spring Boot, PostgreSQL",
      "location": "Maribor",
      "employmentType": "INTERNSHIP",
      "workMode": "HYBRID",
      "deadline": "2026-07-20",
      "active": true,
      "createdAt": "2026-06-22T14:30:00",
      "updatedAt": "2026-06-22T14:30:00"
    }
  ],
  "pageable": {},
  "totalElements": 1,
  "totalPages": 1,
  "size": 10,
  "number": 0
}
```

### Errors

| Status             | Reason                            |
| ------------------ | --------------------------------- |
| `401 Unauthorized` | Missing or invalid token          |
| `403 Forbidden`    | User does not have `COMPANY` role |

---

## Get My Job By ID

```http id="mjv5zt"
GET /api/company/jobs/{id}
```

Returns one job created by the current company.

### Path Parameters

| Parameter | Type | Description |
| --------- | ---: | ----------- |
| `id`      | long | Job ID      |

### Example Request

```http id="tqr1br"
GET /api/company/jobs/1
Authorization: Bearer <companyAccessToken>
```

### Success Response

```http id="xjmr04"
200 OK
```

```json id="l16uvi"
{
  "id": 1,
  "companyId": 1,
  "companyName": "BEST Nis",
  "title": "Backend Intern",
  "description": "Build REST APIs with Spring Boot.",
  "requirements": "Java, Spring Boot, PostgreSQL",
  "location": "Maribor",
  "employmentType": "INTERNSHIP",
  "workMode": "HYBRID",
  "deadline": "2026-07-20",
  "active": true,
  "createdAt": "2026-06-22T14:30:00",
  "updatedAt": "2026-06-22T14:30:00"
}
```

### Errors

| Status             | Reason                                                |
| ------------------ | ----------------------------------------------------- |
| `401 Unauthorized` | Missing or invalid token                              |
| `403 Forbidden`    | User does not have `COMPANY` role                     |
| `404 Not Found`    | Job does not exist or does not belong to this company |

---

## Update Job

```http id="x83pzw"
PUT /api/company/jobs/{id}
```

Updates one job created by the current company.

### Path Parameters

| Parameter | Type | Description |
| --------- | ---: | ----------- |
| `id`      | long | Job ID      |

### Request Body

```json id="vrfykv"
{
  "title": "Backend Developer Intern",
  "description": "Build and maintain REST APIs with Spring Boot.",
  "requirements": "Java, Spring Boot, PostgreSQL, Docker",
  "location": "Maribor",
  "employmentType": "INTERNSHIP",
  "workMode": "HYBRID",
  "deadline": "2026-08-01"
}
```

### Success Response

```http id="m295vx"
200 OK
```

```json id="8o6xf3"
{
  "id": 1,
  "companyId": 1,
  "companyName": "BEST Nis",
  "title": "Backend Developer Intern",
  "description": "Build and maintain REST APIs with Spring Boot.",
  "requirements": "Java, Spring Boot, PostgreSQL, Docker",
  "location": "Maribor",
  "employmentType": "INTERNSHIP",
  "workMode": "HYBRID",
  "deadline": "2026-08-01",
  "active": true,
  "createdAt": "2026-06-22T14:30:00",
  "updatedAt": "2026-06-22T15:00:00"
}
```

### Errors

| Status             | Reason                                                |
| ------------------ | ----------------------------------------------------- |
| `400 Bad Request`  | Invalid request body                                  |
| `401 Unauthorized` | Missing or invalid token                              |
| `403 Forbidden`    | User does not have `COMPANY` role                     |
| `404 Not Found`    | Job does not exist or does not belong to this company |

---

## Delete Job

```http id="pjufm5"
DELETE /api/company/jobs/{id}
```

Deletes one job created by the current company.

### Path Parameters

| Parameter | Type | Description |
| --------- | ---: | ----------- |
| `id`      | long | Job ID      |

### Example Request

```http id="h9w9ej"
DELETE /api/company/jobs/1
Authorization: Bearer <companyAccessToken>
```

### Success Response

```http id="ky03e3"
204 No Content
```

### Errors

| Status             | Reason                                                |
| ------------------ | ----------------------------------------------------- |
| `401 Unauthorized` | Missing or invalid token                              |
| `403 Forbidden`    | User does not have `COMPANY` role                     |
| `404 Not Found`    | Job does not exist or does not belong to this company |

---

# Company Application Review API

Base path:

```text id="xgqoot"
/api/company/jobs
```

These endpoints require the `COMPANY` role.

---

## Get Applications For Job

```http id="z5bch1"
GET /api/company/jobs/{jobId}/applications
```

Returns all applications for one job owned by the current company.

### Path Parameters

| Parameter | Type | Description |
| --------- | ---: | ----------- |
| `jobId`   | long | Job ID      |

### Example Request

```http id="lpv60o"
GET /api/company/jobs/1/applications
Authorization: Bearer <companyAccessToken>
```

### Success Response

```http id="mljdvd"
200 OK
```

```json id="f0yhb1"
[
  {
    "id": 1,
    "jobId": 1,
    "jobTitle": "Backend Intern",
    "companyId": 1,
    "companyName": "BEST Nis",
    "userId": 2,
    "cvId": 1,
    "cvFirstName": "Marko",
    "cvLastName": "Milenovic",
    "status": "APPLIED",
    "appliedAt": "2026-06-22T14:30:00",
    "updatedAt": "2026-06-22T14:30:00"
  }
]
```

### Errors

| Status             | Reason                                                |
| ------------------ | ----------------------------------------------------- |
| `401 Unauthorized` | Missing or invalid token                              |
| `403 Forbidden`    | User does not have `COMPANY` role                     |
| `404 Not Found`    | Job does not exist or does not belong to this company |

---

## Update Application Status

```http id="yv22sc"
PATCH /api/company/jobs/applications/{applicationId}/status
```

Updates the status of a job application.

### Path Parameters

| Parameter       | Type | Description    |
| --------------- | ---: | -------------- |
| `applicationId` | long | Application ID |

### Request Body

```json id="wasdnj"
{
  "status": "SHORTLISTED"
}
```

### Available Statuses

```text id="s3b83d"
APPLIED
REVIEWED
SHORTLISTED
CONTACTED
REJECTED
ACCEPTED
WITHDRAWN
```

### Example Request

```http id="22qxcw"
PATCH /api/company/jobs/applications/1/status
Authorization: Bearer <companyAccessToken>
Content-Type: application/json
```

```json id="d65pu7"
{
  "status": "SHORTLISTED"
}
```

### Success Response

```http id="59f81v"
200 OK
```

```json id="9d180j"
{
  "id": 1,
  "jobId": 1,
  "jobTitle": "Backend Intern",
  "companyId": 1,
  "companyName": "BEST Nis",
  "userId": 2,
  "cvId": 1,
  "cvFirstName": "Marko",
  "cvLastName": "Milenovic",
  "status": "SHORTLISTED",
  "appliedAt": "2026-06-22T14:30:00",
  "updatedAt": "2026-06-22T15:00:00"
}
```

### Errors

| Status             | Reason                                                        |
| ------------------ | ------------------------------------------------------------- |
| `400 Bad Request`  | Invalid application status                                    |
| `401 Unauthorized` | Missing or invalid token                                      |
| `403 Forbidden`    | User does not have `COMPANY` role                             |
| `404 Not Found`    | Application does not exist or does not belong to this company |

---

# DTO Reference

## JobRequest

```json id="hkdba3"
{
  "title": "Backend Intern",
  "description": "Build REST APIs with Spring Boot.",
  "requirements": "Java, Spring Boot, PostgreSQL",
  "location": "Maribor",
  "employmentType": "INTERNSHIP",
  "workMode": "HYBRID",
  "deadline": "2026-07-20"
}
```

| Field            | Type   | Required |
| ---------------- | ------ | -------: |
| `title`          | string |       no |
| `description`    | string |       no |
| `requirements`   | string |       no |
| `location`       | string |       no |
| `employmentType` | enum   |       no |
| `workMode`       | enum   |       no |
| `deadline`       | date   |       no |

---

## JobResponse

```json id="j3iiwu"
{
  "id": 1,
  "companyId": 1,
  "companyName": "BEST Nis",
  "title": "Backend Intern",
  "description": "Build REST APIs with Spring Boot.",
  "requirements": "Java, Spring Boot, PostgreSQL",
  "location": "Maribor",
  "employmentType": "INTERNSHIP",
  "workMode": "HYBRID",
  "deadline": "2026-07-20",
  "active": true,
  "createdAt": "2026-06-22T14:30:00",
  "updatedAt": "2026-06-22T14:30:00"
}
```

| Field            | Type     |
| ---------------- | -------- |
| `id`             | long     |
| `companyId`      | long     |
| `companyName`    | string   |
| `title`          | string   |
| `description`    | string   |
| `requirements`   | string   |
| `location`       | string   |
| `employmentType` | enum     |
| `workMode`       | enum     |
| `deadline`       | date     |
| `active`         | boolean  |
| `createdAt`      | datetime |
| `updatedAt`      | datetime |

---

## JobSearchFilter

Used as query parameters for:

```http id="t9w7nn"
GET /api/jobs/search
```

Example:

```http id="ymxkhj"
GET /api/jobs/search?keyword=backend&location=Maribor&workMode=HYBRID&employmentType=INTERNSHIP&companyName=BEST
```

| Field            | Type   | Required |
| ---------------- | ------ | -------: |
| `keyword`        | string |       no |
| `location`       | string |       no |
| `workMode`       | enum   |       no |
| `employmentType` | enum   |       no |
| `companyName`    | string |       no |

---

## JobApplicationResponse

```json id="yucm0t"
{
  "id": 1,
  "jobId": 1,
  "jobTitle": "Backend Intern",
  "companyId": 1,
  "companyName": "BEST Nis",
  "userId": 2,
  "cvId": 1,
  "cvFirstName": "Marko",
  "cvLastName": "Milenovic",
  "status": "APPLIED",
  "appliedAt": "2026-06-22T14:30:00",
  "updatedAt": "2026-06-22T14:30:00"
}
```

| Field         | Type     |
| ------------- | -------- |
| `id`          | long     |
| `jobId`       | long     |
| `jobTitle`    | string   |
| `companyId`   | long     |
| `companyName` | string   |
| `userId`      | long     |
| `cvId`        | long     |
| `cvFirstName` | string   |
| `cvLastName`  | string   |
| `status`      | enum     |
| `appliedAt`   | datetime |
| `updatedAt`   | datetime |

---

## UpdateApplicationStatusRequest

```json id="ae3hxf"
{
  "status": "REVIEWED"
}
```

| Field    | Type | Required |
| -------- | ---- | -------: |
| `status` | enum |       no |

---

# Enums

## ApplicationStatus

```text id="snjn5q"
APPLIED
REVIEWED
SHORTLISTED
CONTACTED
REJECTED
ACCEPTED
WITHDRAWN
```

## EmploymentType

```text id="nlbr99"
INTERNSHIP
STUDENT_WORK
PART_TIME
FULL_TIME
```

## WorkMode

```text id="r06fli"
ONSITE
REMOTE
HYBRID
```

---

# Notes

The request DTOs shown here do not contain validation annotations, so fields are documented as optional unless your service layer validates them manually.

Job list and search endpoints return a Spring `Page<JobResponse>` response.

The important field in paginated responses is:

```json id="q2dp3n"
{
  "content": []
}
```

Default pagination for job lists:

```text id="ze67u7"
size = 10
sort = createdAt
```

`GET /api/jobs` and `GET /api/jobs/{id}` return only active jobs.

`/api/company/jobs` endpoints operate only on jobs belonging to the authenticated company.

`/api/user/applications` endpoints operate only on applications belonging to the authenticated user.
