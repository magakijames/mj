// One place for every call to the backend.
import { API_BASE, url } from './config.js';
import { getUser, clearAuth } from './auth.js';

// Reads the value of a cookie the page is allowed to see, or '' when it is not set.
function readCookie(name) {
  const match = document.cookie.split('; ').find(part => part.startsWith(name + '='));
  return match ? decodeURIComponent(match.slice(name.length + 1)) : '';
}

// Sends a request to the backend and returns the JSON it answers with.
//   path       the address after /api, for example '/products'
//   method     GET (default), POST, PUT, PATCH or DELETE
//   body       an object to send as JSON
//   form       a FormData to send instead of JSON, used for uploading a photo
//   anonymous  true for sign-in, register and password reset: a 401 there means "wrong password",
//              not "your sign-in has ended"
// The sign-in itself travels in a cookie the browser attaches by itself; page scripts cannot read it.
// Requests that change something also carry the X-XSRF-TOKEN header, copied from the XSRF-TOKEN
// cookie, which proves the request comes from this site and not from another website.
// Throws an Error whose message is ready to show to the user.
export async function api(path, { method = 'GET', body, form, anonymous = false } = {}) {
  const headers = {};
  if (body !== undefined) headers['Content-Type'] = 'application/json';
  if (method !== 'GET') headers['X-XSRF-TOKEN'] = readCookie('XSRF-TOKEN');

  let response;
  try {
    response = await fetch(API_BASE + path, {
      method, headers, credentials: 'include',
      body: form ?? (body === undefined ? undefined : JSON.stringify(body)),
    });
  } catch {
    throw new Error('Cannot reach the server. Check that the backend is running.');
  }

  if (response.status === 401 && !anonymous && getUser()) {
    // The sign-in expired, the password was reset or the account was switched off: start again.
    clearAuth();
    location.href = url('pages/login.html');
    throw new Error('Please sign in again.');
  }

  const data = response.status === 204 ? null : await response.json().catch(() => null);
  if (!response.ok) {
    const fallback = { 401: 'Please sign in first.', 403: 'You do not have permission to do that.', 404: 'Not found.', 429: 'Too many requests. Wait a minute and try again.' }[response.status];
    const error = new Error((data && data.message) || fallback || 'Something went wrong. Please try again.');
    // The server's full answer, for the few callers that need more than the message
    // (the sign-in page looks for mfaRequired).
    error.data = data || {};
    throw error;
  }
  return data;
}
