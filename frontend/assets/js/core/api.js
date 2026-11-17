// One place for every call to the backend.
import { API_BASE, url } from './config.js';
import { getUser, clearAuth } from './auth.js';

// The forgery-check value (see CsrfSupport.java). The server sends it in the X-XSRF-TOKEN header of
// every answer; it is kept here and sent back on every request that changes something.
let csrfToken = '';

// Keeps the forgery-check value from an answer, when the answer carries one.
function rememberCsrf(response) {
  const value = response.headers.get('X-XSRF-TOKEN');
  if (value) csrfToken = value;
}

// Reads the value of a cookie the page is allowed to see, or '' when it is not set.
// Only used when no answer has supplied the forgery-check value yet.
function readCookie(name) {
  const match = document.cookie.split('; ').find(part => part.startsWith(name + '='));
  return match ? decodeURIComponent(match.slice(name.length + 1)) : '';
}

// Asks the server for a fresh forgery-check value, by loading the site text (an open, harmless request).
async function refreshCsrf() {
  try {
    rememberCsrf(await fetch(API_BASE + '/settings', { credentials: 'include' }));
  } catch { /* the request after this one will report that the server cannot be reached */ }
}

// Sends a request to the backend and returns the JSON it answers with.
//   path       the address after /api, for example '/products'
//   method     GET (default), POST, PUT, PATCH or DELETE
//   body       an object to send as JSON
//   form       a FormData to send instead of JSON, used for uploading a photo
//   anonymous  true for sign-in, register and password reset: a 401 there means "wrong password",
//              not "your sign-in has ended"
// The sign-in itself travels in a cookie the browser attaches by itself; page scripts cannot read it.
// If the server refuses a request because the forgery-check value is missing or out of date, a fresh
// value is fetched and the request is sent once more.
// Throws an Error whose message is ready to show to the user.
export async function api(path, options = {}) {
  const { method = 'GET', body, form, anonymous = false, isRetry = false } = options;
  const headers = {};
  if (body !== undefined) headers['Content-Type'] = 'application/json';
  if (method !== 'GET') {
    if (!csrfToken) await refreshCsrf();
    headers['X-XSRF-TOKEN'] = csrfToken || readCookie('XSRF-TOKEN');
  }

  let response;
  try {
    response = await fetch(API_BASE + path, {
      method, headers, credentials: 'include',
      body: form ?? (body === undefined ? undefined : JSON.stringify(body)),
    });
  } catch {
    throw new Error('Cannot reach the server. Check that the backend is running.');
  }
  rememberCsrf(response);

  if (response.status === 401 && !anonymous && getUser()) {
    // The sign-in expired, the password was reset or the account was switched off: start again.
    clearAuth();
    location.href = url('pages/login.html');
    throw new Error('Please sign in again.');
  }

  const data = response.status === 204 ? null : await response.json().catch(() => null);

  if (response.status === 403 && data && data.csrf && !isRetry) {
    await refreshCsrf();
    return api(path, { ...options, isRetry: true });
  }

  if (!response.ok) {
    if (data === null) {
      // Not JSON at all: this answer did not come from the Snap & Pack server.
      const error = new Error(`Unexpected answer from ${API_BASE} (HTTP ${response.status}). `
        + 'Check that the address in your browser is the one run.sh or run.bat printed, '
        + 'and that no other program is using that port.');
      error.data = {};
      throw error;
    }
    const fallback = { 401: 'Please sign in first.', 403: 'You do not have permission to do that.', 404: 'Not found.', 429: 'Too many requests. Wait a minute and try again.' }[response.status];
    const error = new Error(data.message || fallback || 'Something went wrong. Please try again.');
    // The server's full answer, for the few callers that need more than the message
    // (the sign-in page looks for mfaRequired).
    error.data = data;
    throw error;
  }
  return data;
}
