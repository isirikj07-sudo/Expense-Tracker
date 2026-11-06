# API

All routes share `/api`. Use same-origin cookies. Authenticate by registering or signing in; requests without an account session return 401. Records owned by another account return 404. JSON request/response bodies use standard ISO dates and decimal monetary amounts.

Before a POST, PUT, or DELETE, fetch `GET /api/csrf` and send its `token` value in the `X-XSRF-TOKEN` header while retaining the response cookies. A missing or invalid token returns 403.

| Method | Route | Purpose |
| --- | --- | --- |
| GET | /csrf | Get CSRF token |
| GET | /categories | List accepted categories |
| POST | /auth/register | `{name,email,password}`; returns public user |
| POST | /auth/login | `{email,password}`; returns public user |
| GET | /auth/me | Current public user |
| POST | /auth/logout | End session |
| GET | /transactions?month=2026-11 | Transactions in month, newest first |
| POST | /transactions | Create transaction |
| PUT | /transactions/{id} | Replace transaction values |
| DELETE | /transactions/{id} | Permanently delete transaction |
| GET | /budgets?month=2026-11 | Monthly category budgets |
| POST | /budgets | Upsert `{month,category,amount}` |
| DELETE | /budgets/{id} | Permanently delete budget |

Example transaction request:

```json
{"description":"Weekly groceries","amount":1250.50,"type":"EXPENSE","category":"Food & dining","date":"2026-11-06","notes":"Weekly shop"}
```

`type` is `INCOME` or `EXPENSE`. Amounts must be positive with at most two decimal places and at most 12 integer digits. Description: 1–120 characters; notes: at most 500. Categories must match `/categories`. Passwords: 8–72 characters, at most 72 UTF-8 bytes. Emails are normalized to lowercase; duplicate registration returns 409. Validation errors return 400 with a `message` field. Success returns 200, including empty bodies for delete/logout.

The frontend computes summaries and exports CSV from the current month's records. Net balance is monthly income minus monthly expenses; it is not an all-time account balance. Savings rate is `(income-expenses)/income*100`, or zero when there is no income. Chart weeks are days 1–7, 8–14, 15–21, and 22–end of month.
