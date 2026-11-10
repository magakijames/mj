(function () {
  'use strict';

  // ---- Store settings: edit these to match your shop ----
  var CURRENCY = 'USD';
  var LOCALE = 'en-US';
  var FREE_SHIPPING_OVER = 75;
  var SHIPPING_FEE = 6;
  var CART_KEY = 'crownbrim.cart.v1';

  var FITTED = ['6 7/8', '7', '7 1/8', '7 1/4', '7 3/8', '7 1/2', '7 5/8', '7 3/4', '8'];
  var STRETCH = ['S/M', 'M/L', 'L/XL'];
  var ONE = ['One size'];

  var STYLES = {
    '59FIFTY': { label: 'Fitted', blurb: 'Flat brim, exact sizes', sizes: FITTED, flat: true },
    '9FIFTY': { label: 'Snapback', blurb: 'Flat brim, snap closure', sizes: ONE, flat: true },
    '9FORTY': { label: 'Adjustable', blurb: 'Curved brim, strap back', sizes: ONE, flat: false },
    '39THIRTY': { label: 'Stretch fit', blurb: 'Curved brim, flex band', sizes: STRETCH, flat: false }
  };

  // ---- Catalogue: add, remove or reprice caps here ----
  var PRODUCTS = [
    { id: 'f-navy', style: '59FIFTY', name: 'Classic Navy Fitted', price: 44, crown: '#1c2a4a', brim: '#1c2a4a', under: '#8a8f98', badge: 'Best seller',
      desc: 'The original fitted cap. Structured six-panel crown, flat brim and a grey undervisor.' },
    { id: 'f-black', style: '59FIFTY', name: 'Blackout Fitted', price: 44, crown: '#17181b', brim: '#17181b', under: '#17181b',
      desc: 'All black from button to undervisor. Wool-blend crown with a high, structured profile.' },
    { id: 'f-cream', style: '59FIFTY', name: 'Cream & Forest Fitted', price: 48, crown: '#efe6d2', brim: '#254f3a', under: '#254f3a', badge: 'New',
      desc: 'Two-tone fitted with a cream crown and a forest green brim and button.' },
    { id: 'f-burg', style: '59FIFTY', name: 'Burgundy Fitted', price: 46, crown: '#6d1f2c', brim: '#6d1f2c', under: '#d9a7b0',
      desc: 'Deep burgundy crown with a soft pink undervisor for a flash of colour.' },
    { id: 's-red', style: '9FIFTY', name: 'Scarlet Snapback', price: 38, crown: '#c5281c', brim: '#17181b', under: '#2f8a57',
      desc: 'Bright scarlet crown over a black flat brim, with a green undervisor and snap closure.' },
    { id: 's-grey', style: '9FIFTY', name: 'Heather Grey Snapback', price: 36, crown: '#a3a7ad', brim: '#1c2a4a', under: '#1c2a4a',
      desc: 'Heather grey crown, navy brim. One size, adjusts with the snap at the back.' },
    { id: 's-sky', style: '9FIFTY', name: 'Sky Blue Snapback', price: 38, crown: '#7fb4dc', brim: '#7fb4dc', under: '#efe6d2', badge: 'New',
      desc: 'Light blue all over with a cream undervisor. Same shape as the fitted, easier to size.' },
    { id: 'a-olive', style: '9FORTY', name: 'Olive Adjustable', price: 30, crown: '#5c6340', brim: '#5c6340', under: '#5c6340',
      desc: 'Washed cotton twill with a curved brim and a cloth strap. The easy everyday cap.' },
    { id: 'a-black', style: '9FORTY', name: 'Black Adjustable', price: 30, crown: '#17181b', brim: '#17181b', under: '#17181b', badge: 'Best seller',
      desc: 'A plain black curved-brim cap with an adjustable strap. Goes with everything.' },
    { id: 'a-sand', style: '9FORTY', name: 'Sand Adjustable', price: 32, crown: '#d7c3a0', brim: '#d7c3a0', under: '#8a6f4a',
      desc: 'Sand cotton crown with a tan undervisor. Soft, light and broken in from day one.' },
    { id: 't-navy', style: '39THIRTY', name: 'Navy Stretch Fit', price: 34, crown: '#1c2a4a', brim: '#1c2a4a', under: '#1c2a4a',
      desc: 'Closed back with a stretch sweatband, so it sits like a fitted without exact sizing.' },
    { id: 't-char', style: '39THIRTY', name: 'Charcoal Stretch Fit', price: 34, crown: '#43464c', brim: '#17181b', under: '#17181b',
      desc: 'Charcoal stretch crown with a black curved brim. Three size bands cover most heads.' }
  ];

  var byId = {};
  PRODUCTS.forEach(function (p) { byId[p.id] = p; });

  var money = new Intl.NumberFormat(LOCALE, { style: 'currency', currency: CURRENCY, maximumFractionDigits: 0 });
  function fmt(n) { return money.format(n); }
  function $(id) { return document.getElementById(id); }
  function el(tag, cls, text) {
    var n = document.createElement(tag);
    if (cls) n.className = cls;
    if (text != null) n.textContent = text;
    return n;
  }

  // ---- Cap illustration ----
  function capSvg(p) {
    var flat = STYLES[p.style].flat;
    var brim = flat
      ? 'M52 96 C34 96 14 99 6 104 C3 106 4 110 8 110 C40 109 84 106 112 101 Z'
      : 'M52 96 C36 98 18 106 10 116 C8 119 10 122 14 121 C44 114 84 108 112 101 Z';
    var under = flat
      ? 'M8 110 C40 109 84 106 112 101 L108 104 C80 109 40 113 10 113 Z'
      : 'M14 121 C44 114 84 108 112 101 L108 105 C82 111 46 118 17 124 Z';
    return '<svg viewBox="0 0 200 140" role="img" aria-label="' + p.name + '">' +
      '<ellipse cx="104" cy="128" rx="78" ry="6" fill="rgba(0,0,0,.12)"/>' +
      '<path d="' + under + '" fill="' + p.under + '"/>' +
      '<path d="' + brim + '" fill="' + p.brim + '"/>' +
      '<path d="M50 98 C46 54 78 24 118 24 C158 24 184 58 182 102 C140 108 92 106 50 98 Z" fill="' + p.crown + '"/>' +
      '<path d="M118 24 C100 44 90 72 90 104 M118 24 C132 46 140 76 142 106" fill="none" stroke="rgba(255,255,255,.22)" stroke-width="1.2"/>' +
      '<path d="M50 98 C92 106 140 108 182 102" fill="none" stroke="rgba(0,0,0,.25)" stroke-width="2"/>' +
      '<circle cx="74" cy="60" r="1.8" fill="rgba(0,0,0,.35)"/><circle cx="118" cy="50" r="1.8" fill="rgba(0,0,0,.35)"/><circle cx="158" cy="62" r="1.8" fill="rgba(0,0,0,.35)"/>' +
      '<ellipse cx="118" cy="24" rx="7" ry="3.2" fill="' + p.brim + '" stroke="rgba(0,0,0,.25)" stroke-width="1"/>' +
      '</svg>';
  }

  // ---- Catalogue rendering ----
  var state = { filter: 'all', query: '', sort: 'featured' };

  function visibleProducts() {
    var q = state.query.trim().toLowerCase();
    var list = PRODUCTS.filter(function (p) {
      if (state.filter !== 'all' && p.style !== state.filter) return false;
      if (!q) return true;
      return (p.name + ' ' + p.style + ' ' + STYLES[p.style].label).toLowerCase().indexOf(q) !== -1;
    });
    if (state.sort === 'price-asc') list.sort(function (a, b) { return a.price - b.price; });
    if (state.sort === 'price-desc') list.sort(function (a, b) { return b.price - a.price; });
    if (state.sort === 'name') list.sort(function (a, b) { return a.name.localeCompare(b.name); });
    return list;
  }

  function renderGrid() {
    var grid = $('productGrid');
    var list = visibleProducts();
    grid.textContent = '';
    list.forEach(function (p) {
      var card = el('article', 'card');
      if (p.badge) card.appendChild(el('span', 'badge', p.badge));
      var art = el('button', 'card-art');
      art.type = 'button';
      art.setAttribute('aria-label', 'View ' + p.name);
      art.innerHTML = capSvg(p);
      art.addEventListener('click', function () { openProduct(p.id, art); });
      card.appendChild(art);

      var body = el('div', 'card-body');
      body.appendChild(el('span', 'card-style', p.style + ' · ' + STYLES[p.style].label));
      body.appendChild(el('h3', 'card-name', p.name));
      var foot = el('div', 'card-foot');
      foot.appendChild(el('p', 'price', fmt(p.price)));
      var btn = el('button', 'btn btn-primary', 'Choose size');
      btn.type = 'button';
      btn.addEventListener('click', function () { openProduct(p.id, btn); });
      foot.appendChild(btn);
      body.appendChild(foot);
      card.appendChild(body);
      grid.appendChild(card);
    });
    $('emptyState').hidden = list.length > 0;
    $('resultCount').textContent = list.length + (list.length === 1 ? ' cap' : ' caps');
  }

  function setFilter(f) {
    state.filter = f;
    Array.prototype.forEach.call($('filterChips').children, function (c) {
      c.setAttribute('aria-pressed', String(c.dataset.filter === f));
    });
    renderGrid();
  }

  function renderFilters() {
    var chips = $('filterChips');
    [['all', 'All']].concat(Object.keys(STYLES).map(function (s) { return [s, s]; })).forEach(function (pair) {
      var c = el('button', 'chip', pair[1]);
      c.type = 'button';
      c.dataset.filter = pair[0];
      c.setAttribute('aria-pressed', String(pair[0] === state.filter));
      c.addEventListener('click', function () { setFilter(pair[0]); });
      chips.appendChild(c);
    });

    var sg = $('styleGrid');
    Object.keys(STYLES).forEach(function (s) {
      var card = el('button', 'style-card');
      card.type = 'button';
      card.appendChild(el('strong', null, s));
      card.appendChild(el('span', null, STYLES[s].label + ' · ' + STYLES[s].blurb));
      card.addEventListener('click', function () {
        setFilter(s);
        $('shop').scrollIntoView();
      });
      sg.appendChild(card);
    });
  }

  // ---- Dialogs ----
  var openDialog = null;
  var returnFocus = null;

  function show(dialog, trigger) {
    if (openDialog) openDialog.hidden = true; else returnFocus = trigger || document.activeElement;
    openDialog = dialog;
    dialog.hidden = false;
    $('overlay').hidden = false;
    document.body.classList.add('locked');
    var first = dialog.querySelector('[data-close]');
    if (first) first.focus();
  }

  function closeDialog() {
    if (!openDialog) return;
    openDialog.hidden = true;
    openDialog = null;
    $('overlay').hidden = true;
    document.body.classList.remove('locked');
    if (returnFocus && document.contains(returnFocus)) returnFocus.focus();
    returnFocus = null;
  }

  // ---- Product detail ----
  var current = { id: null, size: null };

  function openProduct(id, trigger) {
    var p = byId[id];
    var sizes = STYLES[p.style].sizes;
    current = { id: id, size: sizes.length === 1 ? sizes[0] : null };
    $('pmArt').innerHTML = capSvg(p);
    $('pmStyle').textContent = p.style + ' · ' + STYLES[p.style].label;
    $('pmName').textContent = p.name;
    $('pmPrice').textContent = fmt(p.price);
    $('pmDesc').textContent = p.desc;
    $('pmError').hidden = true;
    var box = $('pmSizes');
    box.textContent = '';
    sizes.forEach(function (s) {
      var b = el('button', 'size', s);
      b.type = 'button';
      b.setAttribute('aria-pressed', String(s === current.size));
      b.addEventListener('click', function () {
        current.size = s;
        $('pmError').hidden = true;
        Array.prototype.forEach.call(box.children, function (o) {
          o.setAttribute('aria-pressed', String(o === b));
        });
      });
      box.appendChild(b);
    });
    show($('productModal'), trigger);
  }

  // ---- Cart ----
  var cart = loadCart();

  function loadCart() {
    try {
      var raw = JSON.parse(localStorage.getItem(CART_KEY) || '[]');
      if (!Array.isArray(raw)) return [];
      return raw.filter(function (i) {
        return i && byId[i.id] && STYLES[byId[i.id].style].sizes.indexOf(i.size) !== -1 && i.qty > 0;
      }).map(function (i) { return { id: i.id, size: i.size, qty: Math.min(99, Math.floor(i.qty)) }; });
    } catch (e) { return []; }
  }

  function saveCart() {
    try { localStorage.setItem(CART_KEY, JSON.stringify(cart)); } catch (e) { /* storage unavailable */ }
  }

  function totals() {
    var subtotal = cart.reduce(function (s, i) { return s + byId[i.id].price * i.qty; }, 0);
    var shipping = subtotal === 0 || subtotal >= FREE_SHIPPING_OVER ? 0 : SHIPPING_FEE;
    return { subtotal: subtotal, shipping: shipping, total: subtotal + shipping };
  }

  function addToCart(id, size) {
    var line = cart.filter(function (i) { return i.id === id && i.size === size; })[0];
    if (line) line.qty = Math.min(99, line.qty + 1); else cart.push({ id: id, size: size, qty: 1 });
    saveCart();
    renderCart();
  }

  function renderCart() {
    var box = $('cartItems');
    box.textContent = '';
    var count = cart.reduce(function (s, i) { return s + i.qty; }, 0);
    $('cartCount').textContent = count;
    $('cartFoot').hidden = cart.length === 0;

    if (!cart.length) {
      box.appendChild(el('p', 'cart-empty', 'Your cart is empty.'));
      return;
    }

    cart.forEach(function (item, idx) {
      var p = byId[item.id];
      var row = el('div', 'cart-item');
      var thumb = el('div', 'thumb');
      thumb.innerHTML = capSvg(p);
      row.appendChild(thumb);

      var mid = el('div');
      mid.appendChild(el('h4', null, p.name));
      mid.appendChild(el('small', null, p.style + ' · ' + item.size));
      var qty = el('div', 'qty');
      var minus = el('button', null, '−');
      minus.type = 'button';
      minus.setAttribute('aria-label', 'Decrease quantity of ' + p.name);
      var plus = el('button', null, '+');
      plus.type = 'button';
      plus.setAttribute('aria-label', 'Increase quantity of ' + p.name);
      minus.addEventListener('click', function () { changeQty(idx, -1); });
      plus.addEventListener('click', function () { changeQty(idx, 1); });
      qty.appendChild(minus);
      qty.appendChild(el('span', null, String(item.qty)));
      qty.appendChild(plus);
      mid.appendChild(qty);
      row.appendChild(mid);

      var right = el('div', 'line');
      right.appendChild(el('div', null, fmt(p.price * item.qty)));
      var rm = el('button', 'remove', 'Remove');
      rm.type = 'button';
      rm.setAttribute('aria-label', 'Remove ' + p.name);
      rm.addEventListener('click', function () { changeQty(idx, -item.qty); });
      right.appendChild(rm);
      row.appendChild(right);
      box.appendChild(row);
    });

    var t = totals();
    $('cartSubtotal').textContent = fmt(t.subtotal);
    $('cartShipping').textContent = t.shipping ? fmt(t.shipping) : 'Free';
    $('cartTotal').textContent = fmt(t.total);
  }

  function changeQty(idx, delta) {
    var item = cart[idx];
    if (!item) return;
    item.qty = Math.min(99, item.qty + delta);
    if (item.qty <= 0) cart.splice(idx, 1);
    saveCart();
    renderCart();
  }

  // ---- Toast ----
  var toastTimer;
  function toast(msg) {
    var t = $('toast');
    t.textContent = msg;
    t.classList.add('show');
    clearTimeout(toastTimer);
    toastTimer = setTimeout(function () { t.classList.remove('show'); }, 2200);
  }

  // ---- Checkout ----
  var checkoutMarkup;

  function openCheckout() {
    if (!cart.length) return;
    $('coBody').innerHTML = checkoutMarkup;
    $('coTotal').textContent = fmt(totals().total);
    $('checkoutForm').addEventListener('submit', submitOrder);
    show($('checkoutModal'));
  }

  function submitOrder(e) {
    e.preventDefault();
    var form = e.target;
    var err = $('coError');
    var data = {};
    ['name', 'email', 'phone', 'address'].forEach(function (k) { data[k] = form.elements[k].value.trim(); });
    var problem = '';
    if (!data.name || !data.phone || !data.address) problem = 'Fill in every field so we can deliver your order.';
    else if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(data.email)) problem = 'Enter a valid email address.';
    if (problem) {
      err.textContent = problem;
      err.hidden = false;
      return;
    }

    // No backend yet: the order is only confirmed on screen. Send `order` to
    // your server or payment provider here when one is connected.
    var order = { ref: 'CB-' + Date.now().toString(36).toUpperCase(), customer: data, items: cart.slice(), totals: totals() };

    var body = $('coBody');
    body.textContent = '';
    body.appendChild(el('p', 'eyebrow', 'Order placed'));
    body.appendChild(el('h3', null, 'Thanks, ' + data.name.split(' ')[0] + '.'));
    var p = el('p');
    p.appendChild(document.createTextNode('Your order reference is '));
    p.appendChild(el('span', 'order-ref', order.ref));
    p.appendChild(document.createTextNode('. Total: ' + fmt(order.totals.total) + '.'));
    body.appendChild(p);
    var done = el('button', 'btn btn-primary btn-block', 'Keep shopping');
    done.type = 'button';
    done.addEventListener('click', closeDialog);
    body.appendChild(done);

    cart = [];
    saveCart();
    renderCart();
  }

  // ---- Wire up ----
  function init() {
    checkoutMarkup = $('coBody').innerHTML;
    $('year').textContent = new Date().getFullYear();
    document.querySelector('[data-free-ship]').textContent = fmt(FREE_SHIPPING_OVER);
    $('heroArt').innerHTML = capSvg(byId['f-cream']);

    renderFilters();
    renderGrid();
    renderCart();

    $('search').addEventListener('input', function (e) { state.query = e.target.value; renderGrid(); });
    $('sort').addEventListener('change', function (e) { state.sort = e.target.value; renderGrid(); });

    $('pmAdd').addEventListener('click', function () {
      if (!current.size) { $('pmError').hidden = false; return; }
      addToCart(current.id, current.size);
      closeDialog();
      toast(byId[current.id].name + ' added to cart');
    });

    $('cartOpen').addEventListener('click', function () { show($('cartDrawer'), $('cartOpen')); });
    $('checkoutOpen').addEventListener('click', openCheckout);
    $('overlay').addEventListener('click', closeDialog);
    document.addEventListener('click', function (e) {
      if (e.target.closest('[data-close]')) closeDialog();
    });
    document.addEventListener('keydown', function (e) {
      if (e.key === 'Escape') closeDialog();
    });
  }

  init();
})();
