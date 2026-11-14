// "Forgot your password?" page: asks the backend to make a reset link for an email address.
import { api } from '../core/api.js';
import { $, mountStoreChrome } from '../core/ui.js';

await mountStoreChrome();

// Sends the email to the backend, then shows the same message whether or not the email has an account.
$('#forgotForm').addEventListener('submit', async e => {
  e.preventDefault();
  const error = $('#formError');
  error.textContent = '';
  try {
    await api('/auth/forgot-password', { method: 'POST', anonymous: true, body: { email: $('#email').value.trim() } });
    $('#forgotForm').hidden = true;
    $('#done').hidden = false;
  } catch (err) {
    error.textContent = err.message;
  }
});
