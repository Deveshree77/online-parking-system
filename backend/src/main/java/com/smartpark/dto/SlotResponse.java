package com.smartpark.dto;

import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class SlotResponse {
    private Long id;
    private String slotNumber;
    private String floorLevel;
    private String slotType;
    private boolean available;
    private boolean locked;
    private Long parkingLotId;
}
