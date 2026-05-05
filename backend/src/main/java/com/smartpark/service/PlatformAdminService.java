package com.smartpark.service;

import com.smartpark.dto.AdminDashboardResponse;
import com.smartpark.dto.BookingResponse;
import com.smartpark.dto.ParkingLotRequest;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Admin operations service using EntityManager JPQL queries exclusively.
 * Avoids calling Lombok-generated model methods to work around JDK 24 compatibility issues.
 */
@Service
public class PlatformAdminService {

    private final EntityManager em;

    public PlatformAdminService(EntityManager em) {
        this.em = em;
    }

    @SuppressWarnings("unchecked")
    public AdminDashboardResponse getDashboardStats() {
        // Total revenue from completed/active bookings
        BigDecimal totalRevenue = queryScalar(
                "SELECT COALESCE(SUM(b.totalAmount),0) FROM Booking b WHERE b.status IN ('ACTIVE','COMPLETED')",
                BigDecimal.class);

        LocalDateTime todayStart = LocalDate.now().atStartOfDay();
        BigDecimal todayRevenue = queryScalarWithParam(
                "SELECT COALESCE(SUM(b.totalAmount),0) FROM Booking b WHERE b.status IN ('ACTIVE','COMPLETED') AND b.createdAt >= :start",
                BigDecimal.class, "start", todayStart);

        // Booking counts
        long totalBookings = queryScalar("SELECT COUNT(b) FROM Booking b", Long.class);
        long activeBookings = queryScalar("SELECT COUNT(b) FROM Booking b WHERE b.status = 'ACTIVE'", Long.class);
        long pendingBookings = queryScalar("SELECT COUNT(b) FROM Booking b WHERE b.status = 'PENDING'", Long.class);
        long completedBookings = queryScalar("SELECT COUNT(b) FROM Booking b WHERE b.status = 'COMPLETED'", Long.class);
        long cancelledBookings = queryScalar("SELECT COUNT(b) FROM Booking b WHERE b.status = 'CANCELLED'", Long.class);

        // Totals
        long totalUsers = queryScalar("SELECT COUNT(u) FROM User u", Long.class);
        long totalLots = queryScalar("SELECT COUNT(p) FROM ParkingLot p", Long.class);
        long totalSlots = queryScalar("SELECT COUNT(s) FROM Slot s", Long.class);

        // Occupancy
        double occupancy = totalSlots > 0 ? Math.min((double) activeBookings / totalSlots * 100, 100) : 0;

        // Recent bookings (last 10) as DTOs
        List<BookingResponse> recentBookings = queryBookings(null, 10);

        // Top lots by revenue
        List<Object[]> topLotsRaw = em.createQuery(
                "SELECT s.parkingLot.name, SUM(b.totalAmount), COUNT(b) FROM Booking b " +
                "JOIN b.slot s WHERE b.status IN ('ACTIVE','COMPLETED') " +
                "GROUP BY s.parkingLot.name ORDER BY SUM(b.totalAmount) DESC", Object[].class)
                .setMaxResults(5).getResultList();

        List<AdminDashboardResponse.LotRevenueItem> topLots = topLotsRaw.stream()
                .map(r -> AdminDashboardResponse.LotRevenueItem.builder()
                        .lotName((String) r[0])
                        .revenue(r[1] != null ? (BigDecimal) r[1] : BigDecimal.ZERO)
                        .bookingCount(r[2] != null ? (Long) r[2] : 0L)
                        .build())
                .collect(Collectors.toList());

        return AdminDashboardResponse.builder()
                .totalRevenue(totalRevenue)
                .todayRevenue(todayRevenue)
                .totalBookings(totalBookings)
                .activeBookings(activeBookings)
                .pendingBookings(pendingBookings)
                .completedBookings(completedBookings)
                .cancelledBookings(cancelledBookings)
                .totalUsers(totalUsers)
                .totalParkingLots(totalLots)
                .totalSlots(totalSlots)
                .occupancyRate(Math.round(occupancy * 10.0) / 10.0)
                .recentBookings(recentBookings)
                .topLots(topLots)
                .build();
    }

    public List<BookingResponse> getAllBookings(String status) {
        return queryBookings(status, 500);
    }

