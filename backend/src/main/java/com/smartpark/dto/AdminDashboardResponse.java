package com.smartpark.dto;

import java.math.BigDecimal;
import java.util.List;

public class AdminDashboardResponse {
    private BigDecimal totalRevenue;
    private BigDecimal todayRevenue;
    private long totalBookings;
    private long activeBookings;
    private long pendingBookings;
    private long completedBookings;
    private long cancelledBookings;
    private long totalUsers;
    private long totalParkingLots;
    private long totalSlots;
    private double occupancyRate;
    private List<BookingResponse> recentBookings;
    private List<LotRevenueItem> topLots;

    public AdminDashboardResponse() {}

    // Getters & Setters
    public BigDecimal getTotalRevenue() { return totalRevenue; }
    public void setTotalRevenue(BigDecimal totalRevenue) { this.totalRevenue = totalRevenue; }
    public BigDecimal getTodayRevenue() { return todayRevenue; }
    public void setTodayRevenue(BigDecimal todayRevenue) { this.todayRevenue = todayRevenue; }
    public long getTotalBookings() { return totalBookings; }
    public void setTotalBookings(long totalBookings) { this.totalBookings = totalBookings; }
    public long getActiveBookings() { return activeBookings; }
    public void setActiveBookings(long activeBookings) { this.activeBookings = activeBookings; }
    public long getPendingBookings() { return pendingBookings; }
    public void setPendingBookings(long pendingBookings) { this.pendingBookings = pendingBookings; }
    public long getCompletedBookings() { return completedBookings; }
    public void setCompletedBookings(long completedBookings) { this.completedBookings = completedBookings; }
    public long getCancelledBookings() { return cancelledBookings; }
    public void setCancelledBookings(long cancelledBookings) { this.cancelledBookings = cancelledBookings; }
    public long getTotalUsers() { return totalUsers; }
    public void setTotalUsers(long totalUsers) { this.totalUsers = totalUsers; }
    public long getTotalParkingLots() { return totalParkingLots; }
    public void setTotalParkingLots(long totalParkingLots) { this.totalParkingLots = totalParkingLots; }
    public long getTotalSlots() { return totalSlots; }
    public void setTotalSlots(long totalSlots) { this.totalSlots = totalSlots; }
    public double getOccupancyRate() { return occupancyRate; }
    public void setOccupancyRate(double occupancyRate) { this.occupancyRate = occupancyRate; }
    public List<BookingResponse> getRecentBookings() { return recentBookings; }
    public void setRecentBookings(List<BookingResponse> recentBookings) { this.recentBookings = recentBookings; }
    public List<LotRevenueItem> getTopLots() { return topLots; }
    public void setTopLots(List<LotRevenueItem> topLots) { this.topLots = topLots; }

    // Builder
    public static AdminDashboardResponseBuilder builder() { return new AdminDashboardResponseBuilder(); }

    public static class AdminDashboardResponseBuilder {
        private final AdminDashboardResponse obj = new AdminDashboardResponse();
        public AdminDashboardResponseBuilder totalRevenue(BigDecimal v) { obj.totalRevenue = v; return this; }
        public AdminDashboardResponseBuilder todayRevenue(BigDecimal v) { obj.todayRevenue = v; return this; }
        public AdminDashboardResponseBuilder totalBookings(long v) { obj.totalBookings = v; return this; }
        public AdminDashboardResponseBuilder activeBookings(long v) { obj.activeBookings = v; return this; }
        public AdminDashboardResponseBuilder pendingBookings(long v) { obj.pendingBookings = v; return this; }
        public AdminDashboardResponseBuilder completedBookings(long v) { obj.completedBookings = v; return this; }
        public AdminDashboardResponseBuilder cancelledBookings(long v) { obj.cancelledBookings = v; return this; }
        public AdminDashboardResponseBuilder totalUsers(long v) { obj.totalUsers = v; return this; }
        public AdminDashboardResponseBuilder totalParkingLots(long v) { obj.totalParkingLots = v; return this; }
        public AdminDashboardResponseBuilder totalSlots(long v) { obj.totalSlots = v; return this; }
        public AdminDashboardResponseBuilder occupancyRate(double v) { obj.occupancyRate = v; return this; }
        public AdminDashboardResponseBuilder recentBookings(List<BookingResponse> v) { obj.recentBookings = v; return this; }
        public AdminDashboardResponseBuilder topLots(List<LotRevenueItem> v) { obj.topLots = v; return this; }
        public AdminDashboardResponse build() { return obj; }
    }

    // Inner class
    public static class LotRevenueItem {
        private Long lotId;
        private String lotName;
        private BigDecimal revenue;
        private long bookingCount;

        public LotRevenueItem() {}

        public Long getLotId() { return lotId; }
        public void setLotId(Long lotId) { this.lotId = lotId; }
        public String getLotName() { return lotName; }
        public void setLotName(String lotName) { this.lotName = lotName; }
        public BigDecimal getRevenue() { return revenue; }
        public void setRevenue(BigDecimal revenue) { this.revenue = revenue; }
        public long getBookingCount() { return bookingCount; }
        public void setBookingCount(long bookingCount) { this.bookingCount = bookingCount; }

        public static LotRevenueItemBuilder builder() { return new LotRevenueItemBuilder(); }

        public static class LotRevenueItemBuilder {
            private final LotRevenueItem obj = new LotRevenueItem();
            public LotRevenueItemBuilder lotId(Long v) { obj.lotId = v; return this; }
            public LotRevenueItemBuilder lotName(String v) { obj.lotName = v; return this; }
            public LotRevenueItemBuilder revenue(BigDecimal v) { obj.revenue = v; return this; }
            public LotRevenueItemBuilder bookingCount(long v) { obj.bookingCount = v; return this; }
            public LotRevenueItem build() { return obj; }
        }
    }
}
