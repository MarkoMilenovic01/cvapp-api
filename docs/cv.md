# User CV API Documentation

Base URL:

```text
/api/user/cv
```

These endpoints are intended for authenticated users with the `USER` role.

Use the access token from login or register:

```http
Authorization: Bearer <accessToken>
```

---

# CV Profile API

Base path:

```text
/api/user/cv
```

---

## Get My CV

```http
GET /api/user/cv
```

Returns the CV profile of the currently authenticated user.

### Example Request

```http
GET /api/user/cv
Authorization: Bearer <userAccessToken>
```

### Success Response

```http
200 OK
```

```json
{
  "id": 1,
  "firstName": "Marko",
  "lastName": "Milenovic",
  "phone": "+386 123 456",
  "address": "Maribor, Slovenia",
  "summary": "Backend developer with Spring Boot experience.",
  "linkedinUrl": "https://linkedin.com/in/example",
  "githubUrl": "https://github.com/example",
  "education": [],
  "experience": [],
  "skills": [],
  "photoUrl": "https://example.com/photo.jpg",
  "pdfUrl": "https://example.com/cv.pdf",
  "createdAt": "2026-06-22T14:30:00"
}
```

> Exact fields depend on your `CVResponse` DTO.

### Errors

| Status             | Reason                         |
| ------------------ | ------------------------------ |
| `401 Unauthorized` | Missing or invalid token       |
| `403 Forbidden`    | User does not have `USER` role |
| `404 Not Found`    | CV does not exist              |

---

## Create Or Update My CV

```http
PUT /api/user/cv
```

Creates the user's CV if it does not exist, or updates it if it already exists.

### Request Body

```json
{
  "firstName": "Marko",
  "lastName": "Milenovic",
  "phone": "+386 123 456",
  "address": "Maribor, Slovenia",
  "summary": "Backend developer with Spring Boot experience.",
  "linkedinUrl": "https://linkedin.com/in/example",
  "githubUrl": "https://github.com/example",
  "education": [
    {
      "institution": "University of Maribor",
      "degree": "Master's",
      "fieldOfStudy": "AI Engineering",
      "startDate": "2024-10-01",
      "endDate": "2026-09-30",
      "current": true
    }
  ],
  "experience": [
    {
      "companyName": "BEST Nis",
      "position": "Backend Developer",
      "description": "Built REST APIs using Spring Boot.",
      "startDate": "2025-01-01",
      "endDate": "2025-09-01",
      "current": false
    }
  ],
  "skills": [
    {
      "name": "Java",
      "level": "Advanced"
    }
  ]
}
```

### Success Response

```http
200 OK
```

```json
{
  "id": 1,
  "firstName": "Marko",
  "lastName": "Milenovic",
  "phone": "+386 123 456",
  "address": "Maribor, Slovenia",
  "summary": "Backend developer with Spring Boot experience.",
  "linkedinUrl": "https://linkedin.com/in/example",
  "githubUrl": "https://github.com/example",
  "education": [],
  "experience": [],
  "skills": [],
  "photoUrl": "https://example.com/photo.jpg",
  "pdfUrl": "https://example.com/cv.pdf",
  "createdAt": "2026-06-22T14:30:00"
}
```

> Exact fields depend on your `CVResponse` DTO.

### Errors

| Status             | Reason                         |
| ------------------ | ------------------------------ |
| `400 Bad Request`  | Invalid request body           |
| `401 Unauthorized` | Missing or invalid token       |
| `403 Forbidden`    | User does not have `USER` role |

---

## Delete My CV

```http
DELETE /api/user/cv
```

Deletes the CV of the currently authenticated user.

### Example Request

```http
DELETE /api/user/cv
Authorization: Bearer <userAccessToken>
```

### Success Response

```http
204 No Content
```

### Errors

| Status             | Reason                         |
| ------------------ | ------------------------------ |
| `401 Unauthorized` | Missing or invalid token       |
| `403 Forbidden`    | User does not have `USER` role |
| `404 Not Found`    | CV does not exist              |

