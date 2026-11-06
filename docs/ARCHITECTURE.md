# Architecture

The Spring Boot application serves both static frontend assets and JSON endpoints on one origin. Maven copies the three frontend source files into the jar at build time, so edit them in `frontend/` and rebuild or restart the Maven development process.

`ApiController` validates requests and resolves the current user from a server-side HTTP session. Every record read, update, and delete scopes its query by this user ID. Registration and login invalidate the previous session before creating a new one. BCrypt stores password hashes; public user responses omit the hash. Spring Security provides CSRF and response headers. The explicit UserDetailsService prevents generation of an unrelated default account; authentication occurs in the account endpoints.

`Models` defines users, transactions, and category budgets. Monetary values use BigDecimal and DECIMAL(14,2). Database uniqueness protects email addresses and one budget per user/month/category. Repositories use Spring Data JPA. H2 persists data locally; the mysql profile changes datasource settings without changing application code.

The frontend keeps loaded monthly data in memory, derives dashboard values and category totals, and refreshes after mutations. Text entering HTML templates is escaped; inputs are validated again on the server. Monthly requests have a version counter to ignore stale responses. CSV exports neutralize potentially executable spreadsheet cells.

The project intentionally has no remote analytics, external fonts, CDN assets, or frontend build dependency. The chart uses CSS bars. No demo records are inserted into personal accounts.

Automated tests start the real HTTP server and use independent cookie jars to verify session behavior, CSRF, data isolation, and persistence operations against an isolated H2 in-memory schema. MySQL operation must also be checked against your deployment database before hosting.
