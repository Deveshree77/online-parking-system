/**
 * SmartPark — Booking History
 */
document.addEventListener('DOMContentLoaded', () => {
    SmartPark.updateNavbar();

    if (!SmartPark.requireAuth()) return;

    let allBookings = [];

    loadBookingHistory();

    async function loadBookingHistory() {
        try {
            allBookings = await SmartPark.apiFetch('/booking/history');
            renderBookings(allBookings);
            updateStats(allBookings);
        } catch (error) {
            document.getElementById('historyBody').innerHTML = `
                <tr><td colspan="8" class="text-center text-muted" style="padding:var(--space-3xl);">
                    <div class="empty-state">
                        <div class="empty-icon">⚠️</div>
                        <h3>Error loading bookings</h3>
                        <p>${error.message}</p>
                    </div>
                </td></tr>
            `;
        }
    }

    function renderBookings(bookings) {
        const body = document.getElementById('historyBody');

        if (bookings.length === 0) {
            body.innerHTML = `
                <tr><td colspan="8" class="text-center text-muted" style="padding:var(--space-3xl);">
                    <div class="empty-state">
                        <div class="empty-icon">📋</div>
                        <h3>No bookings yet</h3>
                        <p>Your parking bookings will appear here</p>
                        <a href="dashboard.html" class="btn btn-primary mt-lg">Find Parking</a>
                    </div>
                </td></tr>
            `;
            return;
        }

        body.innerHTML = bookings.map(b => `
            <tr>
                <td>
                    <span style="font-family: monospace; font-size: 0.8rem; color: var(--accent-blue);">
                        ${b.bookingRef ? b.bookingRef.substring(0, 8) + '...' : '—'}
                    </span>
                </td>
                <td>
                    <strong>${b.parkingLotName || '—'}</strong>
                    <br><small class="text-muted">${b.parkingLotAddress || ''}</small>
                </td>
                <td>${b.slotNumber || '—'}</td>
                <td>${SmartPark.formatDateTimeShort(b.startTime)}</td>
                <td>${SmartPark.formatDateTimeShort(b.endTime)}</td>
                <td>
                    <strong>${SmartPark.formatCurrency(b.totalAmount)}</strong>
                    ${b.extraCharge > 0 ? `<br><small class="text-danger">+${SmartPark.formatCurrency(b.extraCharge)} extra</small>` : ''}
                </td>
                <td>${SmartPark.getStatusBadge(b.status)}</td>
                <td>
                    ${b.status === 'ACTIVE' ? `
                        <button class="btn btn-sm btn-secondary" onclick="viewQr(${b.id})">QR</button>
                        <button class="btn btn-sm btn-danger" onclick="exitParking(${b.id})" style="margin-left:4px;">Exit</button>
                    ` : b.status === 'PENDING' ? `
                        <button class="btn btn-sm btn-danger" onclick="cancelBooking(${b.id})">Cancel</button>
                    ` : b.qrToken ? `
                        <button class="btn btn-sm btn-secondary" onclick="viewQr(${b.id})">QR</button>
                    ` : '—'}
                </td>
            </tr>
        `).join('');
    }

    function updateStats(bookings) {
        document.getElementById('statTotal').textContent = bookings.length;
        document.getElementById('statActive').textContent = bookings.filter(b => b.status === 'ACTIVE').length;
        document.getElementById('statPending').textContent = bookings.filter(b => b.status === 'PENDING').length;
        document.getElementById('statCompleted').textContent = bookings.filter(b => b.status === 'COMPLETED').length;
    }

    // ---- Filter Tabs ----
    document.getElementById('filterTabs').addEventListener('click', (e) => {
        if (!e.target.classList.contains('filter-tab')) return;

        // Update active tab
        document.querySelectorAll('.filter-tab').forEach(t => t.classList.remove('active'));
        e.target.classList.add('active');

        const status = e.target.dataset.status;
        if (status === 'all') {
            renderBookings(allBookings);
        } else {
            renderBookings(allBookings.filter(b => b.status === status));
        }
    });

    // ---- Actions ----
    window.viewQr = function(bookingId) {
        window.location.href = `qr-token.html?bookingId=${bookingId}`;
    };

    window.exitParking = async function(bookingId) {
        if (!confirm('Are you sure you want to record your exit? Late fees may apply.')) return;

        try {
            const result = await SmartPark.apiFetch(`/booking/${bookingId}/exit`, {
                method: 'PATCH'
            });

            SmartPark.showToast('Exit recorded successfully!', 'success');

            if (result.extraCharge > 0) {
                SmartPark.showToast(`Extra charge: ${SmartPark.formatCurrency(result.extraCharge)}`, 'warning');
            }

            loadBookingHistory(); // Refresh
        } catch (error) {
            SmartPark.showToast(error.message, 'error');
        }
    };

    window.cancelBooking = async function(bookingId) {
        if (!confirm('Are you sure you want to cancel this booking?')) return;

        try {
            await SmartPark.apiFetch(`/booking/${bookingId}/cancel`, {
                method: 'PATCH'
            });

            SmartPark.showToast('Booking cancelled', 'success');
            loadBookingHistory(); // Refresh
        } catch (error) {
            SmartPark.showToast(error.message, 'error');
        }
    };
});
