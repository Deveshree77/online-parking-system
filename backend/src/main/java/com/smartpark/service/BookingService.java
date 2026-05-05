package com.smartpark.service;

import com.smartpark.dto.BookingRequest;
import com.smartpark.dto.BookingResponse;
import com.smartpark.exception.SlotAlreadyBookedException;
import com.smartpark.model.*;
import com.smartpark.repository.BookingRepository;
import com.smartpark.repository.SlotRepository;
import com.smartpark.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class BookingService {

    private final BookingRepository bookingRepository;
    private final SlotRepository slotRepository;
    private final UserRepository userRepository;
    private final SlotLockService slotLockService;
    private final QrCodeService qrCodeService;
    private final LoggingService loggingService;

    public BookingService(BookingRepository bookingRepository, SlotRepository slotRepository,
                          UserRepository userRepository, SlotLockService slotLockService,
                          QrCodeService qrCodeService, LoggingService loggingService) {
        this.bookingRepository = bookingRepository;
        this.slotRepository = slotRepository;
        this.userRepository = userRepository;
        this.slotLockService = slotLockService;
        this.qrCodeService = qrCodeService;
        this.loggingService = loggingService;
    }

    @Transactional
    public BookingResponse createBooking(Long userId, BookingRequest request) {
        // 1. Validate the lock if provided
        if (request.getLockId() != null) {
            slotLockService.validateLock(request.getLockId(), userId);
        }

        // 2. Acquire pessimistic lock on the slot
        Slot slot = slotRepository.findByIdWithLock(request.getSlotId())
                .orElseThrow(() -> new IllegalArgumentException("Slot not found: " + request.getSlotId()));

        // 3. Double-check no overlapping booking exists
        boolean hasOverlap = bookingRepository.existsOverlappingBooking(
                request.getSlotId(), request.getStartTime(), request.getEndTime());
        if (hasOverlap) {
            throw new SlotAlreadyBookedException("Slot has been booked by another user. Please choose a different slot.");
        }

        // 4. Calculate amount
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        ParkingLot lot = slot.getParkingLot();
        long hours = Duration.between(request.getStartTime(), request.getEndTime()).toHours();
        if (hours < 1) hours = 1;  // minimum 1 hour

        // Determine rate based on vehicle/slot type
        BigDecimal hourlyRate = lot.getRatePerHour(); // Default fallback
        if (slot.getSlotType() == Slot.SlotType.TWO_WHEELER && lot.getRateTwoWheeler() != null) {
            hourlyRate = lot.getRateTwoWheeler();
        } else if (slot.getSlotType() == Slot.SlotType.FOUR_WHEELER && lot.getRateFourWheeler() != null) {
            hourlyRate = lot.getRateFourWheeler();
        }

        BigDecimal baseAmount = hourlyRate.multiply(BigDecimal.valueOf(hours));

        // 5. Generate booking reference and QR code
        String bookingRef = UUID.randomUUID().toString();
        String qrContent = String.format("SMARTPARK|%s|%s|%s|%s",
                bookingRef, slot.getSlotNumber(), request.getStartTime(), request.getEndTime());
        String qrToken = qrCodeService.generateQrCode(qrContent);

        // 6. Create booking
        Booking booking = Booking.builder()
                .bookingRef(bookingRef)
                .user(user)
                .slot(slot)
                .startTime(request.getStartTime())
                .endTime(request.getEndTime())
                .bufferMinutes(lot.getBufferMinutes())
                .status(Booking.BookingStatus.PENDING)
                .baseAmount(baseAmount)
                .extraCharge(BigDecimal.ZERO)
                .totalAmount(baseAmount)
                .qrToken(qrToken)
                .build();

        booking = bookingRepository.save(booking);

        // 7. Release the lock
        if (request.getLockId() != null) {
            slotLockService.releaseLock(request.getLockId());
        }

        loggingService.info("BookingService", "Booking created", userId,
                Map.of("bookingRef", bookingRef, "slotId", slot.getId(), "amount", baseAmount));

        return mapToResponse(booking);
    }

    @Transactional
    public BookingResponse confirmBooking(Long bookingId, Long userId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new IllegalArgumentException("Booking not found"));

        if (!booking.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("Booking does not belong to you");
        }

        booking.setStatus(Booking.BookingStatus.ACTIVE);
        booking = bookingRepository.save(booking);

        loggingService.info("BookingService", "Booking confirmed", userId,
                Map.of("bookingId", bookingId, "bookingRef", booking.getBookingRef()));

        return mapToResponse(booking);
    }

    @Transactional
    public BookingResponse recordExit(Long bookingId, Long userId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new IllegalArgumentException("Booking not found"));

        // userId null = admin override (skip ownership check)
        if (userId != null && !booking.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("Booking does not belong to you");
        }

        LocalDateTime now = LocalDateTime.now();
        booking.setActualExitTime(now);

        // Calculate extra charge if exited late
        LocalDateTime bufferEnd = booking.getEndTime().plusMinutes(booking.getBufferMinutes());
        if (now.isAfter(bufferEnd)) {
            long extraMinutes = Duration.between(bufferEnd, now).toMinutes();
            ParkingLot lot = booking.getSlot().getParkingLot();
            BigDecimal extraCharge = lot.getExtraRatePerMinute()
                    .multiply(BigDecimal.valueOf(extraMinutes))
                    .setScale(2, RoundingMode.HALF_UP);
            booking.setExtraCharge(extraCharge);
            booking.setTotalAmount(booking.getBaseAmount().add(extraCharge));

            loggingService.warn("BookingService", "Late exit - extra charge applied", userId,
                    Map.of("bookingId", bookingId, "extraMinutes", extraMinutes, "extraCharge", extraCharge));
        }

        booking.setStatus(Booking.BookingStatus.COMPLETED);
        booking = bookingRepository.save(booking);

        return mapToResponse(booking);
    }

    public List<BookingResponse> getUserBookings(Long userId) {
        return bookingRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public List<BookingResponse> getUserBookingsByStatus(Long userId, String status) {
        Booking.BookingStatus bookingStatus = Booking.BookingStatus.valueOf(status.toUpperCase());
        return bookingRepository.findByUserIdAndStatusOrderByCreatedAtDesc(userId, bookingStatus).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public BookingResponse getBooking(Long bookingId, Long userId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new IllegalArgumentException("Booking not found"));
        if (!booking.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("Booking does not belong to you");
        }
        return mapToResponse(booking);
    }

    @Transactional
    public void cancelBooking(Long bookingId, Long userId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new IllegalArgumentException("Booking not found"));
        if (!booking.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("Booking does not belong to you");
        }
        booking.setStatus(Booking.BookingStatus.CANCELLED);
        bookingRepository.save(booking);

        loggingService.info("BookingService", "Booking cancelled", userId,
                Map.of("bookingId", bookingId));
    }

    private BookingResponse mapToResponse(Booking booking) {
        Slot slot = booking.getSlot();
        ParkingLot lot = slot.getParkingLot();

        return BookingResponse.builder()
                .id(booking.getId())
                .bookingRef(booking.getBookingRef())
                .slotId(slot.getId())
                .slotNumber(slot.getSlotNumber())
                .parkingLotName(lot.getName())
                .parkingLotAddress(lot.getAddress())
                .startTime(booking.getStartTime())
                .endTime(booking.getEndTime())
                .actualExitTime(booking.getActualExitTime())
                .bufferMinutes(booking.getBufferMinutes())
                .status(booking.getStatus().name())
                .baseAmount(booking.getBaseAmount())
                .extraCharge(booking.getExtraCharge())
                .totalAmount(booking.getTotalAmount())
                .qrToken(booking.getQrToken())
                .createdAt(booking.getCreatedAt())
                .build();
    }
}
