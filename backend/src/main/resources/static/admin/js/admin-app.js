/**
 * SmartPark Admin — Shared Utilities
 */
const AdminApp = (() => {
    const API_BASE = 'http://localhost:8080/api';

    function getToken() { return localStorage.getItem('sp_token'); }
    function getUser() {
        const u = localStorage.getItem('sp_user');
        return u ? JSON.parse(u) : null;
    }

    function saveAuth(data) {
        localStorage.setItem('sp_token', data.token);
        localStorage.setItem('sp_user', JSON.stringify({
            id: data.userId, fullName: data.fullName, email: data.email, role: data.role
        }));
    }

    function logout() {
        localStorage.removeItem('sp_token');
        localStorage.removeItem('sp_user');
        window.location.href = 'login.html';
    }

    function isLoggedIn() { return !!getToken(); }

    function isAdmin() {
        const user = getUser();
        return user && user.role === 'ADMIN';
    }

    function requireAdmin() {
        if (!isLoggedIn() || !isAdmin()) {
            showToast('Admin access required', 'error');
            setTimeout(() => window.location.href = 'login.html', 1000);
            return false;
        }
        return true;
    }

    async function apiFetch(endpoint, options = {}) {
        const url = `${API_BASE}${endpoint}`;
        const headers = { 'Content-Type': 'application/json', ...options.headers };
        const token = getToken();
        if (token) headers['Authorization'] = `Bearer ${token}`;

        try {
            const response = await fetch(url, { ...options, headers });
            if (response.status === 401) { logout(); throw new Error('Session expired'); }
            if (response.status === 403) { throw new Error('Access denied. Admin privileges required.'); }
            const data = await response.json();
            if (!response.ok) throw new Error(data.message || data.error || 'Something went wrong');
            return data;
        } catch (error) {
            if (error.message.includes('Failed to fetch')) {
                throw new Error('Cannot connect to server. Is the backend running?');
            }
            throw error;
        }
    }

    function showToast(message, type = 'info', duration = 4000) {
        const container = document.getElementById('toastContainer');
        if (!container) return;
        const toast = document.createElement('div');
        toast.className = `toast toast-${type}`;
        toast.textContent = message;
        container.appendChild(toast);
        setTimeout(() => {
            toast.style.opacity = '0';
            toast.style.transform = 'translateX(100%)';
            toast.style.transition = 'all 0.3s ease';
            setTimeout(() => toast.remove(), 300);
        }, duration);
    }

    function formatCurrency(amount) {
        if (amount == null) return '—';
        return `₹${parseFloat(amount).toLocaleString('en-IN', { minimumFractionDigits: 2, maximumFractionDigits: 2 })}`;
    }

    function formatDateTime(iso) {
        if (!iso) return '—';
        return new Date(iso).toLocaleString('en-IN', {
            day: '2-digit', month: 'short', year: 'numeric',
            hour: '2-digit', minute: '2-digit', hour12: true
        });
    }

    function formatDate(iso) {
        if (!iso) return '—';
        return new Date(iso).toLocaleDateString('en-IN', { day: '2-digit', month: 'short', year: 'numeric' });
    }

    function getStatusBadge(status) {
        const map = {
            'ACTIVE': 'badge-active', 'PENDING': 'badge-pending',
            'COMPLETED': 'badge-completed', 'CANCELLED': 'badge-cancelled',
            'ADMIN': 'badge-admin', 'USER': 'badge-user'
        };
        return `<span class="badge ${map[status] || 'badge-pending'}">${status}</span>`;
    }

    function initSidebar() {
        const user = getUser();
        const nameEl = document.getElementById('sidebarUserName');
        const avatarEl = document.getElementById('sidebarAvatar');
        if (nameEl && user) nameEl.textContent = user.fullName;
        if (avatarEl && user) avatarEl.textContent = user.fullName.charAt(0).toUpperCase();

        // Highlight active link
        const currentPage = window.location.pathname.split('/').pop() || 'index.html';
        document.querySelectorAll('.sidebar-link').forEach(link => {
            const href = link.getAttribute('href');
            if (href === currentPage || (currentPage === '' && href === 'index.html')) {
                link.classList.add('active');
            }
        });
    }

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

    function openModal(id) { document.getElementById(id).classList.add('active'); }
    function closeModal(id) { document.getElementById(id).classList.remove('active'); }

    return {
        API_BASE, getToken, getUser, saveAuth, logout, isLoggedIn, isAdmin,
        requireAdmin, apiFetch, showToast, formatCurrency, formatDateTime, formatDate,
        getStatusBadge, initSidebar, setButtonLoading, openModal, closeModal
    };
})();
