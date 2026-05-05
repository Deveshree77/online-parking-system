/**
 * SmartPark Admin — Dashboard Logic
 */
document.addEventListener('DOMContentLoaded', () => {
    if (!AdminApp.requireAdmin()) return;
    AdminApp.initSidebar();

    // Date display
    document.getElementById('currentDate').textContent = new Date().toLocaleDateString('en-IN', {
        weekday: 'long', day: 'numeric', month: 'long', year: 'numeric'
    });

    loadDashboard();
    loadRevenueChart();
});

async function loadDashboard() {
    try {
        const data = await AdminApp.apiFetch('/admin/dashboard');

        // Stat cards
        document.getElementById('statRevenue').textContent = AdminApp.formatCurrency(data.totalRevenue);
        document.getElementById('statBookings').textContent = data.totalBookings;
        document.getElementById('statActive').textContent = data.activeBookings;
        document.getElementById('statOccupancy').textContent = data.occupancyRate + '%';
        document.getElementById('statUsers').textContent = data.totalUsers;
        document.getElementById('statLots').textContent = data.totalParkingLots;
        document.getElementById('statSlots').textContent = data.totalSlots;
        document.getElementById('statTodayRevenue').textContent = AdminApp.formatCurrency(data.todayRevenue);

        // Booking status chart
        renderStatusChart(data);

        // Recent bookings table
        renderRecentBookings(data.recentBookings);

        // Top lots
        renderTopLots(data.topLots);

    } catch (error) {
        AdminApp.showToast('Failed to load dashboard: ' + error.message, 'error');
    }
}

async function loadRevenueChart() {
    try {
        const data = await AdminApp.apiFetch('/admin/revenue?days=7');
        renderRevenueChart(data);
    } catch (error) {
        console.error('Revenue chart error:', error);
    }
}

function renderRevenueChart(data) {
    const ctx = document.getElementById('revenueChart').getContext('2d');
    const labels = data.labels.map(d => {
        const date = new Date(d);
        return date.toLocaleDateString('en-IN', { day: '2-digit', month: 'short' });
    });

    new Chart(ctx, {
        type: 'line',
        data: {
            labels: labels,
            datasets: [{
                label: 'Revenue (₹)',
                data: data.data.map(Number),
                borderColor: '#8b5cf6',
                backgroundColor: 'rgba(139, 92, 246, 0.1)',
                borderWidth: 2,
                fill: true,
                tension: 0.4,
                pointBackgroundColor: '#8b5cf6',
                pointBorderColor: '#fff',
                pointBorderWidth: 2,
                pointRadius: 4
            }]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            plugins: {
                legend: { display: false },
                tooltip: {
                    backgroundColor: '#1e1b4b',
                    titleColor: '#f1f5f9',
                    bodyColor: '#94a3b8',
                    borderColor: 'rgba(139,92,246,0.3)',
                    borderWidth: 1,
                    callbacks: {
                        label: ctx => '₹' + ctx.parsed.y.toLocaleString('en-IN')
                    }
                }
            },
            scales: {
                x: { grid: { color: 'rgba(255,255,255,0.05)' }, ticks: { color: '#64748b', font: { size: 11 } } },
                y: {
                    grid: { color: 'rgba(255,255,255,0.05)' },
                    ticks: { color: '#64748b', font: { size: 11 }, callback: v => '₹' + v }
                }
            }
        }
    });
}

function renderStatusChart(data) {
    const ctx = document.getElementById('statusChart').getContext('2d');
    new Chart(ctx, {
        type: 'doughnut',
        data: {
            labels: ['Active', 'Pending', 'Completed', 'Cancelled'],
            datasets: [{
                data: [data.activeBookings, data.pendingBookings, data.completedBookings, data.cancelledBookings],
                backgroundColor: ['#10b981', '#f59e0b', '#3b82f6', '#ef4444'],
                borderColor: '#111827',
                borderWidth: 3
            }]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            plugins: {
                legend: {
                    position: 'bottom',
                    labels: { color: '#94a3b8', font: { size: 11 }, padding: 16 }
                }
            },
            cutout: '65%'
        }
    });
}

function renderRecentBookings(bookings) {
    const tbody = document.getElementById('recentBookingsTable');
    if (!bookings || bookings.length === 0) {
        tbody.innerHTML = '<tr><td colspan="5" class="text-center text-muted">No bookings yet</td></tr>';
        return;
    }
    tbody.innerHTML = bookings.map(b => `
        <tr>
            <td style="font-family:monospace; font-size:0.75rem;">${b.bookingRef.substring(0, 8)}...</td>
            <td>${b.parkingLotName}</td>
            <td>${b.slotNumber}</td>
            <td>${AdminApp.formatCurrency(b.totalAmount)}</td>
            <td>${AdminApp.getStatusBadge(b.status)}</td>
        </tr>
    `).join('');
}

function renderTopLots(lots) {
    const container = document.getElementById('topLotsContainer');
    if (!lots || lots.length === 0) {
        container.innerHTML = '<div class="empty-state"><p class="text-muted">No data yet</p></div>';
        return;
    }
    container.innerHTML = lots.map((lot, i) => `
        <div style="display:flex;align-items:center;gap:var(--space-md);padding:var(--space-md) 0;${i > 0 ? 'border-top:1px solid var(--border-color);' : ''}">
            <div style="width:28px;height:28px;border-radius:50%;background:var(--gradient-admin);display:flex;align-items:center;justify-content:center;font-size:0.75rem;font-weight:700;">${i + 1}</div>
            <div style="flex:1;min-width:0;">
                <div style="font-weight:600;font-size:0.85rem;white-space:nowrap;overflow:hidden;text-overflow:ellipsis;">${lot.lotName}</div>
                <div style="font-size:0.75rem;color:var(--text-muted);">${lot.bookingCount} bookings</div>
            </div>
            <div style="font-weight:700;color:var(--accent-emerald);font-size:0.9rem;">${AdminApp.formatCurrency(lot.revenue)}</div>
        </div>
    `).join('');
}
