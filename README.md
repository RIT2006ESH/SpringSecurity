SpringSecurity

Project Overview


Name: spring_security
Type: Spring Boot web application (Thymeleaf server-rendered UI)
Primary features: user registration & verification, role-based authentication (admin/manager/executive/customer), banking features (customers, accounts, deposits/withdrawals, transactions, loans), KYC upload and review, email and SMS notifications, file storage (S3).
Tech stack: Spring Boot 3.4.5 (parent), Java (upgrade target: 21), Maven Wrapper 3.9.16, Thymeleaf, Spring Security, JPA/Hibernate, MySQL (connector), AWS SDK (S3), Twilio SDK, Lombok.


System Architecture (high-level)

![image.png](https://eraser.imgix.net/workspaces/6x4Oh595QfwZiI8xHios/dMwocc0MeTZ2IGsV23/image_s-OF2CKQab93bPt0v6wgb.png?ixlib=js-3.8.0 "architecture diagram")

ER-Diagram

![image.png](https://eraser.imgix.net/workspaces/6x4Oh595QfwZiI8xHios/dMwocc0MeTZ2IGsV23/image_uoKSqaY0QESqdCnj5Kz2J.png?ixlib=js-3.8.0 "ER diagram")

Feature Implementation Details


Core modules and responsibilities:

controller: MVC endpoints and pages (login, register, dashboards, approval, banking APIs)
service: Business logic (UserService, EmailService, etc.)
repository: JPA repositories for persistence (UserRepository, banking repos)
security: JWT generation and Spring Security configuration
banking: banking domain (Customer, Account, Transaction, Loan, KYC)
dto: request/response DTOs for forms and API endpoints



Key implemented features and endpoints (non-exhaustive):

Authentication

GET /login (login page)
POST /login (process login, sets HTTP-only JWT cookie)



Registration & verification

GET /register, POST /register (customer)
GET /admin/register, POST /admin/register (admin)
/manager/register, /executive/register variants
GET /verify-email (verify token or prompt)



Banking REST & UI flows

Customer CRUD & account listing (CustomerController)
Account creation, deposit, withdrawal, transactions (AccountController, TransactionController)
KYC upload and review (KycApiController)



Admin flows

Account approval (AccountApprovalController)
Dashboards for roles (ManagerDashboardController, ExecutiveDashboardController)








Security Features (as of latest updates)


Password complexity enforcement aligned with CERT-In guidelines:

Minimum 8 characters with uppercase, lowercase, digit, and special character
Common/weak password blocklist
Password history (prevents reuse of last 5 passwords)
Account lockout after repeated failed login attempts
Password expiry (90-day forced reset)



RSA + AES hybrid encryption for sensitive data (Aadhaar, PAN, KYC documents)
Persistent RSA key storage (survives application restarts)
Role-based access control (RBAC) via Spring Security
JWT-based stateless authentication with HTTP-only cookies


Testing Report


Environment used for test runs:

Maven Wrapper: 3.9.16 (project .mvn/wrapper/maven-wrapper.properties)
JDKs available: 17, 21 (21 installed and verified)
Tests executed using mvnw.cmd -DskipTests=false test from project root



Test suite summary:

Test files discovered: src/test/java/net/java/spring_security/SpringSecurityApplicationTests.java (context load test).
Observations from runs:

Spring Boot context starts successfully (Spring Boot 3.4.5).
Hibernate ORM 6.6.13.Final initialized; HikariCP connected to datasource.
No unit/integration test failures observed in the contextLoads test.
Final build/test summary: local run completed; no failing tests.






Noted runtime warnings/observations to address:

"Standard Commons Logging discovery..." — project includes commons-logging; consider removing duplicates.
Hibernate warns that hibernate.dialect need not be specified; check application.properties for redundant config.
Database driver reported as undefined/unknown in logs — ensure datasource url/username/password are configured for CI or mocked in tests to avoid false positives.





Recommendations from testing


Add more unit tests for services (UserService, EmailService) and controller MVC tests using @WebMvcTest.
Add integration tests for critical flows (registration → email verification → manager approval → login) using Testcontainers to avoid relying on local DB.
CI pipeline: add GitHub Actions or Azure pipeline that runs ./mvnw -DskipTests=false test on JDK 21 to validate the Java upgrade continuously.
Add test coverage reporting (JaCoCo) and enforce minimum coverage thresholds.


Future Enhancements (prioritized)


Security

Rotate from storing JWT in a cookie to a secure SameSite cookie and refresh tokens with CSRF protections for stateful flows.
Harden password policy, add account lockout after failed attempts — ✅ Implemented.
Add 2FA.



Reliability & Observability

Add structured logging (logback config), centralize metrics (Micrometer/Prometheus), and error tracing (Sentry/OpenTelemetry).
Add health checks and readiness/liveness endpoints.



Developer Experience

Add Dockerfile(s) and docker-compose for local dev (MySQL, app) and test containers for CI tests.
Provide README.md with quickstart, environment variables, and setup guide.



APIs & UX

Add REST API versioning and OpenAPI/Swagger docs for banking APIs.
Convert key flows to REST + single-page frontend (optional) for better mobile support.



Data & Compliance

Implement data retention and secure storage for KYC documents (encryption) — ✅ Implemented (RSA+AES hybrid encryption).
Add audit logging for account approvals, KYC decisions, and sensitive actions.
Migrate local file storage to S3 for KYC documents (in progress).