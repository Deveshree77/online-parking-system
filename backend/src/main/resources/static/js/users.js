/**
 * SmartPark Admin — Users Management
 */
document.addEventListener('DOMContentLoaded', () => {
    if (!AdminApp.requireAdmin()) return;
    AdminApp.initSidebar();

    let allUsers = [];

    loadUsers();

    document.getElementById('searchInput').addEventListener('input', () => {
        const query = document.getElementById('searchInput').value.toLowerCase();
        if (!query) { renderUsers(allUsers); return; }
        const filtered = allUsers.filter(u =>
            u.fullName.toLowerCase().includes(query) ||
            u.email.toLowerCase().includes(query)
        );
        renderUsers(filtered);
    });

    async function loadUsers() {
        const tbody = document.getElementById('usersTable');
        tbody.innerHTML = '<tr><td colspan="8" class="text-center text-muted">Loading...</td></tr>';

        try {
            allUsers = await AdminApp.apiFetch('/admin/users');
            renderUsers(allUsers);
        } catch (error) {
            tbody.innerHTML = `<tr><td colspan="8" class="text-center text-muted">${error.message}</td></tr>`;
            AdminApp.showToast('Failed to load users: ' + error.message, 'error');
        }
    }

    function renderUsers(users) {
        const tbody = document.getElementById('usersTable');
        if (users.length === 0) {
            tbody.innerHTML = '<tr><td colspan="8" class="text-center text-muted">No users found</td></tr>';
            return;
        }

        tbody.innerHTML = users.map(u => `
            <tr>
                <td style="color:var(--text-muted);">#${u.id}</td>
                <td>
                    <div style="display:flex;align-items:center;gap:var(--space-sm);">
                        <div style="width:32px;height:32px;border-radius:50%;background:${u.role === 'ADMIN' ? 'var(--gradient-admin)' : 'var(--bg-glass)'};display:flex;align-items:center;justify-content:center;font-size:0.75rem;font-weight:700;border:1px solid var(--border-color);">
                            ${u.fullName.charAt(0).toUpperCase()}
                        </div>
                        <div>
                            <div style="font-weight:600;font-size:0.85rem;">${u.fullName}</div>
                        </div>
                    </div>
                </td>
                <td style="font-size:0.8rem;color:var(--text-secondary);">${u.email}</td>
                <td style="font-size:0.8rem;">${u.phone || '—'}</td>
                <td>${AdminApp.getStatusBadge(u.role)}</td>
                <td style="text-align:center;font-weight:600;">${u.totalBookings}</td>
                <td style="font-weight:600;color:var(--accent-emerald);">${AdminApp.formatCurrency(u.totalSpent)}</td>
                <td style="font-size:0.8rem;color:var(--text-muted);">${AdminApp.formatDate(u.createdAt)}</td>
            </tr>
        `).join('');
    }
});
