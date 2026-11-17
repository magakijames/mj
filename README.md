# Snap & Pack

An online store for bags and caps, in three parts:

```
frontend/   The website (HTML, CSS, JavaScript). Open it with VS Code Live Server.
backend/    The API (Java 21, Spring Boot, Maven). Open it in IntelliJ IDEA, Eclipse or VS Code.
database/   SQL scripts for Microsoft SQL Server.
```

## 1. Database (Microsoft SQL Server)

In SQL Server Management Studio, connected as an administrator, run these in order. Each is run once.

| Script | What it does |
| --- | --- |
| `database/01_create_database.sql` | Creates an empty database called `SnapAndPack`. |
| `database/02_schema.sql` | Creates the main tables. Select the `SnapAndPack` database first. |
| `database/03_security_and_settings.sql` | Adds sign-in protection, password reset links, editable site text, the activity record and extra data rules. |
| `database/05_roles_payments_notifications.sql` | Adds the new staff roles, room for Argon2id password hashes, sign-out support, M-Pesa payments and notifications. |
| `database/06_mfa_and_email_confirmation.sql` | Adds two-step sign-in and email confirmation. It gives the website's login its permissions on the new tables itself. |
| `database/04_app_login_permissions.sql` | Run after 05, and again if you add tables by hand. Gives the backend's login (`clearance_app`) only the rights it needs, through a database role. Edit the login name inside the script if yours is different. |

Select the `SnapAndPack` database before running 02, 03, 05 and 06. On a new database the order is 01, 02, 03, 05, 04, 06.

**Upgrading a database that is already running:** run only the scripts you have not run yet, in number
order, and then `04` again. Your products, accounts and orders are kept. Script 05 renames the old
`ADMIN` role to `SUPER_ADMIN` and `PRODUCT_MANAGER` to `INVENTORY_MANAGER`.

The backend connects with SQL Server authentication (a login name and password), over TCP on port 1433.

## 2. Backend (Java)

You need JDK 21 or newer. Nothing else: Maven is downloaded for you the first time.

**On Windows** (in the `backend` folder):

- `build.bat` builds the backend. Run it again whenever you change the Java code.
- `run.bat` starts it. It asks for your SQL Server login name and password, and builds first if needed.
  If port 8080 is taken it uses the next free one and tells you the address. If the server cannot start,
  it says why; the full log is in `backend/logs/server.log`.
- `run.bat h2` starts it on a temporary in-memory database, without SQL Server.

Double-click them, or run them from a Command Prompt. Stop the server with Ctrl+C.

**On Linux (Kali, Ubuntu, Debian and others)**, in a terminal in the `backend` folder:

```bash
sudo apt install openjdk-21-jdk   # once, if Java 21 is not installed
chmod +x build.sh run.sh          # once, if the files lost their "executable" mark when unzipped
./build.sh                        # build; run again after changing Java code
./run.sh                          # start with SQL Server (asks for the login; the password is hidden)
./run.sh h2                       # start without SQL Server, on a temporary database
```

They do the same as the Windows files. If SQL Server is on another computer, set its address first:
`export DB_URL="jdbc:sqlserver://192.168.1.20:1433;databaseName=SnapAndPack;encrypt=true;trustServerCertificate=true"`.
You can also set `DB_USERNAME` and `DB_PASSWORD` the same way so `run.sh` does not ask.

**From an IDE:**

1. Open the `backend` folder as a Maven project.
2. Give it your database login. Either set the environment variables `DB_USERNAME` and `DB_PASSWORD`
   in the run configuration, or edit `backend/src/main/resources/application.properties`.
3. Run `SnapAndPackApplication`.

When the log says "Snap & Pack is running", open <http://localhost:8080> in your browser. The backend
serves the website as well as the API, so that one address is the whole store. On its first start it
creates the admin account and loads the 57 starting products.

**First super admin sign-in:** `admin@snapandpack.local` / `ChangeMe123!`
Change these before real use: set `ADMIN_EMAIL` and `ADMIN_PASSWORD` before the first start, and set
`JWT_SECRET` to a long random value.

**No SQL Server yet?** `run.bat h2` (or `mvnw spring-boot:run -Dspring-boot.run.profiles=h2`) runs the API on a
temporary in-memory database. Everything is erased when it stops.

## 3. Frontend

Nothing to start: with the backend running, <http://localhost:8080> is the website, and the admin area
is at <http://localhost:8080/admin/index.html>.

When the backend is started from the `backend` folder (as `run.bat` does), it reads the `frontend` folder
straight from disk, so after editing a page or style you only need to refresh the browser. Changes to
Java code need `build.bat` and a restart.

VS Code Live Server still works if you prefer it: open the `frontend` folder and start Live Server on
`index.html`. The pages then call the backend on port 8080, so it must be running too.

## Who can do what

| Role | What they can do |
| --- | --- |
| Super admin | Everything: products, orders, site settings, the activity record, creating staff, changing roles, switching accounts on or off, making password reset links |
| Admin | Runs the shop day to day: products, stock and orders. No access to accounts, settings or the activity record |
| Inventory manager | Add, edit and delete products; upload photos; update stock |
| Order manager | See all orders, change their status, re-check a payment with M-Pesa |
| Support | See orders and customers, without changing anything |
| Customer | Shop, place orders, pay with M-Pesa, see their own orders |

