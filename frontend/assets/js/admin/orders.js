// Admin orders page: every order, with a status control for roles allowed to change it.
import { api } from '../core/api.js';
import { ORDER_STATUSES, CAN } from '../core/config.js';
import { $, esc, money, dateTime, toast } from '../core/ui.js';
import { openAdminPage, showError } from './layout.js';

let canEdit = false;

// Fetches every order from the backend and draws the list.
async function load() {
  try {
    const orders = await api('/orders');
    $('#orderCount').textContent = orders.length + (orders.length === 1 ? ' order' : ' orders');
    $('#orderList').innerHTML = orders.length ? orders.map(row).join('') : '<p class="empty">No orders yet.</p>';
  } catch (error) {
    showError('#orderList', error);
  }
}

// The HTML for one order. Roles that may change the status get a drop-down; others see a label.
// A cancelled order is final, so it never gets the drop-down.
function row(o) {
  const locked = !canEdit || o.status === 'CANCELLED';
  const control = locked
    ? `<span class="status ${o.status.toLowerCase()}">${ORDER_STATUSES[o.status]}</span>`
    : `<label>Status <select data-order="${o.id}">${Object.entries(ORDER_STATUSES).map(([value, label]) =>
        `<option value="${value}" ${value === o.status ? 'selected' : ''}>${label}</option>`).join('')}</select></label>`;
  return `<article class="orderbox">
    <header><b>Order #${o.id}</b><span class="meta">${dateTime(o.createdAt)}</span><span class="grow"></span>${control}</header>
    <p class="meta">${esc(o.customerName)} · ${esc(o.customerEmail)} · ${esc(o.phone)}<br>${esc(o.address)}</p>
    <ul>${o.items.map(i => `<li>${i.quantity} × ${esc(i.productName)} <span class="meta">(${esc(i.color)}, ${esc(i.size)})</span></li>`).join('')}</ul>
    <p class="pay"><b>Total ${money(o.total)}</b>
      ${o.paid ? `<span class="status delivered">Paid</span> <span class="meta">M-Pesa receipt ${esc(o.paymentReference)}</span>`
        : `<span class="status pending">Not paid</span>${canEdit && o.status !== 'CANCELLED' ? ` <button class="btn small ghost" data-check="${o.id}">Check with M-Pesa</button>` : ''}`}</p>
  </article>`;
}

// Saves a new status when a drop-down is changed. Cancelling asks first, because it cannot be undone.
$('#orderList').addEventListener('change', async e => {
  const select = e.target.closest('[data-order]');
  if (!select) return;
  if (select.value === 'CANCELLED' && !confirm('Cancel this order? The stock goes back on sale and the order cannot be reopened.')) return load();
  try {
    await api(`/orders/${select.dataset.order}/status`, { method: 'PATCH', body: { status: select.value } });
    toast('Order marked ' + ORDER_STATUSES[select.value].toLowerCase());
  } catch (error) {
    toast(error.message);
  }
  load();
});

// "Check with M-Pesa": asks M-Pesa how the order's latest payment request ended, for when no
// confirmation arrived by itself.
$('#orderList').addEventListener('click', async e => {
  const button = e.target.closest('[data-check]');
  if (!button) return;
  try {
    const payment = await api(`/orders/${button.dataset.check}/payment/check`, { method: 'POST' });
    toast({ SUCCESS: 'Payment confirmed', FAILED: 'That payment failed', PENDING: 'M-Pesa has no answer yet' }[payment.status]);
  } catch (error) {
    toast(error.message);
  }
  load();
});

const user = await openAdminPage('orders');
if (user) {
  canEdit = CAN.editOrders.includes(user.role);
  if (!canEdit) $('#readOnlyNote').hidden = false;
  load();
}
