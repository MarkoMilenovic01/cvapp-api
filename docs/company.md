# Company API Documentation

Base URL:

```text id="1xdi6c"
/api/company
```

All company endpoints require an authenticated user with the `COMPANY` role.

Use the access token from login or company invite acceptance:

```http id="k2s2bk"
Authorization: Bearer <accessToken>
```

---

# Company CV API

Base path:

```text id="laecxu"
/api/company/cvs
```

---

## Get All CVs

```http id="bl9nln"
GET /api/company/cvs
```

Returns a paginated list of CV summaries visible to the company.

### Query Parameters

Spring pagination is supported.

| Parameter |    Type |     Default | Description            |
| --------- | ------: | ----------: | ---------------------- |
| `page`    | integer |         `0` | Page number            |
| `size`    | integer |        `10` | Number of CVs per page |
| `sort`    |  string | `createdAt` | Sort field             |

### Example Request

```http id="qfwae7"
GET /api/company/cvs?page=0&size=10&sort=createdAt
Authorization: Bearer <companyAccessToken>
```

### Success Response

```http id="p5kfsg"
200 OK
```

```json id="27xesz"
{
  "content": [
    {
      "id": 1,
      "firstName": "Marko",
      "lastName": "Milenovic",
      "summary": "Backend developer with Spring Boot experience.",
      "favorite": false
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

| Status             | Reason                   |
| ------------------ | ------------------------ |
| `401 Unauthorized` | Missing or invalid token |
| `403 Forbidden`    | User is not company      |

---

## Get CV By ID

```http id="sgc0hv"
GET /api/company/cvs/{id}
```

Returns the full CV profile by ID.

### Path Parameters

| Parameter | Type | Description |
| --------- | ---: | ----------- |
| `id`      | long | CV ID       |

### Example Request

```http id="nsdy7u"
GET /api/company/cvs/1
Authorization: Bearer <companyAccessToken>
```

### Success Response

```http id="7eqgc6"
200 OK
```

```json id="dspgeo"
{
  "id": 1,
  "firstName": "Marko",
  "lastName": "Milenovic",
  "summary": "Backend developer with Spring Boot experience.",
  "location": "Maribor",
  "skills": ["Java", "Spring Boot", "PostgreSQL", "Docker"],
  "createdAt": "2026-06-22T14:30:00"
}
```

### Errors

| Status             | Reason                   |
| ------------------ | ------------------------ |
| `401 Unauthorized` | Missing or invalid token |
| `403 Forbidden`    | User is not company      |
| `404 Not Found`    | CV does not exist        |

> Exact response fields depend on your `CVResponse` DTO.

---

## Search CVs

```http id="9e6gei"
GET /api/company/cvs/search
```

Searches CVs using optional filters.

### Query Parameters

| Parameter  | Type    | Required | Description            |
| ---------- | ------- | -------: | ---------------------- |
| `keyword`  | string  |       no | General search keyword |
| `skill`    | string  |       no | Skill filter           |
| `location` | string  |       no | Location filter        |
| `page`     | integer |       no | Page number            |
| `size`     | integer |       no | Page size              |
| `sort`     | string  |       no | Sort field             |

Default pagination:

```text id="ncjk2d"
size = 10
sort = createdAt
```

### Example Request

```http id="pp3zse"
GET /api/company/cvs/search?keyword=backend&skill=Java&location=Maribor&page=0&size=10
Authorization: Bearer <companyAccessToken>
```

### Success Response

```http id="ncpc4r"
200 OK
```

```json id="4pn8dp"
{
  "content": [
    {
      "id": 1,
      "firstName": "Marko",
      "lastName": "Milenovic",
      "summary": "Backend developer with Spring Boot experience.",
      "favorite": true
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

| Status             | Reason                   |
| ------------------ | ------------------------ |
| `401 Unauthorized` | Missing or invalid token |
| `403 Forbidden`    | User is not company      |

---

# Favorite CV API

Base path:

```text id="88myrg"
/api/company
```

---

## Add CV To Favorites

```http id="k5nz5e"
POST /api/company/cvs/{id}/favorite
```

Adds a CV to the company's favorites.

### Path Parameters

| Parameter | Type | Description |
| --------- | ---: | ----------- |
| `id`      | long | CV ID       |

### Example Request

```http id="69k22l"
POST /api/company/cvs/1/favorite
Authorization: Bearer <companyAccessToken>
```

### Success Response

```http id="6n5k0h"
200 OK
```

### Errors

| Status             | Reason                   |
| ------------------ | ------------------------ |
| `401 Unauthorized` | Missing or invalid token |
| `403 Forbidden`    | User is not company      |
| `404 Not Found`    | CV does not exist        |
| `409 Conflict`     | CV is already favorited  |

---

## Remove CV From Favorites

```http id="6y1iqd"
DELETE /api/company/cvs/{id}/favorite
```

Removes a CV from the company's favorites.

### Path Parameters

| Parameter | Type | Description |
| --------- | ---: | ----------- |
| `id`      | long | CV ID       |

### Example Request

```http id="h7ekgy"
DELETE /api/company/cvs/1/favorite
Authorization: Bearer <companyAccessToken>
```

### Success Response

```http id="5d2q2z"
204 No Content
```

### Errors

| Status             | Reason                                       |
| ------------------ | -------------------------------------------- |
| `401 Unauthorized` | Missing or invalid token                     |
| `403 Forbidden`    | User is not company                          |
| `404 Not Found`    | CV does not exist or favorite does not exist |

---

## Get Favorite CVs

```http id="ywocap"
GET /api/company/favorites
```

Returns all CVs favorited by the current company.

### Example Request

```http id="wlfurm"
GET /api/company/favorites
Authorization: Bearer <companyAccessToken>
```

### Success Response

```http id="6mv8iy"
200 OK
```

```json id="6fpovv"
[
  {
    "id": 1,
    "firstName": "Marko",
    "lastName": "Milenovic",
    "summary": "Backend developer with Spring Boot experience.",
    "favorite": true
  }
]
```

### Errors

| Status             | Reason                   |
| ------------------ | ------------------------ |
| `401 Unauthorized` | Missing or invalid token |
| `403 Forbidden`    | User is not company      |

---

# CV View History API

Base path:

```text id="wd4ei0"
/api/company/history
```

---

## Get CV View History

```http id="l8nrsy"
GET /api/company/history
```

Returns the CVs viewed by the current company.

### Example Request

```http id="m667kx"
GET /api/company/history
Authorization: Bearer <companyAccessToken>
```

### Success Response

```http id="1lo9ks"
200 OK
```

```json id="ol8ncn"
[
  {
    "cvId": 1,
    "firstName": "Marko",
    "lastName": "Milenovic",
    "viewedAt": "2026-06-22T14:30:00"
  }
]
```

### Errors

| Status             | Reason                   |
| ------------------ | ------------------------ |
| `401 Unauthorized` | Missing or invalid token |
| `403 Forbidden`    | User is not company      |

---

# Company Profile API

Base path:

```text id="d5cmuw"
/api/company/me
```

---

## Get My Company Profile

```http id="3djxu2"
GET /api/company/me
```

Returns the profile of the currently authenticated company.

### Example Request

```http id="4sdgwb"
GET /api/company/me
Authorization: Bearer <companyAccessToken>
```

### Success Response

```http id="ii9m7c"
200 OK
```

```json id="7wdmbf"
{
  "id": 1,
  "name": "BEST Nis",
  "description": "Tech company focused on student opportunities.",
  "website": "https://best.eu.org",
  "industry": "Education",
  "photoUrl": "https://example.com/company-photo.jpg",
  "createdAt": "2026-06-22T14:30:00"
}
```

### Errors

| Status             | Reason                         |
| ------------------ | ------------------------------ |
| `401 Unauthorized` | Missing or invalid token       |
| `403 Forbidden`    | User is not company            |
| `404 Not Found`    | Company profile does not exist |

---

## Update My Company Profile

```http id="3zvmk8"
PUT /api/company/me
```

Updates the profile of the currently authenticated company.

### Request Body

```json id="p7j77g"
{
  "name": "BEST Nis",
  "description": "Tech company focused on student opportunities.",
  "website": "https://best.eu.org",
  "industry": "Education"
}
```

### Fields

| Field         | Type   | Required | Description         |
| ------------- | ------ | -------: | ------------------- |
| `name`        | string |       no | Company name        |
| `description` | string |       no | Company description |
| `website`     | string |       no | Company website     |
| `industry`    | string |       no | Company industry    |

### Example Request

```http id="wqgeh3"
PUT /api/company/me
Authorization: Bearer <companyAccessToken>
Content-Type: application/json
```

```json id="eydu9f"
{
  "name": "BEST Nis",
  "description": "Tech company focused on student opportunities.",
  "website": "https://best.eu.org",
  "industry": "Education"
}
```

### Success Response

```http id="8fe8y8"
200 OK
```

```json id="50oepr"
{
  "id": 1,
  "name": "BEST Nis",
  "description": "Tech company focused on student opportunities.",
  "website": "https://best.eu.org",
  "industry": "Education",
  "photoUrl": "https://example.com/company-photo.jpg",
  "createdAt": "2026-06-22T14:30:00"
}
```

### Errors

| Status             | Reason                         |
| ------------------ | ------------------------------ |
| `400 Bad Request`  | Invalid request body           |
| `401 Unauthorized` | Missing or invalid token       |
| `403 Forbidden`    | User is not company            |
| `404 Not Found`    | Company profile does not exist |

---

# Company Upload API

Base path:

```text id="r9aip2"
/api/company
```

---

## Upload Company Photo

```http id="y0nquu"
POST /api/company/photo
```

Uploads or replaces the current company's profile photo.

This endpoint expects `multipart/form-data`.

### Request Body

| Field  | Type | Required | Description          |
| ------ | ---- | -------: | -------------------- |
| `file` | file |      yes | Image file to upload |

### Example Request

```http id="1h6bkn"
POST /api/company/photo
Authorization: Bearer <companyAccessToken>
Content-Type: multipart/form-data
```

```text id="02aojc"
file=<image-file>
```

### Example cURL

```bash id="i9ew3d"
curl -X POST "http://localhost:8080/api/company/photo" \
  -H "Authorization: Bearer <companyAccessToken>" \
  -F "file=@company-photo.png"
```

### Success Response

```http id="tsw98b"
200 OK
```

```json id="0nxhep"
{
  "url": "https://example.com/company-photo.jpg"
}
```

### Errors

| Status                       | Reason                       |
| ---------------------------- | ---------------------------- |
| `400 Bad Request`            | Missing file or invalid file |
| `401 Unauthorized`           | Missing or invalid token     |
| `403 Forbidden`              | User is not company          |
| `413 Payload Too Large`      | File is too large            |
| `415 Unsupported Media Type` | Unsupported file type        |

> Exact response fields depend on your `UploadResponse` DTO.

---

## Delete Company Photo

```http id="9kwv69"
DELETE /api/company/photo
```

Deletes the current company's profile photo.

### Example Request

```http id="lx1bgf"
DELETE /api/company/photo
Authorization: Bearer <companyAccessToken>
```

### Success Response

```http id="w4a1vv"
204 No Content
```

### Errors

| Status             | Reason                       |
| ------------------ | ---------------------------- |
| `401 Unauthorized` | Missing or invalid token     |
| `403 Forbidden`    | User is not company          |
| `404 Not Found`    | Company photo does not exist |

---

# DTO Reference

## CompanyCVSummaryResponse

```json id="sgkt72"
{
  "id": 1,
  "firstName": "Marko",
  "lastName": "Milenovic",
  "summary": "Backend developer with Spring Boot experience.",
  "favorite": false
}
```

| Field       | Type    |
| ----------- | ------- |
| `id`        | long    |
| `firstName` | string  |
| `lastName`  | string  |
| `summary`   | string  |
| `favorite`  | boolean |

---

## CVSearchRequest

Used as query parameters for:

```http id="du04t7"
GET /api/company/cvs/search
```

Example:

```http id="zbebip"
GET /api/company/cvs/search?keyword=backend&skill=Java&location=Maribor
```

| Field      | Type   | Required |
| ---------- | ------ | -------: |
| `keyword`  | string |       no |
| `skill`    | string |       no |
| `location` | string |       no |

---

## CVViewResponse

```json id="8xh64n"
{
  "cvId": 1,
  "firstName": "Marko",
  "lastName": "Milenovic",
  "viewedAt": "2026-06-22T14:30:00"
}
```

| Field       | Type     |
| ----------- | -------- |
| `cvId`      | long     |
| `firstName` | string   |
| `lastName`  | string   |
| `viewedAt`  | datetime |

---

## CompanyRequest

```json id="toh0gz"
{
  "name": "BEST Nis",
  "description": "Tech company focused on student opportunities.",
  "website": "https://best.eu.org",
  "industry": "Education"
}
```

| Field         | Type   |
| ------------- | ------ |
| `name`        | string |
| `description` | string |
| `website`     | string |
| `industry`    | string |

---

## CompanyResponse

```json id="wggk5x"
{
  "id": 1,
  "name": "BEST Nis",
  "description": "Tech company focused on student opportunities.",
  "website": "https://best.eu.org",
  "industry": "Education",
  "photoUrl": "https://example.com/company-photo.jpg",
  "createdAt": "2026-06-22T14:30:00"
}
```

| Field         | Type     |
| ------------- | -------- |
| `id`          | long     |
| `name`        | string   |
| `description` | string   |
| `website`     | string   |
| `industry`    | string   |
| `photoUrl`    | string   |
| `createdAt`   | datetime |

---

## UploadResponse

Example:

```json id="fl0p01"
{
  "url": "https://example.com/company-photo.jpg"
}
```

> Exact fields depend on your `UploadResponse` record/class.

---

# Notes

All endpoints in this document require the `COMPANY` role.

CV list and CV search endpoints return a Spring `Page<T>` response.

The important field in paginated responses is:

```json id="s1vh0y"
{
  "content": []
}
```

Default CV pagination:

```text id="ex29t1"
size = 10
sort = createdAt
```

Favorites are company-specific.

CV view history is company-specific.

Company photo upload uses `multipart/form-data`.

Company profile update uses `application/json`.
