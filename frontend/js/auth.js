/**
 * SmartPark — Authentication (Login & Signup)
 */
document.addEventListener('DOMContentLoaded', () => {
    // Redirect if already logged in
    if (SmartPark.isLoggedIn()) {
        window.location.href = 'dashboard.html';
        return;
    }

    // ---- Login Form ----
    const loginForm = document.getElementById('loginForm');
    if (loginForm) {
        loginForm.addEventListener('submit', async (e) => {
            e.preventDefault();
            const btn = document.getElementById('loginBtn');
            SmartPark.setButtonLoading(btn, true);

            try {
                const data = await SmartPark.apiFetch('/auth/login', {
                    method: 'POST',
                    body: JSON.stringify({
                        email: document.getElementById('email').value.trim(),
                        password: document.getElementById('password').value
                    })
                });

                SmartPark.saveAuth(data);
                SmartPark.showToast('Login successful! Redirecting...', 'success');
                setTimeout(() => window.location.href = 'dashboard.html', 800);
            } catch (error) {
                SmartPark.showToast(error.message, 'error');
                SmartPark.setButtonLoading(btn, false);
            }
        });
    }

    // ---- Signup Form ----
    const signupForm = document.getElementById('signupForm');
    if (signupForm) {
        signupForm.addEventListener('submit', async (e) => {
            e.preventDefault();
            const btn = document.getElementById('signupBtn');

            const password = document.getElementById('password').value;
            const confirmPassword = document.getElementById('confirmPassword').value;

            if (password !== confirmPassword) {
                SmartPark.showToast('Passwords do not match', 'error');
                return;
            }

            SmartPark.setButtonLoading(btn, true);

            try {
                const data = await SmartPark.apiFetch('/auth/signup', {
                    method: 'POST',
                    body: JSON.stringify({
                        fullName: document.getElementById('fullName').value.trim(),
                        email: document.getElementById('email').value.trim(),
                        password: password,
                        phone: document.getElementById('phone').value.trim()
                    })
                });

                SmartPark.saveAuth(data);
                SmartPark.showToast('Account created! Redirecting...', 'success');
                setTimeout(() => window.location.href = 'dashboard.html', 800);
            } catch (error) {
                SmartPark.showToast(error.message, 'error');
                SmartPark.setButtonLoading(btn, false);
            }
        });
    }
});
