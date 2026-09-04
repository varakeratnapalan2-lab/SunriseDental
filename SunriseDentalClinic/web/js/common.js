/**
 * SunriseDental — Shared frontend utilities
 */
const APP_NAME = 'SunriseDental';
function getContextPath() {
  const path = window.location.pathname;
  if (path.toLowerCase().indexOf('/sunrisedentalclinic') !== -1) {
    return '/SunriseDentalClinic';
  }
  return '';
}
const API_BASE = getContextPath();
const NAV_MENUS = {
  ADMIN: [
    { key: 'dashboard', href: API_BASE + '/admin/dashboard.html', icon: '📊', label: 'Dashboard' },
    { key: 'appointments', href: API_BASE + '/admin/all-appointments.html', icon: '📅', label: 'All Appointments' },
    { key: 'reports', href: API_BASE + '/admin/reports.html', icon: '📈', label: 'Reports' },
    { key: 'staff', href: API_BASE + '/admin/staff-users.html', icon: '👥', label: 'Staff Users' },
    { key: 'settings', href: API_BASE + '/admin/settings.html', icon: '⚙️', label: 'Settings' },
    { key: 'help', href: API_BASE + '/help.html', icon: '❓', label: 'Help' }
  ],
  RECEPTIONIST: [
    { key: 'dashboard', href: API_BASE + '/receptionist/dashboard.html', icon: '📊', label: 'Dashboard' },
    { key: 'register', href: API_BASE + '/receptionist/register-appointment.html', icon: '➕', label: 'Register Appointment' },
    { key: 'search', href: API_BASE + '/receptionist/search-appointment.html', icon: '🔍', label: 'Search Appointment' },
    { key: 'billing', href: API_BASE + '/receptionist/billing.html', icon: '🧾', label: 'Calculate & Print Bill' },
    { key: 'today', href: API_BASE + '/receptionist/today-appointments.html', icon: '📋', label: "Today's Appointments" },
    { key: 'reminder', href: API_BASE + '/receptionist/send-reminder.html', icon: '📧', label: 'Send Reminder' },
    { key: 'help', href: API_BASE + '/help.html', icon: '❓', label: 'Help' }
  ],
  DENTIST: [
    { key: 'dashboard', href: API_BASE + '/dentist/dashboard.html', icon: '📊', label: 'Dashboard' },
    { key: 'my-appts', href: API_BASE + '/dentist/my-appointments.html', icon: '🦷', label: 'My Appointments Today' },
    { key: 'search', href: API_BASE + '/dentist/search-appointment.html', icon: '🔍', label: 'Search Appointment' },
    { key: 'status', href: API_BASE + '/dentist/update-status.html', icon: '✏️', label: 'Update Status' },
    { key: 'help', href: API_BASE + '/help.html', icon: '❓', label: 'Help' }
  ]
};

const ROLE_DASHBOARD = {
  ADMIN: 'admin/dashboard.html',
  RECEPTIONIST: 'receptionist/dashboard.html',
  DENTIST: 'dentist/dashboard.html'
};

function getSession() {
  try {
    const raw = sessionStorage.getItem('sunriseSession');
    return raw ? JSON.parse(raw) : null;
  } catch {
    return null;
  }
}

function setSession(data) {
  sessionStorage.setItem('sunriseSession', JSON.stringify(data));
}

function clearSession() {
  sessionStorage.removeItem('sunriseSession');
}

function requireAuth(allowedRoles) {
  const session = getSession();
  if (!session || !session.role) {
    redirectToLogin();
    return null;
  }
  if (allowedRoles && !allowedRoles.includes(session.role)) {
    window.location.href = '../' + (ROLE_DASHBOARD[session.role] || 'index.html');
    return null;
  }
  return session;
}

function redirectToLogin() {
  const path = window.location.pathname;
  if (path.includes('/admin/') || path.includes('/receptionist/') || path.includes('/dentist/')) {
    window.location.href = '../index.html';
  } else if (!path.endsWith('index.html') && !path.endsWith('/')) {
    window.location.href = 'index.html';
  }
}

function roleLabel(role) {
  const map = { ADMIN: 'Administrator', RECEPTIONIST: 'Receptionist', DENTIST: 'Dentist' };
  return map[role] || role;
}