    @SuppressWarnings("unchecked")
    public List<Map<String, Object>> getAllUsers() {
        List<Object[]> rows = em.createQuery(
            "SELECT u.id, u.fullName, u.email, u.phone, u.role, u.createdAt, " +
            "(SELECT COUNT(b) FROM Booking b WHERE b.user.id = u.id), " +
            "(SELECT COALESCE(SUM(b.totalAmount),0) FROM Booking b WHERE b.user.id = u.id AND b.status IN ('ACTIVE','COMPLETED')) " +
            "FROM User u ORDER BY u.createdAt DESC", Object[].class).getResultList();

        return rows.stream().map(r -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", r[0]);
            m.put("fullName", r[1]);
            m.put("email", r[2]);
            m.put("phone", r[3]);
            m.put("role", r[4] != null ? r[4].toString() : "USER");
            m.put("createdAt", r[5]);
            m.put("totalBookings", r[6]);
            m.put("totalSpent", r[7] != null ? r[7] : BigDecimal.ZERO);
            return m;
        }).collect(Collectors.toList());
    }

    @Transactional
    public Map<String, Object> createParkingLot(ParkingLotRequest request) {
        // Use native query to insert parking lot
        Query q = em.createNativeQuery(
            "INSERT INTO parking_lots (name, address, city, latitude, longitude, total_slots, rate_per_hour, extra_rate_per_minute, buffer_minutes, created_at) " +
            "VALUES (:name, :addr, :city, :lat, :lng, :slots, :rate, :extra, :buf, :now)");
        q.setParameter("name", request.getName());
        q.setParameter("addr", request.getAddress());
        q.setParameter("city", request.getCity());
        q.setParameter("lat", request.getLatitude());
        q.setParameter("lng", request.getLongitude());
        q.setParameter("slots", request.getTotalSlots());
        q.setParameter("rate", request.getRatePerHour());
        q.setParameter("extra", request.getExtraRatePerMinute() != null ? request.getExtraRatePerMinute() : new BigDecimal("2.00"));
        q.setParameter("buf", request.getBufferMinutes() != null ? request.getBufferMinutes() : 15);
        q.setParameter("now", LocalDateTime.now());
        q.executeUpdate();

        // Get the new lot ID
        Object newId = em.createNativeQuery("SELECT MAX(id) FROM parking_lots").getSingleResult();
        Long lotId = ((Number) newId).longValue();

        // Auto-generate slots
        for (int i = 1; i <= request.getTotalSlots(); i++) {
            String prefix = String.valueOf((char) ('A' + ((i - 1) / 10)));
            String slotNumber = prefix + "-" + String.format("%02d", ((i - 1) % 10) + 1);

            em.createNativeQuery("INSERT INTO slots (parking_lot_id, slot_number, floor_level, slot_type, is_active) VALUES (:lid, :sn, 'G', 'REGULAR', true)")
                    .setParameter("lid", lotId)
                    .setParameter("sn", slotNumber)
                    .executeUpdate();
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("id", lotId);
        result.put("name", request.getName());
        result.put("message", "Parking lot created with " + request.getTotalSlots() + " slots");
        return result;
    }

    @Transactional
    public Map<String, Object> updateParkingLot(Long id, ParkingLotRequest request) {
        int updated = em.createNativeQuery(
            "UPDATE parking_lots SET name=:name, address=:addr, city=:city, latitude=:lat, longitude=:lng, " +
            "total_slots=:slots, rate_per_hour=:rate WHERE id=:id")
            .setParameter("name", request.getName())
            .setParameter("addr", request.getAddress())
            .setParameter("city", request.getCity())
            .setParameter("lat", request.getLatitude())
            .setParameter("lng", request.getLongitude())
            .setParameter("slots", request.getTotalSlots())
            .setParameter("rate", request.getRatePerHour())
            .setParameter("id", id)
            .executeUpdate();

        if (updated == 0) throw new IllegalArgumentException("Parking lot not found: " + id);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("id", id);
        result.put("name", request.getName());
        result.put("message", "Parking lot updated successfully");
        return result;
    }

    @Transactional
    public void deleteParkingLot(Long id) {
        em.createNativeQuery("DELETE FROM slots WHERE parking_lot_id = :id").setParameter("id", id).executeUpdate();
        int deleted = em.createNativeQuery("DELETE FROM parking_lots WHERE id = :id").setParameter("id", id).executeUpdate();
        if (deleted == 0) throw new IllegalArgumentException("Parking lot not found: " + id);
    }

    public Map<String, Object> getRevenueAnalytics(int days) {
        Map<LocalDate, BigDecimal> dailyRevenue = new TreeMap<>();
        for (int i = days; i >= 0; i--) {
            dailyRevenue.put(LocalDate.now().minusDays(i), BigDecimal.ZERO);
        }

        LocalDateTime startDate = LocalDate.now().minusDays(days).atStartOfDay();
        @SuppressWarnings("unchecked")
        List<Object[]> rows = em.createQuery(
            "SELECT CAST(b.createdAt AS LocalDate), SUM(b.totalAmount) FROM Booking b " +
            "WHERE b.status IN ('ACTIVE','COMPLETED') AND b.createdAt >= :start " +
            "GROUP BY CAST(b.createdAt AS LocalDate)", Object[].class)
            .setParameter("start", startDate).getResultList();

        for (Object[] row : rows) {
            LocalDate day = (LocalDate) row[0];
            BigDecimal amount = row[1] != null ? (BigDecimal) row[1] : BigDecimal.ZERO;
            dailyRevenue.put(day, amount);
        }

        List<String> labels = dailyRevenue.keySet().stream().map(LocalDate::toString).collect(Collectors.toList());
        List<BigDecimal> data = new ArrayList<>(dailyRevenue.values());

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("labels", labels);
        result.put("data", data);
        result.put("totalRevenue", data.stream().reduce(BigDecimal.ZERO, BigDecimal::add));
        return result;
    }

    // --- Helper methods ---

    @SuppressWarnings("unchecked")
    private List<BookingResponse> queryBookings(String status, int limit) {
        String jpql = "SELECT b.id, b.bookingRef, s.id, s.slotNumber, p.name, p.address, " +
                "b.startTime, b.endTime, b.actualExitTime, b.bufferMinutes, b.status, " +
                "b.baseAmount, b.extraCharge, b.totalAmount, b.qrToken, b.createdAt " +
                "FROM Booking b JOIN b.slot s JOIN s.parkingLot p ";
        if (status != null && !status.isBlank()) {
            jpql += "WHERE b.status = '" + status.toUpperCase() + "' ";
        }
        jpql += "ORDER BY b.createdAt DESC";

        List<Object[]> rows = em.createQuery(jpql, Object[].class).setMaxResults(limit).getResultList();

        return rows.stream().map(r -> BookingResponse.builder()
                .id(r[0] != null ? ((Number) r[0]).longValue() : null)
                .bookingRef((String) r[1])
                .slotId(r[2] != null ? ((Number) r[2]).longValue() : null)
                .slotNumber((String) r[3])
                .parkingLotName((String) r[4])
                .parkingLotAddress((String) r[5])
                .startTime((LocalDateTime) r[6])
                .endTime((LocalDateTime) r[7])
                .actualExitTime((LocalDateTime) r[8])
                .bufferMinutes(r[9] != null ? ((Number) r[9]).intValue() : null)
                .status(r[10] != null ? r[10].toString() : null)
                .baseAmount((BigDecimal) r[11])
                .extraCharge((BigDecimal) r[12])
                .totalAmount((BigDecimal) r[13])
                .qrToken((String) r[14])
                .createdAt((LocalDateTime) r[15])
                .build()
        ).collect(Collectors.toList());
    }

    @SuppressWarnings("unchecked")
    private <T> T queryScalar(String jpql, Class<T> type) {
        Object result = em.createQuery(jpql).getSingleResult();
        if (result == null) {
            if (type == Long.class) return type.cast(0L);
            if (type == BigDecimal.class) return type.cast(BigDecimal.ZERO);
        }
        if (type == Long.class && result instanceof Number) return type.cast(((Number) result).longValue());
        return type.cast(result);
    }

    @SuppressWarnings("unchecked")
    private <T> T queryScalarWithParam(String jpql, Class<T> type, String paramName, Object paramValue) {
        Object result = em.createQuery(jpql).setParameter(paramName, paramValue).getSingleResult();
        if (result == null) {
            if (type == Long.class) return type.cast(0L);
            if (type == BigDecimal.class) return type.cast(BigDecimal.ZERO);
        }
        if (type == Long.class && result instanceof Number) return type.cast(((Number) result).longValue());
        return type.cast(result);
    }
}
