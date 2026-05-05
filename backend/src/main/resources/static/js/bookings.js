/**
 * SmartPark Admin — Bookings Management
 */
document.addEventListener('DOMContentLoaded', () => {
    if (!AdminApp.requireAdmin()) return;
    AdminApp.initSidebar();

    let allBookings = [];

    loadBookings();

    document.getElementById('statusFilter').addEventListener('change', () => loadBookings());
    document.getElementById('searchInput').addEventListener('input', () => filterBookings());

    async function loadBookings() {
        const status = document.getElementById('statusFilter').value;
        const tbody = document.getElementById('bookingsTable');
        tbody.innerHTML = '<tr><td colspan="8" class="text-center text-muted">Loading...</td></tr>';

        try {
            const endpoint = status ? `/admin/bookings?status=${status}` : '/admin/bookings';
            allBookings = await AdminApp.apiFetch(endpoint);
            renderBookings(allBookings);
        } catch (error) {
            tbody.innerHTML = `<tr><td colspan="8" class="text-center text-muted">${error.message}</td></tr>`;
            AdminApp.showToast('Failed to load bookings: ' + error.message, 'error');
        }
    }

    function filterBookings() {
        const query = document.getElementById('searchInput').value.toLowerCase();
        if (!query) { renderBookings(allBookings); return; }
        const filtered = allBookings.filter(b =>
            b.bookingRef.toLowerCase().includes(query) ||
            b.parkingLotName.toLowerCase().includes(query) ||
            b.slotNumber.toLowerCase().includes(query)
        );
        renderBookings(filtered);
    }

    function renderBookings(bookings) {
        const tbody = document.getElementById('bookingsTable');
        if (bookings.length === 0) {
            tbody.innerHTML = '<tr><td colspan="8" class="text-center text-muted">No bookings found</td></tr>';
            return;
        }

        tbody.innerHTML = bookings.map(b => `
            <tr>
                <td style="font-family:monospace;font-size:0.75rem;">${b.bookingRef.substring(0, 8)}...</td>
                <td>${b.parkingLotName}</td>
                <td>${b.slotNumber}</td>
                <td>${AdminApp.formatDateTime(b.startTime)}</td>
                <td>${AdminApp.formatDateTime(b.endTime)}</td>
                <td style="font-weight:600;color:var(--accent-emerald);">${AdminApp.formatCurrency(b.totalAmount)}</td>
                <td>${AdminApp.getStatusBadge(b.status)}</td>
                <td>
                    ${b.status === 'ACTIVE' ? `<button class="btn btn-sm btn-danger" onclick="recordExit(${b.id})">Record Exit</button>` : ''}
                    ${b.status === 'PENDING' ? `<span class="text-muted" style="font-size:0.75rem;">Awaiting payment</span>` : ''}
                    ${b.status === 'COMPLETED' ? `<span class="text-muted" style="font-size:0.75rem;">Done</span>` : ''}
                    ${b.status === 'CANCELLED' ? `<span class="text-muted" style="font-size:0.75rem;">—</span>` : ''}
                </td>
            </tr>
        `).join('');
    }

    // Make recordExit globally accessible
    window.recordExit = async function(bookingId) {
        if (!confirm('Record exit for this booking? Any late fees will be calculated.')) return;
        try {
            await AdminApp.apiFetch(`/admin/bookings/${bookingId}/exit`, { method: 'PATCH' });
            AdminApp.showToast('Exit recorded successfully!', 'success');
            loadBookings();
        } catch (error) {
            AdminApp.showToast('Failed: ' + error.message, 'error');
        }
    };
});
