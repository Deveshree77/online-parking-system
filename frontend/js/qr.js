/**
 * SmartPark — QR Token Display
 */
document.addEventListener('DOMContentLoaded', () => {
    SmartPark.updateNavbar();

    if (!SmartPark.requireAuth()) return;

    const bookingId = SmartPark.getParam('bookingId');

    if (bookingId) {
        loadBookingFromAPI(bookingId);
    } else {
        // Try session storage (direct from payment success)
        const lastBooking = sessionStorage.getItem('sp_last_booking');
        if (lastBooking) {
            const booking = JSON.parse(lastBooking);
            displayBooking(booking);
        } else {
            SmartPark.showToast('No booking data found', 'error');
            setTimeout(() => window.location.href = 'history.html', 1500);
        }
    }

    async function loadBookingFromAPI(id) {
        try {
            const booking = await SmartPark.apiFetch(`/booking/${id}`);
            displayBooking(booking);
        } catch (error) {
            SmartPark.showToast(error.message, 'error');
        }
    }

    function displayBooking(booking) {
        // QR Code
        const qrImage = document.getElementById('qrCodeImage');
        if (booking.qrToken) {
            qrImage.src = booking.qrToken;
            qrImage.alt = `QR Token: ${booking.bookingRef}`;
        } else {
            // Fallback: generate a simple text display
            document.getElementById('qrCodeWrapper').innerHTML = `
                <div style="width:250px;height:250px;display:flex;align-items:center;justify-content:center;background:#f3f4f6;border-radius:12px;">
                    <div style="text-align:center;color:#374151;">
                        <div style="font-size:2rem;margin-bottom:8px;">🎫</div>
                        <div style="font-family:monospace;font-size:0.8rem;word-break:break-all;">${booking.bookingRef}</div>
                    </div>
                </div>
            `;
        }

        // Booking details
        document.getElementById('qrBookingRef').textContent = booking.bookingRef || '—';
        document.getElementById('qrLotName').textContent = booking.parkingLotName || '—';
        document.getElementById('qrSlotNumber').textContent = booking.slotNumber || '—';
        document.getElementById('qrStatus').innerHTML = SmartPark.getStatusBadge(booking.status);
        document.getElementById('qrStartTime').textContent = SmartPark.formatDateTime(booking.startTime);
        document.getElementById('qrEndTime').textContent = SmartPark.formatDateTime(booking.endTime);
        document.getElementById('qrAmount').textContent = SmartPark.formatCurrency(booking.totalAmount);
        document.getElementById('qrBuffer').textContent = `${booking.bufferMinutes || 15} mins grace period`;

        document.title = `QR Token: ${booking.bookingRef?.substring(0, 8)} — SmartPark`;
    }
});
