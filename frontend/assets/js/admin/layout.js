// Shared by every admin page: checks who is signed in and draws the admin header and menu.
import { api } from '../core/api.js';
import { url, ADMIN_PAGES } from '../core/config.js';
import { getUser, getToken, setAuth, isStaff, requireSignIn, signOut } from '../core/auth.js';
import { $, esc, roleLabel } from '../core/ui.js';

// Returns the signed-in staff member, or null after sending the visitor elsewhere.
// The role is re-read from the server, so a change made by the admin shows up straight away.
export async function openAdminPage(pageKey) {
  if (!requireSignIn()) return null;
  let user;
  try {
    user = await api('/auth/me');
  } catch (error) {
    document.body.innerHTML = `<p class="empty">${esc(error.message)}</p>`;
    return null;
  }
  setAuth({ token: getToken(), user });

  const page = ADMIN_PAGES.find(p => p.key === pageKey);
  if (!isStaff(user)) { location.href = url('index.html'); return null; }
  if (!page.roles.includes(user.role)) { location.href = url('admin/index.html'); return null; }

  const menu = ADMIN_PAGES.filter(p => p.roles.includes(user.role));
  $('#adminHeader').innerHTML = `
    <header class="header admin-header">
      <a class="logo" href="${url('admin/index.html')}">Snap &amp; Pack<small>Admin area</small></a>
      <nav class="admin-nav" aria-label="Admin">
        ${menu.map(p => `<a href="${url(p.path)}" ${p.key === pageKey ? 'class="on" aria-current="page"' : ''}>${p.key === 'users' && user.role !== 'ADMIN' ? 'Customers' : p.label}</a>`).join('')}
      </nav>
      <div class="actions">
        <span class="who">${esc(user.name)} <span class="badge">${roleLabel(user.role)}</span></span>
        <a class="link" href="${url('index.html')}">View store</a>
        <button class="link" id="signOutBtn">Sign out</button>
      </div>
    </header>`;
  $('#signOutBtn').addEventListener('click', signOut);
  return user;
}

export function showError(target, error) {
  $(target).innerHTML = `<p class="empty">${esc(error.message)}</p>`;
}