---

# Education API

Base path:

```text
/api/user/cv/education
```

---

## Get All Education Items

```http
GET /api/user/cv/education
```

Returns all education entries for the current user's CV.

### Example Request

```http
GET /api/user/cv/education
Authorization: Bearer <userAccessToken>
```

### Success Response

```http
200 OK
```

```json
[
  {
    "id": 1,
    "institution": "University of Maribor",
    "degree": "Master's",
    "fieldOfStudy": "AI Engineering",
    "startDate": "2024-10-01",
    "endDate": "2026-09-30",
    "current": true
  }
]
```

> Exact fields depend on your `EducationResponse` DTO.

### Errors

| Status             | Reason                         |
| ------------------ | ------------------------------ |
| `401 Unauthorized` | Missing or invalid token       |
| `403 Forbidden`    | User does not have `USER` role |

---

## Add Education Item

```http
POST /api/user/cv/education
```

Adds a new education entry to the current user's CV.

### Request Body

```json
{
  "institution": "University of Maribor",
  "degree": "Master's",
  "fieldOfStudy": "AI Engineering",
  "startDate": "2024-10-01",
  "endDate": "2026-09-30",
  "current": true
}
```

### Success Response

```http
200 OK
```

```json
{
  "id": 1,
  "institution": "University of Maribor",
  "degree": "Master's",
  "fieldOfStudy": "AI Engineering",
  "startDate": "2024-10-01",
  "endDate": "2026-09-30",
  "current": true
}
```

### Errors

| Status             | Reason                         |
| ------------------ | ------------------------------ |
| `400 Bad Request`  | Invalid request body           |
| `401 Unauthorized` | Missing or invalid token       |
| `403 Forbidden`    | User does not have `USER` role |
| `404 Not Found`    | CV does not exist              |

---

## Update Education Item

```http
PUT /api/user/cv/education/{id}
```

Updates an existing education entry.

### Path Parameters

| Parameter | Type | Description        |
| --------- | ---: | ------------------ |
| `id`      | long | Education entry ID |

### Request Body

```json
{
  "institution": "University of Maribor",
  "degree": "Master's",
  "fieldOfStudy": "AI Engineering",
  "startDate": "2024-10-01",
  "endDate": "2026-09-30",
  "current": true
}
```

### Success Response

```http
200 OK
```

```json
{
  "id": 1,
  "institution": "University of Maribor",
  "degree": "Master's",
  "fieldOfStudy": "AI Engineering",
  "startDate": "2024-10-01",
  "endDate": "2026-09-30",
  "current": true
}
```

### Errors

| Status             | Reason                         |
| ------------------ | ------------------------------ |
| `400 Bad Request`  | Invalid request body           |
| `401 Unauthorized` | Missing or invalid token       |
| `403 Forbidden`    | User does not have `USER` role |
| `404 Not Found`    | Education entry does not exist |

---

## Delete Education Item

```http
DELETE /api/user/cv/education/{id}
```

Deletes an education entry.

### Path Parameters

| Parameter | Type | Description        |
| --------- | ---: | ------------------ |
| `id`      | long | Education entry ID |

### Success Response

```http
204 No Content
```

### Errors

| Status             | Reason                         |
| ------------------ | ------------------------------ |
| `401 Unauthorized` | Missing or invalid token       |
| `403 Forbidden`    | User does not have `USER` role |
| `404 Not Found`    | Education entry does not exist |

---

# Experience API

Base path:

```text
/api/user/cv/experience
```

---

## Get All Experience Items

```http
GET /api/user/cv/experience
```

Returns all experience entries for the current user's CV.

### Example Request

```http
GET /api/user/cv/experience
Authorization: Bearer <userAccessToken>
```

### Success Response

```http
200 OK
```

```json
[
  {
    "id": 1,
    "companyName": "BEST Nis",
    "position": "Backend Developer",
    "description": "Built REST APIs using Spring Boot.",
    "startDate": "2025-01-01",
    "endDate": "2025-09-01",
    "current": false
  }
]
```

