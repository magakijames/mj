// "My orders" page for the signed-in user.
import { api } from '../core/api.js';
import { url, ORDER_STATUSES } from '../core/config.js';
import { requireSignIn } from '../core/auth.js';
import { $, esc, money, dateTime, toast, mountStoreChrome } from '../core/ui.js';

mountStoreChrome();

export function orderCard(order, { showCustomer = false } = {}) {
  return `<article class="orderbox">
    <header><b>Order #${order.id}</b><span class="meta">${dateTime(order.createdAt)}</span>
      <span class="status ${order.status.toLowerCase()}">${ORDER_STATUSES[order.status]}</span></header>
    ${showCustomer ? `<p class="meta">${esc(order.customerName)} · ${esc(order.customerEmail)} · ${esc(order.phone)}<br>${esc(order.address)}</p>` : ''}
    <ul>${order.items.map(i => `<li>${i.quantity} × ${esc(i.productName)} <span class="meta">(${esc(i.color)}, ${esc(i.size)})</span></li>`).join('')}</ul>
    <b>Total ${money(order.total)}</b>
  </article>`;
}

if (requireSignIn()) {
  if (new URLSearchParams(location.search).has('placed')) toast('Order placed. Thank you!');
  try {
    const orders = await api('/orders/mine');
    $('#orders').innerHTML = orders.length ? orders.map(o => orderCard(o)).join('')
      : `<p class="empty">You have no orders yet. <a href="${url('index.html')}">Start shopping</a>.</p>`;
  } catch (error) {
    $('#orders').innerHTML = `<p class="empty">${esc(error.message)}</p>`;
  }
}
