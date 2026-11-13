# Crown & Brim

A static storefront for selling caps and bags. No build step: open `index.html` in a browser, or serve the folder with any static host.

- `index.html` — page structure
- `styles.css` — styling
- `app.js` — catalogue, filters, cart and checkout

To change the caps, prices, currency or delivery fee, edit the settings and `PRODUCTS` list at the top of `app.js`.

Checkout confirms the order on screen only. Connect a server or payment provider in `submitOrder` in `app.js` before taking real orders.
