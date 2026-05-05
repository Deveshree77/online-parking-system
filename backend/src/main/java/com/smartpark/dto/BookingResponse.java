package com.smartpark.dto;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class BookingResponse {
    private Long id;
    private String bookingRef;
    private Long slotId;
    private String slotNumber;
    private String parkingLotName;
    private String parkingLotAddress;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private LocalDateTime actualExitTime;
    private Integer bufferMinutes;
    private String status;
    private BigDecimal baseAmount;
    private BigDecimal extraCharge;
    private BigDecimal totalAmount;
    private String qrToken;
    private LocalDateTime createdAt;
}
