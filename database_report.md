# SmartPark Database Table Report
Generated on: 2026-04-29

This report provides a detailed overview of the database schema and initial data for the SmartPark Management System.

---

## 1. Schema Overview

The system uses an **H2 Relational Database** with the following tables:

### A. `users` (User Management)
Stores all registered users and admin accounts.
- **Columns:** `id`, `full_name`, `email`, `password_hash`, `phone`, `role` (USER/ADMIN), `created_at`.
- **Primary Key:** `id`
- **Unique Constraints:** `email`

### B. `parking_lots` (Location Management)
Stores details of different parking areas/locations.
- **Columns:** `id`, `name`, `address`, `city`, `latitude`, `longitude`, `total_slots`, `rate_per_hour`, `extra_rate_per_minute`, `buffer_minutes`.
- **Primary Key:** `id`

### C. `slots` (Parking Spaces)
Individual slots within a specific parking lot.
- **Columns:** `id`, `parking_lot_id`, `slot_number`, `floor_level`, `slot_type` (REGULAR, COMPACT, LARGE, HANDICAP), `is_active`.
- **Foreign Key:** `parking_lot_id` -> `parking_lots(id)`

### D. `bookings` (Transaction Records)
Stores all parking reservations made by users.
- **Columns:** `id`, `booking_ref`, `user_id`, `slot_id`, `start_time`, `end_time`, `actual_exit_time`, `status` (PENDING, COMPLETED, CANCELLED), `total_amount`, `qr_token`.
- **Foreign Keys:** `user_id` -> `users(id)`, `slot_id` -> `slots(id)`

### E. `payments` (Payment Tracking)
Stores payment status and references for each booking.
- **Columns:** `id`, `booking_id`, `payment_ref`, `gateway`, `amount`, `currency`, `status` (SUCCESS, FAILED).
- **Foreign Key:** `booking_id` -> `bookings(id)`

### F. `slot_locks` (Concurrency Prevention)
Temporary locks created when a user selects a slot but hasn't paid yet.
- **Columns:** `id`, `slot_id`, `user_id`, `lock_start`, `lock_expiry`, `is_active`.

---

## 2. Seed Data Summary (Default Values)

The following data is pre-loaded into your system via `V2__seed_data.sql`:

### Admin Account
- **Email:** `admin@smartpark.com`
- **Password:** `admin123`
- **Role:** `ADMIN`

### Default Parking Lots
1. **Amanora Mall Parking** (Hadapsar, Pune) - ₹30/hr
2. **BKC Parking Complex** (Bandra, Mumbai) - ₹80/hr
3. **DLF Cyber City Parking** (Gurgaon) - ₹100/hr
4. **Saket Mall Parking** (New Delhi) - ₹70/hr
5. **Brigade Road Parking** (Bangalore) - ₹50/hr
6. **Salt Lake Parking** (Sector V, Kolkata) - ₹40/hr
7. **Tidel Park Parking** (Adyar, Chennai) - ₹60/hr

---

## 3. Data Integrity Rules
1. **One Booking Per Slot:** The system prevents overlapping bookings for the same slot.
2. **5-Minute Lock:** When a user selects a slot, it is locked for 5 minutes. If payment isn't completed, the lock expires automatically.
3. **Role-Based Access:** Admin endpoints are protected and only accessible to users with `ROLE_ADMIN`.
