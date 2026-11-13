# Snap & Pack

A static storefront for selling bags and caps. No build step: open `index.html` in a browser, or serve the folder with any static host.

- `index.html` — page structure
- `style.css` — styling
- `app.js` — products, filters, cart, sign-in, orders and the admin panel
- `images/` — product photos

Prices are in Kenyan shillings (KSh). The starting products are the `SEED` list at the top of `app.js`; after first load, products are edited from the admin panel.

This is a front end only. Products, accounts, carts and orders are stored in each visitor's own browser (`localStorage`), so orders do not reach the shop and the sign-in is not real security. Connect a server and a payment provider before taking real orders.
