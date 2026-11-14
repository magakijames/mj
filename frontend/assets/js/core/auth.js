// Who is signed in. The token and user details are kept in the browser between pages.
import { url } from './config.js';

const KEY = 'sp_auth';

// Reads the saved sign-in ({ token, user }) from the browser, or null when nobody is signed in.
function read() {
  try { return JSON.parse(localStorage.getItem(KEY)); } catch { return null; }
}

// The signed-in user's details, or null.
export const getUser = () => read()?.user ?? null;

// The sign-in token sent with each request, or null.
export const getToken = () => read()?.token ?? null;

// Saves a sign-in ({ token, user }) after signing in or registering.
export const setAuth = auth => localStorage.setItem(KEY, JSON.stringify(auth));

// Forgets the saved sign-in.
export const clearAuth = () => localStorage.removeItem(KEY);

// True for every role except customer.
export const isStaff = user => !!user && user.role !== 'CUSTOMER';

// Signs out and goes back to the store home page.
export function signOut() {
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
