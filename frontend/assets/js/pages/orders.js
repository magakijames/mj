// "My orders" page for the signed-in user, including paying for an order with M-Pesa.
import { api } from '../core/api.js';
import { url, ORDER_STATUSES } from '../core/config.js';
import { requireSignIn } from '../core/auth.js';
import { $, esc, money, dateTime, toast, mountStoreChrome } from '../core/ui.js';

await mountStoreChrome();

let orders = [];
const justPlaced = Number(new URLSearchParams(location.search).get('placed')) || null;

// The payment part of an order card: a receipt when paid, otherwise a phone box and "Pay with M-Pesa".
function paymentBox(order) {
  if (order.paid) return `<p class="pay paid"><span class="status delivered">Paid</span> M-Pesa receipt ${esc(order.paymentReference)}</p>`;
  if (order.status === 'CANCELLED') return '';
  return `<form class="pay" data-pay="${order.id}">
    <span class="status pending">Not paid yet</span>
    <label class="visually-hidden" for="phone${order.id}">M-Pesa phone number</label>
    <input id="phone${order.id}" name="phone" type="tel" value="${esc(order.phone)}" placeholder="0712345678" required>
    <button class="btn small">Pay ${money(order.total)} with M-Pesa</button>
    <span class="meta pay-note" role="status"></span>
  </form>`;
}

// The HTML for one order: number, date, status, lines, total and payment.
function orderCard(order) {
  return `<article class="orderbox ${order.id === justPlaced ? 'highlight' : ''}">
    <header><b>Order #${order.id}</b><span class="meta">${dateTime(order.createdAt)}</span>
      <span class="status ${order.status.toLowerCase()}">${ORDER_STATUSES[order.status]}</span></header>
    <ul>${order.items.map(i => `<li>${i.quantity} × ${esc(i.productName)} <span class="meta">(${esc(i.color)}, ${esc(i.size)})</span></li>`).join('')}</ul>
    <b>Total ${money(order.total)}</b>
    ${paymentBox(order)}
  </article>`;
}

// Fetches the user's orders and draws them.
async function load() {
  try {
    orders = await api('/orders/mine');
    $('#orders').innerHTML = orders.length ? orders.map(orderCard).join('')
      : `<p class="empty">You have no orders yet. <a href="${url('index.html')}">Start shopping</a>.</p>`;
  } catch (error) {
    $('#orders').innerHTML = `<p class="empty">${esc(error.message)}</p>`;
  }
}

// Waits for M-Pesa to confirm a payment: asks the server every 4 seconds for up to 2 minutes.
// The order only shows as paid when the server says so; nothing here can mark it paid.
async function waitForPayment(orderId, note) {
  for (let attempt = 0; attempt < 30; attempt++) {
    await new Promise(resolve => setTimeout(resolve, 4000));
    let payment;
    try { payment = await api(`/orders/${orderId}/payment/check`, { method: 'POST' }); } catch { continue; }
    if (payment.status === 'SUCCESS') { toast('Payment received. Thank you!'); return load(); }
    if (payment.status === 'FAILED') { note.textContent = 'The payment did not go through. You can try again.'; return; }
  }
  note.textContent = 'No answer from M-Pesa yet. If you paid, this page will show it shortly; otherwise try again.';
}

// "Pay with M-Pesa": asks the server to send the payment prompt to the phone, then waits for the result.
$('#orders').addEventListener('submit', async e => {
  const form = e.target.closest('[data-pay]');
  if (!form) return;
  e.preventDefault();
  const note = form.querySelector('.pay-note');
  const button = form.querySelector('button');
  button.disabled = true;
  note.textContent = 'Sending the request to your phone…';
  try {
    const payment = await api(`/orders/${form.dataset.pay}/pay`, { method: 'POST', body: { phone: form.phone.value.trim() } });
    if (payment.status === 'SUCCESS') {
      toast(payment.simulated ? 'Test mode: payment simulated, no money was taken' : 'Payment received. Thank you!');
      return load();
    }
    note.textContent = 'Check your phone and enter your M-Pesa PIN. Waiting for confirmation…';
    await waitForPayment(form.dataset.pay, note);
  } catch (error) {
    note.textContent = error.message;
  }
  button.disabled = false;
});

// Only signed-in users have orders; others are sent to the sign-in page.
if (requireSignIn()) {
  if (justPlaced) toast('Order placed. Pay with M-Pesa to confirm it.');
  load();
}
