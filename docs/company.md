# Company API

Company-management routes require a `COMPANY` token. User-facing directory routes require `USER`.

## Company profile

| Method | Path | Description | Success |
| --- | --- | --- | --- |
| `GET` | `/api/company/me` | Own profile | `200 CompanyResponse` |
| `PUT` | `/api/company/me` | Update profile | `200 CompanyResponse` |
| `POST` | `/api/company/photo` | Upload multipart image field `file` | `200 UploadResponse` |
| `DELETE` | `/api/company/photo` | Delete photo | `204` |

```json
{
  "name": "Example Ltd",
  "description": "Software company",
  "website": "https://example.com",
  "industry": "Technology"
}
```

Name is required and limited to 255, description to 5,000, website to 255 and industry to 100 characters. Website must be empty or an HTTP(S) URL. Response fields are `id`, `name`, `description`, `website`, `industry`, `photoUrl`, `createdAt`.

## CV discovery

Base path: `/api/company/cvs`.

| Method | Path | Description | Success |
| --- | --- | --- | --- |
| `GET` | `/api/company/cvs` | Paginated CV summaries | `200 Page` |
| `GET` | `/api/company/cvs/{id}` | Full CV and record unique view | `200 CVResponse` |
| `GET` | `/api/company/cvs/search` | Filtered summaries | `200 Page` |

Search parameters: `keyword` (max 200), `skill` (max 100), `location` (max 255), plus Spring `page`, `size`, `sort`. Defaults are size 10 and sort `createdAt`; maximum size is 100.

Summary fields: `id`, `firstName`, `lastName`, `summary`, `favorite`.

## Favorites and view history

| Method | Path | Success |
| --- | --- | --- |
| `POST` | `/api/company/cvs/{cvId}/favorite` | `200` |
| `DELETE` | `/api/company/cvs/{cvId}/favorite` | `204` |
| `GET` | `/api/company/favorites` | `200` summaries |
| `GET` | `/api/company/history` | `200` view history |

A company/CV favorite and company/CV view are unique. History fields are `cvId`, `firstName`, `lastName`, `viewedAt`.

## User-facing company directory

These routes require `USER`:

| Method | Path | Description |
| --- | --- | --- |
| `GET` | `/api/companies/{companyId}` | Company profile |
| `GET` | `/api/companies/{companyId}/jobs` | Company's active, unexpired jobs |

The job list uses `page`, `size`, `sort`, defaults to size 10/sort `createdAt`, and caps size at 100.

## Errors

| Status | Meaning |
| --- | --- |
| `400` | Validation, upload or page-size error |
| `401` | Missing/invalid token |
| `403` | Wrong role |
| `404` | Company or CV does not exist |
| `409` | Duplicate favorite or conflicting operation |