> Exact fields depend on your `ExperienceResponse` DTO.

### Errors

| Status             | Reason                         |
| ------------------ | ------------------------------ |
| `401 Unauthorized` | Missing or invalid token       |
| `403 Forbidden`    | User does not have `USER` role |

---

## Add Experience Item

```http
POST /api/user/cv/experience
```

Adds a new experience entry to the current user's CV.

### Request Body

```json
{
  "companyName": "BEST Nis",
  "position": "Backend Developer",
  "description": "Built REST APIs using Spring Boot.",
  "startDate": "2025-01-01",
  "endDate": "2025-09-01",
  "current": false
}
```

### Success Response

```http
200 OK
```

```json
{
  "id": 1,
  "companyName": "BEST Nis",
  "position": "Backend Developer",
  "description": "Built REST APIs using Spring Boot.",
  "startDate": "2025-01-01",
  "endDate": "2025-09-01",
  "current": false
}
```

### Errors

| Status             | Reason                         |
| ------------------ | ------------------------------ |
| `400 Bad Request`  | Invalid request body           |
| `401 Unauthorized` | Missing or invalid token       |
| `403 Forbidden`    | User does not have `USER` role |
| `404 Not Found`    | CV does not exist              |

---

## Update Experience Item

```http
PUT /api/user/cv/experience/{id}
```

Updates an existing experience entry.

### Path Parameters

| Parameter | Type | Description         |
| --------- | ---: | ------------------- |
| `id`      | long | Experience entry ID |

### Request Body

```json
{
  "companyName": "BEST Nis",
  "position": "Backend Developer",
  "description": "Built REST APIs using Spring Boot.",
  "startDate": "2025-01-01",
  "endDate": "2025-09-01",
  "current": false
}
```

### Success Response

```http
200 OK
```

```json
{
  "id": 1,
  "companyName": "BEST Nis",
  "position": "Backend Developer",
  "description": "Built REST APIs using Spring Boot.",
  "startDate": "2025-01-01",
  "endDate": "2025-09-01",
  "current": false
}
```

### Errors

| Status             | Reason                          |
| ------------------ | ------------------------------- |
| `400 Bad Request`  | Invalid request body            |
| `401 Unauthorized` | Missing or invalid token        |
| `403 Forbidden`    | User does not have `USER` role  |
| `404 Not Found`    | Experience entry does not exist |

---

## Delete Experience Item

```http
DELETE /api/user/cv/experience/{id}
```

Deletes an experience entry.

### Path Parameters

| Parameter | Type | Description         |
| --------- | ---: | ------------------- |
| `id`      | long | Experience entry ID |

### Success Response

```http
204 No Content
```

### Errors

| Status             | Reason                          |
| ------------------ | ------------------------------- |
| `401 Unauthorized` | Missing or invalid token        |
| `403 Forbidden`    | User does not have `USER` role  |
| `404 Not Found`    | Experience entry does not exist |

---

# Skills API

Base path:

```text
/api/user/cv/skills
```

---

## Get All Skills

```http
GET /api/user/cv/skills
```

Returns all skills for the current user's CV.

### Example Request

```http
GET /api/user/cv/skills
Authorization: Bearer <userAccessToken>
```

### Success Response

```http
200 OK
```

```json
[
  {
    "id": 1,
    "name": "Java",
    "level": "Advanced"
  }
]
```

> Exact fields depend on your `SkillResponse` DTO.

### Errors

| Status             | Reason                         |
| ------------------ | ------------------------------ |
| `401 Unauthorized` | Missing or invalid token       |
| `403 Forbidden`    | User does not have `USER` role |

---

## Add Skill

```http
POST /api/user/cv/skills
```

Adds a new skill to the current user's CV.

### Request Body

```json
{
  "name": "Java",
  "level": "Advanced"
}
```

### Success Response

```http
200 OK
```

```json
{
  "id": 1,
  "name": "Java",
  "level": "Advanced"
}
```

### Errors

