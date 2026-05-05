package com.smartpark.service;

import com.smartpark.dto.ParkingLotResponse;
import com.smartpark.dto.SlotResponse;
import com.smartpark.model.ParkingLot;
import com.smartpark.model.Slot;
import com.smartpark.repository.ParkingLotRepository;
import com.smartpark.repository.SlotRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class ParkingService {

    private final ParkingLotRepository parkingLotRepository;
    private final SlotRepository slotRepository;

    public ParkingService(ParkingLotRepository parkingLotRepository, SlotRepository slotRepository) {
        this.parkingLotRepository = parkingLotRepository;
        this.slotRepository = slotRepository;
    }

    public List<ParkingLotResponse> getParkingLotsByCity(String city) {
        List<ParkingLot> lots = parkingLotRepository.findByCityIgnoreCase(city);

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime oneHourLater = now.plusHours(1);

        return lots.stream().map(lot -> {
            int available = slotRepository.countAvailableSlots(lot.getId(), now, oneHourLater);
            return ParkingLotResponse.builder()
                    .id(lot.getId())
                    .name(lot.getName())
                    .address(lot.getAddress())
                    .city(lot.getCity())
                    .latitude(lot.getLatitude())
                    .longitude(lot.getLongitude())
                    .totalSlots(lot.getTotalSlots())
                    .availableSlots(available)
                    .ratePerHour(lot.getRatePerHour())
                    .bufferMinutes(lot.getBufferMinutes())
                    .build();
        }).collect(Collectors.toList());
    }

    public List<ParkingLotResponse> getAllParkingLots() {
        List<ParkingLot> lots = parkingLotRepository.findAll();
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime oneHourLater = now.plusHours(1);

        return lots.stream().map(lot -> {
            int available = slotRepository.countAvailableSlots(lot.getId(), now, oneHourLater);
            return ParkingLotResponse.builder()
                    .id(lot.getId())
                    .name(lot.getName())
                    .address(lot.getAddress())
                    .city(lot.getCity())
                    .latitude(lot.getLatitude())
                    .longitude(lot.getLongitude())
                    .totalSlots(lot.getTotalSlots())
                    .availableSlots(available)
                    .ratePerHour(lot.getRatePerHour())
                    .bufferMinutes(lot.getBufferMinutes())
                    .build();
        }).collect(Collectors.toList());
    }

    public List<SlotResponse> getSlots(Long lotId, LocalDateTime startTime, LocalDateTime endTime) {
        List<Slot> allSlots = slotRepository.findByParkingLotIdAndIsActiveTrue(lotId);
        List<Slot> availableSlots = slotRepository.findAvailableSlots(lotId, startTime, endTime);

        Set<Long> availableIds = availableSlots.stream()
                .map(Slot::getId)
                .collect(Collectors.toSet());

        return allSlots.stream().map(slot -> SlotResponse.builder()
                .id(slot.getId())
                .slotNumber(slot.getSlotNumber())
                .floorLevel(slot.getFloorLevel())
                .slotType(slot.getSlotType().name())
                .available(availableIds.contains(slot.getId()))
                .locked(!availableIds.contains(slot.getId()))
                .parkingLotId(slot.getParkingLot().getId())
                .build()
        ).collect(Collectors.toList());
    }

    public ParkingLot getParkingLot(Long id) {
        return parkingLotRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Parking lot not found: " + id));
    }
}
