// Admin "Site settings" page: the shop name and the other text shown on the store.
import { api } from '../core/api.js';
import { SETTING_SECTIONS } from '../core/config.js';
import { rememberSettings } from '../core/settings.js';
import { $, esc, toast } from '../core/ui.js';
import { openAdminPage, showError } from './layout.js';

// Builds the form from SETTING_SECTIONS and fills it with the current values.
function render(settings) {
  $('#settingsFields').innerHTML = SETTING_SECTIONS.map(section => `
    <fieldset class="panel settings-group">
      <legend>${esc(section.title)}</legend>
      ${section.note ? `<p class="meta">${esc(section.note)}</p>` : ''}
      <div class="settings-grid">
        ${section.fields.map(f => `<label>${esc(f.label)}
          ${f.multiline
            ? `<textarea name="${f.key}" rows="2" maxlength="500">${esc(settings[f.key])}</textarea>`
            : `<input name="${f.key}" maxlength="500" value="${esc(settings[f.key])}" ${f.required ? 'required' : ''}>`}
          ${f.hint ? `<small>${esc(f.hint)}</small>` : ''}
        </label>`).join('')}
      </div>
    </fieldset>`).join('');
  $('#settingsForm').hidden = false;
}

// Saves every field. The store pages pick up the new text the next time they are opened.
$('#settingsForm').addEventListener('submit', async e => {
  e.preventDefault();
  const error = $('#formError');
  error.textContent = '';
  const changes = Object.fromEntries(new FormData(e.target).entries());
  try {
    const saved = await api('/settings', { method: 'PUT', body: changes });
    rememberSettings(saved);
    render(saved);
    $('#adminHeader .logo').firstChild.textContent = saved.shop_name;
    toast('Site settings saved');
  } catch (err) {
    error.textContent = err.message;
  }
});

if (await openAdminPage('settings')) {
  try {
    render(await api('/settings'));
  } catch (error) {
    showError('#settingsFields', error);
  }
}
