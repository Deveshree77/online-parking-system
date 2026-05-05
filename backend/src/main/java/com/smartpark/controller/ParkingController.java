package com.smartpark.controller;

import com.smartpark.dto.ParkingLotResponse;
import com.smartpark.dto.SlotResponse;
import com.smartpark.service.ParkingService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/parking")
public class ParkingController {

    private final ParkingService parkingService;

    public ParkingController(ParkingService parkingService) {
        this.parkingService = parkingService;
    }

    @GetMapping("/lots")
    public ResponseEntity<List<ParkingLotResponse>> getParkingLots(
            @RequestParam(required = false) String city) {
        List<ParkingLotResponse> lots;
        if (city != null && !city.isBlank()) {
            lots = parkingService.getParkingLotsByCity(city);
        } else {
            lots = parkingService.getAllParkingLots();
        }
        return ResponseEntity.ok(lots);
    }

    @GetMapping("/lots/{id}/slots")
    public ResponseEntity<List<SlotResponse>> getSlots(
            @PathVariable Long id,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {
        List<SlotResponse> slots = parkingService.getSlots(id, start, end);
        return ResponseEntity.ok(slots);
    }
}
