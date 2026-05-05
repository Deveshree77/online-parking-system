package com.smartpark.repository;

import com.smartpark.model.SlotLock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface SlotLockRepository extends JpaRepository<SlotLock, Long> {

    @Query("SELECT sl FROM SlotLock sl WHERE sl.slot.id = :slotId " +
           "AND sl.isActive = true AND sl.lockExpiry > CURRENT_TIMESTAMP " +
           "AND sl.startTime < :endTime AND sl.endTime > :startTime")
    List<SlotLock> findActiveLocksForSlot(
            @Param("slotId") Long slotId,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime);

    Optional<SlotLock> findByIdAndUserIdAndIsActiveTrue(Long id, Long userId);

    /**
     * Find all expired but still active locks for cleanup.
     */
    @Query("SELECT sl FROM SlotLock sl WHERE sl.isActive = true AND sl.lockExpiry < CURRENT_TIMESTAMP")
    List<SlotLock> findExpiredLocks();

    @Modifying
    @Query("UPDATE SlotLock sl SET sl.isActive = false WHERE sl.isActive = true AND sl.lockExpiry < CURRENT_TIMESTAMP")
    int deactivateExpiredLocks();
}
