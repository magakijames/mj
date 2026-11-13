// The shopping cart. It lives in the browser until the order is placed.
const KEY = 'sp_cart';

export function getCart() {
  try { return JSON.parse(localStorage.getItem(KEY)) || []; } catch { return []; }
}
function save(cart) { localStorage.setItem(KEY, JSON.stringify(cart)); }

export const cartCount = () => getCart().reduce((n, line) => n + line.quantity, 0);

export function addToCart(productId, color, size) {
  const cart = getCart();
  const line = cart.find(l => l.productId === productId && l.color === color && l.size === size);
  if (line) line.quantity++; else cart.push({ productId, color, size, quantity: 1 });
  save(cart);
}

export function setQuantity(index, quantity) {
  const cart = getCart();
  if (!cart[index]) return;
  if (quantity <= 0) cart.splice(index, 1); else cart[index].quantity = Math.min(99, quantity);
  save(cart);
}

export const quantityInCart = productId => getCart().filter(l => l.productId === productId).reduce((n, l) => n + l.quantity, 0);
export const clearCart = () => localStorage.removeItem(KEY);
