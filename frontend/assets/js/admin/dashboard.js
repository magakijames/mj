// Admin dashboard: totals, and what this staff member's role covers.
import { api } from '../core/api.js';
import { url, ADMIN_PAGES, ROLE_NOTES } from '../core/config.js';
import { $, esc, money, roleLabel } from '../core/ui.js';
import { openAdminPage, showError, pageLabel } from './layout.js';

const user = await openAdminPage('dashboard');
if (user) {
  $('#welcome').textContent = `Welcome, ${user.name.split(' ')[0]}`;
  $('#roleNote').innerHTML = `You are signed in as <b>${roleLabel(user.role)}</b>. ${esc(ROLE_NOTES[user.role])}`;

  // A button for each admin page this role may open.
  $('#shortcuts').innerHTML = ADMIN_PAGES.filter(p => p.key !== 'dashboard' && p.roles.includes(user.role))
    .map(p => `<a class="btn" href="${url(p.path)}">${pageLabel(p, user)}</a>`).join('');

  // The totals across the top.
  try {
    const s = await api('/dashboard/stats');
    $('#stats').innerHTML = [['Products', s.products], ['Orders', s.orders], ['Customers', s.customers], ['Staff', s.staff], ['Revenue', money(s.revenue)]]
      .map(([label, value]) => `<div class="stat"><b>${value}</b>${label}</div>`).join('');
  } catch (error) {
    showError('#stats', error);
  }
}
