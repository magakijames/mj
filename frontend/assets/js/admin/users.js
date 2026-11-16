// Admin users page, with staff and customers on separate tabs.
// The super admin adds staff, changes roles, switches accounts on or off and makes password reset links.
// Support staff see the customer list only, without the controls.
import { api } from '../core/api.js';
import { ROLES, ROLE_NOTES } from '../core/config.js';
import { $, esc, dateTime, toast, roleLabel } from '../core/ui.js';
import { openAdminPage, showError } from './layout.js';

let me = null;
let isAdmin = false;
let users = [];
let tab = 'staff';

// Fetches the accounts from the backend and draws the page.
async function load() {
  try {
    users = await api('/users');
    render();
  } catch (error) {
    showError('#userTable', error);
  }
}

// Draws the two tabs with their counts, and the table for the tab that is open.
function render() {
  const staff = users.filter(u => u.role !== 'CUSTOMER');
  const customers = users.filter(u => u.role === 'CUSTOMER');
  $('#tabs').innerHTML = isAdmin
    ? `<button data-tab="staff" class="${tab === 'staff' ? 'on' : ''}">Staff (${staff.length})</button>
       <button data-tab="customers" class="${tab === 'customers' ? 'on' : ''}">Customers (${customers.length})</button>`
    : '';
  const list = tab === 'staff' ? staff : customers;
  const customerCount = customers.length + (customers.length === 1 ? ' customer' : ' customers');
  $('#userCount').textContent = isAdmin ? `${staff.length} staff · ${customerCount}` : customerCount;
  $('#tabNote').textContent = tab === 'staff'
    ? 'People who work for the shop. Their role decides which admin pages they can use.'
    : 'People who registered on the store to shop.';
  $('#userTable').innerHTML = list.length ? table(list) : `<p class="empty">${tab === 'staff' ? 'No staff yet.' : 'No customers yet.'}</p>`;
}

// The HTML table for a list of accounts. The role and action columns are for the super admin only.
function table(list) {
  return `<table class="tbl">
    <thead><tr><th>Name</th><th>Email</th><th>Role</th><th>Status</th><th>Joined</th>${isAdmin ? '<th><span class="visually-hidden">Actions</span></th>' : ''}</tr></thead>
    <tbody>${list.map(userRow).join('')}</tbody></table>`;
}

// The HTML for one account's row. The super admin's own row has no controls, so they cannot lock themselves out.
function userRow(u) {
  const self = u.id === me.id;
  const role = isAdmin && !self
    ? `<select data-role="${u.id}" aria-label="Role for ${esc(u.name)}">${Object.entries(ROLES).map(([value, label]) =>
        `<option value="${value}" ${value === u.role ? 'selected' : ''}>${label}</option>`).join('')}</select>`
    : `<span class="badge">${roleLabel(u.role)}</span>`;
  const status = (!u.active ? '<span class="status cancelled">Deactivated</span>'
    : u.locked ? '<span class="status pending">Locked</span>'
    : '<span class="status delivered">Active</span>')
    + (u.emailVerified ? '' : ' <span class="status pending">Email not confirmed</span>')
    + (u.mfaEnabled ? ' <span class="status shipped">Two-step on</span>' : '');
  const actions = !isAdmin ? '' : `<td class="actions-cell">${self ? '<span class="meta">You</span>' : `<div class="btn-row">
      ${u.emailVerified ? '' : `<button class="btn small ghost" data-verify="${u.id}">Confirm email</button>`}
      ${u.mfaEnabled ? `<button class="btn small ghost" data-mfa-reset="${u.id}">Reset two-step</button>` : ''}
      ${u.active ? `<button class="btn small ghost" data-reset="${u.id}">Reset password</button>` : ''}
      <button class="btn small ${u.active ? 'danger' : 'ghost'}" data-active="${u.id}" data-to="${!u.active}">${u.active ? 'Deactivate' : 'Activate'}</button>
    </div>`}</td>`;
  return `<tr>
    <td>${esc(u.name)}</td>
    <td>${esc(u.email)}</td>
    <td>${role}</td>
    <td>${status}</td>
    <td>${dateTime(u.createdAt)}</td>
    ${actions}
  </tr>`;
}

// Saves a new role when a role drop-down is changed.
$('#userTable').addEventListener('change', async e => {
  const select = e.target.closest('[data-role]');
  if (!select) return;
  try {
    await api(`/users/${select.dataset.role}/role`, { method: 'PATCH', body: { role: select.value } });
    toast('Role changed to ' + roleLabel(select.value));
  } catch (error) {
    toast(error.message);
  }
  load();
});

