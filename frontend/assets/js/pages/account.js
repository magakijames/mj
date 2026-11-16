// "Account" page: the signed-in user's details, email confirmation and two-step sign-in.
import { api } from '../core/api.js';
import { url } from '../core/config.js';
import { requireSignIn, setAuth, isStaff } from '../core/auth.js';
import { $, esc, toast, roleLabel, mountStoreChrome } from '../core/ui.js';

await mountStoreChrome();

let user = null;

// Fetches the account from the server, so the page always shows the current state, and draws it.
async function load() {
  user = await api('/auth/me');
  setAuth(user);
  render();
}

// Draws the details panel and whichever two-step panel fits: "off" or "on".
function render() {
  $('#details').innerHTML = `
    <p><b>${esc(user.name)}</b><br>${esc(user.email)}<br><span class="badge">${roleLabel(user.role)}</span></p>
    <p>${user.emailVerified
      ? '<span class="status delivered">Email confirmed</span>'
      : '<span class="status pending">Email not confirmed</span> <button class="btn small ghost" id="resendVerify">Send the link again</button>'}</p>`;

  $('#mfaRequiredNote').hidden = !user.mfaSetupRequired;
  $('#mfaOff').hidden = user.mfaEnabled;
  $('#mfaOn').hidden = !user.mfaEnabled;
  $('#mfaScan').hidden = true;
  $('#mfaCodes').hidden = true;
  $('#adminLink').hidden = !(isStaff(user) && !user.mfaSetupRequired);
  $('#adminLink').href = url('admin/index.html');
}

// Shows an error under a form.
const showError = (id, error) => { $(id).textContent = error.message; };

// "Send the link again" for an unconfirmed email address.
document.addEventListener('click', async e => {
  if (!e.target.closest('#resendVerify')) return;
  try {
    const { message } = await api('/auth/resend-verification', { method: 'POST' });
    toast(message);
  } catch (error) {
    toast(error.message);
  }
});

// Step 1 of switching on: confirm the password, then show the QR code and the secret to type by hand.
$('#mfaStart').addEventListener('submit', async e => {
  e.preventDefault();
  $('#mfaStartError').textContent = '';
  try {
    const setup = await api('/auth/mfa/setup', { method: 'POST', body: { password: $('#mfaPassword').value } });
    $('#mfaQr').src = setup.qrImage;
    $('#mfaSecret').textContent = setup.secret.replace(/(.{4})/g, '$1 ').trim();
    $('#mfaOff').hidden = true;
    $('#mfaScan').hidden = false;
    $('#mfaCode').focus();
    e.target.reset();
  } catch (error) {
    showError('#mfaStartError', error);
  }
});

// Step 2 of switching on: a code from the app proves it is set up. The recovery codes are then shown once.
$('#mfaConfirm').addEventListener('submit', async e => {
  e.preventDefault();
  $('#mfaConfirmError').textContent = '';
  try {
    const { recoveryCodes } = await api('/auth/mfa/enable', { method: 'POST', body: { code: $('#mfaCode').value.trim() } });
    await load();
    $('#recoveryList').innerHTML = recoveryCodes.map(code => `<li>${esc(code)}</li>`).join('');
    $('#mfaCodes').hidden = false;
    e.target.reset();
    toast('Two-step sign-in is on');
  } catch (error) {
    showError('#mfaConfirmError', error);
  }
});

// Switching off needs the password and a current code.
$('#mfaDisable').addEventListener('submit', async e => {
  e.preventDefault();
  $('#mfaDisableError').textContent = '';
  try {
    await api('/auth/mfa/disable', { method: 'POST', body: { password: $('#offPassword').value, code: $('#offCode').value.trim() } });
    e.target.reset();
    await load();
    toast('Two-step sign-in is off');
  } catch (error) {
    showError('#mfaDisableError', error);
  }
});

if (requireSignIn()) {
  try {
    await load();
  } catch (error) {
    $('#details').innerHTML = `<p class="empty">${esc(error.message)}</p>`;
  }
}