Anyone can register as a customer. Staff accounts are created by the super admin on the Users page,
which lists staff and customers on separate tabs. The backend checks the role on every request; the
front end only hides what a role cannot use.

## Notifications

Every signed-in user has a **Notifications** link in the header with a count of unseen messages.

| Who | Is told when |
| --- | --- |
| Customer | Their order is received, its status changes, a payment succeeds or fails, their role changes |
| Super admin, admin, order manager | A new order arrives, an order is paid, a payment needs checking |
| Super admin, admin, inventory manager | A product's stock falls to 5 or fewer |

Messages to customers also go out through every outside channel that is switched on:

| Channel | State | To switch on |
| --- | --- | --- |
| Server log | On (for testing) | Set `NOTIFICATIONS_LOG=false` on the live site |
| Email | Code ready, off, not yet tried against a mail server | Steps at the top of `EmailNotifier.java` |
| SMS | Placeholder only | Steps at the top of `SmsNotifier.java` |
| WhatsApp | Placeholder only | Steps at the top of `WhatsAppNotifier.java` |

## Two-step sign-in (MFA)

After the password, sign-in asks for a 6-digit code from an authenticator app on the user's phone
(Google Authenticator, Microsoft Authenticator, Authy and others).

- **Who:** anyone can switch it on from their **Account** page. Super admins and admins must: until
  they do, they can sign in but every staff function refuses them and the admin area sends them to
  the Account page. Set `MFA_REQUIRED_FOR_ADMINS=false` to switch that rule off while testing.
- **Setting up:** Account page, enter your password, scan the QR code with the app (or type the key),
  then type the code the app shows.
- **Recovery codes:** eight one-time codes are shown once, when it is switched on. Each can be used in
  place of an app code if the phone is lost. Keep them somewhere safe.
- **Lost phone and no recovery codes:** the super admin can press "Reset two-step" on that person's
  row on the Users page. For the super admin's own account, an administrator of the database runs:

  ```sql
  UPDATE users SET mfa_enabled = 0, mfa_secret = NULL, mfa_last_step = NULL WHERE email = 'their@email';
  DELETE FROM mfa_recovery_codes WHERE user_id = (SELECT id FROM users WHERE email = 'their@email');
  ```

The authenticator secrets are stored encrypted. Set `MFA_ENCRYPTION_KEY` before going live and keep it
safe: if it changes, everyone has to set up two-step sign-in again.

## Confirming email addresses

A new customer is sent a link to confirm their email address. Until they open it they can browse and
sign in but cannot place an order, and a strip under the header reminds them, with a button to send
the link again. A link works for 24 hours.

- **Until email is connected**, the link is written to the server log (`backend/logs/server.log`),
  and the super admin can press "Confirm email" on the customer's row on the Users page.
- Accounts that existed before this feature, and staff accounts created by the super admin, count as
  confirmed.
- Set `EMAIL_VERIFICATION_REQUIRED=false` to let unconfirmed accounts order while testing.

## Opening the shop through ngrok

`ngrok http 8080` (use the port `run.sh` printed) gives the shop a public https address, useful for
trying it on a phone or for M-Pesa callbacks. Nothing needs setting: the backend recognises that the
request came in over https through ngrok, so sign-in works and the sign-in cookie is sent over https
only. It trusts that information only from programs on the same computer.

The free ngrok plan shows a warning page on the first visit; press "Visit Site". For M-Pesa, put the
https address ngrok prints into `PUBLIC_URL`.

## Payments (M-Pesa)

After placing an order the customer pays from **My orders** with "Pay with M-Pesa".

- **Test mode (the default):** nothing is sent to Safaricom and no money moves. The payment confirms
  itself a moment later through the same code a real Safaricom confirmation uses, and gets a receipt
  number starting with `SIM`.
- **Real mode:** set `MPESA_MODE=live` and the Daraja details. The steps are at the top of
  `DarajaMpesaGateway.java`. This code follows Safaricom's published API but has not yet been run
  against Safaricom, so try it in the Daraja sandbox first.

The amount always comes from the order in the database. An order is marked paid only when M-Pesa
confirms it, the confirmation must match a request the shop made and the order's amount, and a
confirmation that arrives twice is counted once. Staff can press "Check with M-Pesa" on an unpaid order
if a confirmation never arrived.

## Product photos

Staff who manage products can upload a JPEG or PNG (up to 2 MB) from the product form. Files are kept
in an `uploads` folder beside `backend` and `frontend`. Back that folder up along with the database.

## Site settings

On the admin **Site settings** page the super admin can change the shop name, the line under it, the
announcement bar, the home page headline, the four promise boxes (shipping, returns, support, checkout),
the footer line and the contact details. The text is stored in the database.

## Forgotten passwords

