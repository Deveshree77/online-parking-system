/**
 * SmartPark Admin — Parking Lot Management
 */
document.addEventListener('DOMContentLoaded', () => {
    if (!AdminApp.requireAdmin()) return;
    AdminApp.initSidebar();

    loadParkingLots();

    document.getElementById('lotForm').addEventListener('submit', handleSubmit);
});

async function loadParkingLots() {
    const grid = document.getElementById('lotsGrid');
    grid.innerHTML = '<div class="loading-spinner"></div>';

    try {
        const lots = await AdminApp.apiFetch('/parking/lots');
        renderLots(lots);
    } catch (error) {
        grid.innerHTML = `<div class="empty-state"><div class="empty-icon">⚠️</div><h3>Failed to load</h3><p>${error.message}</p></div>`;
    }
}

function renderLots(lots) {
    const grid = document.getElementById('lotsGrid');
    if (lots.length === 0) {
        grid.innerHTML = `<div class="empty-state"><div class="empty-icon">🅿️</div><h3>No parking lots yet</h3><p>Click "Add Parking Lot" to create your first one.</p></div>`;
        return;
    }

    grid.innerHTML = lots.map(lot => `
        <div class="card lot-manage-card">
            <div class="lot-name">${lot.name}</div>
            <div class="lot-meta">📍 ${lot.address} · ${lot.city}</div>
            <div class="lot-stats">
                <div class="lot-stat-item">
                    <div class="lot-stat-value" style="color:var(--accent-emerald);">${lot.availableSlots}</div>
                    <div class="lot-stat-label">Available</div>
                </div>
                <div class="lot-stat-item">
                    <div class="lot-stat-value">${lot.totalSlots}</div>
                    <div class="lot-stat-label">Total Slots</div>
                </div>
                <div class="lot-stat-item">
                    <div class="lot-stat-value" style="color:var(--accent-primary);">₹${lot.ratePerHour}</div>
                    <div class="lot-stat-label">Per Hour</div>
                </div>
            </div>
            <div class="lot-actions">
                <button class="btn btn-sm btn-secondary" onclick="editLot(${lot.id}, '${escapeStr(lot.name)}', '${escapeStr(lot.address)}', '${lot.city}', ${lot.totalSlots}, 0, 0, ${lot.ratePerHour})">
                    ✏️ Edit
                </button>
                <button class="btn btn-sm btn-danger" onclick="deleteLot(${lot.id}, '${escapeStr(lot.name)}')">
                    🗑️ Delete
                </button>
            </div>
        </div>
    `).join('');
}

function escapeStr(str) {
    return str.replace(/'/g, "\\'").replace(/"/g, '\\"');
}

function editLot(id, name, address, city, slots, lat, lng, rate) {
    document.getElementById('modalTitle').textContent = 'Edit Parking Lot';
    document.getElementById('lotSubmitBtn').textContent = 'Update Parking Lot';
    document.getElementById('lotEditId').value = id;
    document.getElementById('lotName').value = name;
    document.getElementById('lotAddress').value = address;
    document.getElementById('lotCity').value = city;
    document.getElementById('lotSlots').value = slots;
    document.getElementById('lotLat').value = lat || '';
    document.getElementById('lotLng').value = lng || '';
    document.getElementById('lotRate').value = rate || '';
    AdminApp.openModal('lotModal');
}

async function handleSubmit(e) {
    e.preventDefault();
    const btn = document.getElementById('lotSubmitBtn');
    AdminApp.setButtonLoading(btn, true);

    const editId = document.getElementById('lotEditId').value;
    const payload = {
        name: document.getElementById('lotName').value,
        address: document.getElementById('lotAddress').value,
        city: document.getElementById('lotCity').value,
        totalSlots: parseInt(document.getElementById('lotSlots').value),
        latitude: parseFloat(document.getElementById('lotLat').value) || 19.0760,
        longitude: parseFloat(document.getElementById('lotLng').value) || 72.8777,
        ratePerHour: parseFloat(document.getElementById('lotRate').value),
        extraRatePerMinute: parseFloat(document.getElementById('lotExtraRate').value) || 2.00,
        bufferMinutes: parseInt(document.getElementById('lotBuffer').value) || 15
    };

    try {
        if (editId) {
            await AdminApp.apiFetch(`/admin/lots/${editId}`, { method: 'PUT', body: JSON.stringify(payload) });
            AdminApp.showToast('Parking lot updated!', 'success');
        } else {
            await AdminApp.apiFetch('/admin/lots', { method: 'POST', body: JSON.stringify(payload) });
            AdminApp.showToast('Parking lot created with auto-generated slots!', 'success');
        }
        AdminApp.closeModal('lotModal');
        resetForm();
        loadParkingLots();
    } catch (error) {
        AdminApp.showToast('Failed: ' + error.message, 'error');
    } finally {
        AdminApp.setButtonLoading(btn, false);
    }
}

window.deleteLot = async function(id, name) {
    if (!confirm(`Delete "${name}"? This will deactivate all slots. This cannot be undone.`)) return;
    try {
        await AdminApp.apiFetch(`/admin/lots/${id}`, { method: 'DELETE' });
        AdminApp.showToast('Parking lot deleted.', 'success');
        loadParkingLots();
    } catch (error) {
        AdminApp.showToast('Failed: ' + error.message, 'error');
    }
};

window.editLot = editLot;

function resetForm() {
    document.getElementById('lotForm').reset();
    document.getElementById('lotEditId').value = '';
    document.getElementById('modalTitle').textContent = 'Add Parking Lot';
    document.getElementById('lotSubmitBtn').textContent = 'Create Parking Lot';
}
