// "Choose a new password" page, opened from a reset link (reset-password.html?token=...).
import { api } from '../core/api.js';
import { url } from '../core/config.js';
import { clearAuth } from '../core/auth.js';
import { $, mountStoreChrome } from '../core/ui.js';

await mountStoreChrome();

const token = new URLSearchParams(location.search).get('token');

// Without a code in the address there is nothing to reset.
if (!token) {
  $('#resetForm').hidden = true;
  $('#missing').hidden = false;
}

// Checks the two passwords match, sends the new one with the code, then goes to the sign-in page.
$('#resetForm').addEventListener('submit', async e => {
  e.preventDefault();
  const error = $('#formError');
  error.textContent = '';
  if ($('#password').value !== $('#confirm').value) {
    error.textContent = 'The two passwords do not match.';
    return;
  }
  try {
    await api('/auth/reset-password', { method: 'POST', anonymous: true, body: { token, password: $('#password').value } });
    clearAuth();
    location.href = url('pages/login.html') + '?reset=1';
  } catch (err) {
    error.textContent = err.message;
  }
});
