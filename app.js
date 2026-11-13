(function () {
  'use strict';

  // ---- Store settings: edit these to match your shop ----
  var CURRENCY_LABEL = 'KSh';
  var FREE_SHIPPING_OVER = 5000;
  var SHIPPING_FEE = 300;
  var PRICE = 1500;
  var CART_KEY = 'crownbrim.cart.v2';

  var FITTED = ['6 7/8', '7', '7 1/8', '7 1/4', '7 3/8', '7 1/2', '7 5/8', '7 3/4', '8'];
  var ONE = ['One size'];

  var STYLES = {
    '59FIFTY': { label: 'Fitted', blurb: 'Flat brim, exact sizes', sizes: FITTED },
    '9FIFTY': { label: 'Snapback', blurb: 'Flat brim, snap closure', sizes: ONE },
    '9FORTY': { label: 'Adjustable', blurb: 'Curved brim, strap back', sizes: ONE }
  };

  // ---- Catalogue: add, remove or reprice products here ----
  // New Era caps take their sizes from `style` and cost PRICE unless given a `price`.
  // `variants` is optional: use it for colours, or when one photo shows more than one cap.
  var PRODUCTS = [
    { id: 'pink-ny-custom', style: '59FIFTY', name: 'Pink NY Custom Fitted', img: 'pink-ny-custom.jpg', badge: 'Limited',
      desc: 'Soft pink crown and brim with a white NY logo, a 1997 side patch and custom embroidery on the front panel.' },
    { id: 'sox-flames', style: '59FIFTY', name: 'Sox Flames Fitted', img: 'sox-flames.jpg', badge: 'Popular',
      variantLabel: 'Colour', variants: ['Black', 'Pink'],
      desc: 'A ring of embroidered flames climbing the crown behind a raised white Sox logo. Comes in black with red flames or pink with magenta flames.' },
    { id: 'blackout-la-ny', style: '59FIFTY', name: 'Blackout Fitted', img: 'blackout-la-ny.jpg',
      variantLabel: 'Logo', variants: ['LA, grey undervisor', 'NY, red undervisor'],
      desc: 'Black on black: a tonal raised logo on a black crown, with a contrast undervisor that only shows when you tip the brim.' },
    { id: 'sox-roses', style: '59FIFTY', name: 'Sox Roses Fitted', img: 'sox-roses.jpg', badge: 'Popular',
      desc: 'Black fitted with a white Sox logo, embroidered red roses and a bee on the front, and champions script on the side.' },
    { id: 'ny-cherry-blossom', style: '9FIFTY', name: 'NY Cherry Blossom Snapback', img: 'ny-cherry-blossom.jpg', badge: 'New',
      variantLabel: 'Colour', variants: ['Black', 'Stone'],
      desc: 'A white NY logo with a pink cherry blossom branch stitched across it and petals falling over the crown.' },
    { id: 'cream-black', style: '9FORTY', name: 'Cream & Black Adjustable', img: 'cream-black-ny-la.jpg',
      variantLabel: 'Logo', variants: ['NY', 'LA'],
      desc: 'Cream crown, black curved brim and a green undervisor, finished with a commemorative patch on the side.' },
    { id: 'ny-black-pin', style: '9FIFTY', name: 'NY Black Snapback with Gold Pin', img: 'ny-black-pin-snapback.jpg',
      desc: 'The classic black snapback with a white NY logo, set off by a small gold pin on the front panel.' },
    { id: 'ny-cream-navy', style: '59FIFTY', name: 'NY Cream & Navy Star Patch Fitted', img: 'ny-cream-navy-star.jpg', badge: 'New',
      desc: 'Cream crown with a navy brim, button and NY logo, plus a gold-stitched star patch on the side.' },
    { id: 'ny-white-navy', style: '59FIFTY', name: 'NY White & Navy Fitted', img: 'ny-white-navy.jpg',
      desc: 'Clean white crown with a navy brim, button and NY logo. Crisp, simple and easy to wear.' },
    { id: 'ny-black-low', style: '59FIFTY', name: 'NY Black Low Profile Fitted', img: 'ny-black-low-profile.jpg',
      desc: 'Black fitted with a white NY logo, in the low profile shape: a shallower crown and a gently curved brim.' },
    { id: 'ny-sky-red', style: '59FIFTY', name: 'NY Sky Blue & Red Fitted', img: 'ny-sky-red.jpg',
      desc: 'Sky blue crown with a red brim and button, and a white NY logo outlined in red.' },
    { id: 'ny-white-purple', style: '59FIFTY', name: 'NY White & Purple Fitted', img: 'ny-white-purple.jpg', more: ['ny-white-purple-front.jpg'],
      desc: 'White crown with a purple brim, a green NY logo outlined in purple and a matching World Series side patch.' },
    { id: 'ny-royal', style: '59FIFTY', name: 'NY Royal Blue Fitted', img: 'ny-royal-blue.jpg',
      desc: 'Royal blue from button to brim with a white NY logo.' },
    { id: 'mets-royal', style: '59FIFTY', name: 'Mets Royal Fitted', img: 'mets-royal.jpg',
      desc: 'The royal blue Mets cap with the orange NY logo and an orange button.' },
    { id: 'mets-script', style: '59FIFTY', name: 'Mets Champions Script Fitted', img: 'mets-champions-script.jpg',
      desc: 'Royal blue Mets fitted with an orange logo and champions script embroidered down the side.' },
    { id: 'mets-llc', style: '59FIFTY', name: 'Mets Little League Classic 2025 Fitted', img: 'mets-little-league-classic.jpg',
      desc: 'Royal blue Mets fitted with the 2025 Little League Classic patch on the side.' },
    { id: 'mets-post', style: '59FIFTY', name: 'Mets Postseason 2022 Fitted', img: 'mets-postseason-2022.jpg',
      desc: 'Royal blue Mets fitted with the 2022 Postseason patch on the side.' },

    // Bags, and caps and hats from other makers. Prices are in KSh.
    { id: "anti-theft-laptop-backpack", cat: "Bags", group: "Unisex", name: "Anti-Theft Laptop Backpack", img: "p0.jpg", price: 7800, sizes: ["M", "L"], variantLabel: 'Colour', variants: ["Grey", "Black"] },
    { id: "usb-travel-backpack", cat: "Bags", group: "Unisex", name: "USB Travel Backpack", img: "p18.jpg", price: 7150, sizes: ["L"] },
    { id: "everyday-casual-backpack", cat: "Bags", group: "Men", name: "Everyday Casual Backpack", img: "p44.jpg", price: 5200, sizes: ["M", "L"], variantLabel: 'Colour', variants: ["Grey", "Black", "Navy"] },
    { id: "minimal-backpack", cat: "Bags", group: "Unisex", name: "Minimal Backpack", img: "p54.jpg", price: 6500, sizes: ["M", "L"], variantLabel: 'Colour', variants: ["Navy", "Black", "Green"] },
    { id: "quilted-leather-backpack", cat: "Bags", group: "Women", name: "Quilted Leather Backpack", img: "p48.jpg", price: 8450, sizes: ["M"], variantLabel: 'Colour', variants: ["Brown", "Black"] },
    { id: "burgundy-leather-backpack", cat: "Bags", group: "Women", name: "Burgundy Leather Backpack", img: "p55.jpg", price: 9100, sizes: ["M"], variantLabel: 'Colour', variants: ["Red", "Brown"] },
    { id: "unicorn-kids-backpack", cat: "Bags", group: "Kids", name: "Unicorn Kids Backpack", img: "p32.jpg", price: 3900, sizes: ["S"] },
    { id: "checkered-crossbody-bag", cat: "Bags", group: "Men", name: "Checkered Crossbody Bag", img: "p2.jpg", price: 3250, sizes: ["S"], variantLabel: 'Colour', variants: ["Grey", "Black"] },
    { id: "leather-messenger-bag", cat: "Bags", group: "Men", name: "Leather Messenger Bag", img: "p36.jpg", price: 5850, sizes: ["S", "M"] },
    { id: "gym-duffel-bag", cat: "Bags", group: "Men", name: "Gym Duffel Bag", img: "p11.jpg", price: 5500, sizes: ["M", "L"] },
    { id: "navy-gold-trim-handbag", cat: "Bags", group: "Women", name: "Navy Gold-Trim Handbag", img: "p3.jpg", price: 9750, sizes: ["M"] },
    { id: "navy-classic-handbag", cat: "Bags", group: "Women", name: "Navy Classic Handbag", img: "p7.jpg", price: 9100, sizes: ["M"], variantLabel: 'Colour', variants: ["Navy", "Black"] },
    { id: "blush-structured-tote", cat: "Bags", group: "Women", name: "Blush Structured Tote", img: "p9.jpg", price: 7800, sizes: ["M", "L"], variantLabel: 'Colour', variants: ["Beige", "Pink"] },
    { id: "pink-satchel-handbag", cat: "Bags", group: "Women", name: "Pink Satchel Handbag", img: "p17.jpg", price: 7150, sizes: ["M"], variantLabel: 'Colour', variants: ["Pink", "Black"] },
    { id: "mini-pink-crossbody", cat: "Bags", group: "Women", name: "Mini Pink Crossbody", img: "p16.jpg", price: 4550, sizes: ["S"] },
    { id: "grey-handbag-with-purse", cat: "Bags", group: "Women", name: "Grey Handbag with Purse", img: "p26.jpg", price: 10400, sizes: ["M"], variantLabel: 'Colour', variants: ["Grey", "Beige"] },
    { id: "3-piece-handbag-set", cat: "Bags", group: "Women", name: "3-Piece Handbag Set", img: "p25.jpg", price: 11050, sizes: ["M", "L"], variantLabel: 'Colour', variants: ["Navy", "Brown"] },
    { id: "black-tassel-handbag", cat: "Bags", group: "Women", name: "Black Tassel Handbag", img: "p40.jpg", price: 6500, sizes: ["M"] },
    { id: "black-bow-work-tote", cat: "Bags", group: "Women", name: "Black Bow Work Tote", img: "p47.jpg", price: 7550, sizes: ["L"] },
    { id: "brown-saddle-crossbody", cat: "Bags", group: "Women", name: "Brown Saddle Crossbody", img: "p43.jpg", price: 5050, sizes: ["S"] },
    { id: "woven-leather-tote", cat: "Bags", group: "Women", name: "Woven Leather Tote", img: "p50.jpg", price: 11700, sizes: ["L"] },
    { id: "black-work-tote", cat: "Bags", group: "Women", name: "Black Work Tote", img: "p51.jpg", price: 9500, sizes: ["L"] },
    { id: "classic-baseball-cap", cat: "Caps & hats", group: "Unisex", name: "Classic Baseball Cap", img: "p12.jpg", price: 2600, sizes: ["S/M", "L/XL"], variantLabel: 'Colour', variants: ["Black", "Navy", "Grey"] },
    { id: "sport-mesh-cap", cat: "Caps & hats", group: "Men", name: "Sport Mesh Cap", img: "p29.jpg", price: 2850, sizes: ["S/M", "L/XL"], variantLabel: 'Colour', variants: ["Black", "Grey", "Navy"] },
    { id: "embroidered-cross-cap", cat: "Caps & hats", group: "Unisex", name: "Embroidered Cross Cap", img: "p46.jpg", price: 3000, sizes: ["One size"] },
    { id: "quilted-leather-cap", cat: "Caps & hats", group: "Men", name: "Quilted Leather Cap", img: "p41.jpg", price: 3900, sizes: ["One size"], variantLabel: 'Colour', variants: ["Black", "Navy"] },
    { id: "red-fade-baseball-cap", cat: "Caps & hats", group: "Women", name: "Red Fade Baseball Cap", img: "p49.jpg", price: 2450, sizes: ["One size"], variantLabel: 'Colour', variants: ["Red", "White"] },
    { id: "flat-brim-snapback", cat: "Caps & hats", group: "Men", name: "Flat Brim Snapback", img: "p19.jpg", price: 3000, sizes: ["One size"], variantLabel: 'Colour', variants: ["Black", "Beige", "Blue", "Red", "White"], soldOut: true },
    { id: "brimless-docker-cap", cat: "Caps & hats", group: "Men", name: "Brimless Docker Cap", img: "p21.jpg", price: 2200, sizes: ["One size"] },
    { id: "sports-brimless-cap", cat: "Caps & hats", group: "Unisex", name: "Sports Brimless Cap", img: "p24.jpg", price: 2350, sizes: ["One size"], variantLabel: 'Colour', variants: ["Beige", "Grey", "Green", "Navy"] },
    { id: "bucket-hat", cat: "Caps & hats", group: "Unisex", name: "Bucket Hat", img: "p10.jpg", price: 3250, sizes: ["S/M", "L/XL"] },
    { id: "camo-army-cap", cat: "Caps & hats", group: "Men", name: "Camo Army Cap", img: "p31.jpg", price: 2600, sizes: ["One size"], variantLabel: 'Colour', variants: ["Green", "Khaki"] },
    { id: "mesh-army-cap", cat: "Caps & hats", group: "Men", name: "Mesh Army Cap", img: "p35.jpg", price: 2450, sizes: ["One size"] },
    { id: "graffiti-print-cap", cat: "Caps & hats", group: "Unisex", name: "Graffiti Print Cap", img: "p39.jpg", price: 2750, sizes: ["One size"], variantLabel: 'Colour', variants: ["Black", "White"] },
    { id: "ribbed-beanie", cat: "Caps & hats", group: "Unisex", name: "Ribbed Beanie", img: "p15.jpg", price: 1950, sizes: ["One size"] },
    { id: "fisherman-beanie-set", cat: "Caps & hats", group: "Unisex", name: "Fisherman Beanie Set", img: "p52.jpg", price: 2100, sizes: ["One size"], variantLabel: 'Colour', variants: ["Brown", "Black"] },
    { id: "faux-fur-pom-hat", cat: "Caps & hats", group: "Women", name: "Faux Fur Pom Hat", img: "p27.jpg", price: 3500, sizes: ["One size"] },
    { id: "flat-ivy-caps", cat: "Caps & hats", group: "Men", name: "Flat Ivy Caps", img: "p6.jpg", price: 3100, sizes: ["S/M", "L/XL"], variantLabel: 'Colour', variants: ["Navy", "Black", "Grey"] }
  ];

  var CATEGORIES = {
    'New Era': 'Fitted, snapback and adjustable',
    'Caps & hats': 'Baseball caps, beanies and bucket hats',
    'Bags': 'Backpacks, handbags and totes'
  };

  var byId = {};
  PRODUCTS.forEach(function (p) {
    p.cat = p.cat || 'New Era';
    p.price = p.price || PRICE;
    p.sizes = p.sizes || STYLES[p.style].sizes;
    p.tag = p.style ? p.style + ' · ' + STYLES[p.style].label : p.cat + ' · ' + p.group;
    byId[p.id] = p;
  });

  function fmt(n) { return CURRENCY_LABEL + ' ' + n.toLocaleString('en-KE'); }
  function $(id) { return document.getElementById(id); }
  function el(tag, cls, text) {
    var n = document.createElement(tag);
    if (cls) n.className = cls;
    if (text != null) n.textContent = text;
    return n;
  }
  function photo(file, alt) {
    var i = el('img');
    i.src = 'images/' + file;
    i.alt = alt;
    i.loading = 'lazy';
    return i;
  }

  // ---- Catalogue rendering ----
  var state = { filter: 'all', query: '', sort: 'featured' };

  function visibleProducts() {
    var q = state.query.trim().toLowerCase();
    var list = PRODUCTS.filter(function (p) {
      if (state.filter !== 'all' && p.cat !== state.filter) return false;
      if (!q) return true;
      return (p.name + ' ' + p.cat + ' ' + p.tag).toLowerCase().indexOf(q) !== -1;
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
      if (p.soldOut || p.badge) card.appendChild(el('span', 'badge', p.soldOut ? 'Sold out' : p.badge));
      var art = el('button', 'card-art');
      art.type = 'button';
      art.setAttribute('aria-label', 'View ' + p.name);
      art.appendChild(photo(p.img, ''));
      if (!p.style) art.classList.add('contain');
      art.addEventListener('click', function () { openProduct(p.id, art); });
      card.appendChild(art);

      var body = el('div', 'card-body');
      body.appendChild(el('span', 'card-style', p.tag));
      body.appendChild(el('h3', 'card-name', p.name));
      var foot = el('div', 'card-foot');
      foot.appendChild(el('p', 'price', fmt(p.price)));
      var btn = el('button', 'btn btn-primary', p.soldOut ? 'View' : 'Choose');
      btn.type = 'button';
      btn.setAttribute('aria-label', (p.soldOut ? 'View ' : 'Choose options for ') + p.name);
      btn.addEventListener('click', function () { openProduct(p.id, btn); });
      foot.appendChild(btn);
      body.appendChild(foot);
      card.appendChild(body);
      grid.appendChild(card);
    });
    $('emptyState').hidden = list.length > 0;
    $('resultCount').textContent = list.length + (list.length === 1 ? ' item' : ' items');
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
    [['all', 'All']].concat(Object.keys(CATEGORIES).map(function (s) { return [s, s]; })).forEach(function (pair) {
      var c = el('button', 'chip', pair[1]);
      c.type = 'button';
      c.dataset.filter = pair[0];
      c.setAttribute('aria-pressed', String(pair[0] === state.filter));
      c.addEventListener('click', function () { setFilter(pair[0]); });
      chips.appendChild(c);
    });

    var sg = $('styleGrid');
    Object.keys(CATEGORIES).forEach(function (s) {
      var card = el('button', 'style-card');
      card.type = 'button';
      card.appendChild(el('strong', null, s));
      card.appendChild(el('span', null, CATEGORIES[s]));
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
  var current = { id: null, size: null, variant: null };

  function pickers(box, options, key) {
    box.textContent = '';
    options.forEach(function (o) {
      var b = el('button', 'size', o);
      b.type = 'button';
      b.setAttribute('aria-pressed', String(o === current[key]));
      b.addEventListener('click', function () {
        current[key] = o;
        $('pmError').hidden = true;
        Array.prototype.forEach.call(box.children, function (n) {
          n.setAttribute('aria-pressed', String(n === b));
        });
      });
      box.appendChild(b);
    });
  }

  function openProduct(id, trigger) {
    var p = byId[id];
    var sizes = p.sizes;
    current = { id: id, size: sizes.length === 1 ? sizes[0] : null, variant: p.variants ? null : '' };

    var art = $('pmArt');
    art.textContent = '';
    art.classList.toggle('contain', !p.style);
    var main = photo(p.img, p.name);
    main.loading = 'eager';
    art.appendChild(main);
    if (p.more) {
      var thumbs = el('div', 'thumbs');
      [p.img].concat(p.more).forEach(function (file, i) {
        var t = el('button', 'thumb-btn');
        t.type = 'button';
        t.setAttribute('aria-label', 'Show photo ' + (i + 1));
        t.setAttribute('aria-pressed', String(i === 0));
        t.appendChild(photo(file, ''));
        t.addEventListener('click', function () {
          main.src = 'images/' + file;
          Array.prototype.forEach.call(thumbs.children, function (n) {
            n.setAttribute('aria-pressed', String(n === t));
          });
        });
        thumbs.appendChild(t);
      });
      art.appendChild(thumbs);
    }

    $('pmStyle').textContent = p.tag;
    $('pmName').textContent = p.name;
    $('pmPrice').textContent = fmt(p.price);
    $('pmDesc').textContent = p.desc || '';
    $('pmDesc').hidden = !p.desc;
    $('pmError').hidden = true;
    $('pmAdd').disabled = !!p.soldOut;
    $('pmAdd').textContent = p.soldOut ? 'Sold out' : 'Add to cart';

    $('pmVariantField').hidden = !p.variants;
    if (p.variants) {
      $('pmVariantLegend').textContent = p.variantLabel;
      pickers($('pmVariants'), p.variants, 'variant');
    }
    pickers($('pmSizes'), sizes, 'size');
    show($('productModal'), trigger);
  }

  // ---- Cart ----
  var cart = loadCart();

  function validLine(i) {
    var p = i && byId[i.id];
    if (!p || p.soldOut || p.sizes.indexOf(i.size) === -1 || !(i.qty > 0)) return false;
    return p.variants ? p.variants.indexOf(i.variant) !== -1 : !i.variant;
  }

  function loadCart() {
    try {
      var raw = JSON.parse(localStorage.getItem(CART_KEY) || '[]');
      if (!Array.isArray(raw)) return [];
      return raw.filter(validLine).map(function (i) {
        return { id: i.id, size: i.size, variant: i.variant || '', qty: Math.min(99, Math.floor(i.qty)) };
      });
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

  function addToCart(id, size, variant) {
    var line = cart.filter(function (i) { return i.id === id && i.size === size && i.variant === variant; })[0];
    if (line) line.qty = Math.min(99, line.qty + 1); else cart.push({ id: id, size: size, variant: variant, qty: 1 });
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
      thumb.appendChild(photo(p.img, ''));
      row.appendChild(thumb);

      var mid = el('div');
      mid.appendChild(el('h4', null, p.name));
      mid.appendChild(el('small', null, [item.variant, item.size].filter(Boolean).join(' · ')));
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
    body.appendChild(el('h3', null, 'Thank you, ' + data.name.split(' ')[0] + '.'));
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

    renderFilters();
    renderGrid();
    renderCart();

    $('search').addEventListener('input', function (e) { state.query = e.target.value; renderGrid(); });
    $('sort').addEventListener('change', function (e) { state.sort = e.target.value; renderGrid(); });

    $('pmAdd').addEventListener('click', function () {
      var p = byId[current.id];
      var missing = [];
      if (current.variant === null) missing.push(p.variantLabel.toLowerCase());
      if (!current.size) missing.push('size');
      if (missing.length) {
        $('pmError').textContent = 'Choose a ' + missing.join(' and a ') + ' first.';
        $('pmError').hidden = false;
        return;
      }
      addToCart(current.id, current.size, current.variant);
      closeDialog();
      toast(p.name + ' added to cart');
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
