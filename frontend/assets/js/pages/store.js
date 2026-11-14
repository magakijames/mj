// Home page: hero slider, moving photo strip, filters and the product grid.
import { api } from '../core/api.js';
import { COLORS } from '../core/config.js';
import { addToCart, quantityInCart } from '../core/cart-store.js';
import { $, esc, escLines, money, imageUrl, toast, refreshCartCount, mountStoreChrome } from '../core/ui.js';

const settings = await mountStoreChrome({ search: true });
showSiteText(settings);

let products = [];
let filters = { category: '', gender: '', color: '', size: '', q: '' };

const SIDE_LINKS = [
  ['All products', '', ''], ['Bags', 'category', 'Bag'], ['Caps', 'category', 'Cap'],
  ['Men', 'gender', 'Men'], ['Women', 'gender', 'Women'], ['Kids', 'gender', 'Kids'], ['Unisex', 'gender', 'Unisex'],
];

// Fills in the headline and the four promise boxes from the site settings.
function showSiteText(s) {
  $('#heroTitle').innerHTML = escLines(s.hero_title);
  $('#heroText').textContent = s.hero_text;
  $('#perks').innerHTML = [1, 2, 3, 4]
    .filter(n => s[`perk${n}_title`] || s[`perk${n}_text`])
    .map(n => `<li><b>${esc(s[`perk${n}_title`])}</b>${esc(s[`perk${n}_text`])}</li>`).join('');
  $('#newsletterTitle').textContent = `Join the ${s.shop_name} community`;
}

// Every different value of one product field (colours or sizes), sorted, for the filter lists.
const unique = key => [...new Set(products.flatMap(p => p[key]))].sort();

// The <option> tags for a filter drop-down, with the current choice selected.
const options = (label, values, current) =>
  `<option value="">${label}</option>` + values.map(v => `<option ${v === current ? 'selected' : ''}>${esc(v)}</option>`).join('');

// Changes one filter and redraws the grid.
function setFilter(key, value) {
  filters[key] = value;
  render();
}

// Clears every filter and the search box.
function clearFilters() {
  filters = { category: '', gender: '', color: '', size: '', q: '' };
  $('#q').value = '';
  render();
}

// Searches for some text and scrolls down to the results.
function search(text) {
  $('#q').value = text;
  setFilter('q', text);
  $('#shop').scrollIntoView({ behavior: 'smooth' });
}

// Draws the filter controls, the side menu and the products that match the filters.
function render() {
  $('#fCategory').innerHTML = options('All types', ['Bag', 'Cap'], filters.category);
  $('#fGender').innerHTML = options('All genders', ['Men', 'Women', 'Unisex', 'Kids'], filters.gender);
  $('#fColor').innerHTML = options('All colours', unique('colors'), filters.color);
  $('#fSize').innerHTML = options('All sizes', unique('sizes'), filters.size);

  $('#sideList').innerHTML = SIDE_LINKS.map(([label, key, value]) =>
    `<li><button class="${key && filters[key] === value ? 'on' : ''}" data-key="${key}" data-value="${value}">${label}</button></li>`).join('');

  const q = filters.q.trim().toLowerCase();
  const list = products.filter(p =>
    (!filters.category || p.category === filters.category) &&
    (!filters.gender || p.gender === filters.gender) &&
    (!filters.color || p.colors.includes(filters.color)) &&
    (!filters.size || p.sizes.includes(filters.size)) &&
    (!q || (p.name + ' ' + p.category + ' ' + p.gender + ' ' + p.colors.join(' ')).toLowerCase().includes(q)));

  $('#grid').innerHTML = list.length ? list.map(card).join('') : '<p class="empty">No products match these filters. Try clearing them.</p>';
}

