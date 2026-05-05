/**
 * SmartPark — Booking & Payment Flow
 */
document.addEventListener('DOMContentLoaded', () => {
    SmartPark.updateNavbar();

    if (!SmartPark.requireAuth()) return;

    // Load booking data from session
    const bookingDataStr = sessionStorage.getItem('sp_booking');
    if (!bookingDataStr) {
        SmartPark.showToast('No booking data found. Please select a slot first.', 'error');
        setTimeout(() => window.location.href = 'dashboard.html', 1500);
        return;
    }

    const bookingData = JSON.parse(bookingDataStr);
    let bookingId = null;
    let timerInterval = null;

    // ---- Populate Summary ----
    document.getElementById('summaryLot').textContent = bookingData.lotName;
    document.getElementById('summarySlot').textContent = `${bookingData.slotNumber} (${bookingData.slotType})`;
    document.getElementById('summaryStart').textContent = SmartPark.formatDateTime(bookingData.startTime);
    document.getElementById('summaryEnd').textContent = SmartPark.formatDateTime(bookingData.endTime);
    document.getElementById('summaryDuration').textContent = `${bookingData.hours} hour(s)`;
    document.getElementById('summaryBuffer').textContent = `${bookingData.bufferMinutes} mins`;
    document.getElementById('paymentAmount').textContent = SmartPark.formatCurrency(bookingData.amount);

    // ---- Countdown Timer ----
    startCountdown(bookingData.lockExpiry);

    function startCountdown(expiryStr) {
        const timerValue = document.getElementById('timerValue');
        const timerCircle = document.getElementById('timerCircle');

        timerInterval = setInterval(() => {
            const expiry = new Date(expiryStr);
            const now = new Date();
            const diff = expiry - now;

            if (diff <= 0) {
                clearInterval(timerInterval);
                timerValue.textContent = '0:00';
                timerCircle.classList.add('danger');
                SmartPark.showToast('Slot lock expired! Please go back and select again.', 'error');
                document.getElementById('payNowBtn').disabled = true;
                return;
            }

            const minutes = Math.floor(diff / 60000);
            const seconds = Math.floor((diff % 60000) / 1000);
            timerValue.textContent = `${minutes}:${seconds.toString().padStart(2, '0')}`;

            // Warning states
            if (diff < 60000) {
                timerCircle.classList.remove('warning');
                timerCircle.classList.add('danger');
            } else if (diff < 120000) {
                timerCircle.classList.add('warning');
            }
        }, 1000);
    }

    // ---- Create Booking First ----
    createBooking();

    async function createBooking() {
        try {
            const response = await SmartPark.apiFetch('/booking/confirm', {
                method: 'POST',
                body: JSON.stringify({
                    slotId: bookingData.slotId,
                    startTime: new Date(bookingData.startTime).toISOString().replace('Z', ''),
                    endTime: new Date(bookingData.endTime).toISOString().replace('Z', ''),
                    lockId: bookingData.lockId
                })
            });

            bookingId = response.id;

            // Store for QR page
            sessionStorage.setItem('sp_last_booking', JSON.stringify(response));
        } catch (error) {
            SmartPark.showToast('Failed to create booking: ' + error.message, 'error');
        }
    }

    // ---- Pay Now ----
    document.getElementById('payNowBtn').addEventListener('click', async () => {
        if (!bookingId) {
            SmartPark.showToast('Booking not created yet. Please wait...', 'error');
            return;
        }

        const btn = document.getElementById('payNowBtn');
        SmartPark.setButtonLoading(btn, true);

        try {
            // Step 1: Create payment intent
            const paymentIntent = await SmartPark.apiFetch('/payment/create-intent', {
                method: 'POST',
                body: JSON.stringify({ bookingId })
            });

            // Step 2: Confirm payment (simulated)
            await new Promise(resolve => setTimeout(resolve, 1500)); // Simulate processing

            const paymentResult = await SmartPark.apiFetch('/payment/confirm', {
                method: 'POST',
                body: JSON.stringify({
                    bookingId,
                    paymentIntentId: paymentIntent.paymentRef
                })
            });

            // Success!
            clearInterval(timerInterval);
            document.getElementById('timerSection').style.display = 'none';
            document.getElementById('paymentForm').style.display = 'none';
            document.getElementById('successSection').classList.remove('hidden');

            // Update QR link
            document.getElementById('viewQrBtn').href = `qr-token.html?bookingId=${bookingId}`;

            SmartPark.showToast('Payment successful! Booking confirmed.', 'success');

            // Clean up session
            sessionStorage.removeItem('sp_booking');

        } catch (error) {
            SmartPark.showToast('Payment failed: ' + error.message, 'error');
            SmartPark.setButtonLoading(btn, false);
        }
    });
});
