# Jobs and Applications API

Job discovery/application routes require `USER`; job management/review routes require `COMPANY`.

## Shared job model

```json
{
  "title": "Backend Intern",
  "description": "Build REST APIs",
  "requirements": "Java and Spring Boot",
  "location": "Nis",
  "employmentType": "INTERNSHIP",
  "workMode": "HYBRID",
  "deadline": "2026-12-31"
}
```

Title is required/max 255; description required/max 10,000; requirements max 10,000; location max 255. Deadline must be today or later when a job is created or updated.

Employment types: `INTERNSHIP`, `STUDENT_WORK`, `PART_TIME`, `FULL_TIME`. Work modes: `ONSITE`, `REMOTE`, `HYBRID`.

`JobResponse` fields: `id`, `companyId`, `companyName`, all request fields, `active`, `createdAt`, `updatedAt`.

## User job discovery

| Method | Path | Description | Success |
| --- | --- | --- | --- |
| `GET` | `/api/jobs` | Active, unexpired jobs | `200 Page<JobResponse>` |
| `GET` | `/api/jobs/{id}` | Active, unexpired job | `200 JobResponse` |
| `GET` | `/api/jobs/search` | Filter active jobs | `200 Page<JobResponse>` |
| `GET` | `/api/companies/{companyId}/jobs` | Active jobs for company | `200 Page<JobResponse>` |

Search parameters: `keyword` (max 200), `location` (max 255), `workMode`, `employmentType`, `companyName` (max 255), plus `page`, `size`, `sort`.

All lists default to `page=0`, `size=10`, `sort=createdAt`; maximum size is 100. Jobs with past deadlines are not visible even if their stored `active` flag is true.

## User applications

Base path: `/api/user/applications`.

| Method | Path | Description | Success |
| --- | --- | --- | --- |
| `POST` | `/api/user/applications/jobs/{jobId}/apply` | Apply with current CV | `201` |
| `GET` | `/api/user/applications` | Own applications | `200` list |
| `DELETE` | `/api/user/applications/{applicationId}` | Withdraw own application | `204` |

Applying requires an existing CV, an active/unexpired job, and no previous application by that user to that job.

Application response fields: `id`, `jobId`, `jobTitle`, `companyId`, `companyName`, `userId`, `cvId`, `cvFirstName`, `cvLastName`, `status`, `appliedAt`, `updatedAt`.

## Company job management

Base path: `/api/company/jobs`.

| Method | Path | Body | Success |
| --- | --- | --- | --- |
| `POST` | `/api/company/jobs` | `JobRequest` | `201` |
| `GET` | `/api/company/jobs` | pagination | `200 Page` |
| `GET` | `/api/company/jobs/{id}` | — | `200` |
| `PUT` | `/api/company/jobs/{id}` | `JobRequest` | `200` |
| `PATCH` | `/api/company/jobs/{id}/active` | `{"active":false}` | `200` |
| `DELETE` | `/api/company/jobs/{id}` | — | `204` |
| `GET` | `/api/company/jobs/{jobId}/applications` | — | `200` list |
| `PATCH` | `/api/company/jobs/applications/{applicationId}/status` | status body | `200` |

Companies can only access their own jobs and applications. An expired job cannot be activated.

Allowed company status body values:

```json
{"status":"SHORTLISTED"}
```

Allowed: `REVIEWED`, `SHORTLISTED`, `CONTACTED`, `REJECTED`, `ACCEPTED`. `APPLIED` is initial, and `WITHDRAWN` belongs to the user workflow.

## Errors

| Status | Meaning |
| --- | --- |
| `400` | Invalid filters/body, past deadline, page size above 100, expired activation/application |
| `401` | Missing/invalid token |
| `403` | Wrong role or company does not own resource |
| `404` | Job/application/company not found or job not publicly visible |
| `409` | Duplicate application or invalid state conflict |
