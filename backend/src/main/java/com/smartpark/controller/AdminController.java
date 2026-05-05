package com.smartpark.controller;

import com.smartpark.dto.*;
import com.smartpark.service.PlatformAdminService;
import com.smartpark.service.BookingService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final PlatformAdminService adminService;
    private final BookingService bookingService;

    public AdminController(PlatformAdminService adminService, BookingService bookingService) {
        this.adminService = adminService;
        this.bookingService = bookingService;
    }

    /** Dashboard aggregate stats */
    @GetMapping("/dashboard")
    public ResponseEntity<AdminDashboardResponse> getDashboard() {
        return ResponseEntity.ok(adminService.getDashboardStats());
    }

    /** All bookings with optional status filter */
    @GetMapping("/bookings")
    public ResponseEntity<List<BookingResponse>> getAllBookings(
            @RequestParam(required = false) String status) {
        return ResponseEntity.ok(adminService.getAllBookings(status));
    }

    /** Admin-triggered exit for a booking */
    @PatchMapping("/bookings/{id}/exit")
    public ResponseEntity<BookingResponse> recordExit(@PathVariable Long id) {
        BookingResponse response = bookingService.recordExit(id, null);
        return ResponseEntity.ok(response);
    }

    /** Revenue analytics (last N days) */
    @GetMapping("/revenue")
    public ResponseEntity<Map<String, Object>> getRevenue(
            @RequestParam(defaultValue = "7") int days) {
        return ResponseEntity.ok(adminService.getRevenueAnalytics(days));
    }

    /** All registered users */
    @GetMapping("/users")
    public ResponseEntity<List<Map<String, Object>>> getAllUsers() {
        return ResponseEntity.ok(adminService.getAllUsers());
    }

    /** Create parking lot */
    @PostMapping("/lots")
    public ResponseEntity<Map<String, Object>> createLot(@Valid @RequestBody ParkingLotRequest request) {
        Map<String, Object> result = adminService.createParkingLot(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }

    /** Update parking lot */
    @PutMapping("/lots/{id}")
    public ResponseEntity<Map<String, Object>> updateLot(
            @PathVariable Long id,
            @Valid @RequestBody ParkingLotRequest request) {
        Map<String, Object> result = adminService.updateParkingLot(id, request);
        return ResponseEntity.ok(result);
    }

    /** Delete parking lot */
    @DeleteMapping("/lots/{id}")
    public ResponseEntity<Map<String, String>> deleteLot(@PathVariable Long id) {
        adminService.deleteParkingLot(id);
        return ResponseEntity.ok(Map.of("message", "Parking lot deleted successfully"));
    }
}
