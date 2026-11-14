# Snap & Pack

An online store for bags and caps, in three parts:

```
frontend/   The website (HTML, CSS, JavaScript). Open it with VS Code Live Server.
backend/    The API (Java 17, Spring Boot, Maven). Open it in IntelliJ IDEA, Eclipse or VS Code.
database/   SQL scripts for Microsoft SQL Server.
```

## 1. Database (Microsoft SQL Server)

In SQL Server Management Studio, connected as an administrator, run these in order. Each is run once.

| Script | What it does |
| --- | --- |
| `database/01_create_database.sql` | Creates an empty database called `SnapAndPack`. |
| `database/02_schema.sql` | Creates the main tables. Select the `SnapAndPack` database first. |
| `database/03_security_and_settings.sql` | Adds sign-in protection, password reset links, editable site text, the activity record and extra data rules. Select the `SnapAndPack` database first. |
| `database/04_app_login_permissions.sql` | Gives the backend's login (`clearance_app`) only the rights it needs, table by table. Edit the login name inside the script if yours is different. |

**Upgrading a database that is already running:** run only `03` and then `04`. Your products, accounts
and orders are kept.

The backend connects with SQL Server authentication (a login name and password), over TCP on port 1433.

## 2. Backend (Java)

You need JDK 17 or newer. Nothing else: Maven is downloaded for you the first time.

**On Windows, with the batch files** (in the `backend` folder):

- `build.bat` builds the backend. Run it again whenever you change the Java code.
- `run.bat` starts it. It asks for your SQL Server login name and password, and builds first if needed.
  If port 8080 is taken it uses the next free one and tells you the address. If the server cannot start,
  it says why; the full log is in `backend/logs/server.log`.
- `run.bat h2` starts it on a temporary in-memory database, without SQL Server.

Double-click them, or run them from a Command Prompt. Stop the server with Ctrl+C.

**From an IDE:**

1. Open the `backend` folder as a Maven project.
2. Give it your database login. Either set the environment variables `DB_USERNAME` and `DB_PASSWORD`
   in the run configuration, or edit `backend/src/main/resources/application.properties`.
3. Run `SnapAndPackApplication`.

When the log says "Snap & Pack is running", open <http://localhost:8080> in your browser. The backend
serves the website as well as the API, so that one address is the whole store. On its first start it
creates the admin account and loads the 57 starting products.

**First admin sign-in:** `admin@snapandpack.local` / `ChangeMe123!`
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
| Admin | Everything: products, orders, site settings, the activity record, creating staff, changing roles, switching accounts on or off, making password reset links |
| Product manager | Add, edit and delete products; update stock |
| Order manager | See all orders and change their status |
| Support | See orders and customers, without changing anything |
| Customer | Shop, place orders, see their own orders |

Anyone can register as a customer. Staff accounts are created by the admin on the Users page, which
lists staff and customers on separate tabs. The backend checks the role on every request; the front end
only hides what a role cannot use.

## Site settings

On the admin **Site settings** page the admin can change the shop name, the line under it, the
announcement bar, the home page headline, the four promise boxes (shipping, returns, support, checkout),
the footer line and the contact details. The text is stored in the database.

## Forgotten passwords

1. The user clicks "Forgot your password?" on the sign-in page and enters their email.
2. A reset link is created. It works once, for 30 minutes, and a newer link cancels older ones.
3. **Until email is connected**, the link is written to the server log (`backend/logs/server.log`), and
   the admin can also make a link for any account with "Reset password" on the Users page and pass it on.
4. Opening the link lets the user choose a new password. Their old sign-ins stop working.

When email is added later, it replaces `LogNotifier` (see `backend/.../notification/`); nothing else changes.

## Security

- Passwords are stored only as BCrypt hashes, and must be 8 to 72 characters with a letter and a number.
- Five wrong passwords in a row lock an account for 15 minutes.
- Sign-in tokens stop working when a password is reset or an account is switched off.
- Reset links are stored only as hashes, so reading the database does not reveal a usable link.
- The database refuses bad data itself (CHECK, UNIQUE and FOREIGN KEY rules), and the backend's login can
  neither delete accounts or orders nor change the activity record (`04_app_login_permissions.sql`).
- Every change by staff and every sign-in lock or password reset is written to the activity record.
- Pages are sent with a Content-Security-Policy and other protective headers.
- All database access uses parameters, never text pasted into SQL.

Before going live, also set `JWT_SECRET` to a long random value, change the first admin password, and
serve the site over HTTPS.

## Folder guide

```
frontend/
  index.html                 Store home page
  pages/                     login, register, forgot-password, reset-password, cart (with checkout), orders
  admin/                     index (dashboard), products, orders, users, settings, activity
  assets/
    css/                     base.css (shared), store.css, admin.css
    js/core/                 config, api, auth, cart-store, settings, ui  (shared by all pages)
    js/pages/                one script per store page
    js/admin/                one script per admin page, plus layout.js
    images/                  product photos

backend/src/main/java/com/snapandpack/
  controller/                the API addresses (/api/...)
  service/                   the rules: placing orders, stock, roles
  repository/                database access
  model/                     the tables as Java classes
  dto/                       the shapes of requests and responses
  security/                  sign-in tokens, password rules
  notification/              messages to users (log only for now; email, SMS and WhatsApp later)
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
  04_app_login_permissions.sql
```

## API summary

| Method and path | Who | Purpose |
| --- | --- | --- |
| `POST /api/auth/register` | anyone | Create a customer account |
| `POST /api/auth/login` | anyone | Sign in |
| `GET /api/auth/me` | signed in | Current user and role |
| `POST /api/auth/forgot-password` | anyone | Create a password reset link |
| `POST /api/auth/reset-password` | anyone with a link | Set a new password |
| `GET /api/products`, `GET /api/products/{id}` | anyone | Browse products |
| `POST /api/products`, `PUT/DELETE /api/products/{id}` | Admin, Product manager | Manage products |
| `POST /api/orders` | signed in | Place an order |
| `GET /api/orders/mine` | signed in | Own orders |
| `GET /api/orders` | Admin, Order manager, Support | All orders |
| `PATCH /api/orders/{id}/status` | Admin, Order manager | Change order status |
| `GET /api/users` | Admin (all), Support (customers) | List accounts |
| `POST /api/users` | Admin | Create an account with any role |
| `PATCH /api/users/{id}/role`, `PATCH /api/users/{id}/active` | Admin | Change role, switch on or off |
| `POST /api/users/{id}/password-reset` | Admin | Make a reset link for an account |
| `GET /api/settings` | anyone | Shop name and site text |
| `PUT /api/settings` | Admin | Change site text |
| `GET /api/audit` | Admin | Activity record |
| `GET /api/dashboard/stats` | any staff | Dashboard totals |

## Not built yet

- Payment. Orders are saved, but no money is collected.
- Email, SMS and WhatsApp messages. Password reset links are handed over by the admin until email is connected.
- Changing your own password while signed in (use "Forgot your password?" for now).
- Uploading product photos from the admin page (photos are added to `frontend/assets/images` by hand).
