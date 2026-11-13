// Who is signed in. The token and user details are kept in the browser between pages.
import { url } from './config.js';

const KEY = 'sp_auth';

function read() {
  try { return JSON.parse(localStorage.getItem(KEY)); } catch { return null; }
}

export const getUser = () => read()?.user ?? null;
export const getToken = () => read()?.token ?? null;
export const setAuth = auth => localStorage.setItem(KEY, JSON.stringify(auth));
export const clearAuth = () => localStorage.removeItem(KEY);
export const isStaff = user => !!user && user.role !== 'CUSTOMER';

export function signOut() {
  clearAuth();
  location.href = url('index.html');
}

// Sends visitors who are not signed in to the sign-in page, then back to where they were.
export function requireSignIn() {
  const user = getUser();
  if (!user) {
    location.href = url('pages/login.html') + '?next=' + encodeURIComponent(location.href);
    return null;
  }
  return user;
}

// After signing in: back to the page that asked for it, else the admin area for staff, else the store.
export function goAfterSignIn(user) {
  const next = new URLSearchParams(location.search).get('next');
  const sameSite = next && new URL(next, location.href).origin === location.origin;
  location.href = sameSite ? next : url(isStaff(user) ? 'admin/index.html' : 'index.html');
}
