// "Notifications" page: the signed-in user's messages, for customers and staff alike.
import { api } from '../core/api.js';
import { url } from '../core/config.js';
import { requireSignIn } from '../core/auth.js';
import { $, esc, dateTime, mountStoreChrome } from '../core/ui.js';

await mountStoreChrome();

// The HTML for one message. Unseen messages are marked, and a message with a link opens that page.
function row(n) {
  const title = n.link ? `<a href="${esc(url(n.link))}" data-open="${n.id}">${esc(n.title)}</a>` : esc(n.title);
  return `<article class="notif ${n.read ? '' : 'unread'}">
    <div><b>${title}</b>${n.read ? '' : ' <span class="status pending">New</span>'}<p>${esc(n.body)}</p></div>
    <span class="meta nowrap">${dateTime(n.createdAt)}</span>
  </article>`;
}

// Fetches the messages and draws the list.
async function load() {
  try {
    const { unread, items } = await api('/notifications');
    $('#summary').textContent = unread ? `${unread} new` : 'Nothing new';
    $('#markAll').hidden = unread === 0;
    $('#list').innerHTML = items.length ? items.map(row).join('') : '<p class="empty">You have no notifications yet.</p>';
  } catch (error) {
    $('#list').innerHTML = `<p class="empty">${esc(error.message)}</p>`;
  }
}

// "Mark all as read".
$('#markAll').addEventListener('click', async () => {
  await api('/notifications/read-all', { method: 'POST' });
  load();
});

// Opening a message's link marks that message as seen on the way.
$('#list').addEventListener('click', e => {
  const link = e.target.closest('[data-open]');
  if (link) api(`/notifications/${link.dataset.open}/read`, { method: 'POST' }).catch(() => {});
});

if (requireSignIn()) load();
