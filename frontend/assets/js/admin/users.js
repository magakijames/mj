// Admin users page. The admin adds staff, changes roles and switches accounts on or off.
// Support staff see the customer list only, without the controls.
import { api } from '../core/api.js';
import { ROLES, ROLE_NOTES } from '../core/config.js';
import { $, esc, dateTime, toast, roleLabel } from '../core/ui.js';
import { openAdminPage, showError } from './layout.js';

let me = null;
let isAdmin = false;

async function load() {
  try {
    render(await api('/users'));
  } catch (error) {
    showError('#userTable', error);
  }
}

function render(users) {
  $('#userCount').textContent = users.length + (users.length === 1 ? ' account' : ' accounts');
  $('#userTable').innerHTML = `<table class="tbl">
    <thead><tr><th>Name</th><th>Email</th><th>Role</th><th>Status</th><th>Joined</th>${isAdmin ? '<th><span class="visually-hidden">Actions</span></th>' : ''}</tr></thead>
    <tbody>${users.map(u => {
      const self = u.id === me.id;
      const role = isAdmin && !self
        ? `<select data-role="${u.id}" aria-label="Role for ${esc(u.name)}">${Object.entries(ROLES).map(([value, label]) =>
            `<option value="${value}" ${value === u.role ? 'selected' : ''}>${label}</option>`).join('')}</select>`
        : `<span class="badge">${roleLabel(u.role)}</span>`;
      const action = !isAdmin ? '' : `<td class="actions-cell">${self ? '<span class="meta">You</span>'
        : `<button class="btn small ${u.active ? 'danger' : 'ghost'}" data-active="${u.id}" data-to="${!u.active}">${u.active ? 'Deactivate' : 'Activate'}</button>`}</td>`;
      return `<tr>
        <td>${esc(u.name)}</td>
        <td>${esc(u.email)}</td>
        <td>${role}</td>
        <td><span class="status ${u.active ? 'delivered' : 'cancelled'}">${u.active ? 'Active' : 'Deactivated'}</span></td>
        <td>${dateTime(u.createdAt)}</td>
        ${action}
      </tr>`;
    }).join('')}</tbody></table>`;
}

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

document.addEventListener('click', async e => {
  if (e.target.closest('#addUser')) {
    $('#userForm').reset();
    $('#formError').textContent = '';
    showRoleNote();
    $('#userModal').hidden = false;
    $('#uName').focus();
    return;
  }
  if (e.target.closest('[data-close]')) { $('#userModal').hidden = true; return; }
  const toggle = e.target.closest('[data-active]');
  if (toggle) {
    const active = toggle.dataset.to === 'true';
    if (!active && !confirm('Deactivate this account? They will be signed out and unable to sign in.')) return;
    try {
      await api(`/users/${toggle.dataset.active}/active`, { method: 'PATCH', body: { active } });
      toast(active ? 'Account activated' : 'Account deactivated');
    } catch (error) {
      toast(error.message);
    }
    load();
  }
});

const showRoleNote = () => { $('#roleNote').textContent = ROLE_NOTES[$('#uRole').value]; };
$('#uRole').innerHTML = Object.entries(ROLES).filter(([value]) => value !== 'CUSTOMER')
  .map(([value, label]) => `<option value="${value}" ${value === 'PRODUCT_MANAGER' ? 'selected' : ''}>${label}</option>`).join('');
$('#uRole').addEventListener('change', showRoleNote);

$('#userForm').addEventListener('submit', async e => {
  e.preventDefault();
  try {
    await api('/users', { method: 'POST', body: { name: $('#uName').value.trim(), email: $('#uEmail').value.trim(), password: $('#uPassword').value, role: $('#uRole').value } });
    $('#userModal').hidden = true;
    toast('Account created');
    load();
  } catch (error) {
    $('#formError').textContent = error.message;
  }
});

me = await openAdminPage('users');
if (me) {
  isAdmin = me.role === 'ADMIN';
  $('#pageTitle').textContent = isAdmin ? 'Users' : 'Customers';
  $('#addUser').hidden = !isAdmin;
  $('#intro').textContent = isAdmin
    ? 'Everyone with an account. Add staff, change what they can do, or switch an account off.'
    : 'Customer accounts. Only the admin can change them.';
  load();
}
