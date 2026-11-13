// Sign-in page.
import { api } from '../core/api.js';
import { setAuth, getUser, goAfterSignIn } from '../core/auth.js';
import { $, mountStoreChrome } from '../core/ui.js';

mountStoreChrome();
if (getUser()) goAfterSignIn(getUser());

// Keep the "next" address when moving to the register page.
$('#registerLink').href += location.search;

$('#loginForm').addEventListener('submit', async e => {
  e.preventDefault();
  const error = $('#formError');
  error.textContent = '';
  try {
    const auth = await api('/auth/login', { method: 'POST', body: { email: $('#email').value.trim(), password: $('#password').value } });
    setAuth(auth);
    goAfterSignIn(auth.user);
  } catch (err) {
    error.textContent = err.message;
  }
});