function initAppShell(requiredRole, activeKey) {
  const roles = requiredRole ? [requiredRole] : null;
  const session = requireAuth(roles);
  if (!session) return;

  const sidebar = document.getElementById('sidebar');
  if (!sidebar) return;

  const menu = NAV_MENUS[session.role] || [];
  const navHtml = menu
    .map(
      (item) =>
        `<a href="${item.href}" class="${item.key === activeKey ? 'active' : ''}">
          <span class="nav-icon">${item.icon}</span>${item.label}
        </a>`
    )
    .join('');

  sidebar.innerHTML = `
    <div class="sidebar-brand">
      <div class="logo-sm">🦷</div>
      <h2>Sunrise Dental</h2>
      <span>CLINIC MANAGEMENT</span>
    </div>
    <div class="sidebar-user">
      <strong>${escapeHtml(session.fullName || session.username)}</strong>
      <em>${roleLabel(session.role)}</em>
    </div>
    <nav class="sidebar-nav">${navHtml}</nav>
    <div class="sidebar-footer">
      <button type="button" class="btn btn-logout" id="btnLogout">🚪 Logout</button>
    </div>
  `;

  document.getElementById('btnLogout')?.addEventListener('click', logout);

  if (!document.getElementById('mobileMenuBtn')) {
    const btn = document.createElement('button');
    btn.id = 'mobileMenuBtn';
    btn.className = 'mobile-menu-btn';
    btn.setAttribute('aria-label', 'Menu');
    btn.textContent = '☰';
    btn.onclick = () => sidebar.classList.toggle('open');
    document.body.appendChild(btn);
  }

  ensureToastContainer();
  ensureLoadingOverlay();
}

async function apiCall(method, url, body) {
  const opts = {
    method,
    headers: { 'Content-Type': 'application/json' },
    credentials: 'include'
  };
  if (body && method !== 'GET') {
    opts.body = JSON.stringify(body);
  }
  const res = await fetch(API_BASE + url, opts);
  let data = null;
  const text = await res.text();
  try {
    data = text ? JSON.parse(text) : null;
  } catch {
    // If response is HTML (e.g. 404/500 Tomcat page), do not expose it
    const isHtml = text.trim().startsWith('<');
    data = { message: isHtml ? 'Server unavailable or endpoint not found.' : text };
  }
  if (!res.ok) {
    const err = new Error(data?.message || 'Request failed');
    err.status = res.status;
    err.data = data;
    throw err;
  }
  return data;
}

async function logout() {
  showLoading(true);
  try {
    await apiCall('POST', '/api/auth/logout', {});
  } catch {
    /* offline demo */
  }
  clearSession();
  showLoading(false);
  showToast('Logged out successfully', 'success');
  setTimeout(() => {
    const path = window.location.pathname;
    window.location.href = path.includes('/admin/') || path.includes('/receptionist/') || path.includes('/dentist/')
      ? '../index.html'
      : 'index.html';
  }, 400);
}

function showToast(message, type) {
  ensureToastContainer();
  const el = document.createElement('div');
  el.className = `toast toast-${type || 'info'}`;
  el.textContent = message;
  document.getElementById('toastContainer').appendChild(el);
  setTimeout(() => el.remove(), 4000);
}

function ensureToastContainer() {
  if (!document.getElementById('toastContainer')) {
    const c = document.createElement('div');
    c.id = 'toastContainer';
    c.className = 'toast-container';
    document.body.appendChild(c);
  }
}

function showLoading(visible) {
  ensureLoadingOverlay();
  document.getElementById('loadingOverlay').classList.toggle('visible', !!visible);
}

function ensureLoadingOverlay() {
  if (!document.getElementById('loadingOverlay')) {
    const d = document.createElement('div');
    d.id = 'loadingOverlay';
    d.className = 'loading-overlay';
    d.innerHTML = '<div class="spinner"></div>';
    document.body.appendChild(d);
  }
}

