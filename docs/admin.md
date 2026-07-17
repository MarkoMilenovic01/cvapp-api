# Admin API

All `/api/admin/**` endpoints require `Authorization: Bearer <adminAccessToken>` and the `ADMIN` role.

## Users

Base path: `/api/admin/users`.

| Method | Path | Description | Success |
| --- | --- | --- | --- |
| `GET` | `/api/admin/users` | Paginated users | `200 Page<AdminUserResponse>` |
| `GET` | `/api/admin/users/{id}` | User details | `200` |
| `PATCH` | `/api/admin/users/{id}/toggle` | Enable/disable | `200` |
| `PATCH` | `/api/admin/users/{id}/role` | Change role | `200` |
| `DELETE` | `/api/admin/users/{id}` | Delete account and owned data | `204` |

Role body:

```json
{"role":"ADMIN"}
```

Allowed roles are `USER`, `COMPANY` and `ADMIN`. A company profile is required for `COMPANY`; an account with a company profile cannot become `USER`. Administrators cannot disable, demote or delete themselves, and the final enabled administrator is protected. Role changes revoke refresh tokens.

User response fields: `id`, `email`, `role`, `enabled`, `provider`, `createdAt`.

## Companies

Base path: `/api/admin/companies`.

| Method | Path | Description | Success |
| --- | --- | --- | --- |
| `GET` | `/api/admin/companies` | Paginated companies | `200 Page<AdminCompanyResponse>` |
| `GET` | `/api/admin/companies/{id}` | Company details | `200` |
| `DELETE` | `/api/admin/companies/{id}` | Delete profile, owner and dependent data | `204` |

Company response fields: `id`, `userId`, `email`, `name`, `description`, `website`, `industry`, `photoUrl`, `createdAt`.

## Jobs

Base path: `/api/admin/jobs`.

| Method | Path | Description | Success |
| --- | --- | --- | --- |
| `GET` | `/api/admin/jobs` | Paginated jobs, including inactive | `200 Page<AdminJobResponse>` |
| `GET` | `/api/admin/jobs/{id}` | Job details | `200` |
| `PATCH` | `/api/admin/jobs/{id}/toggle` | Toggle active state | `200` |
| `DELETE` | `/api/admin/jobs/{id}` | Delete job and applications | `204` |

An expired job cannot be reactivated. Admin job fields: `id`, `companyId`, `companyName`, `title`, `location`, `employmentType`, `workMode`, `deadline`, `active`, `createdAt`.

## Statistics

`GET /api/admin/stats` returns:

```json
{
  "totalUsers": 100,
  "totalCompanies": 12,
  "totalCVs": 70,
  "totalJobs": 25,
  "activeJobs": 14,
  "inactiveJobs": 11,
  "totalApplications": 90
}
```

`activeJobs` counts jobs that are active and not past their deadline; expired active rows are included in `inactiveJobs`.

## Pagination and errors

List defaults are `page=0`, `size=20`, `sort=createdAt`. Maximum size is 100.

| Status | Meaning |
| --- | --- |
| `400` | Invalid input or page size above 100 |
| `401` | Missing/invalid access token |
| `403` | Authenticated user is not an administrator |
| `404` | Target does not exist |
| `409` | Unsafe self/last-admin operation or invalid role transition |