| Status             | Reason                         |
| ------------------ | ------------------------------ |
| `400 Bad Request`  | Invalid request body           |
| `401 Unauthorized` | Missing or invalid token       |
| `403 Forbidden`    | User does not have `USER` role |
| `404 Not Found`    | CV does not exist              |

---

## Update Skill

```http
PUT /api/user/cv/skills/{id}
```

Updates an existing skill.

### Path Parameters

| Parameter | Type | Description |
| --------- | ---: | ----------- |
| `id`      | long | Skill ID    |

### Request Body

```json
{
  "name": "Java",
  "level": "Advanced"
}
```

### Success Response

```http
200 OK
```

```json
{
  "id": 1,
  "name": "Java",
  "level": "Advanced"
}
```

### Errors

| Status             | Reason                         |
| ------------------ | ------------------------------ |
| `400 Bad Request`  | Invalid request body           |
| `401 Unauthorized` | Missing or invalid token       |
| `403 Forbidden`    | User does not have `USER` role |
| `404 Not Found`    | Skill does not exist           |

---

## Delete Skill

```http
DELETE /api/user/cv/skills/{id}
```

Deletes a skill.

### Path Parameters

| Parameter | Type | Description |
| --------- | ---: | ----------- |
| `id`      | long | Skill ID    |

### Success Response

```http
204 No Content
```

### Errors

| Status             | Reason                         |
| ------------------ | ------------------------------ |
| `401 Unauthorized` | Missing or invalid token       |
| `403 Forbidden`    | User does not have `USER` role |
| `404 Not Found`    | Skill does not exist           |

---

# CV Upload API

Base path:

```text
/api/user/cv
```

These endpoints use `multipart/form-data` for uploads.

---

## Upload CV Photo

```http
POST /api/user/cv/photo
```

Uploads or replaces the current user's CV profile photo.

### Request Body

| Field  | Type | Required | Description |
| ------ | ---- | -------: | ----------- |
| `file` | file |      yes | Image file  |

### Example Request

```http
POST /api/user/cv/photo
Authorization: Bearer <userAccessToken>
Content-Type: multipart/form-data
```

```text
file=<image-file>
```

### Example cURL

```bash
curl -X POST "http://localhost:8080/api/user/cv/photo" \
  -H "Authorization: Bearer <userAccessToken>" \
  -F "file=@photo.png"
```

### Success Response

```http
200 OK
```

```json
{
  "url": "https://example.com/photo.jpg"
}
```

> Exact fields depend on your `UploadResponse` DTO.

### Errors

| Status                       | Reason                         |
| ---------------------------- | ------------------------------ |
| `400 Bad Request`            | Missing file or invalid file   |
| `401 Unauthorized`           | Missing or invalid token       |
| `403 Forbidden`              | User does not have `USER` role |
| `413 Payload Too Large`      | File is too large              |
| `415 Unsupported Media Type` | Unsupported file type          |

---

## Delete CV Photo

```http
DELETE /api/user/cv/photo
```

Deletes the current user's CV profile photo.

### Example Request

```http
DELETE /api/user/cv/photo
Authorization: Bearer <userAccessToken>
```

### Success Response

```http
204 No Content
```

### Errors

| Status             | Reason                         |
| ------------------ | ------------------------------ |
| `401 Unauthorized` | Missing or invalid token       |
| `403 Forbidden`    | User does not have `USER` role |
| `404 Not Found`    | CV photo does not exist        |

---

## Upload CV PDF

```http
POST /api/user/cv/pdf
```

Uploads or replaces the current user's CV PDF file.

### Request Body

| Field  | Type | Required | Description |
| ------ | ---- | -------: | ----------- |
| `file` | file |      yes | PDF file    |

### Example Request

```http
POST /api/user/cv/pdf
Authorization: Bearer <userAccessToken>
Content-Type: multipart/form-data
```

```text
file=<pdf-file>
```

### Example cURL

```bash
curl -X POST "http://localhost:8080/api/user/cv/pdf" \
  -H "Authorization: Bearer <userAccessToken>" \
  -F "file=@cv.pdf"
```

### Success Response

