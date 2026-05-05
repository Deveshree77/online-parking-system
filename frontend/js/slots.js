/**
 * SmartPark — Slot Grid & Selection
 */
document.addEventListener('DOMContentLoaded', () => {
    SmartPark.updateNavbar();

    const lotId = SmartPark.getParam('lotId');
    if (!lotId) {
        SmartPark.showToast('No parking area selected', 'error');
        setTimeout(() => window.location.href = 'dashboard.html', 1000);
        return;
    }

    let selectedSlot = null;
    let lotData = null;

    // Set default times (start: now + 30 min, end: now + 1.5 hours)
    const now = new Date();
    const startDefault = new Date(now.getTime() + 30 * 60000);
    const endDefault = new Date(now.getTime() + 90 * 60000);

    document.getElementById('startTime').value = SmartPark.getLocalDateTimeString(startDefault);
    document.getElementById('endTime').value = SmartPark.getLocalDateTimeString(endDefault);

    // Load lot info from the parking lots list
    loadLotInfo();

    async function loadLotInfo() {
        try {
            const lots = await SmartPark.apiFetch(`/parking/lots`);
            lotData = lots.find(l => l.id == lotId);

            if (lotData) {
                document.getElementById('lotName').textContent = lotData.name;
                document.getElementById('lotAddress').textContent = `📍 ${lotData.address}`;
                
                // Show rates for both vehicle types
                document.getElementById('statRate4W').textContent = `₹${lotData.rateFourWheeler || lotData.ratePerHour}`;
                document.getElementById('statRate2W').textContent = `₹${lotData.rateTwoWheeler || (lotData.ratePerHour / 2)}`;
                
                document.getElementById('statTotal').textContent = lotData.totalSlots;
                document.getElementById('statAvailable').textContent = lotData.availableSlots;
                document.getElementById('statBuffer').textContent = lotData.bufferMinutes;
                document.title = `${lotData.name} — SmartPark`;
            }
        } catch (error) {
            SmartPark.showToast(error.message, 'error');
        }
    }

    // ---- Check Availability ----
    const checkBtn = document.getElementById('checkAvailability');
    checkBtn.addEventListener('click', () => {
        loadSlots();
    });

    async function loadSlots() {
        const startTime = document.getElementById('startTime').value;
        const endTime = document.getElementById('endTime').value;

        if (!startTime || !endTime) {
            SmartPark.showToast('Please select start and end time', 'error');
            return;
        }

        if (new Date(endTime) <= new Date(startTime)) {
            SmartPark.showToast('End time must be after start time', 'error');
            return;
        }

        const slotGrid = document.getElementById('slotGrid');
        slotGrid.innerHTML = '<div class="loading-spinner"></div>';
        selectedSlot = null;
        document.getElementById('bookingAction').style.display = 'none';

        // Show loading state on button
        SmartPark.setButtonLoading(checkBtn, true);

        try {
            const start = new Date(startTime).toISOString().replace('Z', '');
            const end = new Date(endTime).toISOString().replace('Z', '');

            const slots = await SmartPark.apiFetch(
                `/parking/lots/${lotId}/slots?start=${start}&end=${end}`
            );

            // Filter slots based on selected vehicle type
            const selectedVehicleType = document.querySelector('input[name="vehicleType"]:checked').value;
            const filteredSlots = slots.filter(s => s.slotType === selectedVehicleType);

            renderSlotGrid(filteredSlots);

            // Update available count for filtered slots
            const available = filteredSlots.filter(s => s.available).length;
            document.getElementById('statAvailable').textContent = available;

            // Auto-scroll to the slot grid so user can see the results
            document.getElementById('slotGrid').scrollIntoView({ behavior: 'smooth', block: 'start' });

            SmartPark.showToast(`${available} slot(s) available`, 'success');
        } catch (error) {
            slotGrid.innerHTML = `
                <div class="empty-state">
                    <div class="empty-icon">⚠️</div>
                    <h3>Error loading slots</h3>
                    <p>${error.message}</p>
                </div>
            `;
        } finally {
            // Restore button text
            SmartPark.setButtonLoading(checkBtn, false);
        }
    }

    function renderSlotGrid(slots) {
        const slotGrid = document.getElementById('slotGrid');
        document.getElementById('slotLegend').style.display = 'flex';

        if (slots.length === 0) {
            slotGrid.innerHTML = `
                <div class="empty-state">
                    <div class="empty-icon">🅿️</div>
                    <h3>No slots found</h3>
                    <p>This parking area has no configured slots</p>
                </div>
            `;
            return;
        }

        // Group by floor
        const floors = {};
        slots.forEach(slot => {
            const floor = slot.floorLevel || 'G';
            if (!floors[floor]) floors[floor] = [];
            floors[floor].push(slot);
        });

        let html = '';
        Object.keys(floors).sort().forEach(floor => {
            html += `<div style="grid-column:1/-1;margin-top:var(--space-md);"><span class="text-muted" style="font-size:0.8rem;text-transform:uppercase;letter-spacing:1px;">Floor ${floor}</span></div>`;
            floors[floor].forEach(slot => {
                const stateClass = slot.available ? 'available' : (slot.locked ? 'locked' : 'booked');
                const typeIcons = {
                    'TWO_WHEELER': '🏍️',
                    'FOUR_WHEELER': '🚗',
                    'COMPACT': '🚗',
                    'REGULAR': '🚙',
                    'LARGE': '🚐',
                    'HANDICAP': '♿'
                };

                html += `
                    <div class="slot-item ${stateClass}" 
                         data-slot-id="${slot.id}" 
                         data-slot-number="${slot.slotNumber}"
                         data-slot-type="${slot.slotType}"
                         data-slot-floor="${slot.floorLevel}"
                         data-available="${slot.available}"
                         onclick="selectSlot(this)">
                        <span style="font-size:1.2rem;">${typeIcons[slot.slotType] || '🚙'}</span>
                        <span class="slot-number">${slot.slotNumber}</span>
                        <span class="slot-type">${slot.slotType}</span>
                    </div>
                `;
            });
        });

        slotGrid.innerHTML = html;
    }

    // ---- Slot Selection ----
    window.selectSlot = function(el) {
        if (el.dataset.available !== 'true') {
            SmartPark.showToast('This slot is not available', 'error');
            return;
        }

        // Remove previous selection
        document.querySelectorAll('.slot-item.selected').forEach(s => s.classList.remove('selected'));

        // Select this slot
        el.classList.add('selected');
        selectedSlot = {
            id: parseInt(el.dataset.slotId),
            number: el.dataset.slotNumber,
            type: el.dataset.slotType,
            floor: el.dataset.slotFloor
        };

        updateBookingSummary();
    };

    function updateBookingSummary() {
        if (!selectedSlot || !lotData) return;

        const startTime = document.getElementById('startTime').value;
        const endTime = document.getElementById('endTime').value;
        const hours = SmartPark.getHoursDiff(startTime, endTime);
        
        // Use vehicle specific rate
        const selectedVehicleType = document.querySelector('input[name="vehicleType"]:checked').value;
        const rate = selectedVehicleType === 'TWO_WHEELER' 
            ? (lotData.rateTwoWheeler || lotData.ratePerHour / 2)
            : (lotData.rateFourWheeler || lotData.ratePerHour);
            
        const amount = hours * parseFloat(rate);

        const summary = document.getElementById('bookingSummary');
        summary.innerHTML = `
            <div class="summary-item">
                <div class="summary-label">Parking Area</div>
                <div class="summary-value">${lotData.name}</div>
            </div>
            <div class="summary-item">
                <div class="summary-label">Slot</div>
                <div class="summary-value">${selectedSlot.number} (${selectedSlot.type})</div>
            </div>
            <div class="summary-item">
                <div class="summary-label">Start Time</div>
                <div class="summary-value">${SmartPark.formatDateTime(startTime)}</div>
            </div>
            <div class="summary-item">
                <div class="summary-label">End Time</div>
                <div class="summary-value">${SmartPark.formatDateTime(endTime)}</div>
            </div>
            <div class="summary-item">
                <div class="summary-label">Duration</div>
                <div class="summary-value">${hours} hour(s)</div>
            </div>
            <div class="summary-item">
                <div class="summary-label">Estimated Cost</div>
                <div class="summary-value text-success">${SmartPark.formatCurrency(amount)}</div>
            </div>
        `;

        document.getElementById('bookingAction').style.display = 'block';
        document.getElementById('bookingAction').scrollIntoView({ behavior: 'smooth', block: 'center' });
    }

    // ---- Lock & Proceed ----
    document.getElementById('lockAndProceed').addEventListener('click', async () => {
        if (!SmartPark.requireAuth()) return;
        if (!selectedSlot) {
            SmartPark.showToast('Please select a slot first', 'error');
            return;
        }

        const btn = document.getElementById('lockAndProceed');
        SmartPark.setButtonLoading(btn, true);

        const startTime = document.getElementById('startTime').value;
        const endTime = document.getElementById('endTime').value;

        try {
            const lockData = await SmartPark.apiFetch('/booking/lock', {
                method: 'POST',
                body: JSON.stringify({
                    slotId: selectedSlot.id,
                    startTime: new Date(startTime).toISOString().replace('Z', ''),
                    endTime: new Date(endTime).toISOString().replace('Z', '')
                })
            });

            SmartPark.showToast('Slot locked! Redirecting to payment...', 'success');

            // Store booking data for payment page
            const bookingData = {
                lockId: lockData.lockId,
                slotId: selectedSlot.id,
                slotNumber: selectedSlot.number,
                slotType: selectedSlot.type,
                lotName: lotData.name,
                lotAddress: lotData.address,
                startTime: startTime,
                endTime: endTime,
                ratePerHour: selectedVehicleType === 'TWO_WHEELER' 
                    ? (lotData.rateTwoWheeler || lotData.ratePerHour / 2)
                    : (lotData.rateFourWheeler || lotData.ratePerHour),
                bufferMinutes: lotData.bufferMinutes,
                lockExpiry: lockData.lockExpiry,
                hours: SmartPark.getHoursDiff(startTime, endTime),
                amount: amount
            };

            sessionStorage.setItem('sp_booking', JSON.stringify(bookingData));
            setTimeout(() => window.location.href = 'booking.html', 800);
        } catch (error) {
            SmartPark.showToast(error.message, 'error');
            SmartPark.setButtonLoading(btn, false);
        }
    });
});
