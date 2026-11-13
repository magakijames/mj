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

You need JDK 17 or newer. Maven downloads everything else.

1. Open the `backend` folder in your IDE as a Maven project.
2. Give it your database login. Either set the environment variables `DB_USERNAME` and `DB_PASSWORD`
   in the run configuration, or edit `backend/src/main/resources/application.properties`.
3. Run `SnapAndPackApplication`. From a terminal: `mvn spring-boot:run` inside `backend`.

The API starts on <http://localhost:8080>. On its first start it creates the admin account and loads
the 57 starting products.

**First admin sign-in:** `admin@snapandpack.local` / `ChangeMe123!`
Change these before real use: set `ADMIN_EMAIL` and `ADMIN_PASSWORD` before the first start, and set
`JWT_SECRET` to a long random value.

**No SQL Server yet?** `mvn spring-boot:run -Dspring-boot.run.profiles=h2` runs the API on a temporary
in-memory database. Everything is erased when it stops.

## 3. Frontend

1. Open the `frontend` folder in VS Code.
2. Right-click `index.html` and choose "Open with Live Server".

The backend must be running, because the pages load products and accounts from it. The API address is
set in `frontend/assets/js/core/config.js`.

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
