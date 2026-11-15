# Security checklist

This follows the security plan section by section. Each point is marked:

- **Built**: working in the code now, and tested on a stand-in database.
- **Ready, off**: the code is written and explained, but needs your accounts or keys before it can run.
  It has not been run against the outside service.
- **At deployment**: cannot be done in code; it is a step when the site is put on a server.
- **Not built**: still to do.

Nothing here has been run against a real Microsoft SQL Server, Safaricom or a mail server yet.

## Recommended software

| Item | State |
| --- | --- |
| Java 21, Spring Boot, SQL Server, Spring Data JPA, Microsoft JDBC driver, Spring Security | Built |
| Argon2id password hashing | Built. Old BCrypt passwords still work and are converted at the next sign-in |
| HTML, CSS, JavaScript front end | Built |
| Safaricom Daraja payments | Ready, off (test mode is on) |
| Flyway database migrations | Not built. The numbered scripts in `database/` are run by hand for now, because Flyway would have to take over a database that was created by hand. Worth adding before deployment |
| Nginx reverse proxy, Linux or cloud server | At deployment |
| Monitoring | Partly built: the activity record and `backend/logs/server.log`. Alerts are at deployment |

## Backend security

### A. Authentication

| Point | State |
| --- | --- |
| Secure registration, sign-in and sign-out | Built. Sign-out ends the user's sign-ins on every device |
| Password hashing with Argon2id and a salt per password | Built |
| Password reset | Built with one-time, 30-minute links. Sent by email once email is switched on; until then the super admin hands the link over |
| Sign-in rate limiting | Built: 20 sign-in, register and reset requests a minute per address (`RATE_LIMIT_AUTH`) |
| Temporary lockout | Built: 5 wrong passwords lock the account for 15 minutes |
| Session expiration | Built: a sign-in lasts 8 hours (`app.jwt.hours-valid`), then the user signs in again. It does not end sooner when the user is idle |
| Account verification (confirming the email or phone) | Not built. Needs email or SMS switched on first |
| Multi-factor authentication for administrators | Not built |

### B. Authorization

| Point | State |
| --- | --- |
| Roles CUSTOMER, ADMIN, INVENTORY_MANAGER, ORDER_MANAGER, SUPER_ADMIN | Built, plus SUPPORT (read-only), which already existed |
| Ownership checks on customer orders | Built. Orders, payments and notifications that belong to someone else answer "not found" |
| Managing categories | Not built. Categories are fixed as Bag and Cap |

### C. Input validation

| Point | State |
| --- | --- |
| Names, emails, phone numbers, addresses, quantities, prices, ids, amounts | Built, in the request checks, the services and database CHECK rules |
| Final price worked out on the server | Built. The browser sends only product, colour, size and quantity |

### D. SQL injection

Built. All database access goes through Spring Data JPA with parameters; no SQL is assembled from text
typed by a user. The website's database login has only the rights it needs.

### E. API security

| Point | State |
| --- | --- |
| Sign-in on protected addresses, role checks, request validation | Built |
| Rate limiting | Built, in memory. With more than one server it needs a shared store |
| Maximum request size | Built: 64 KB for JSON, 2 MB for a photo |
| Safe error messages | Built. Database errors and stack traces go to the log, never to the browser |
| CORS restrictions | Built for testing. Set `CORS_ALLOWED_ORIGINS` to the site's own address at deployment |
| CSRF protection | Built |
| Security headers | Built: Content-Security-Policy, no framing, no content sniffing, referrer policy |
| HTTPS only | At deployment. Then also set `COOKIES_SECURE=true` |

### F. M-Pesa payments

| Point | State |
| --- | --- |
| Amount taken from the order in the database | Built |
| STK push started from the backend | Ready, off (`DarajaMpesaGateway.java`); simulated in test mode |
| Checkout request id saved | Built |
| Callback received and matched to the expected request | Built, and tested with made-up callbacks |
| Amount, status and receipt verified | Built |
| Duplicate callbacks ignored | Built |
| Order updated only after verification | Built. Nothing the browser sends can mark an order paid |
| Reconciliation for missing callbacks | Built: "Check with M-Pesa" asks Safaricom directly |
| Credentials kept in environment variables, never in front-end code or logs | Built |
| Reconciling against Safaricom's merchant statements | Not built. A manual task for now |
| Refunds | Not built |

