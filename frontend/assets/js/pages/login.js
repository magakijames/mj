// Sign-in page, including the second step for accounts with two-step sign-in.
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

// Shows the box for the authenticator code, when the server says the account needs one.
function askForCode() {
  $('#codeField').hidden = false;
  $('#code').required = true;
  $('#code').focus();
}

// Sends the email and password (and the code, once asked for) to the backend.
// On success remembers who signed in and moves on.
$('#loginForm').addEventListener('submit', async e => {
  e.preventDefault();
  const error = $('#formError');
  error.textContent = '';
  const body = { email: $('#email').value.trim(), password: $('#password').value };
  if (!$('#codeField').hidden) body.code = $('#code').value.trim();
  try {
    const user = await api('/auth/login', { method: 'POST', anonymous: true, body });
    setAuth(user);
    goAfterSignIn(user);
  } catch (err) {
    if (err.data.mfaRequired) {
      askForCode();
      $('#notice').textContent = err.message;
      $('#notice').hidden = false;
    } else {
      error.textContent = err.message;
      if (!$('#codeField').hidden) $('#code').select();
    }
  }
});
