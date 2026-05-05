package com.smartpark.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "slots", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"parking_lot_id", "slot_number"})
})
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Slot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parking_lot_id", nullable = false)
    private ParkingLot parkingLot;

    @Column(name = "slot_number", nullable = false, length = 10)
    private String slotNumber;

    @Column(name = "floor_level", length = 10)
    private String floorLevel;

    @Enumerated(EnumType.STRING)
    @Column(name = "slot_type", length = 20)
    private SlotType slotType;

    @Column(name = "is_active")
    private Boolean isActive;

    public enum SlotType {
        TWO_WHEELER, FOUR_WHEELER, COMPACT, REGULAR, LARGE, HANDICAP
    }

    @PrePersist
    protected void onCreate() {
        if (floorLevel == null) floorLevel = "G";
        if (slotType == null) slotType = SlotType.REGULAR;
        if (isActive == null) isActive = true;
    }
}
