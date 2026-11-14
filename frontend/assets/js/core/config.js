// Settings shared by every page.

// Where the Java backend runs.
// Opened through the backend (for example http://localhost:8080): the API is on the same server.
// Opened through VS Code Live Server (ports 5500 and up): the API is the backend on port 8080.
const LIVE_SERVER = /^55\d\d$/.test(location.port);
export const API_BASE = LIVE_SERVER ? 'http://localhost:8080/api' : '/api';

// The folder that holds index.html, worked out from this file's own address,
// so links and images work from any page however the site is served.
const ROOT = new URL('../../../', import.meta.url);

// Turns a path inside the site (such as 'pages/cart.html') into a full web address.
export const url = path => new URL(path, ROOT).href;

// Roles, as the backend names them, with the labels shown on screen.
export const ROLES = {
  ADMIN: 'Admin',
  PRODUCT_MANAGER: 'Product manager',
  ORDER_MANAGER: 'Order manager',
  SUPPORT: 'Support',
  CUSTOMER: 'Customer',
};

export const ROLE_NOTES = {
  ADMIN: 'Controls everything, including site settings, staff accounts and roles.',
  PRODUCT_MANAGER: 'Adds, edits and removes products and updates stock.',
  ORDER_MANAGER: 'Sees all orders and updates their status.',
  SUPPORT: 'Can look at orders and customers but cannot change them.',
  CUSTOMER: 'Shops and sees their own orders.',
};

// Which roles may open each admin page. The backend enforces the same rules;
// this list only decides which links and pages the front end shows.
export const ADMIN_PAGES = [
  { key: 'dashboard', label: 'Dashboard', path: 'admin/index.html', roles: ['ADMIN', 'PRODUCT_MANAGER', 'ORDER_MANAGER', 'SUPPORT'] },
  { key: 'products', label: 'Products', path: 'admin/products.html', roles: ['ADMIN', 'PRODUCT_MANAGER'] },
  { key: 'orders', label: 'Orders', path: 'admin/orders.html', roles: ['ADMIN', 'ORDER_MANAGER', 'SUPPORT'] },
  { key: 'users', label: 'Users', path: 'admin/users.html', roles: ['ADMIN', 'SUPPORT'] },
  { key: 'settings', label: 'Site settings', path: 'admin/settings.html', roles: ['ADMIN'] },
  { key: 'activity', label: 'Activity', path: 'admin/activity.html', roles: ['ADMIN'] },
];

export const ORDER_STATUSES = { PENDING: 'Pending', SHIPPED: 'Shipped', DELIVERED: 'Delivered', CANCELLED: 'Cancelled' };

export const COLORS = {
  Black: '#222', Green: '#1f6b4a', Navy: '#1f3a5f', Beige: '#d8c3a5', Red: '#c0392b', Grey: '#888', White: '#fff',
  Pink: '#f4a6a6', Blue: '#3b6fb6', Cream: '#efe6d2', Stone: '#cfc6c4', 'Sky blue': '#8fc1e6', Purple: '#6b3fa0',
  Brown: '#7a5230', Khaki: '#b5a36a',
};

// The site text used until the server answers, and if it cannot be reached.
// The real values are kept in the database and edited on the admin "Site settings" page.
export const DEFAULT_SETTINGS = {
  shop_name: 'Snap & Pack',
  tagline: 'Bags & caps for every day',
  announcement: 'Free shipping on orders over KSh 6,500 · Easy 30-day returns',
  hero_title: 'Carry it well.\nWear it better.',
  hero_text: 'Backpacks, handbags and caps in the sizes and colours you actually want.',
  perk1_title: 'Free shipping', perk1_text: 'On orders over KSh 6,500',
  perk2_title: 'Easy returns', perk2_text: '30 days, no fuss',
  perk3_title: 'Customer support', perk3_text: 'Here every day',
  perk4_title: 'Secure checkout', perk4_text: 'Your details stay safe',
  footer_about: 'Your one-stop shop for bags and caps.',
  contact_email: '',
  contact_phone: '',
};

// The form on the admin "Site settings" page, section by section.
export const SETTING_SECTIONS = [
  { title: 'Shop', fields: [
    { key: 'shop_name', label: 'Shop name', required: true },
    { key: 'tagline', label: 'Line under the shop name' },
  ] },
  { title: 'Announcement bar', note: 'The dark strip at the very top of every store page.', fields: [
    { key: 'announcement', label: 'Announcement' },
  ] },
  { title: 'Home page headline', fields: [
    { key: 'hero_title', label: 'Headline', multiline: true, hint: 'Press Enter to start a second line.' },
    { key: 'hero_text', label: 'Text under the headline', multiline: true },
  ] },
  { title: 'Promise boxes', note: 'The four boxes under the headline: shipping, returns, support and checkout.', fields: [
    { key: 'perk1_title', label: 'Box 1 title (shipping)' }, { key: 'perk1_text', label: 'Box 1 text' },
    { key: 'perk2_title', label: 'Box 2 title (returns)' }, { key: 'perk2_text', label: 'Box 2 text' },
    { key: 'perk3_title', label: 'Box 3 title (support)' }, { key: 'perk3_text', label: 'Box 3 text' },
    { key: 'perk4_title', label: 'Box 4 title (checkout)' }, { key: 'perk4_text', label: 'Box 4 text' },
  ] },
  { title: 'Footer and contact', fields: [
    { key: 'footer_about', label: 'About line in the footer' },
    { key: 'contact_email', label: 'Contact email' },
    { key: 'contact_phone', label: 'Contact phone' },
  ] },
];

// Plain-language labels for the codes in the activity record.
export const ACTIVITY_LABELS = {
  PRODUCT_CREATED: 'Product added', PRODUCT_UPDATED: 'Product edited', PRODUCT_DELETED: 'Product deleted',
  ORDER_STATUS_CHANGED: 'Order status changed',
  USER_CREATED: 'Account created', USER_ROLE_CHANGED: 'Role changed',
  USER_ACTIVATED: 'Account activated', USER_DEACTIVATED: 'Account deactivated',
  SETTINGS_UPDATED: 'Site settings changed',
  PASSWORD_RESET_REQUESTED: 'Password reset requested', PASSWORD_RESET_LINK_CREATED: 'Reset link created by admin',
  PASSWORD_RESET_COMPLETED: 'Password changed', LOGIN_LOCKED: 'Sign-in locked',
};
