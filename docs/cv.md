# User CV API

All routes require a `USER` access token. Base profile path: `/api/user/cv`.

## Profile

| Method | Path | Description | Success |
| --- | --- | --- | --- |
| `GET` | `/api/user/cv` | Current user's CV | `200 CVResponse` |
| `PUT` | `/api/user/cv` | Create or update profile fields | `200 CVResponse` |
| `DELETE` | `/api/user/cv` | Delete CV and owned sections/files | `204` |

Profile request:

```json
{
  "firstName": "Marko",
  "lastName": "Milenovic",
  "phone": "+381600000000",
  "address": "Nis, Serbia",
  "summary": "Backend developer",
  "linkedinUrl": "https://linkedin.com/in/example",
  "githubUrl": "https://github.com/example"
}
```

Limits: names 100, phone 20, address/URLs 255 and summary 5,000 characters. `CVResponse` additionally contains `education`, `experience`, `projects`, `skills`, `createdAt`, `profilePhotoUrl` and `pdfUrl`.

Sections are managed through their own endpoints; they are not part of `CVRequest`.

## Education

Base path: `/api/user/cv/education`.

| Method | Path | Success |
| --- | --- | --- |
| `GET` | base | `200` list |
| `POST` | base | `201` created item |
| `PUT` | `/{id}` | `200` updated item |
| `DELETE` | `/{id}` | `204` |

```json
{
  "institution": "University of Nis",
  "degree": "BSc",
  "fieldOfStudy": "Computer Science",
  "startDate": "2022-10-01",
  "endDate": null,
  "current": true
}
```

Institution is required; institution, degree and field are limited to 255 characters. Dates must be consistent, and a current entry must not have an end date.

## Experience

Base path: `/api/user/cv/experience`; methods and success codes match Education.

```json
{
  "companyName": "Example Ltd",
  "position": "Backend Intern",
  "experienceType": "INTERNSHIP",
  "description": "Built REST APIs",
  "startDate": "2025-01-01",
  "endDate": "2025-06-30",
  "current": false
}
```

`companyName`, `position` and `experienceType` are required. Names are limited to 255 and description to 5,000 characters.

Experience types: `FULL_TIME`, `PART_TIME`, `INTERNSHIP`, `STUDENT_WORK`, `VOLUNTEER`, `FREELANCE`, `CONTRACT`.

## Projects

Base path: `/api/user/cv/projects`; methods and success codes match Education.

```json
{
  "name": "CVApp",
  "description": "Recruitment platform",
  "projectUrl": "https://example.com",
  "repositoryUrl": "https://github.com/example/cvapp",
  "startDate": "2025-01-01",
  "endDate": null,
  "current": true
}
```

Name is required and limited to 255, description to 5,000, and project/repository URLs to 500 characters.

## Skills

Base path: `/api/user/cv/skills`; methods and success codes match Education.

```json
{"name":"SPRING_BOOT","level":"ADVANCED"}
```

Levels: `BEGINNER`, `INTERMEDIATE`, `ADVANCED`.

Skill names: `JAVA`, `SPRING_BOOT`, `POSTGRESQL`, `DOCKER`, `GIT`, `REACT`, `TYPESCRIPT`, `JAVASCRIPT`, `HTML`, `CSS`, `PYTHON`, `MACHINE_LEARNING`, `TENSORFLOW`, `PANDAS`, `SQL`, `NODE_JS`, `EXPRESS`, `AWS`, `FIGMA`, `MONGODB`.

A CV cannot contain the same skill name twice.

## Uploads

| Method | Path | Content | Success |
| --- | --- | --- | --- |
| `POST` | `/api/user/cv/photo` | multipart field `file`, image | `200 UploadResponse` |
| `DELETE` | `/api/user/cv/photo` | — | `204` |
| `POST` | `/api/user/cv/pdf` | multipart field `file`, PDF | `200 UploadResponse` |
| `DELETE` | `/api/user/cv/pdf` | — | `204` |

Example:

```bash
curl -X POST "$API/api/user/cv/photo" \
  -H "Authorization: Bearer $TOKEN" \
  -F 'file=@profile.jpg'
```

Uploads are stored in Cloudinary. Invalid type, empty file and excessive size return `400`.

## Errors

| Status | Meaning |
| --- | --- |
| `400` | Validation/upload error or inconsistent dates |
| `401` | Missing/invalid token |
| `403` | Wrong role or attempted access to another user's section |
| `404` | CV or section does not exist |
| `409` | Duplicate skill |
