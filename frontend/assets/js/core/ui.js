// Small helpers used on every page, plus the shared header and footer.
import { url, ROLES, API_ORIGIN } from './config.js';
import { getUser, setAuth, isStaff, signOut } from './auth.js';
import { cartCount } from './cart-store.js';
import { loadSettings } from './settings.js';
import { notificationLink, watchNotifications } from './notifications.js';

// Finds the first element matching a CSS selector.
export const $ = selector => document.querySelector(selector);

// Makes text safe to place inside HTML, so nothing typed by a person can run as code.
export const esc = value => String(value ?? '').replace(/[&<>"']/g, c => ({ '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;' }[c]));

// Like esc, and also turns line breaks into <br>.
export const escLines = value => esc(value).replace(/\n/g, '<br>');

// Formats an amount as Kenyan shillings, for example "KSh 1,500".
export const money = amount => 'KSh ' + Math.round(Number(amount)).toLocaleString('en-KE');

// Formats a date and time from the server for reading.
export const dateTime = iso => new Date(iso).toLocaleString('en-KE', { dateStyle: 'medium', timeStyle: 'short' });

// Works out the address of a product photo. A product stores one of:
//   a full web address (https://...), a photo uploaded by staff (uploads/...), which the backend serves,
//   or a photo that ships with the site (assets/images/...).
export function imageUrl(image) {
  if (!image) return '';
  if (/^https?:\/\//.test(image)) return image;
  if (image.startsWith('uploads/')) return API_ORIGIN + '/' + image;
  return url(image);
}

// The on-screen label for a role code.
export const roleLabel = role => ROLES[role] || role;

// Shows a short message at the bottom of the screen for a few seconds.
export function toast(message) {
  let box = $('#toast');
  if (!box) {
    box = document.createElement('div');
    box.id = 'toast';
    box.className = 'toast';
    box.setAttribute('role', 'status');
    document.body.appendChild(box);
  }
  box.textContent = message;
  box.hidden = false;
  clearTimeout(toast.timer);
  toast.timer = setTimeout(() => { box.hidden = true; }, 2600);
}

// Updates the number shown next to "Cart" in the header.
export function refreshCartCount() {
  const count = $('#cartCount');
  if (count) count.textContent = cartCount();
}

// Puts the shop's name into the browser tab title in place of the built-in one.
export function applyShopName(settings) {
  document.title = document.title.replace('Snap & Pack', settings.shop_name);
}

// Shows a strip under the header asking the user to confirm their email address, with a button to
// send the link again. It first asks the server for the current state, because the link may have
// been opened in another browser since this one last checked.
async function showEmailBanner() {
  let user;
  try {
    const { api } = await import('./api.js');
    user = await api('/auth/me');
    setAuth(user);
    if (user.emailVerified) return;
    $('#siteHeader').insertAdjacentHTML('afterend', `<div class="verify-bar" id="verifyBar">
      Please confirm your email address before placing an order. Open the link we sent to <b>${esc(user.email)}</b>.
      <button class="link" id="resendVerify">Send the link again</button></div>`);
    $('#resendVerify').addEventListener('click', async () => {
      try {
        toast((await api('/auth/resend-verification', { method: 'POST' })).message);
      } catch (error) {
        toast(error.message);
      }
    });
  } catch { /* not signed in any more: nothing to show */ }
}

// Draws the top bar, header and footer of the store pages using the site settings, and returns
// those settings. The top bar and header stay pinned to the top of the screen while the page scrolls. Pass { search: true } on the home page to include the search box.
export async function mountStoreChrome({ search = false } = {}) {
  const settings = await loadSettings();
  const user = getUser();
  applyShopName(settings);

  const links = [];
  if (!user) links.push(`<a class="link" href="${url('pages/login.html')}">Sign in</a>`);
  if (user && isStaff(user)) links.push(`<a class="link" href="${url('admin/index.html')}">Admin panel</a>`);
  if (user) links.push(`<a class="link" href="${url('pages/orders.html')}">My orders</a>`);
  if (user) links.push(notificationLink());
  if (user) links.push(`<a class="link" href="${url('pages/account.html')}">Account</a>`);
  if (user) links.push(`<button class="link" id="signOutBtn">Sign out (${esc(user.name.split(' ')[0])})</button>`);
  links.push(`<a class="link" href="${url('pages/cart.html')}">Cart (<span id="cartCount">${cartCount()}</span>)</a>`);

  $('#siteHeader').innerHTML = `
    ${settings.announcement ? `<div class="topbar">${esc(settings.announcement)}</div>` : ''}
    <header class="header">
      <a class="logo" href="${url('index.html')}">${esc(settings.shop_name)}<small>${esc(settings.tagline)}</small></a>
      ${search ? '<form class="search" id="searchForm" role="search"><input id="q" type="search" placeholder="Search bags and caps…" aria-label="Search bags and caps"><button>Search</button></form>' : '<span class="spacer"></span>'}
      <nav class="actions" aria-label="Account">${links.join('')}</nav>
    </header>`;

  const contact = [settings.contact_email, settings.contact_phone].filter(Boolean).map(esc).join('<br>');
  $('#siteFooter').innerHTML = `
    <footer class="footer">
      <div><b>${esc(settings.shop_name)}</b><p>${esc(settings.footer_about)}</p></div>
      <div><b>Shop</b><p>Bags<br>Caps<br>Men · Women · Kids</p></div>
      <div><b>Help</b><p>Track order<br>Returns &amp; refunds<br>Size guide</p></div>
      <div><b>Contact</b><p>${contact || 'About us<br>Contact us'}</p></div>
      <small>© ${new Date().getFullYear()} ${esc(settings.shop_name)}. All rights reserved.</small>
    </footer>`;

  $('#signOutBtn')?.addEventListener('click', signOut);
  if (user && !user.emailVerified) showEmailBanner();
  watchNotifications(count => toast(count === 1 ? 'You have a new notification' : `You have ${count} new notifications`));
  return settings;
}
