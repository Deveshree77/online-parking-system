package com.smartpark.controller;

import com.smartpark.dto.BookingRequest;
import com.smartpark.dto.BookingResponse;
import com.smartpark.dto.SlotLockRequest;
import com.smartpark.model.SlotLock;
import com.smartpark.security.JwtTokenProvider;
import com.smartpark.service.BookingService;
import com.smartpark.service.SlotLockService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/booking")
public class BookingController {

    private final BookingService bookingService;
    private final SlotLockService slotLockService;
    private final JwtTokenProvider tokenProvider;

    public BookingController(BookingService bookingService, SlotLockService slotLockService,
                             JwtTokenProvider tokenProvider) {
        this.bookingService = bookingService;
        this.slotLockService = slotLockService;
        this.tokenProvider = tokenProvider;
    }

    @PostMapping("/lock")
    public ResponseEntity<Map<String, Object>> lockSlot(
            @Valid @RequestBody SlotLockRequest request,
            @RequestHeader("Authorization") String authHeader) {
        Long userId = extractUserId(authHeader);
        SlotLock lock = slotLockService.lockSlot(userId, request);

        Map<String, Object> response = new HashMap<>();
        response.put("lockId", lock.getId());
        response.put("slotId", request.getSlotId());
        response.put("lockExpiry", lock.getLockExpiry().toString());
        response.put("message", "Slot locked for 5 minutes. Complete payment to confirm booking.");

        return ResponseEntity.ok(response);
    }

    @PostMapping("/confirm")
    public ResponseEntity<BookingResponse> confirmBooking(
            @Valid @RequestBody BookingRequest request,
            @RequestHeader("Authorization") String authHeader) {
        Long userId = extractUserId(authHeader);
        BookingResponse response = bookingService.createBooking(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/history")
    public ResponseEntity<List<BookingResponse>> getBookingHistory(
            @RequestHeader("Authorization") String authHeader,
            @RequestParam(required = false) String status) {
        Long userId = extractUserId(authHeader);
        List<BookingResponse> history;
        if (status != null && !status.isBlank()) {
            history = bookingService.getUserBookingsByStatus(userId, status);
        } else {
            history = bookingService.getUserBookings(userId);
        }
        return ResponseEntity.ok(history);
    }

    @GetMapping("/{id}")
    public ResponseEntity<BookingResponse> getBooking(
            @PathVariable Long id,
            @RequestHeader("Authorization") String authHeader) {
        Long userId = extractUserId(authHeader);
        BookingResponse response = bookingService.getBooking(id, userId);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/exit")
    public ResponseEntity<BookingResponse> recordExit(
            @PathVariable Long id,
            @RequestHeader("Authorization") String authHeader) {
        Long userId = extractUserId(authHeader);
        BookingResponse response = bookingService.recordExit(id, userId);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/cancel")
    public ResponseEntity<Map<String, String>> cancelBooking(
            @PathVariable Long id,
            @RequestHeader("Authorization") String authHeader) {
        Long userId = extractUserId(authHeader);
        bookingService.cancelBooking(id, userId);
        Map<String, String> response = Map.of("message", "Booking cancelled successfully");
        return ResponseEntity.ok(response);
    }

    private Long extractUserId(String authHeader) {
        String token = authHeader.replace("Bearer ", "");
        return tokenProvider.getUserIdFromToken(token);
    }
}