// Opens the empty "Add staff account" form.
function openUserForm() {
  $('#userForm').reset();
  $('#formError').textContent = '';
  showRoleNote();
  $('#userModal').hidden = false;
  $('#uName').focus();
}

// Switches an account off (after asking) or back on.
async function setActive(id, active) {
  if (!active && !confirm('Deactivate this account? They will be signed out and unable to sign in.')) return;
  try {
    await api(`/users/${id}/active`, { method: 'PATCH', body: { active } });
    toast(active ? 'Account activated' : 'Account deactivated');
  } catch (error) {
    toast(error.message);
  }
  load();
}

// Asks the backend for a one-time password reset link for this account and shows it to the admin.
async function makeResetLink(id) {
  const user = users.find(u => u.id === id);
  try {
    const result = await api(`/users/${id}/password-reset`, { method: 'POST' });
    $('#resetFor').textContent = `${user.name} (${user.email})`;
    $('#resetLink').value = result.link;
    $('#resetExpiry').textContent = dateTime(result.expiresAt);
    $('#resetModal').hidden = false;
    $('#resetLink').select();
  } catch (error) {
    toast(error.message);
  }
}

// Marks an account's email as confirmed by hand, for example while email is not connected.
async function confirmEmail(id) {
  const user = users.find(u => u.id === id);
  if (!confirm(`Mark ${user.email} as confirmed? Only do this if you know the address belongs to them.`)) return;
  try {
    await api(`/users/${id}/verify-email`, { method: 'POST' });
    toast('Email marked as confirmed');
  } catch (error) {
    toast(error.message);
  }
  load();
}

// Removes two-step sign-in from an account whose owner lost their phone and recovery codes.
async function resetTwoStep(id) {
  const user = users.find(u => u.id === id);
  if (!confirm(`Remove two-step sign-in from ${user.name}'s account? They will have to set it up again.`)) return;
  try {
    await api(`/users/${id}/mfa-reset`, { method: 'POST' });
    toast('Two-step sign-in removed');
  } catch (error) {
    toast(error.message);
  }
  load();
}

// Copies the reset link so the admin can paste it into a message.
async function copyResetLink() {
  try {
    await navigator.clipboard.writeText($('#resetLink').value);
    toast('Link copied');
  } catch {
    $('#resetLink').select();
    toast('Select the link and copy it');
  }
}

// One click handler for the page's buttons.
document.addEventListener('click', e => {
  const tabButton = e.target.closest('[data-tab]');
  if (tabButton) { tab = tabButton.dataset.tab; return render(); }
  if (e.target.closest('#addUser')) return openUserForm();
  if (e.target.closest('#copyLink')) return copyResetLink();
  const close = e.target.closest('[data-close]');
  if (close) { close.closest('.modal').hidden = true; return; }
  const toggle = e.target.closest('[data-active]');
  if (toggle) return setActive(toggle.dataset.active, toggle.dataset.to === 'true');
  const verify = e.target.closest('[data-verify]');
  if (verify) return confirmEmail(+verify.dataset.verify);
  const mfaReset = e.target.closest('[data-mfa-reset]');
  if (mfaReset) return resetTwoStep(+mfaReset.dataset.mfaReset);
  const reset = e.target.closest('[data-reset]');
  if (reset) makeResetLink(+reset.dataset.reset);
});

// Shows what the role chosen in the "Add staff account" form can do.
const showRoleNote = () => { $('#roleNote').textContent = ROLE_NOTES[$('#uRole').value]; };
$('#uRole').innerHTML = Object.entries(ROLES).filter(([value]) => value !== 'CUSTOMER')
  .map(([value, label]) => `<option value="${value}" ${value === 'INVENTORY_MANAGER' ? 'selected' : ''}>${label}</option>`).join('');
$('#uRole').addEventListener('change', showRoleNote);

// Creates the staff account from the form.
$('#userForm').addEventListener('submit', async e => {
  e.preventDefault();
  try {
    await api('/users', { method: 'POST', body: { name: $('#uName').value.trim(), email: $('#uEmail').value.trim(), password: $('#uPassword').value, role: $('#uRole').value } });
    $('#userModal').hidden = true;
    tab = 'staff';
    toast('Account created');
    load();
  } catch (error) {
    $('#formError').textContent = error.message;
  }
});

me = await openAdminPage('users');
if (me) {
  isAdmin = me.role === 'SUPER_ADMIN';
  tab = isAdmin ? 'staff' : 'customers';
  $('#pageTitle').textContent = isAdmin ? 'Users' : 'Customers';
  $('#addUser').hidden = !isAdmin;
  if (!isAdmin) $('#readOnlyNote').hidden = false;
  load();
}
