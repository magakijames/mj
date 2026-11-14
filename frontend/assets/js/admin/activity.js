// Admin "Activity" page: the record of who changed what, newest first.
import { api } from '../core/api.js';
import { ACTIVITY_LABELS } from '../core/config.js';
import { $, esc, dateTime } from '../core/ui.js';
import { openAdminPage, showError } from './layout.js';

// The HTML for one line of the record.
function row(entry) {
  return `<tr>
    <td class="nowrap">${dateTime(entry.at)}</td>
    <td>${entry.actorEmail ? esc(entry.actorEmail) : '<span class="meta">Not signed in</span>'}</td>
    <td>${esc(ACTIVITY_LABELS[entry.action] || entry.action)}</td>
    <td>${esc(entry.details)}</td>
  </tr>`;
}

if (await openAdminPage('activity')) {
  try {
    const entries = await api('/audit');
    $('#activityTable').innerHTML = entries.length ? `<table class="tbl">
      <thead><tr><th>When</th><th>Who</th><th>What</th><th>Details</th></tr></thead>
      <tbody>${entries.map(row).join('')}</tbody></table>` : '<p class="empty">Nothing recorded yet.</p>';
  } catch (error) {
    showError('#activityTable', error);
  }
}
