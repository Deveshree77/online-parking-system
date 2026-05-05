package com.smartpark.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public class ParkingLotRequest {

    @NotBlank(message = "Parking lot name is required")
    private String name;

    @NotBlank(message = "Address is required")
    private String address;

    @NotBlank(message = "City is required")
    private String city;

    @NotNull(message = "Latitude is required")
    private Double latitude;

    @NotNull(message = "Longitude is required")
    private Double longitude;

    @NotNull(message = "Total slots count is required")
    @Min(value = 1, message = "Must have at least 1 slot")
    private Integer totalSlots;

    @NotNull(message = "Rate per hour is required")
    @DecimalMin(value = "0.01")
    private BigDecimal ratePerHour;

    private BigDecimal extraRatePerMinute;
    private Integer bufferMinutes;

    public ParkingLotRequest() {}

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }

    public Double getLatitude() { return latitude; }
    public void setLatitude(Double latitude) { this.latitude = latitude; }

    public Double getLongitude() { return longitude; }
    public void setLongitude(Double longitude) { this.longitude = longitude; }

    public Integer getTotalSlots() { return totalSlots; }
    public void setTotalSlots(Integer totalSlots) { this.totalSlots = totalSlots; }

    public BigDecimal getRatePerHour() { return ratePerHour; }
    public void setRatePerHour(BigDecimal ratePerHour) { this.ratePerHour = ratePerHour; }

    public BigDecimal getExtraRatePerMinute() { return extraRatePerMinute; }
    public void setExtraRatePerMinute(BigDecimal extraRatePerMinute) { this.extraRatePerMinute = extraRatePerMinute; }

    public Integer getBufferMinutes() { return bufferMinutes; }
    public void setBufferMinutes(Integer bufferMinutes) { this.bufferMinutes = bufferMinutes; }
}
