// Create-account page. New accounts are always customers; staff are added by the admin.
import { api } from '../core/api.js';
import { setAuth, getUser, goAfterSignIn } from '../core/auth.js';
import { $, mountStoreChrome } from '../core/ui.js';

await mountStoreChrome();

// Already signed in: no need for this page.
if (getUser()) goAfterSignIn(getUser());

// Keep the "next" address when moving to the sign-in page.
$('#loginLink').href += location.search;

// Sends the new account's details to the backend; on success saves the sign-in and moves on.
$('#registerForm').addEventListener('submit', async e => {
  e.preventDefault();
  const error = $('#formError');
  error.textContent = '';
  try {
    const auth = await api('/auth/register', {
      method: 'POST', anonymous: true,
      body: { name: $('#name').value.trim(), email: $('#email').value.trim(), password: $('#password').value },
    });
    setAuth(auth);
    goAfterSignIn(auth.user);
  } catch (err) {
    error.textContent = err.message;
  }
});
