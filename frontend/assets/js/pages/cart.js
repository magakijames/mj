// Cart and checkout page.
import { api } from '../core/api.js';
import { url } from '../core/config.js';
import { getUser } from '../core/auth.js';
import { getCart, setQuantity, clearCart } from '../core/cart-store.js';
import { $, esc, money, imageUrl, toast, refreshCartCount, mountStoreChrome } from '../core/ui.js';

mountStoreChrome();

let products = [];

function render() {
  const cart = getCart();
  const lines = cart.map((line, index) => ({ line, index, product: products.find(p => p.id === line.productId) }));
  const total = lines.reduce((sum, { line, product }) => sum + (product ? product.price * line.quantity : 0), 0);

  $('#cartLines').innerHTML = lines.length ? lines.map(({ line, index, product }) => product ? `
    <div class="ci">
      <img src="${esc(imageUrl(product.image))}" alt="">
      <div class="ci-info"><b>${esc(product.name)}</b><span class="meta">${esc(line.color)} · ${esc(line.size)}</span><span class="meta">${money(product.price)} each</span></div>
      <div class="qty">
        <button data-step="-1" data-index="${index}" aria-label="One fewer ${esc(product.name)}">−</button>
        <span>${line.quantity}</span>
        <button data-step="1" data-index="${index}" aria-label="One more ${esc(product.name)}">+</button>
      </div>
      <b class="ci-total">${money(product.price * line.quantity)}</b>
    </div>` : `
    <div class="ci"><div class="ci-info"><b>This product is no longer sold</b></div>
      <button class="link" data-step="-999" data-index="${index}">Remove</button></div>`).join('')
    : `<p class="empty">Your cart is empty. <a href="${url('index.html')}">Browse bags and caps</a>.</p>`;

  $('#cartTotal').textContent = money(total);
  $('#checkout').hidden = !lines.length;
  $('#signInNote').hidden = !!getUser();
  $('#checkoutForm').hidden = !getUser();
  refreshCartCount();
}

$('#cartLines').addEventListener('click', e => {
  const button = e.target.closest('[data-step]');
  if (!button) return;
  const index = +button.dataset.index;
  setQuantity(index, getCart()[index].quantity + +button.dataset.step);
  render();
});

$('#signInLink').href = url('pages/login.html') + '?next=' + encodeURIComponent(location.href);

$('#checkoutForm').addEventListener('submit', async e => {
  e.preventDefault();
  const error = $('#formError');
  error.textContent = '';
  const items = getCart().filter(line => products.some(p => p.id === line.productId));
  try {
    await api('/orders', { method: 'POST', body: { items, phone: $('#phone').value.trim(), address: $('#address').value.trim() } });
    clearCart();
    location.href = url('pages/orders.html') + '?placed=1';
  } catch (err) {
    error.textContent = err.message;
  }
});

try {
  products = await api('/products');
  render();
} catch (error) {
  $('#cartLines').innerHTML = `<p class="empty">${esc(error.message)}</p>`;
}
