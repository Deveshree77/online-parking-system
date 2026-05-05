package com.smartpark.service;

import com.smartpark.dto.SlotLockRequest;
import com.smartpark.exception.SlotAlreadyBookedException;
import com.smartpark.model.Slot;
import com.smartpark.model.SlotLock;
import com.smartpark.model.User;
import com.smartpark.repository.BookingRepository;
import com.smartpark.repository.SlotLockRepository;
import com.smartpark.repository.SlotRepository;
import com.smartpark.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
public class SlotLockService {

    private final SlotLockRepository slotLockRepository;
    private final SlotRepository slotRepository;
    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final LoggingService loggingService;

    @Value("${app.slot.lock.duration-minutes:5}")
    private int lockDurationMinutes;

    public SlotLockService(SlotLockRepository slotLockRepository, SlotRepository slotRepository,
                           BookingRepository bookingRepository, UserRepository userRepository,
                           LoggingService loggingService) {
        this.slotLockRepository = slotLockRepository;
        this.slotRepository = slotRepository;
        this.bookingRepository = bookingRepository;
        this.userRepository = userRepository;
        this.loggingService = loggingService;
    }

    @Transactional
    public SlotLock lockSlot(Long userId, SlotLockRequest request) {
        // 1. Acquire pessimistic lock on the slot row
        Slot slot = slotRepository.findByIdWithLock(request.getSlotId())
                .orElseThrow(() -> new IllegalArgumentException("Slot not found: " + request.getSlotId()));

        // 2. Check if slot is already booked for this time range
        boolean hasOverlap = bookingRepository.existsOverlappingBooking(
                request.getSlotId(), request.getStartTime(), request.getEndTime());
        if (hasOverlap) {
            throw new SlotAlreadyBookedException("Slot is already booked for the selected time range");
        }

        // 3. Check if slot is already locked by another user
        List<SlotLock> existingLocks = slotLockRepository.findActiveLocksForSlot(
                request.getSlotId(), request.getStartTime(), request.getEndTime());
        if (!existingLocks.isEmpty()) {
            throw new SlotAlreadyBookedException("Slot is temporarily locked by another user. Please try again.");
        }

        // 4. Create the lock
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        LocalDateTime now = LocalDateTime.now();
        SlotLock lock = SlotLock.builder()
                .slot(slot)
                .user(user)
                .lockStart(now)
                .lockExpiry(now.plusMinutes(lockDurationMinutes))
                .startTime(request.getStartTime())
                .endTime(request.getEndTime())
                .isActive(true)
                .build();

        lock = slotLockRepository.save(lock);

        loggingService.info("SlotLockService", "Slot locked", userId,
                Map.of("slotId", request.getSlotId(), "lockId", lock.getId(),
                       "expiry", lock.getLockExpiry().toString()));

        return lock;
    }

    public SlotLock validateLock(Long lockId, Long userId) {
        SlotLock lock = slotLockRepository.findByIdAndUserIdAndIsActiveTrue(lockId, userId)
                .orElseThrow(() -> new IllegalArgumentException("Lock not found or does not belong to you"));

        if (lock.isExpired()) {
            lock.setIsActive(false);
            slotLockRepository.save(lock);
            throw new com.smartpark.exception.SlotLockExpiredException(
                    "Your slot lock has expired. Please select the slot again.");
        }

        return lock;
    }

    @Transactional
    public void releaseLock(Long lockId) {
        slotLockRepository.findById(lockId).ifPresent(lock -> {
            lock.setIsActive(false);
            slotLockRepository.save(lock);
        });
    }
}
