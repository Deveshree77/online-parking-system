/**
 * SmartPark — Shared Utilities & App Configuration
 */
const SmartPark = (() => {
    const API_BASE = 'http://localhost:8080/api';

    // ---- Auth Helpers ----
    function getToken() {
        return localStorage.getItem('sp_token');
    }

    function getUser() {
        const user = localStorage.getItem('sp_user');
        return user ? JSON.parse(user) : null;
    }

    function saveAuth(data) {
        localStorage.setItem('sp_token', data.token);
        localStorage.setItem('sp_user', JSON.stringify({
            id: data.userId,
            fullName: data.fullName,
            email: data.email,
            role: data.role || 'USER'
        }));
    }

    function logout() {
        localStorage.removeItem('sp_token');
        localStorage.removeItem('sp_user');
        window.location.href = 'login.html';
    }

    function isLoggedIn() {
        return !!getToken();
    }

    function requireAuth() {
        if (!isLoggedIn()) {
            showToast('Please log in to continue', 'error');
            setTimeout(() => window.location.href = 'login.html', 1000);
            return false;
        }
        return true;
    }

    // ---- API Fetch Wrapper ----
    async function apiFetch(endpoint, options = {}) {
        const url = `${API_BASE}${endpoint}`;
        const headers = {
            'Content-Type': 'application/json',
            ...options.headers
        };

        const token = getToken();
        if (token) {
            headers['Authorization'] = `Bearer ${token}`;
        }

        try {
            const response = await fetch(url, {
                ...options,
                headers
            });

            if (response.status === 401) {
                logout();
                throw new Error('Session expired. Please login again.');
            }

            const data = await response.json();

            if (!response.ok) {
                throw new Error(data.message || data.error || 'Something went wrong');
            }

            return data;
        } catch (error) {
            if (error.message.includes('Failed to fetch')) {
                throw new Error('Cannot connect to server. Please ensure the backend is running.');
            }
            throw error;
        }
    }

    // ---- Toast Notifications ----
    function showToast(message, type = 'info', duration = 4000) {
        const container = document.getElementById('toastContainer');
        if (!container) return;

        const icons = { success: '✓', error: '✕', info: 'ℹ', warning: '⚠' };
        const toast = document.createElement('div');
        toast.className = `toast toast-${type}`;
        toast.innerHTML = `<span>${icons[type] || ''}</span> ${message}`;
        container.appendChild(toast);

        setTimeout(() => {
            toast.style.opacity = '0';
            toast.style.transform = 'translateX(100%)';
            toast.style.transition = 'all 0.3s ease';
            setTimeout(() => toast.remove(), 300);
        }, duration);
    }

    // ---- Navbar Update ----
    function updateNavbar() {
        const navAuth = document.getElementById('navAuth');
        if (!navAuth) return;

        if (isLoggedIn()) {
            const user = getUser();
            const adminLink = user && user.role === 'ADMIN' 
                ? '<a href="admin/index.html" class="btn btn-sm" style="background:linear-gradient(135deg,#8b5cf6,#6366f1);color:white;">⚡ Admin</a>' 
                : '';
            navAuth.innerHTML = `
                ${adminLink}
                <span class="user-name">👤 ${user ? user.fullName : 'User'}</span>
                <button class="btn btn-secondary btn-sm" onclick="SmartPark.logout()">Logout</button>
            `;
        } else {
            navAuth.innerHTML = `
                <a href="login.html" class="btn btn-secondary btn-sm">Log In</a>
                <a href="signup.html" class="btn btn-primary btn-sm">Sign Up</a>
            `;
        }
    }

    // ---- Date/Time Helpers ----
    function formatDateTime(isoString) {
        if (!isoString) return '—';
        const date = new Date(isoString);
        return date.toLocaleString('en-IN', {
            day: '2-digit', month: 'short', year: 'numeric',
            hour: '2-digit', minute: '2-digit', hour12: true
        });
    }

    function formatDateTimeShort(isoString) {
        if (!isoString) return '—';
        const date = new Date(isoString);
        return date.toLocaleString('en-IN', {
            day: '2-digit', month: 'short',
            hour: '2-digit', minute: '2-digit', hour12: true
        });
    }

    function formatCurrency(amount) {
        if (amount == null) return '—';
        return `₹${parseFloat(amount).toFixed(2)}`;
    }

    function getLocalDateTimeString(date) {
        const offset = date.getTimezoneOffset();
        const local = new Date(date.getTime() - offset * 60000);
        return local.toISOString().slice(0, 16);
    }

    function toISOString(localDateTimeStr) {
        return new Date(localDateTimeStr).toISOString().replace('Z', '');
    }

    function getHoursDiff(start, end) {
        const ms = new Date(end) - new Date(start);
        return Math.max(1, Math.ceil(ms / (1000 * 60 * 60)));
    }

    // ---- Status Badge ----
    function getStatusBadge(status) {
        const map = {
            'ACTIVE': 'badge-active',
            'PENDING': 'badge-pending',
            'COMPLETED': 'badge-completed',
            'CANCELLED': 'badge-cancelled'
        };
        return `<span class="badge ${map[status] || 'badge-pending'}">${status}</span>`;
    }

    // ---- Loading Button ----
    function setButtonLoading(btn, loading) {
        if (loading) {
            btn.dataset.originalText = btn.innerHTML;
            btn.innerHTML = '<div class="spinner"></div> Processing...';
            btn.disabled = true;
        } else {
            btn.innerHTML = btn.dataset.originalText || btn.innerHTML;
            btn.disabled = false;
        }
    }

    // ---- URL Params ----
    function getParam(name) {
        return new URLSearchParams(window.location.search).get(name);
    }

    return {
        API_BASE,
        getToken,
        getUser,
        saveAuth,
        logout,
        isLoggedIn,
        requireAuth,
        apiFetch,
        showToast,
        updateNavbar,
        formatDateTime,
        formatDateTimeShort,
        formatCurrency,
        getLocalDateTimeString,
        toISOString,
        getHoursDiff,
        getStatusBadge,
        setButtonLoading,
        getParam
    };
})();
