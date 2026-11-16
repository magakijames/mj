// "Confirm your email" page, opened from the link sent to a new account (verify-email.html?token=...).
import { api } from '../core/api.js';
import { getUser, setAuth } from '../core/auth.js';
import { $, mountStoreChrome } from '../core/ui.js';

const token = new URLSearchParams(location.search).get('token');

// Sends the code from the link to the backend and shows the result.
async function confirm() {
  if (!token) {
    $('#result').textContent = 'This page needs a confirmation link. Sign in and use "Send the link again".';
    return;
  }
  try {
    await api('/auth/verify-email', { method: 'POST', anonymous: true, body: { token } });
    $('#result').textContent = 'Thank you. Your email address is confirmed and you can place orders.';
    $('#result').classList.add('ok');
    // If this browser is signed in as that account, remember that it is now confirmed.
    const user = getUser();
    if (user) setAuth({ ...user, emailVerified: true });
  } catch (error) {
    $('#result').textContent = error.message;
  }
}

await confirm();
await mountStoreChrome();
