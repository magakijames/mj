/* Snap & Pack – Bags & Caps. Frontend only: data lives in the browser (localStorage). */
const $ = s => document.querySelector(s);
const esc = s => String(s).replace(/[&<>"']/g, c => ({'&':'&amp;','<':'&lt;','>':'&gt;','"':'&quot;',"'":'&#39;'}[c]));
const money = n => 'KSh ' + Math.round(Number(n)).toLocaleString('en-KE');
const load = (k, d) => { try { return JSON.parse(localStorage.getItem(k)) ?? d; } catch { return d; } };
const save = (k, v) => localStorage.setItem(k, JSON.stringify(v));

const COLORS = {Black:'#222',Green:'#1f6b4a',Navy:'#1f3a5f',Beige:'#d8c3a5',Red:'#c0392b',Grey:'#888',White:'#fff',Pink:'#f4a6a6',Blue:'#3b6fb6',Cream:'#efe6d2',Stone:'#cfc6c4','Sky blue':'#8fc1e6',Purple:'#6b3fa0',Brown:'#7a5230',Khaki:'#b5a36a'};

const SEED = [
  {"id": 1, "name": "Anti-Theft Laptop Backpack", "category": "Bag", "gender": "Unisex", "colors": ["Grey", "Black"], "sizes": ["M", "L"], "price": 7800, "stock": 24, "image": "images/p0.jpg"},
  {"id": 2, "name": "USB Travel Backpack", "category": "Bag", "gender": "Unisex", "colors": ["Black"], "sizes": ["L"], "price": 7150, "stock": 30, "image": "images/p18.jpg"},
  {"id": 3, "name": "Everyday Casual Backpack", "category": "Bag", "gender": "Men", "colors": ["Grey", "Black", "Navy"], "sizes": ["M", "L"], "price": 5200, "stock": 28, "image": "images/p44.jpg"},
  {"id": 4, "name": "Minimal Backpack", "category": "Bag", "gender": "Unisex", "colors": ["Navy", "Black", "Green"], "sizes": ["M", "L"], "price": 6500, "stock": 30, "image": "images/p54.jpg"},
  {"id": 5, "name": "Quilted Leather Backpack", "category": "Bag", "gender": "Women", "colors": ["Brown", "Black"], "sizes": ["M"], "price": 8450, "stock": 15, "image": "images/p48.jpg"},
  {"id": 6, "name": "Burgundy Leather Backpack", "category": "Bag", "gender": "Women", "colors": ["Red", "Brown"], "sizes": ["M"], "price": 9100, "stock": 10, "image": "images/p55.jpg"},
  {"id": 7, "name": "Unicorn Kids Backpack", "category": "Bag", "gender": "Kids", "colors": ["Pink"], "sizes": ["S"], "price": 3900, "stock": 20, "image": "images/p32.jpg"},
  {"id": 8, "name": "Checkered Crossbody Bag", "category": "Bag", "gender": "Men", "colors": ["Grey", "Black"], "sizes": ["S"], "price": 3250, "stock": 22, "image": "images/p2.jpg"},
  {"id": 9, "name": "Leather Messenger Bag", "category": "Bag", "gender": "Men", "colors": ["Black"], "sizes": ["S", "M"], "price": 5850, "stock": 14, "image": "images/p36.jpg"},
  {"id": 10, "name": "Gym Duffel Bag", "category": "Bag", "gender": "Men", "colors": ["Black"], "sizes": ["M", "L"], "price": 5500, "stock": 18, "image": "images/p11.jpg"},
  {"id": 11, "name": "Navy Gold-Trim Handbag", "category": "Bag", "gender": "Women", "colors": ["Navy"], "sizes": ["M"], "price": 9750, "stock": 8, "image": "images/p3.jpg"},
  {"id": 12, "name": "Navy Classic Handbag", "category": "Bag", "gender": "Women", "colors": ["Navy", "Black"], "sizes": ["M"], "price": 9100, "stock": 12, "image": "images/p7.jpg"},
  {"id": 13, "name": "Blush Structured Tote", "category": "Bag", "gender": "Women", "colors": ["Beige", "Pink"], "sizes": ["M", "L"], "price": 7800, "stock": 16, "image": "images/p9.jpg"},
  {"id": 14, "name": "Pink Satchel Handbag", "category": "Bag", "gender": "Women", "colors": ["Pink", "Black"], "sizes": ["M"], "price": 7150, "stock": 20, "image": "images/p17.jpg"},
  {"id": 15, "name": "Mini Pink Crossbody", "category": "Bag", "gender": "Women", "colors": ["Pink"], "sizes": ["S"], "price": 4550, "stock": 25, "image": "images/p16.jpg"},
  {"id": 16, "name": "Grey Handbag with Purse", "category": "Bag", "gender": "Women", "colors": ["Grey", "Beige"], "sizes": ["M"], "price": 10400, "stock": 9, "image": "images/p26.jpg"},
  {"id": 17, "name": "3-Piece Handbag Set", "category": "Bag", "gender": "Women", "colors": ["Navy", "Brown"], "sizes": ["M", "L"], "price": 11050, "stock": 6, "image": "images/p25.jpg"},
  {"id": 18, "name": "Black Tassel Handbag", "category": "Bag", "gender": "Women", "colors": ["Black"], "sizes": ["M"], "price": 6500, "stock": 18, "image": "images/p40.jpg"},
  {"id": 19, "name": "Black Bow Work Tote", "category": "Bag", "gender": "Women", "colors": ["Black"], "sizes": ["L"], "price": 7550, "stock": 11, "image": "images/p47.jpg"},
  {"id": 20, "name": "Brown Saddle Crossbody", "category": "Bag", "gender": "Women", "colors": ["Brown"], "sizes": ["S"], "price": 5050, "stock": 20, "image": "images/p43.jpg"},
  {"id": 21, "name": "Woven Leather Tote", "category": "Bag", "gender": "Women", "colors": ["Brown"], "sizes": ["L"], "price": 11700, "stock": 7, "image": "images/p50.jpg"},
  {"id": 22, "name": "Black Work Tote", "category": "Bag", "gender": "Women", "colors": ["Black"], "sizes": ["L"], "price": 9500, "stock": 10, "image": "images/p51.jpg"},
  {"id": 23, "name": "Classic Baseball Cap", "category": "Cap", "gender": "Unisex", "colors": ["Black", "Navy", "Grey"], "sizes": ["S/M", "L/XL"], "price": 2600, "stock": 60, "image": "images/p12.jpg"},
  {"id": 24, "name": "Sport Mesh Cap", "category": "Cap", "gender": "Men", "colors": ["Black", "Grey", "Navy"], "sizes": ["S/M", "L/XL"], "price": 2850, "stock": 40, "image": "images/p29.jpg"},
  {"id": 25, "name": "Embroidered Cross Cap", "category": "Cap", "gender": "Unisex", "colors": ["Black"], "sizes": ["One size"], "price": 3000, "stock": 35, "image": "images/p46.jpg"},
  {"id": 26, "name": "Quilted Leather Cap", "category": "Cap", "gender": "Men", "colors": ["Black", "Navy"], "sizes": ["One size"], "price": 3900, "stock": 15, "image": "images/p41.jpg"},
  {"id": 27, "name": "Red Fade Baseball Cap", "category": "Cap", "gender": "Women", "colors": ["Red", "White"], "sizes": ["One size"], "price": 2450, "stock": 30, "image": "images/p49.jpg"},
  {"id": 28, "name": "Flat Brim Snapback", "category": "Cap", "gender": "Men", "colors": ["Black", "Beige", "Blue", "Red", "White"], "sizes": ["One size"], "price": 3000, "stock": 0, "image": "images/p19.jpg"},
  {"id": 29, "name": "Brimless Docker Cap", "category": "Cap", "gender": "Men", "colors": ["Black"], "sizes": ["One size"], "price": 2200, "stock": 45, "image": "images/p21.jpg"},
  {"id": 30, "name": "Sports Brimless Cap", "category": "Cap", "gender": "Unisex", "colors": ["Beige", "Grey", "Green", "Navy"], "sizes": ["One size"], "price": 2350, "stock": 38, "image": "images/p24.jpg"},
  {"id": 31, "name": "Bucket Hat", "category": "Cap", "gender": "Unisex", "colors": ["Black"], "sizes": ["S/M", "L/XL"], "price": 3250, "stock": 22, "image": "images/p10.jpg"},
  {"id": 32, "name": "Camo Army Cap", "category": "Cap", "gender": "Men", "colors": ["Green", "Khaki"], "sizes": ["One size"], "price": 2600, "stock": 26, "image": "images/p31.jpg"},
  {"id": 33, "name": "Mesh Army Cap", "category": "Cap", "gender": "Men", "colors": ["Green"], "sizes": ["One size"], "price": 2450, "stock": 30, "image": "images/p35.jpg"},
  {"id": 34, "name": "Graffiti Print Cap", "category": "Cap", "gender": "Unisex", "colors": ["Black", "White"], "sizes": ["One size"], "price": 2750, "stock": 20, "image": "images/p39.jpg"},
  {"id": 35, "name": "Ribbed Beanie", "category": "Cap", "gender": "Unisex", "colors": ["Black"], "sizes": ["One size"], "price": 1950, "stock": 50, "image": "images/p15.jpg"},
  {"id": 36, "name": "Fisherman Beanie Set", "category": "Cap", "gender": "Unisex", "colors": ["Brown", "Black"], "sizes": ["One size"], "price": 2100, "stock": 40, "image": "images/p52.jpg"},
  {"id": 37, "name": "Faux Fur Pom Hat", "category": "Cap", "gender": "Women", "colors": ["Grey"], "sizes": ["One size"], "price": 3500, "stock": 14, "image": "images/p27.jpg"},
  {"id": 38, "name": "Flat Ivy Caps", "category": "Cap", "gender": "Men", "colors": ["Navy", "Black", "Grey"], "sizes": ["S/M", "L/XL"], "price": 3100, "stock": 18, "image": "images/p6.jpg"},
  {"id": 39, "name": "New Era Pink NY Custom Fitted", "category": "Cap", "gender": "Unisex", "colors": ["Pink"], "sizes": ["6 7/8", "7", "7 1/8", "7 1/4", "7 3/8", "7 1/2", "7 5/8", "7 3/4", "8"], "price": 1500, "stock": 20, "image": "images/pink-ny-custom.jpg"},
  {"id": 40, "name": "New Era Sox Flames Fitted", "category": "Cap", "gender": "Unisex", "colors": ["Black", "Pink"], "sizes": ["6 7/8", "7", "7 1/8", "7 1/4", "7 3/8", "7 1/2", "7 5/8", "7 3/4", "8"], "price": 1500, "stock": 20, "image": "images/sox-flames.jpg"},
  {"id": 41, "name": "New Era Blackout LA Fitted", "category": "Cap", "gender": "Unisex", "colors": ["Black"], "sizes": ["6 7/8", "7", "7 1/8", "7 1/4", "7 3/8", "7 1/2", "7 5/8", "7 3/4", "8"], "price": 1500, "stock": 20, "image": "images/blackout-la-ny.jpg"},
  {"id": 42, "name": "New Era Blackout NY Fitted", "category": "Cap", "gender": "Unisex", "colors": ["Black"], "sizes": ["6 7/8", "7", "7 1/8", "7 1/4", "7 3/8", "7 1/2", "7 5/8", "7 3/4", "8"], "price": 1500, "stock": 20, "image": "images/blackout-la-ny.jpg"},
  {"id": 43, "name": "New Era Sox Roses Fitted", "category": "Cap", "gender": "Unisex", "colors": ["Black"], "sizes": ["6 7/8", "7", "7 1/8", "7 1/4", "7 3/8", "7 1/2", "7 5/8", "7 3/4", "8"], "price": 1500, "stock": 20, "image": "images/sox-roses.jpg"},
  {"id": 44, "name": "New Era NY Cherry Blossom Snapback", "category": "Cap", "gender": "Unisex", "colors": ["Black", "Stone"], "sizes": ["One size"], "price": 1500, "stock": 20, "image": "images/ny-cherry-blossom.jpg"},
  {"id": 45, "name": "New Era Cream & Black NY Adjustable", "category": "Cap", "gender": "Unisex", "colors": ["Cream"], "sizes": ["One size"], "price": 1500, "stock": 20, "image": "images/cream-black-ny-la.jpg"},
  {"id": 46, "name": "New Era Cream & Black LA Adjustable", "category": "Cap", "gender": "Unisex", "colors": ["Cream"], "sizes": ["One size"], "price": 1500, "stock": 20, "image": "images/cream-black-ny-la.jpg"},
  {"id": 47, "name": "New Era NY Black Snapback with Gold Pin", "category": "Cap", "gender": "Unisex", "colors": ["Black"], "sizes": ["One size"], "price": 1500, "stock": 20, "image": "images/ny-black-pin-snapback.jpg"},
  {"id": 48, "name": "New Era NY Cream & Navy Star Patch Fitted", "category": "Cap", "gender": "Unisex", "colors": ["Cream"], "sizes": ["6 7/8", "7", "7 1/8", "7 1/4", "7 3/8", "7 1/2", "7 5/8", "7 3/4", "8"], "price": 1500, "stock": 20, "image": "images/ny-cream-navy-star.jpg"},
  {"id": 49, "name": "New Era NY White & Navy Fitted", "category": "Cap", "gender": "Unisex", "colors": ["White"], "sizes": ["6 7/8", "7", "7 1/8", "7 1/4", "7 3/8", "7 1/2", "7 5/8", "7 3/4", "8"], "price": 1500, "stock": 20, "image": "images/ny-white-navy.jpg"},
  {"id": 50, "name": "New Era NY Black Low Profile Fitted", "category": "Cap", "gender": "Unisex", "colors": ["Black"], "sizes": ["6 7/8", "7", "7 1/8", "7 1/4", "7 3/8", "7 1/2", "7 5/8", "7 3/4", "8"], "price": 1500, "stock": 20, "image": "images/ny-black-low-profile.jpg"},
  {"id": 51, "name": "New Era NY Sky Blue & Red Fitted", "category": "Cap", "gender": "Unisex", "colors": ["Sky blue"], "sizes": ["6 7/8", "7", "7 1/8", "7 1/4", "7 3/8", "7 1/2", "7 5/8", "7 3/4", "8"], "price": 1500, "stock": 20, "image": "images/ny-sky-red.jpg"},
  {"id": 52, "name": "New Era NY White & Purple Fitted", "category": "Cap", "gender": "Unisex", "colors": ["White"], "sizes": ["6 7/8", "7", "7 1/8", "7 1/4", "7 3/8", "7 1/2", "7 5/8", "7 3/4", "8"], "price": 1500, "stock": 20, "image": "images/ny-white-purple.jpg"},
  {"id": 53, "name": "New Era NY Royal Blue Fitted", "category": "Cap", "gender": "Unisex", "colors": ["Blue"], "sizes": ["6 7/8", "7", "7 1/8", "7 1/4", "7 3/8", "7 1/2", "7 5/8", "7 3/4", "8"], "price": 1500, "stock": 20, "image": "images/ny-royal-blue.jpg"},
  {"id": 54, "name": "New Era Mets Royal Fitted", "category": "Cap", "gender": "Unisex", "colors": ["Blue"], "sizes": ["6 7/8", "7", "7 1/8", "7 1/4", "7 3/8", "7 1/2", "7 5/8", "7 3/4", "8"], "price": 1500, "stock": 20, "image": "images/mets-royal.jpg"},
  {"id": 55, "name": "New Era Mets Champions Script Fitted", "category": "Cap", "gender": "Unisex", "colors": ["Blue"], "sizes": ["6 7/8", "7", "7 1/8", "7 1/4", "7 3/8", "7 1/2", "7 5/8", "7 3/4", "8"], "price": 1500, "stock": 20, "image": "images/mets-champions-script.jpg"},
  {"id": 56, "name": "New Era Mets Little League Classic 2025 Fitted", "category": "Cap", "gender": "Unisex", "colors": ["Blue"], "sizes": ["6 7/8", "7", "7 1/8", "7 1/4", "7 3/8", "7 1/2", "7 5/8", "7 3/4", "8"], "price": 1500, "stock": 20, "image": "images/mets-little-league-classic.jpg"},
  {"id": 57, "name": "New Era Mets Postseason 2022 Fitted", "category": "Cap", "gender": "Unisex", "colors": ["Blue"], "sizes": ["6 7/8", "7", "7 1/8", "7 1/4", "7 3/8", "7 1/2", "7 5/8", "7 3/4", "8"], "price": 1500, "stock": 20, "image": "images/mets-postseason-2022.jpg"}
];

let products = load('zv_products3', null) || (save('zv_products3', SEED), SEED);
let users = load('zv_users', null) || (save('zv_users', [{id:1,name:'Admin',email:'admin@store.com',password:'admin123',role:'admin'}]), load('zv_users'));
let orders = load('zv_orders', []);
let cart = load('zv_cart', []);
let session = load('zv_session', null);
let F = {cat:'', gender:'', color:'', size:'', q:''};
let editingId = null, authMode = 'in', tab = 'products';

const isAdmin = () => session && session.role === 'admin';
const saveAll = () => { save('zv_products3', products); save('zv_users', users); save('zv_orders', orders); save('zv_cart', cart); };
function toast(msg) { const t = $('#toast'); t.textContent = msg; t.hidden = false; clearTimeout(toast.t); toast.t = setTimeout(() => t.hidden = true, 2200); }

/* ---------- views ---------- */
function show(v) {
  if (v === 'admin' && !isAdmin()) v = 'store';
  if (v === 'orders' && !session) v = 'store';
  ['store','orders','admin'].forEach(n => $('#' + n + 'View').hidden = n !== v);
  if (v === 'admin') renderAdmin();
  if (v === 'orders') renderMyOrders();
  window.scrollTo(0, 0);
}
function refreshHeader() {
  $('#accountBtn').textContent = session ? `Sign out (${session.name.split(' ')[0]})` : 'Sign in';
  $('#adminLink').hidden = !isAdmin();
  $('#ordersLink').hidden = !session || isAdmin();
  $('#cartCount').textContent = cart.reduce((n, i) => n + i.qty, 0);
}

/* ---------- store ---------- */
function options(label, arr, cur) { return `<option value="">${label}</option>` + arr.map(a => `<option ${a === cur ? 'selected' : ''}>${esc(a)}</option>`).join(''); }
const uniq = key => [...new Set(products.flatMap(p => [].concat(p[key])))].sort();

function setF(k, v) { F[k] = v; renderStore(); if (k === 'q') document.getElementById('shop').scrollIntoView({behavior:'smooth'}); }
function clearF() { F = {cat:'', gender:'', color:'', size:'', q:''}; $('#q').value = ''; renderStore(); }

function renderStore() {
  $('#fCat').innerHTML = options('All types', ['Bag','Cap'], F.cat);
  $('#fGender').innerHTML = options('All genders', ['Men','Women','Unisex','Kids'], F.gender);
  $('#fColor').innerHTML = options('All colours', uniq('colors'), F.color);
  $('#fSize').innerHTML = options('All sizes', uniq('sizes'), F.size);
  const side = [['All products','',''],['Bags','cat','Bag'],['Caps','cat','Cap'],['Men','gender','Men'],['Women','gender','Women'],['Kids','gender','Kids'],['Unisex','gender','Unisex']];
  $('#sideList').innerHTML = side.map(([l,k,v]) => `<li class="${k && F[k] === v ? 'on' : ''}" onclick="${k ? `setF('${k}','${v}')` : 'clearF()'}">${l}</li>`).join('');

  const q = F.q.trim().toLowerCase();
  const list = products.filter(p =>
    (!F.cat || p.category === F.cat) && (!F.gender || p.gender === F.gender) &&
    (!F.color || p.colors.includes(F.color)) && (!F.size || p.sizes.includes(F.size)) &&
    (!q || (p.name + p.category + p.gender + p.colors.join(' ')).toLowerCase().includes(q)));

  $('#grid').innerHTML = list.length ? list.map(card).join('') : '<p>No products match these filters. Try clearing them.</p>';
}
function card(p) {
  const pic = p.image ? `<img src="${esc(p.image)}" alt="${esc(p.name)}" loading="lazy">` : 'No photo yet';
  const tag = p.stock === 0 ? '<span class="tag out">Sold out</span>' : p.stock < 10 ? `<span class="tag out">Only ${p.stock} left</span>` : '';
  return `<div class="card">
    <div class="pic">${tag}${pic}</div>
    <h4>${esc(p.name)}</h4>
    <div class="meta">${esc(p.gender)} · ${esc(p.category)}</div>
    <div class="dots">${p.colors.map(c => `<span class="dot" title="${esc(c)}" style="background:${COLORS[c] || '#ccc'}"></span>`).join('')}</div>
    <div class="row">
      <select id="c${p.id}">${p.colors.map(c => `<option>${esc(c)}</option>`).join('')}</select>
      <select id="s${p.id}">${p.sizes.map(s => `<option>${esc(s)}</option>`).join('')}</select>
    </div>
    <div class="row" style="justify-content:space-between;align-items:center">
      <span class="price">${money(p.price)}</span>
      <button class="btn small" ${p.stock === 0 ? 'disabled style="opacity:.5"' : ''} onclick="addToCart(${p.id})">Add to cart</button>
    </div></div>`;
}

/* ---------- cart ---------- */
function addToCart(id) {
  const p = products.find(x => x.id === id);
  const color = $('#c' + id).value, size = $('#s' + id).value;
  const line = cart.find(i => i.id === id && i.color === color && i.size === size);
  const inCart = cart.filter(i => i.id === id).reduce((n, i) => n + i.qty, 0);
  if (inCart >= p.stock) return toast('No more stock available');
  line ? line.qty++ : cart.push({id, color, size, qty: 1});
  saveAll(); refreshHeader(); renderCart(); toast('Added to cart');
}
function changeQty(i, d) { cart[i].qty += d; if (cart[i].qty <= 0) cart.splice(i, 1); saveAll(); refreshHeader(); renderCart(); }
function toggleCart(open) { $('#cart').hidden = !open; if (open) renderCart(); }
function renderCart() {
  let total = 0;
  $('#cartItems').innerHTML = cart.length ? cart.map((i, idx) => {
    const p = products.find(x => x.id === i.id); if (!p) return '';
    total += p.price * i.qty;
    return `<div class="ci"><div><b>${esc(p.name)}</b><br><span class="meta">${esc(i.color)} · ${esc(i.size)}</span></div>
      <div class="qty"><button onclick="changeQty(${idx},-1)">−</button> ${i.qty} <button onclick="changeQty(${idx},1)">+</button><br>${money(p.price * i.qty)}</div></div>`;
  }).join('') : '<p>Your cart is empty. Add a bag or cap to get started.</p>';
  $('#cartTotal').textContent = money(total);
}
function checkout() {
  if (!cart.length) return toast('Your cart is empty');
  if (!session) { toggleCart(false); return openAuth('in'); }
  const items = cart.map(i => { const p = products.find(x => x.id === i.id); return {id:i.id, name:p.name, color:i.color, size:i.size, qty:i.qty, price:p.price}; });
  items.forEach(it => { const p = products.find(x => x.id === it.id); p.stock = Math.max(0, p.stock - it.qty); });
  orders.push({id: Date.now(), userId: session.id, name: session.name, email: session.email, items, total: items.reduce((n, i) => n + i.price * i.qty, 0), status: 'Pending', date: new Date().toLocaleString()});
  cart = []; saveAll(); refreshHeader(); toggleCart(false); renderStore(); toast('Order placed. Thank you!'); show('orders');
}

/* ---------- auth ---------- */
function accountClick() { if (session) { session = null; save('zv_session', null); refreshHeader(); show('store'); toast('Signed out'); } else openAuth('in'); }
function openAuth(mode) {
  authMode = mode; const reg = mode === 'up';
  $('#authTitle').textContent = reg ? 'Create account' : 'Sign in';
  $('#authSubmit').textContent = reg ? 'Create account' : 'Sign in';
  $('#aName').hidden = !reg; $('#aName').required = reg;
  $('#authSwapText').textContent = reg ? 'Already have an account?' : 'New here?';
  $('#authSwap').textContent = reg ? 'Sign in' : 'Create an account';
  $('#authErr').textContent = ''; $('#authModal').hidden = false;
}
$('#authSwap').onclick = e => { e.preventDefault(); openAuth(authMode === 'in' ? 'up' : 'in'); };
$('#authForm').onsubmit = e => {
  e.preventDefault();
  const email = $('#aEmail').value.trim().toLowerCase(), pass = $('#aPass').value;
  if (authMode === 'up') {
    if (users.some(u => u.email === email)) return $('#authErr').textContent = 'That email is already registered. Sign in instead.';
    const u = {id: Date.now(), name: $('#aName').value.trim(), email, password: pass, role: 'client'};
    users.push(u); saveAll(); session = u;
  } else {
    const u = users.find(u => u.email === email && u.password === pass);
    if (!u) return $('#authErr').textContent = 'Wrong email or password.';
    session = u;
  }
  save('zv_session', session); $('#authModal').hidden = true; $('#authForm').reset(); refreshHeader();
  toast('Welcome, ' + session.name.split(' ')[0]); show(isAdmin() ? 'admin' : 'store');
};

/* ---------- client orders ---------- */
function orderHTML(o, admin) {
  return `<div class="orderbox"><b>Order #${o.id}</b> · ${esc(o.date)} · <b>${esc(o.status)}</b>${admin ? ` · ${esc(o.name)} (${esc(o.email)})` : ''}<br>
    ${o.items.map(i => `${i.qty} × ${esc(i.name)} (${esc(i.color)}, ${esc(i.size)})`).join('<br>')}<br><b>Total ${money(o.total)}</b></div>`;
}
function renderMyOrders() {
  const mine = orders.filter(o => o.userId === session.id).reverse();
  $('#myOrders').innerHTML = mine.length ? mine.map(o => orderHTML(o)).join('') : '<p>You have no orders yet.</p>';
}

/* ---------- admin ---------- */
function adminTab(t) { tab = t; renderAdmin(); }
function renderAdmin() {
  const revenue = orders.filter(o => o.status !== 'Cancelled').reduce((n, o) => n + o.total, 0);
  $('#stats').innerHTML = [['Products', products.length], ['Orders', orders.length], ['Customers', users.filter(u => u.role === 'client').length], ['Revenue', money(revenue)]]
    .map(([l, v]) => `<div class="stat"><b>${v}</b>${l}</div>`).join('');
  document.querySelectorAll('.tabs button').forEach(b => b.classList.toggle('on', b.dataset.t === tab));
  const body = $('#adminBody');
  if (tab === 'products') {
    body.innerHTML = `<p><button class="btn" onclick="openProd()">Add product</button></p><div class="scroll"><table class="tbl"><tr><th>Name</th><th>Type</th><th>Gender</th><th>Colours</th><th>Sizes</th><th>Price</th><th>Stock</th><th></th></tr>` +
      products.map(p => `<tr><td>${esc(p.name)}</td><td>${p.category}</td><td>${p.gender}</td><td>${esc(p.colors.join(', '))}</td><td>${esc(p.sizes.join(', '))}</td><td>${money(p.price)}</td><td>${p.stock}</td>
      <td><button class="btn small" onclick="openProd(${p.id})">Edit</button> <button class="btn small danger" onclick="delProd(${p.id})">Delete</button></td></tr>`).join('') + '</table></div>';
  } else if (tab === 'orders') {
    body.innerHTML = orders.length ? [...orders].reverse().map(o => orderHTML(o, true) +
      `<p style="margin:-6px 0 14px">Status: <select onchange="setStatus(${o.id},this.value)">${['Pending','Shipped','Delivered','Cancelled'].map(s => `<option ${s === o.status ? 'selected' : ''}>${s}</option>`).join('')}</select></p>`).join('') : '<p>No orders yet.</p>';
  } else {
    body.innerHTML = `<div class="scroll"><table class="tbl"><tr><th>Name</th><th>Email</th><th>Role</th><th></th></tr>` +
      users.map(u => `<tr><td>${esc(u.name)}</td><td>${esc(u.email)}</td><td>${u.role}</td><td>${u.role === 'admin' ? '' : `<button class="btn small danger" onclick="delUser(${u.id})">Remove</button>`}</td></tr>`).join('') + '</table></div>';
  }
}
function setStatus(id, s) { orders.find(o => o.id === id).status = s; saveAll(); renderAdmin(); toast('Order marked ' + s); }
function delUser(id) { if (confirm('Remove this customer?')) { users = users.filter(u => u.id !== id); saveAll(); renderAdmin(); } }
function delProd(id) { if (confirm('Delete this product?')) { products = products.filter(p => p.id !== id); saveAll(); renderAdmin(); renderStore(); toast('Product deleted'); } }
function openProd(id) {
  editingId = id || null; const p = products.find(x => x.id === id);
  $('#pTitle').textContent = p ? 'Edit product' : 'Add product';
  $('#pName').value = p?.name || ''; $('#pCat').value = p?.category || 'Bag'; $('#pGender').value = p?.gender || 'Unisex';
  $('#pColors').value = p?.colors.join(', ') || ''; $('#pSizes').value = p?.sizes.join(', ') || '';
  $('#pPrice').value = p?.price ?? ''; $('#pStock').value = p?.stock ?? ''; $('#pEmoji').value = p?.image || '';
  $('#prodModal').hidden = false;
}
$('#prodForm').onsubmit = e => {
  e.preventDefault();
  const split = s => s.split(',').map(x => x.trim()).filter(Boolean);
  const data = {name: $('#pName').value.trim(), category: $('#pCat').value, gender: $('#pGender').value, colors: split($('#pColors').value), sizes: split($('#pSizes').value),
    price: +$('#pPrice').value, stock: +$('#pStock').value, image: $('#pEmoji').value.trim()};
  if (editingId) Object.assign(products.find(p => p.id === editingId), data);
  else products.push({id: Date.now(), ...data});
  saveAll(); $('#prodModal').hidden = true; renderAdmin(); renderStore(); toast('Product saved');
};

/* ---------- start ---------- */
$('#q').addEventListener('keydown', e => { if (e.key === 'Enter') setF('q', e.target.value); });
refreshHeader(); renderStore(); renderCart(); initMotion();

/* ---------- moving pictures: hero slider + product strip ---------- */
function initMotion() {
  const withImg = products.filter(p => p.image);
  if (!withImg.length) return;
  const wanted = [54, 48, 12, 3, 21, 11, 29, 9].map(i => withImg.find(p => p.image === `images/p${i}.jpg`)).filter(Boolean);
  const picks = wanted.length >= 3 ? wanted : withImg.slice(0, 6);
  const slider = $('#slider');
  slider.innerHTML = picks.map(p => `<div class="slide"><img src="${esc(p.image)}" alt="${esc(p.name)}">
      <button onclick="setF('q','${esc(p.name).replace(/'/g, '')}')"><span>${esc(p.name)}</span><span>${money(p.price)}</span></button></div>`).join('') +
    `<div class="dots-nav">${picks.map((_, i) => `<i data-i="${i}"></i>`).join('')}</div>`;
  const slides = [...slider.querySelectorAll('.slide')], dots = [...slider.querySelectorAll('.dots-nav i')];
  let n = 0, timer;
  const go = i => { n = (i + slides.length) % slides.length; slides.forEach((s, k) => s.classList.toggle('on', k === n)); dots.forEach((d, k) => d.classList.toggle('on', k === n)); };
  const play = () => { clearInterval(timer); if (!matchMedia('(prefers-reduced-motion: reduce)').matches) timer = setInterval(() => go(n + 1), 3800); };
  dots.forEach(d => d.onclick = () => { go(+d.dataset.i); play(); });
  slider.onmouseenter = () => clearInterval(timer); slider.onmouseleave = play;
  go(0); play();
  const item = p => `<div class="mi" onclick="setF('q','${esc(p.name).replace(/'/g, '')}')"><img src="${esc(p.image)}" alt="${esc(p.name)}" loading="lazy">${esc(p.name)}</div>`;
  const strip = withImg.map(item).join('');
  $('#track').innerHTML = strip + strip;
}
