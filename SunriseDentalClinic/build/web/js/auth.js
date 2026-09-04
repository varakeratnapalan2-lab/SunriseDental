/**
 * SunriseDental — Authentication + Role selector
 */
document.addEventListener('DOMContentLoaded', () => {
  const form = document.getElementById('loginForm');
  if (!form) return;

  const session = getSession();
  if (session?.role) {
    window.location.href = ROLE_DASHBOARD[session.role];
    return;
  }

  initPasswordToggle();
  initRoleSelector(form);

  form.addEventListener('submit', async (e) => {
    e.preventDefault();
    clearFieldErrors(form);

    const username = form.username.value.trim();
    const password = form.password.value;

    let valid = true;
    if (!username) {
      showFieldError(form.username, 'Username is required');
      valid = false;
    }
    if (!password || password.length < 4) {
      showFieldError(form.password, 'Password is required (min 4 characters)');
      valid = false;
    }
    if (!valid) return;

    showLoading(true);
    try {
      const data = await apiCall('POST', '/api/auth/login', { username, password });
      setSession({
        userId: data.userId,
        username: data.username || username,
        role: data.role,
        fullName: data.fullName || username,
        dentistName: data.dentistName || null
      });
      sessionStorage.removeItem(MOCK_MODE_KEY);
      showToast('Login Successfully', 'success');
      setTimeout(() => {
        window.location.href = ROLE_DASHBOARD[data.role];
      }, 800);
    } catch (err) {
      const demo = tryDemoLogin(username, password);
      if (demo) {
        enableMockMode();
        setSession(demo);
        showToast('Welcome, ' + demo.fullName, 'success');
        setTimeout(() => {
          window.location.href = ROLE_DASHBOARD[demo.role];
        }, 400);
      } else {
        showToast(err.message || 'Invalid username or password', 'error');
      }
    } finally {
      showLoading(false);
    }
  });
});

/** Tracks show/hide state — avoids inverted toggle bugs */
let passwordIsVisible = false;

/** Password hidden by default; plain text only when user clicks eye */
function setPasswordVisible(visible) {
  passwordIsVisible = !!visible;
  const input = document.getElementById('password');
  const btn = document.getElementById('togglePassword');
  const wrap = input?.closest('.password-input-wrap');
  const eyeOpen = btn?.querySelector('.eye-open');
  const eyeClosed = btn?.querySelector('.eye-closed');
  if (!input || !btn) return;

  input.setAttribute('type', passwordIsVisible ? 'text' : 'password');

  if (passwordIsVisible) {
    eyeOpen?.classList.remove('hidden');
    eyeClosed?.classList.add('hidden');
  } else {
    eyeOpen?.classList.add('hidden');
    eyeClosed?.classList.remove('hidden');
  }

  wrap?.classList.toggle('is-visible', passwordIsVisible);
  btn.setAttribute('aria-label', passwordIsVisible ? 'Hide password' : 'Show password');
  btn.setAttribute('aria-pressed', passwordIsVisible ? 'true' : 'false');
  btn.title = passwordIsVisible ? 'Hide password' : 'Show password';
}

function hidePassword() {
  setPasswordVisible(false);
}

function initPasswordToggle() {
  const btn = document.getElementById('togglePassword');
  if (!btn) return;

  hidePassword();

  btn.addEventListener('click', (e) => {
    e.preventDefault();
    e.stopPropagation();
    setPasswordVisible(!passwordIsVisible);
  });
}

function initRoleSelector(form) {
  const cards = document.querySelectorAll('.role-card');
  if (!cards.length) return;

  // Role highlight only — username/password must be typed by staff (from DB)
  cards.forEach((card) => {
    card.addEventListener('click', () => {
      cards.forEach((c) => c.classList.remove('active'));
      card.classList.add('active');
      form.username.value = '';
      form.password.value = '';
      hidePassword();
      form.username.focus();
    });
  });
}

function tryDemoLogin(username, password) {
  const users = [
    { username: 'admin', password: 'admin123', role: 'ADMIN', fullName: 'Clinic Administrator', userId: 1 },
    { username: 'reception', password: 'reception123', role: 'RECEPTIONIST', fullName: 'Sarah Jayawardena', userId: 2 },
    { username: 'dentist', password: 'dentist123', role: 'DENTIST', fullName: 'Dr. Nimal Silva', userId: 3, dentistName: 'Dr. Nimal Silva' }
  ];
  const u = users.find((x) => x.username === username && x.password === password);
  if (!u) return null;
  const { password: _, ...session } = u;
  return session;
}

function showFieldError(input, message) {
  input.classList.add('error');
  const group = input.closest('.form-group');
  let err = group?.querySelector('.form-error');
  if (err) {
    err.textContent = message;
    err.classList.add('visible');
  }
}

function clearFieldErrors(form) {
  form.querySelectorAll('.error').forEach((el) => el.classList.remove('error'));
  form.querySelectorAll('.form-error').forEach((el) => {
    el.classList.remove('visible');
    el.textContent = '';
  });
}