### G. Cross-site scripting

Built. Everything typed by a person is escaped before it is shown, pages carry a Content-Security-Policy
that allows scripts only from the site itself, and uploads are checked. The site does not use Thymeleaf;
the same rule is met by the `esc()` helper in `frontend/assets/js/core/ui.js`.

### H. Cross-site request forgery

Built. Every request that changes something must carry a value only this site's pages can read
(`CsrfSupport.java`), and the sign-in cookie is not sent on requests started by other websites.
The only exemption is the address Safaricom reports payments to.

### I. File uploads

| Point | State |
| --- | --- |
| File type allow-list (JPEG, PNG), judged by content | Built |
| Size limit | Built: 2 MB |
| Image content validation | Built. The picture is decoded and written out again from its pixels |
| Server-generated file names | Built |
| Storage outside the program folder | Built: `uploads/`, set with `UPLOAD_DIR` |
| No execute permission on uploads | At deployment (a setting on the server's folder) |
| Malware scanning | Not built |

### J. Session security

| Point | State |
| --- | --- |
| HttpOnly cookie | Built. The sign-in is no longer kept where page scripts can read it |
| SameSite cookie | Built: Strict |
| Secure cookie | Built, switched on with `COOKIES_SECURE=true` once the site has HTTPS |
| New sign-in value at each sign-in, invalidated at sign-out | Built |
| Expiry of inactive sessions | Partly: sign-ins end after 8 hours whether used or not |
| MFA for administrative sessions | Not built |

### K. Logging and monitoring

| Event | State |
| --- | --- |
| Sign-in, failed sign-in, lockout, sign-out | Built, in the activity record |
| Admin activity, product changes, order changes, permission changes | Built |
| Payments | Built: requested, received, failed, amount mismatch, paid twice |
| Refunds | Not built (no refunds yet) |
| Passwords, PINs, secrets and tokens kept out of logs | Built. While testing, messages to users, including reset links, are written to the server log; set `NOTIFICATIONS_LOG=false` on the live site |
| Alerts for suspicious activity | Not built. Staff are notified of payment problems inside the site only |

### L. Dependencies

| Point | State |
| --- | --- |
| Supported Java and Spring Boot releases | Built at the time of writing. Check for newer releases before deployment |
| OWASP Dependency-Check | Ready, not yet run: `mvnw -Psecurity-scan dependency-check:check` in `backend` |
| Source scanning, GitHub security alerts, patching the server and SQL Server | At deployment |

## SQL Server security

| Point | State |
| --- | --- |
| A. Not exposed to the internet, firewalled | At deployment |
| B. Separate accounts | Partly. The website uses its own limited login, not `sa`. A reporting role (`shop_readonly_role`) exists. Scripts are run by an administrator; a dedicated migration login is still to be created |
| C. Dedicated role with table-level permissions | Built: `shop_app_role` in `database/04_app_login_permissions.sql`. No delete on accounts, orders or payments; the activity record is add-only |
| D. Encrypted connections | Built for testing (`encrypt=true`). Install a real certificate and set `trustServerCertificate=false` at deployment |
| E. Encryption at rest (TDE), encrypted backups | At deployment, and depends on your SQL Server edition |
| F. SQL Server Audit | At deployment |

## Before going live

1. Set `JWT_SECRET` to a long random value and change the first super admin password.
2. Put the site behind HTTPS, then set `COOKIES_SECURE=true`, `CORS_ALLOWED_ORIGINS` and `PUBLIC_URL`.
3. Set `NOTIFICATIONS_LOG=false`.
4. Try M-Pesa in the Daraja sandbox, then switch to live keys.
5. Switch on email and send yourself a password reset.
6. Run the dependency check.
7. Do the SQL Server steps marked "At deployment" above.
