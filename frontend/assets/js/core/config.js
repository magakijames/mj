// Settings shared by every page.

// Where the Java backend runs. Change this when the API moves to a real server.
export const API_BASE = 'http://localhost:8080/api';

// The folder that holds index.html, worked out from this file's own address,
// so links and images work from any page however the site is served.
const ROOT = new URL('../../../', import.meta.url);
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
  ADMIN: 'Controls everything, including staff accounts and roles.',
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
];

export const ORDER_STATUSES = { PENDING: 'Pending', SHIPPED: 'Shipped', DELIVERED: 'Delivered', CANCELLED: 'Cancelled' };

export const COLORS = {
  Black: '#222', Green: '#1f6b4a', Navy: '#1f3a5f', Beige: '#d8c3a5', Red: '#c0392b', Grey: '#888', White: '#fff',
  Pink: '#f4a6a6', Blue: '#3b6fb6', Cream: '#efe6d2', Stone: '#cfc6c4', 'Sky blue': '#8fc1e6', Purple: '#6b3fa0',
  Brown: '#7a5230', Khaki: '#b5a36a',
};
