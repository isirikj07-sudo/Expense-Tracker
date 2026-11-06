# Ledger · Expense Tracker

A complete personal expense tracker built with Java 21, Spring Boot 4.1.2, Spring Security, Spring Data JPA, and a responsive vanilla JavaScript frontend. Amounts are displayed in Indian rupees (INR).

## Start locally

Install JDK 21 and double-click `start.cmd`, or run from this project folder in PowerShell:

```powershell
cd backend
.\mvnw.cmd spring-boot:run
```

Open http://localhost:8081 and select **Create an account**. No database installation, Node.js, or frontend development server is required. The Maven build includes `frontend/index.html`, `app.js`, and `styles.css` in the application. If Maven is already installed, `mvn -f backend/pom.xml spring-boot:run` also works from the project root.

The default H2 database is stored in `data/expenses.mv.db` relative to the process working directory. Always start from the same directory to reuse your data. Stop the application before backing up this file. Accounts and transactions survive application restarts; sessions require signing in again after restarting.

## Features

- Registration, sign-in, sign-out, hashed passwords, and account-specific records.
- Income and expense creation, editing, deletion, notes, dates, and 12 categories.
- Monthly net balance, income, expenses, savings rate, weekly cash flow, and category spending.
- Search and filter by transaction type or category.
- Monthly category budgets with progress and overspending indicators. Saving the same category/month updates its limit.
- CSV export of the current month's filtered results, with formula-injection protection.
- Responsive layouts, accessible form labels, keyboard-friendly native dialogs, and empty states.
- Server-side validation, CSRF protection, session rotation on login, HttpOnly session cookies, and a Content Security Policy.

## Test and package

```powershell
mvn -f backend/pom.xml test
mvn -f backend/pom.xml package
cd backend
java -jar target/backend-0.0.1-SNAPSHOT.jar
```

Six HTTP integration tests cover login/logout, transaction CRUD and month filtering, account isolation, budget upserts/deletion, invalid input and CSRF protection, and static frontend serving. Tests use an isolated in-memory database.

## Use MySQL

Create a database using `database/setup.sql`, then run:

```powershell
cd backend
$env:SPRING_PROFILES_ACTIVE = "mysql"
$env:DB_URL = "jdbc:mysql://localhost:3306/expense_tracker"
$env:DB_USERNAME = "expense_app"
$env:DB_PASSWORD = "your-database-password"
.\mvnw.cmd spring-boot:run
```

The MySQL user must have permission to create and alter tables in this database. JPA creates the schema on startup. Keep credentials in environment variables. The MySQL profile is provided but requires a running MySQL instance to validate locally.

## Configuration and deployment

`PORT` defaults to 8081; `SERVER_ADDRESS` defaults to 127.0.0.1 for local use. Set `SERVER_ADDRESS=0.0.0.0` only when hosting intentionally. Serve the UI and API from the same origin. For an HTTPS deployment, set `SERVER_SERVLET_SESSION_COOKIE_SECURE=true`, terminate TLS at your reverse proxy, provision a database backup policy, and replace automatic schema updates with managed migrations before evolving a production database. No password reset or email delivery service is configured.

See `docs/API.md` for the API contract and `docs/ARCHITECTURE.md` for implementation details.
