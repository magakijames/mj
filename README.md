# Snap & Pack

An online store for bags and caps, in three parts:

```
frontend/   The website (HTML, CSS, JavaScript). Open it with VS Code Live Server.
backend/    The API (Java 17, Spring Boot, Maven). Open it in IntelliJ IDEA, Eclipse or VS Code.
database/   SQL scripts for Microsoft SQL Server.
```

## 1. Database (Microsoft SQL Server)

In SQL Server Management Studio:

1. Run `database/01_create_database.sql`. It creates an empty database called `SnapAndPack`.
2. Select the `SnapAndPack` database, then run `database/02_schema.sql`. It creates the tables.

The backend connects with SQL Server authentication (a login name and password), over TCP on port 1433.
If you normally use Windows authentication, enable "SQL Server and Windows Authentication mode" and TCP/IP
for your server first.

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
| Admin | Everything: products, orders, and creating staff, changing roles, switching accounts on or off |
| Product manager | Add, edit and delete products; update stock |
| Order manager | See all orders and change their status |
| Support | See orders and customers, without changing anything |
| Customer | Shop, place orders, see their own orders |

Anyone can register as a customer. Staff accounts are created by the admin on the Users page.
The backend checks the role on every request; the front end only hides what a role cannot use.

## Folder guide

```
frontend/
  index.html                 Store home page
  pages/                     login, register, cart (with checkout), orders
  admin/                     index (dashboard), products, orders, users
  assets/
    css/                     base.css (shared), store.css, admin.css
    js/core/                 config, api, auth, cart-store, ui  (shared by all pages)
    js/pages/                one script per store page
    js/admin/                one script per admin page, plus layout.js
    images/                  product photos

backend/src/main/java/com/snapandpack/
  controller/                the API addresses (/api/...)
  service/                   the rules: placing orders, stock, roles
  repository/                database access
  model/                     the tables as Java classes
  dto/                       the shapes of requests and responses
  security/                  sign-in tokens
  config/                    security rules, first-run data
  exception/                 error messages sent to the front end
backend/src/main/resources/
  application.properties     database login, admin account, allowed front-end addresses
  seed/products.json         the starting products

database/
  01_create_database.sql
  02_schema.sql
```

## API summary

| Method and path | Who | Purpose |
| --- | --- | --- |
| `POST /api/auth/register` | anyone | Create a customer account |
| `POST /api/auth/login` | anyone | Sign in |
| `GET /api/auth/me` | signed in | Current user and role |
| `GET /api/products`, `GET /api/products/{id}` | anyone | Browse products |
| `POST /api/products`, `PUT/DELETE /api/products/{id}` | Admin, Product manager | Manage products |
| `POST /api/orders` | signed in | Place an order |
| `GET /api/orders/mine` | signed in | Own orders |
| `GET /api/orders` | Admin, Order manager, Support | All orders |
| `PATCH /api/orders/{id}/status` | Admin, Order manager | Change order status |
| `GET /api/users` | Admin (all), Support (customers) | List accounts |
| `POST /api/users` | Admin | Create an account with any role |
| `PATCH /api/users/{id}/role`, `PATCH /api/users/{id}/active` | Admin | Change role, switch on or off |
| `GET /api/dashboard/stats` | any staff | Dashboard totals |

## Not built yet

- Payment. Orders are saved, but no money is collected.
- Password reset and changing your own password.
- Uploading product photos from the admin page (photos are added to `frontend/assets/images` by hand).
