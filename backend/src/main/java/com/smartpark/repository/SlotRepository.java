package com.smartpark.repository;

import com.smartpark.model.Slot;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface SlotRepository extends JpaRepository<Slot, Long> {

    List<Slot> findByParkingLotIdAndIsActiveTrue(Long parkingLotId);

    /**
     * Find available slots for a parking lot within a time range.
     */
    @Query("SELECT s FROM Slot s WHERE s.parkingLot.id = :lotId AND s.isActive = true " +
           "AND s.id NOT IN (" +
           "  SELECT b.slot.id FROM Booking b WHERE b.slot.parkingLot.id = :lotId " +
           "  AND (b.status = com.smartpark.model.Booking$BookingStatus.PENDING " +
           "    OR b.status = com.smartpark.model.Booking$BookingStatus.ACTIVE) " +
           "  AND b.startTime < :endTime AND b.endTime > :startTime" +
           ") " +
           "AND s.id NOT IN (" +
           "  SELECT sl.slot.id FROM SlotLock sl WHERE sl.slot.parkingLot.id = :lotId " +
           "  AND sl.isActive = true AND sl.lockExpiry > CURRENT_TIMESTAMP " +
           "  AND sl.startTime < :endTime AND sl.endTime > :startTime" +
           ")")
    List<Slot> findAvailableSlots(
            @Param("lotId") Long lotId,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime);

    /**
     * Pessimistic write lock for concurrency control during booking.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT s FROM Slot s WHERE s.id = :slotId")
    Optional<Slot> findByIdWithLock(@Param("slotId") Long slotId);

    @Query("SELECT COUNT(s) FROM Slot s WHERE s.parkingLot.id = :lotId AND s.isActive = true " +
           "AND s.id NOT IN (" +
           "  SELECT b.slot.id FROM Booking b WHERE b.slot.parkingLot.id = :lotId " +
           "  AND (b.status = com.smartpark.model.Booking$BookingStatus.PENDING " +
           "    OR b.status = com.smartpark.model.Booking$BookingStatus.ACTIVE) " +
           "  AND b.startTime < :endTime AND b.endTime > :startTime" +
           ")")
    int countAvailableSlots(
            @Param("lotId") Long lotId,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime);
}