// The HTML for one product card: photo, name, colour and size choices, price and "Add to cart".
function card(p) {
  const picture = p.image ? `<img src="${esc(imageUrl(p.image))}" alt="${esc(p.name)}" loading="lazy">` : 'No photo yet';
  const tag = p.stock === 0 ? '<span class="tag out">Sold out</span>' : p.stock < 10 ? `<span class="tag out">Only ${p.stock} left</span>` : '';
  return `<article class="card">
    <div class="pic">${tag}${picture}</div>
    <h3>${esc(p.name)}</h3>
    <div class="meta">${esc(p.gender)} · ${esc(p.category)}</div>
    <div class="dots">${p.colors.map(c => `<span class="dot" title="${esc(c)}" style="background:${COLORS[c] || '#ccc'}"></span>`).join('')}</div>
    <div class="row">
      <select aria-label="Colour" data-color="${p.id}">${p.colors.map(c => `<option>${esc(c)}</option>`).join('')}</select>
      <select aria-label="Size" data-size="${p.id}">${p.sizes.map(s => `<option>${esc(s)}</option>`).join('')}</select>
    </div>
    <div class="row buy">
      <span class="price">${money(p.price)}</span>
      <button class="btn small" data-add="${p.id}" ${p.stock === 0 ? 'disabled' : ''}>Add to cart</button>
    </div>
  </article>`;
}

// Puts one of a product in the cart, in the colour and size chosen on its card, unless stock has run out.
function add(id) {
  const product = products.find(p => p.id === id);
  if (quantityInCart(id) >= product.stock) return toast('No more stock available');
  addToCart(id, $(`[data-color="${id}"]`).value, $(`[data-size="${id}"]`).value);
  refreshCartCount();
  toast('Added to cart');
}

// Starts the hero slider (eight products that change every few seconds) and the moving strip of photos.
function startMotion() {
  const withImage = products.filter(p => p.image);
  if (!withImage.length) return;
  const picks = withImage.filter((_, i) => i % Math.ceil(withImage.length / 8) === 0).slice(0, 8);

  const slider = $('#slider');
  slider.innerHTML = picks.map(p => `<div class="slide"><img src="${esc(imageUrl(p.image))}" alt="${esc(p.name)}">
      <button data-find="${esc(p.name)}"><span>${esc(p.name)}</span><span>${money(p.price)}</span></button></div>`).join('') +
    `<div class="dots-nav">${picks.map((_, i) => `<i data-slide="${i}"></i>`).join('')}</div>`;
  const slides = [...slider.querySelectorAll('.slide')];
  const dots = [...slider.querySelectorAll('.dots-nav i')];
  let current = 0, timer;

  // Shows slide number i, wrapping round at either end.
  const go = i => {
    current = (i + slides.length) % slides.length;
    slides.forEach((s, k) => s.classList.toggle('on', k === current));
    dots.forEach((d, k) => d.classList.toggle('on', k === current));
  };
  // Starts the automatic change of slides, unless the visitor has asked their device for less motion.
  const play = () => {
    clearInterval(timer);
    if (!matchMedia('(prefers-reduced-motion: reduce)').matches) timer = setInterval(() => go(current + 1), 3800);
  };
  slider.addEventListener('click', e => {
    const dot = e.target.closest('[data-slide]');
    if (dot) { go(+dot.dataset.slide); play(); }
  });
  slider.addEventListener('mouseenter', () => clearInterval(timer));
  slider.addEventListener('mouseleave', play);
  go(0); play();

  const strip = withImage.map(p => `<button class="mi" data-find="${esc(p.name)}"><img src="${esc(imageUrl(p.image))}" alt="" loading="lazy">${esc(p.name)}</button>`).join('');
  $('#track').innerHTML = strip + strip;
}

// One click handler for everything on the page: add to cart, slider and strip, side menu, clear filters.
document.addEventListener('click', e => {
  const addBtn = e.target.closest('[data-add]');
  if (addBtn) return add(+addBtn.dataset.add);
  const find = e.target.closest('[data-find]');
  if (find) return search(find.dataset.find);
  const side = e.target.closest('#sideList button');
  if (side) return side.dataset.key ? setFilter(side.dataset.key, side.dataset.value) : clearFilters();
  if (e.target.closest('#clearFilters')) return clearFilters();
  if (e.target.closest('#browseBtn')) $('#shop').scrollIntoView({ behavior: 'smooth' });
});
$('#filters').addEventListener('change', e => setFilter(e.target.dataset.filter, e.target.value));
$('#searchForm').addEventListener('submit', e => { e.preventDefault(); search($('#q').value); });
$('#newsletterForm').addEventListener('submit', e => { e.preventDefault(); e.target.reset(); toast('Subscribed. Thank you!'); });

// Load the products and draw the page.
try {
  products = await api('/products');
  render();
  startMotion();
} catch (error) {
  $('#grid').innerHTML = `<p class="empty">${esc(error.message)}</p>`;
}