```http
200 OK
```

```json
{
  "url": "https://example.com/cv.pdf"
}
```

> Exact fields depend on your `UploadResponse` DTO.

### Errors

| Status                       | Reason                         |
| ---------------------------- | ------------------------------ |
| `400 Bad Request`            | Missing file or invalid file   |
| `401 Unauthorized`           | Missing or invalid token       |
| `403 Forbidden`              | User does not have `USER` role |
| `413 Payload Too Large`      | File is too large              |
| `415 Unsupported Media Type` | Unsupported file type          |

---

## Delete CV PDF

```http
DELETE /api/user/cv/pdf
```

Deletes the current user's CV PDF file.

### Example Request

```http
DELETE /api/user/cv/pdf
Authorization: Bearer <userAccessToken>
```

### Success Response

```http
204 No Content
```

### Errors

| Status             | Reason                         |
| ------------------ | ------------------------------ |
| `401 Unauthorized` | Missing or invalid token       |
| `403 Forbidden`    | User does not have `USER` role |
| `404 Not Found`    | CV PDF does not exist          |

---

# DTO Reference

## CVRequest

```json
{
  "firstName": "Marko",
  "lastName": "Milenovic",
  "phone": "+386 123 456",
  "address": "Maribor, Slovenia",
  "summary": "Backend developer with Spring Boot experience.",
  "linkedinUrl": "https://linkedin.com/in/example",
  "githubUrl": "https://github.com/example",
  "education": [],
  "experience": [],
  "skills": []
}
```

| Field         | Type                         | Required |
| ------------- | ---------------------------- | -------: |
| `firstName`   | string                       |       no |
| `lastName`    | string                       |       no |
| `phone`       | string                       |       no |
| `address`     | string                       |       no |
| `summary`     | string                       |       no |
| `linkedinUrl` | string                       |       no |
| `githubUrl`   | string                       |       no |
| `education`   | array of `EducationRequest`  |       no |
| `experience`  | array of `ExperienceRequest` |       no |
| `skills`      | array of `SkillRequest`      |       no |

---

## EducationRequest

```json
{
  "institution": "University of Maribor",
  "degree": "Master's",
  "fieldOfStudy": "AI Engineering",
  "startDate": "2024-10-01",
  "endDate": "2026-09-30",
  "current": true
}
```

| Field          | Type    | Required |
| -------------- | ------- | -------: |
| `institution`  | string  |       no |
| `degree`       | string  |       no |
| `fieldOfStudy` | string  |       no |
| `startDate`    | date    |       no |
| `endDate`      | date    |       no |
| `current`      | boolean |       no |

---

## ExperienceRequest

```json
{
  "companyName": "BEST Nis",
  "position": "Backend Developer",
  "description": "Built REST APIs using Spring Boot.",
  "startDate": "2025-01-01",
  "endDate": "2025-09-01",
  "current": false
}
```

| Field         | Type    | Required |
| ------------- | ------- | -------: |
| `companyName` | string  |       no |
| `position`    | string  |       no |
| `description` | string  |       no |
| `startDate`   | date    |       no |
| `endDate`     | date    |       no |
| `current`     | boolean |       no |

---

## SkillRequest

```json
{
  "name": "Java",
  "level": "Advanced"
}
```

| Field   | Type   | Required |
| ------- | ------ | -------: |
| `name`  | string |       no |
| `level` | string |       no |

---

## UploadResponse

Example:

```json
{
  "url": "https://example.com/file.jpg"
}
```

> Exact fields depend on your `UploadResponse` DTO.

---

# Notes

The request DTOs shown here do not contain validation annotations, so fields are documented as optional unless your service layer validates them manually.

Education, experience, and skills are managed separately, but they can also be included inside `CVRequest`.

Upload endpoints use `multipart/form-data`.

Photo upload expects an image file.

PDF upload expects a PDF file.

The exact shape of `CVResponse`, `EducationResponse`, `ExperienceResponse`, `SkillResponse`, and `UploadResponse` depends on DTOs that were not included here.
