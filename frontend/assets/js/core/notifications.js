// The "Notifications" link in the header and its unseen count.
import { api } from './api.js';
import { url } from './config.js';
import { getUser } from './auth.js';

const CHECK_EVERY_MS = 30000;

// The HTML for the header link. Put it in the header, then call watchNotifications().
export const notificationLink = () =>
  `<a class="link bell" href="${url('pages/notifications.html')}">Notifications<span class="count" id="notifCount" hidden></span></a>`;

// Shows the number of unseen notifications on the header link and keeps it up to date.
// When the number goes up while the page is open, onNew is called so the page can announce it.
export function watchNotifications(onNew) {
  if (!getUser()) return;
  let last = null;

  // Asks the server for the unseen count and updates the header.
  const check = async () => {
    try {
      const { unread } = await api('/notifications/unread-count');
      const badge = document.getElementById('notifCount');
      if (badge) {
        badge.textContent = unread > 99 ? '99+' : unread;
        badge.hidden = unread === 0;
      }
      if (last !== null && unread > last && onNew) onNew(unread - last);
      last = unread;
    } catch { /* not signed in any more, or the server is unreachable: try again next time */ }
  };
  check();
  setInterval(check, CHECK_EVERY_MS);
}
