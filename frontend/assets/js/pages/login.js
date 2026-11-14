// Sign-in page.
import { api } from '../core/api.js';
import { setAuth, getUser, goAfterSignIn } from '../core/auth.js';
import { $, mountStoreChrome } from '../core/ui.js';

await mountStoreChrome();

// Already signed in: no need for this page.
if (getUser()) goAfterSignIn(getUser());

// Keep the "next" address when moving to the register page.
$('#registerLink').href += location.search;

// A note shown after a password reset.
if (new URLSearchParams(location.search).has('reset')) {
  $('#notice').textContent = 'Your password has been changed. Sign in with the new one.';
  $('#notice').hidden = false;
}

// Sends the email and password to the backend; on success saves the sign-in and moves on.
$('#loginForm').addEventListener('submit', async e => {
  e.preventDefault();
  const error = $('#formError');
  error.textContent = '';
  try {
    const auth = await api('/auth/login', { method: 'POST', anonymous: true, body: { email: $('#email').value.trim(), password: $('#password').value } });
    setAuth(auth);
    goAfterSignIn(auth.user);
  } catch (err) {
    error.textContent = err.message;
  }
});
