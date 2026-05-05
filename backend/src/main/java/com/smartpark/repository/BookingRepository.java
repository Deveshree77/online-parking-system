package com.smartpark.repository;

import com.smartpark.model.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {

    List<Booking> findByUserIdOrderByCreatedAtDesc(Long userId);

    Optional<Booking> findByBookingRef(String bookingRef);

    List<Booking> findByUserIdAndStatusOrderByCreatedAtDesc(Long userId, Booking.BookingStatus status);

    /**
     * Check if a slot has any overlapping active bookings in the given time range.
     */
    @Query("SELECT COUNT(b) > 0 FROM Booking b WHERE b.slot.id = :slotId " +
           "AND (b.status = com.smartpark.model.Booking$BookingStatus.PENDING " +
           "  OR b.status = com.smartpark.model.Booking$BookingStatus.ACTIVE) " +
           "AND b.startTime < :endTime AND b.endTime > :startTime")
    boolean existsOverlappingBooking(
            @Param("slotId") Long slotId,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime);

    /**
     * Find pending bookings older than the given threshold (for auto-cancellation).
     */
    @Query("SELECT b FROM Booking b WHERE b.status = com.smartpark.model.Booking$BookingStatus.PENDING AND b.createdAt < :threshold")
    List<Booking> findStalePendingBookings(@Param("threshold") LocalDateTime threshold);
}
