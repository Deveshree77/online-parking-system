package com.smartpark.scheduler;

import com.smartpark.model.Booking;
import com.smartpark.repository.BookingRepository;
import com.smartpark.repository.SlotLockRepository;
import com.smartpark.service.LoggingService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Component
public class SlotCleanupScheduler {

    private final SlotLockRepository slotLockRepository;
    private final BookingRepository bookingRepository;
    private final LoggingService loggingService;

    public SlotCleanupScheduler(SlotLockRepository slotLockRepository,
                                 BookingRepository bookingRepository,
                                 LoggingService loggingService) {
        this.slotLockRepository = slotLockRepository;
        this.bookingRepository = bookingRepository;
        this.loggingService = loggingService;
    }

    @Scheduled(fixedRate = 60000)
    @Transactional
    public void cleanupExpiredLocksAndBookings() {
        try {
            // 1. Release expired slot locks
            int releasedLocks = slotLockRepository.deactivateExpiredLocks();
            if (releasedLocks > 0) {
                loggingService.info("SlotCleanupScheduler",
                        "Released " + releasedLocks + " expired slot locks", null,
                        Map.of("count", releasedLocks));
            }

            // 2. Cancel stale pending bookings (older than 10 minutes without payment)
            LocalDateTime threshold = LocalDateTime.now().minusMinutes(10);
            List<Booking> staleBookings = bookingRepository.findStalePendingBookings(threshold);

            for (Booking booking : staleBookings) {
                booking.setStatus(Booking.BookingStatus.CANCELLED);
                bookingRepository.save(booking);

                loggingService.info("SlotCleanupScheduler",
                        "Auto-cancelled stale pending booking",
                        booking.getUser() != null ? booking.getUser().getId() : null,
                        Map.of("bookingRef", booking.getBookingRef()));
            }
        } catch (Exception e) {
            loggingService.error("SlotCleanupScheduler", "Cleanup failed: " + e.getMessage(), null, Map.of());
        }
    }
}
