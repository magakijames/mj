// The site text the admin can edit (shop name, announcement, promise boxes and so on).
// It is loaded from the backend and remembered in the browser so pages can draw without waiting.
import { api } from './api.js';
import { DEFAULT_SETTINGS } from './config.js';

const KEY = 'sp_settings';

// The settings last received from the server, on top of the built-in defaults.
export function cachedSettings() {
  try {
    return { ...DEFAULT_SETTINGS, ...JSON.parse(localStorage.getItem(KEY)) };
  } catch {
    return { ...DEFAULT_SETTINGS };
  }
}

// Remembers settings in the browser. Called after loading and after the admin saves changes.
export function rememberSettings(settings) {
  localStorage.setItem(KEY, JSON.stringify(settings));
}

// Fetches the current settings from the backend. Falls back to the remembered ones if it cannot be reached.
export async function loadSettings() {
  try {
    const settings = await api('/settings', { anonymous: true });
    rememberSettings(settings);
    return { ...DEFAULT_SETTINGS, ...settings };
  } catch {
    return cachedSettings();
  }
}
