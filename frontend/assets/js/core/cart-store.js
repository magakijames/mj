// The shopping cart. It lives in the browser until the order is placed.
const KEY = 'sp_cart';

// The cart as a list of lines: { productId, color, size, quantity }.
export function getCart() {
  try { return JSON.parse(localStorage.getItem(KEY)) || []; } catch { return []; }
}

// Saves the cart back to the browser.
function save(cart) { localStorage.setItem(KEY, JSON.stringify(cart)); }

// How many items are in the cart in total.
export const cartCount = () => getCart().reduce((n, line) => n + line.quantity, 0);

// Adds one of a product in the chosen colour and size, or raises the quantity if that line exists.
export function addToCart(productId, color, size) {
  const cart = getCart();
  const line = cart.find(l => l.productId === productId && l.color === color && l.size === size);
  if (line) line.quantity++; else cart.push({ productId, color, size, quantity: 1 });
  save(cart);
}

// Sets the quantity of the line at this position. Zero or less removes the line; 99 is the most.
export function setQuantity(index, quantity) {
  const cart = getCart();
  if (!cart[index]) return;
  if (quantity <= 0) cart.splice(index, 1); else cart[index].quantity = Math.min(99, quantity);
  save(cart);
}

// How many of one product are in the cart across all its colours and sizes.
export const quantityInCart = productId => getCart().filter(l => l.productId === productId).reduce((n, l) => n + l.quantity, 0);

// Empties the cart, after an order is placed.
export const clearCart = () => localStorage.removeItem(KEY);
