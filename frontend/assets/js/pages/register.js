// Create-account page. New accounts are always customers; staff are added by the admin.
import { api } from '../core/api.js';
import { setAuth, getUser, goAfterSignIn } from '../core/auth.js';
import { $, mountStoreChrome } from '../core/ui.js';

mountStoreChrome();
if (getUser()) goAfterSignIn(getUser());

$('#loginLink').href += location.search;

$('#registerForm').addEventListener('submit', async e => {
  e.preventDefault();
  const error = $('#formError');
  error.textContent = '';
  try {
    const auth = await api('/auth/register', {
      method: 'POST',
      body: { name: $('#name').value.trim(), email: $('#email').value.trim(), password: $('#password').value },
    });
    setAuth(auth);
    goAfterSignIn(auth.user);
  } catch (err) {
    error.textContent = err.message;
  }
});
