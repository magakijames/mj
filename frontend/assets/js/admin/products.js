// Admin products page: list, add, edit and delete.
import { api } from '../core/api.js';
import { $, esc, money, imageUrl, toast } from '../core/ui.js';
import { openAdminPage, showError } from './layout.js';

let products = [];
let editingId = null;

async function load() {
  try {
    products = await api('/products');
    render();
  } catch (error) {
    showError('#productTable', error);
  }
}

function render() {
  $('#productCount').textContent = products.length + (products.length === 1 ? ' product' : ' products');
  $('#productTable').innerHTML = `<table class="tbl">
    <thead><tr><th>Product</th><th>Type</th><th>For</th><th>Colours</th><th>Sizes</th><th class="num">Price</th><th class="num">Stock</th><th><span class="visually-hidden">Actions</span></th></tr></thead>
    <tbody>${products.map(p => `<tr>
      <td><div class="cell-product">${p.image ? `<img class="thumb" src="${esc(imageUrl(p.image))}" alt="">` : '<span class="thumb"></span>'}<span>${esc(p.name)}</span></div></td>
      <td>${esc(p.category)}</td>
      <td>${esc(p.gender)}</td>
      <td>${esc(p.colors.join(', '))}</td>
      <td>${esc(p.sizes.join(', '))}</td>
      <td class="num">${money(p.price)}</td>
      <td class="num">${p.stock === 0 ? '<span class="status cancelled">0</span>' : p.stock}</td>
      <td class="actions-cell"><div class="btn-row">
        <button class="btn small ghost" data-edit="${p.id}">Edit</button>
        <button class="btn small danger" data-delete="${p.id}">Delete</button>
      </div></td>
    </tr>`).join('')}</tbody></table>`;
}

function openForm(id) {
  editingId = id ?? null;
  const p = products.find(x => x.id === id);
  $('#formTitle').textContent = p ? 'Edit product' : 'Add product';
  $('#pName').value = p?.name ?? '';
  $('#pCategory').value = p?.category ?? 'Bag';
  $('#pGender').value = p?.gender ?? 'Unisex';
  $('#pColors').value = p?.colors.join(', ') ?? '';
  $('#pSizes').value = p?.sizes.join(', ') ?? '';
  $('#pPrice').value = p?.price ?? '';
  $('#pStock').value = p?.stock ?? '';
  $('#pImage').value = p?.image ?? '';
  $('#formError').textContent = '';
  $('#productModal').hidden = false;
  $('#pName').focus();
}
const closeForm = () => { $('#productModal').hidden = true; };

const split = text => text.split(',').map(x => x.trim()).filter(Boolean);

$('#productForm').addEventListener('submit', async e => {
  e.preventDefault();
  const body = {
    name: $('#pName').value.trim(), category: $('#pCategory').value, gender: $('#pGender').value,
    colors: split($('#pColors').value), sizes: split($('#pSizes').value),
    price: Number($('#pPrice').value), stock: Number($('#pStock').value), image: $('#pImage').value.trim(),
  };
  try {
    await api(editingId ? `/products/${editingId}` : '/products', { method: editingId ? 'PUT' : 'POST', body });
    closeForm();
    toast('Product saved');
    load();
  } catch (error) {
    $('#formError').textContent = error.message;
  }
});

document.addEventListener('click', async e => {
  if (e.target.closest('#addProduct')) return openForm();
  if (e.target.closest('[data-close]')) return closeForm();
  const edit = e.target.closest('[data-edit]');
  if (edit) return openForm(+edit.dataset.edit);
  const del = e.target.closest('[data-delete]');
  if (del) {
    const p = products.find(x => x.id === +del.dataset.delete);
    if (!confirm(`Delete "${p.name}"? This cannot be undone.`)) return;
    try {
      await api(`/products/${p.id}`, { method: 'DELETE' });
      toast('Product deleted');
      load();
    } catch (error) {
      toast(error.message);
    }
  }
});

if (await openAdminPage('products')) load();
