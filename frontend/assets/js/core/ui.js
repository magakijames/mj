// Small helpers used on every page, plus the shared header and footer.
import { url, ROLES } from './config.js';
import { getUser, isStaff, signOut } from './auth.js';
import { cartCount } from './cart-store.js';

export const $ = selector => document.querySelector(selector);

// Makes text safe to place inside HTML.
export const esc = value => String(value ?? '').replace(/[&<>"']/g, c => ({ '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;' }[c]));

export const money = amount => 'KSh ' + Math.round(Number(amount)).toLocaleString('en-KE');
export const dateTime = iso => new Date(iso).toLocaleString('en-KE', { dateStyle: 'medium', timeStyle: 'short' });

// Product images are stored as a path inside the site (assets/images/...) or as a full web address.
export const imageUrl = image => (!image ? '' : /^https?:\/\//.test(image) ? image : url(image));

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

export function refreshCartCount() {
  const count = $('#cartCount');
  if (count) count.textContent = cartCount();
}

// Draws the top bar, header and footer of the store pages. Pass { search: true } on the home page.
export function mountStoreChrome({ search = false } = {}) {
  const user = getUser();
  const links = [];
  if (!user) links.push(`<a class="link" href="${url('pages/login.html')}">Sign in</a>`);
  if (user && isStaff(user)) links.push(`<a class="link" href="${url('admin/index.html')}">Admin panel</a>`);
  if (user) links.push(`<a class="link" href="${url('pages/orders.html')}">My orders</a>`);
  if (user) links.push(`<button class="link" id="signOutBtn">Sign out (${esc(user.name.split(' ')[0])})</button>`);
  links.push(`<a class="link" href="${url('pages/cart.html')}">Cart (<span id="cartCount">${cartCount()}</span>)</a>`);

  $('#siteHeader').innerHTML = `
    <div class="topbar">Free shipping on orders over KSh 6,500 · Easy 30-day returns</div>
    <header class="header">
      <a class="logo" href="${url('index.html')}">Snap &amp; Pack<small>Bags &amp; caps for every day</small></a>
      ${search ? '<form class="search" id="searchForm" role="search"><input id="q" type="search" placeholder="Search bags and caps…" aria-label="Search bags and caps"><button>Search</button></form>' : '<span class="spacer"></span>'}
      <nav class="actions" aria-label="Account">${links.join('')}</nav>
    </header>`;

  $('#siteFooter').innerHTML = `
    <footer class="footer">
      <div><b>Snap &amp; Pack</b><p>Your one-stop shop for bags and caps.</p></div>
      <div><b>Shop</b><p>Bags<br>Caps<br>Men · Women · Kids</p></div>
      <div><b>Help</b><p>Track order<br>Returns &amp; refunds<br>Size guide</p></div>
      <div><b>Company</b><p>About us<br>Contact us</p></div>
      <small>© ${new Date().getFullYear()} Snap &amp; Pack. All rights reserved.</small>
    </footer>`;

  $('#signOutBtn')?.addEventListener('click', signOut);
}

export const roleLabel = role => ROLES[role] || role;
