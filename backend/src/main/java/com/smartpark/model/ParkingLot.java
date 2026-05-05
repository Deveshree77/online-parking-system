package com.smartpark.model;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "parking_lots")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ParkingLot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String name;

    @Column(nullable = false, length = 500)
    private String address;

    @Column(nullable = false, length = 100)
    private String city;

    @Column(nullable = false)
    private Double latitude;

    @Column(nullable = false)
    private Double longitude;

    @Column(name = "total_slots", nullable = false)
    private Integer totalSlots;

    @Column(name = "rate_per_hour", nullable = false, precision = 10, scale = 2)
    private BigDecimal ratePerHour;

    @Column(name = "rate_2_wheeler", precision = 10, scale = 2)
    private BigDecimal rateTwoWheeler;

    @Column(name = "rate_4_wheeler", precision = 10, scale = 2)
    private BigDecimal rateFourWheeler;

    @Column(name = "extra_rate_per_minute", precision = 10, scale = 2)
    private BigDecimal extraRatePerMinute;

    @Column(name = "buffer_minutes")
    private Integer bufferMinutes;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (extraRatePerMinute == null) extraRatePerMinute = new BigDecimal("2.00");
        if (bufferMinutes == null) bufferMinutes = 15;
    }
}