function escapeHtml(str) {
  if (str == null) return '';
  return String(str)
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
    .replace(/"/g, '&quot;');
}

function formatDate(iso) {
  if (!iso) return '—';
  const d = new Date(iso);
  return d.toLocaleDateString('en-LK', { year: 'numeric', month: 'short', day: 'numeric' });
}

function formatTime(t) {
  if (!t) return '—';
  return t.length >= 5 ? t.substring(0, 5) : t;
}

function validateContactLK(value) {
  return /^07\d{8}$/.test(String(value).replace(/\s/g, ''));
}

function validateNotPastDate(dateStr) {
  const d = new Date(dateStr);
  const today = new Date();
  today.setHours(0, 0, 0, 0);
  d.setHours(0, 0, 0, 0);
  return d >= today;
}

function validateClinicTime(timeStr) {
  if (!timeStr) return false;
  const [h, m] = timeStr.split(':').map(Number);
  const mins = h * 60 + (m || 0);
  return mins >= 8 * 60 && mins <= 18 * 60;
}

function statusBadge(status) {
  const s = (status || 'Scheduled').replace(/\s/g, '');
  const cls =
    s === 'InProgress' || s === 'In Progress'
      ? 'badge-progress'
      : s === 'Completed'
        ? 'badge-completed'
        : s === 'Cancelled'
          ? 'badge-cancelled'
          : 'badge-scheduled';
  const label = status || 'Scheduled';
  return `<span class="badge ${cls}">${escapeHtml(label)}</span>`;
}

/** Demo mock when backend not connected */
const MOCK_MODE_KEY = 'sunriseMockMode';

function isMockMode() {
  return sessionStorage.getItem(MOCK_MODE_KEY) === '1';
}

function enableMockMode() {
  sessionStorage.setItem(MOCK_MODE_KEY, '1');
}

function getMockStore() {
  let store = JSON.parse(localStorage.getItem('sunriseMockData') || 'null');
  if (!store) {
    store = {
      appointments: [
        {
          appointmentNumber: 'APT0001',
          patientName: 'Kamal Perera',
          address: '45 Galle Road, Colombo 03',
          contactNumber: '0771234567',
          dentistName: 'Dr. Nimal Silva',
          treatmentType: 'Dental Cleaning',
          appointmentDate: new Date().toISOString().slice(0, 10),
          appointmentTime: '10:00',
          status: 'Scheduled',
          treatmentPrice: 4500,
          consultationFee: 1500
        },
        {
          appointmentNumber: 'APT0002',
          patientName: 'Dilini Jayasooriya',
          address: '12 Kandy Road, Kiribathgoda',
          contactNumber: '0719876543',
          dentistName: 'Dr. Anjali Fernando',
          treatmentType: 'Tooth Filling',
          appointmentDate: new Date().toISOString().slice(0, 10),
          appointmentTime: '11:30',
          status: 'Scheduled',
          treatmentPrice: 6000,
          consultationFee: 1500
        },
        {
          appointmentNumber: 'APT0003',
          patientName: 'Saman Kumara',
          address: '88 Main Street, Nugegoda',
          contactNumber: '0754567890',
          dentistName: 'Dr. Ruwan Perera',
          treatmentType: 'Root Canal',
          appointmentDate: new Date().toISOString().slice(0, 10),
          appointmentTime: '14:00',
          status: 'Scheduled',
          treatmentPrice: 25000,
          consultationFee: 1500
        }
      ],
      dentists: [
        { id: 1, name: 'Dr. Nimal Silva', specialization: 'General Dentistry' },
        { id: 2, name: 'Dr. Anjali Fernando', specialization: 'Orthodontics' },
        { id: 3, name: 'Dr. Ruwan Perera', specialization: 'Oral Surgery' }
      ],
      treatments: [
        { id: 1, name: 'Dental Cleaning', price: 4500 },
        { id: 2, name: 'Tooth Filling', price: 6000 },
        { id: 3, name: 'Root Canal', price: 25000 },
        { id: 4, name: 'Tooth Extraction', price: 8000 },
        { id: 5, name: 'Dental Crown', price: 35000 }
      ],
      consultationFee: 1500,
      staff: [
        { id: 1, username: 'admin', fullName: 'Clinic Administrator', role: 'ADMIN', status: 'active' },
        { id: 2, username: 'reception', fullName: 'Sarah Jayawardena', role: 'RECEPTIONIST', status: 'active' },
        { id: 3, username: 'dentist', fullName: 'Dr. Nimal Silva', role: 'DENTIST', status: 'active' },
        { id: 4, username: 'dr.anjali', fullName: 'Dr. Anjali Fernando', role: 'DENTIST', status: 'active' },
        { id: 5, username: 'dr.ruwan', fullName: 'Dr. Ruwan Perera', role: 'DENTIST', status: 'active' }
      ]
    };
    localStorage.setItem('sunriseMockData', JSON.stringify(store));
  }
  return store;
}

function saveMockStore(store) {
  localStorage.setItem('sunriseMockData', JSON.stringify(store));
}
