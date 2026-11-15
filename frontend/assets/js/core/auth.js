// Who is signed in.
// The sign-in itself is a cookie that page scripts cannot read (see SessionCookie.java). What is kept
// here is only the user's name and role, so pages can show the right links without asking the server first.
import { url } from './config.js';

const KEY = 'sp_user';

// The signed-in user's details ({ id, name, email, role }), or null when nobody is signed in.
export function getUser() {
  try { return JSON.parse(localStorage.getItem(KEY)); } catch { return null; }
}

// Remembers who signed in, after signing in or registering.
export const setAuth = user => localStorage.setItem(KEY, JSON.stringify(user));

// Forgets who was signed in.
export const clearAuth = () => localStorage.removeItem(KEY);

// True for every role except customer.
export const isStaff = user => !!user && user.role !== 'CUSTOMER';

// Signs out: tells the server to end the sign-in and delete its cookie, then goes to the store home page.
export async function signOut() {
  const { api } = await import('./api.js');
  try { await api('/auth/logout', { method: 'POST' }); } catch { /* the sign-in had already ended */ }
  clearAuth();
  location.href = url('index.html');
}

// Sends visitors who are not signed in to the sign-in page, then back to where they were.
// Returns the user when signed in, null otherwise.
export function requireSignIn() {
  const user = getUser();
  if (!user) {
    location.href = url('pages/login.html') + '?next=' + encodeURIComponent(location.href);
    return null;
  }
  return user;
}

// After signing in: back to the page that asked for it, else the admin area for staff, else the store.
// The "next" address is only followed when it is on this same site.
export function goAfterSignIn(user) {
  const next = new URLSearchParams(location.search).get('next');
  const sameSite = next && new URL(next, location.href).origin === location.origin;
  location.href = sameSite ? next : url(isStaff(user) ? 'admin/index.html' : 'index.html');
}
