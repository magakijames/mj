// One place for every call to the backend.
import { API_BASE, url } from './config.js';
import { getToken, clearAuth } from './auth.js';

// Sends a request to the backend and returns the JSON it answers with.
//   path       the address after /api, for example '/products'
//   method     GET (default), POST, PUT, PATCH or DELETE
//   body       an object to send as JSON
//   anonymous  true for sign-in, register and password reset, which must not send a sign-in token
// Throws an Error whose message is ready to show to the user.
export async function api(path, { method = 'GET', body, anonymous = false } = {}) {
  const headers = {};
  const token = anonymous ? null : getToken();
  if (token) headers.Authorization = 'Bearer ' + token;
  if (body !== undefined) headers['Content-Type'] = 'application/json';

  let response;
  try {
    response = await fetch(API_BASE + path, { method, headers, body: body === undefined ? undefined : JSON.stringify(body) });
  } catch {
    throw new Error('Cannot reach the server. Check that the backend is running.');
  }

  if (response.status === 401 && token) {
    // The token expired, the password was reset or the account was switched off: sign out and start again.
    clearAuth();
    location.href = url('pages/login.html');
    throw new Error('Please sign in again.');
  }

  const data = response.status === 204 ? null : await response.json().catch(() => null);
  if (!response.ok) {
    const fallback = { 401: 'Please sign in first.', 403: 'You do not have permission to do that.', 404: 'Not found.' }[response.status];
    throw new Error((data && data.message) || fallback || 'Something went wrong. Please try again.');
  }
  return data;
}