1. The user clicks "Forgot your password?" on the sign-in page and enters their email.
2. A reset link is created. It works once, for 30 minutes, and a newer link cancels older ones.
3. **Until email is connected**, the link is written to the server log (`backend/logs/server.log`), and
   the super admin can also make a link for any account with "Reset password" on the Users page and pass it on.
4. Opening the link lets the user choose a new password. Their old sign-ins stop working.

When email is added later, it replaces `LogNotifier` (see `backend/.../notification/`); nothing else changes.

## Security

`SECURITY.md` lists every point of the security plan with what is built, what is ready but switched
off, and what has to be done when the site is deployed.

## Folder guide

```
frontend/
  index.html                 Store home page
  pages/                     login, register, verify-email, forgot-password, reset-password, account,
                             cart (with checkout), orders, notifications
  admin/                     index (dashboard), products, orders, users, settings, activity
  assets/
    css/                     base.css (shared), store.css, admin.css
    js/core/                 config, api, auth, cart-store, settings, notifications, ui  (shared by all pages)
    js/pages/                one script per store page
    js/admin/                one script per admin page, plus layout.js
    images/                  product photos

backend/src/main/java/com/snapandpack/
  controller/                the API addresses (/api/...)
  service/                   the rules: placing orders, stock, roles
  repository/                database access
  model/                     the tables as Java classes
  dto/                       the shapes of requests and responses
  security/                  sign-in cookie and tokens, CSRF, rate limits, password rules, role rules
  notification/              outside channels: log, email, and SMS and WhatsApp placeholders
  payment/                   M-Pesa: test stand-in and the real Daraja link
  web/                       helper for building links to the site
  config/                    security rules, first-run data
  exception/                 error messages sent to the front end
backend/src/main/resources/
  application.properties     database login, admin account, allowed front-end addresses
  seed/products.json         the starting products

database/
  01_create_database.sql
  02_schema.sql
  03_security_and_settings.sql
  04_app_login_permissions.sql      (run after 05)
  05_roles_payments_notifications.sql
  06_mfa_and_email_confirmation.sql

uploads/                     product photos uploaded by staff (created on first upload)
```

## API summary

| Method and path | Who | Purpose |
| --- | --- | --- |
| `POST /api/auth/register` | anyone | Create a customer account |
| `POST /api/auth/login` | anyone | Sign in |
| `POST /api/auth/logout` | signed in | Sign out |
| `POST /api/auth/verify-email` | anyone with a link | Confirm an email address |
| `POST /api/auth/resend-verification` | signed in | Send the confirmation link again |
| `POST /api/auth/mfa/setup`, `/mfa/enable`, `/mfa/disable` | signed in | Set up, switch on and switch off two-step sign-in |
| `GET /api/auth/me` | signed in | Current user and role |
| `POST /api/auth/forgot-password` | anyone | Create a password reset link |
| `POST /api/auth/reset-password` | anyone with a link | Set a new password |
| `GET /api/products`, `GET /api/products/{id}` | anyone | Browse products |
| `POST /api/products`, `PUT/DELETE /api/products/{id}` | Super admin, Admin, Inventory manager | Manage products |
| `POST /api/uploads/product-image` | Super admin, Admin, Inventory manager | Upload a product photo |
| `POST /api/orders` | signed in | Place an order |
| `GET /api/orders/mine` | signed in | Own orders |
| `POST /api/orders/{id}/pay` | the order's owner | Start an M-Pesa payment |
| `POST /api/orders/{id}/payment/check` | owner, order staff | Ask M-Pesa how the payment ended |
| `POST /api/payments/mpesa/callback/{secret}` | Safaricom | Payment result |
| `GET /api/notifications`, `GET /api/notifications/unread-count` | signed in | Own notifications |
| `POST /api/notifications/{id}/read`, `POST /api/notifications/read-all` | signed in | Mark as seen |
| `GET /api/orders` | Super admin, Admin, Order manager, Support | All orders |
| `PATCH /api/orders/{id}/status` | Super admin, Admin, Order manager | Change order status |
| `GET /api/users` | Super admin (all), Support (customers) | List accounts |
| `POST /api/users` | Super admin | Create an account with any role |
| `PATCH /api/users/{id}/role`, `PATCH /api/users/{id}/active` | Super admin | Change role, switch on or off |
| `POST /api/users/{id}/password-reset` | Super admin | Make a reset link for an account |
| `POST /api/users/{id}/mfa-reset` | Super admin | Remove two-step sign-in from an account |
| `POST /api/users/{id}/verify-email` | Super admin | Mark an email as confirmed by hand |
| `GET /api/settings` | anyone | Shop name and site text |
| `PUT /api/settings` | Super admin | Change site text |
| `GET /api/audit` | Super admin | Activity record |
| `GET /api/dashboard/stats` | any staff | Dashboard totals |

## Not built yet

See `SECURITY.md` for the full list. The main gaps:

- Real M-Pesa and email have code but have not been run against Safaricom or a mail server.
- SMS and WhatsApp are placeholders.
- Refunds, and managing product categories from the admin area (categories are fixed as Bag and Cap).
- Changing your own password while signed in (use "Forgot your password?" for now).
