-- V2: Seed Data

-- Parking Lots
INSERT INTO parking_lots (id, name, address, city, latitude, longitude, total_slots, rate_per_hour, extra_rate_per_minute, buffer_minutes) VALUES
(1, 'Phoenix Mall Parking', 'Lower Parel, Mumbai', 'Mumbai', 19.0068, 72.8312, 15, 60.00, 3.00, 15),
(2, 'BKC Parking Complex', 'Bandra Kurla Complex, Mumbai', 'Mumbai', 19.0596, 72.8656, 12, 80.00, 4.00, 15),
(3, 'Connaught Place Parking', 'Block A, Connaught Place, Delhi', 'Delhi', 28.6315, 77.2167, 20, 50.00, 2.50, 15),
(4, 'Saket Mall Parking', 'Select Citywalk, Saket, Delhi', 'Delhi', 28.5285, 77.2192, 18, 70.00, 3.50, 10),
(5, 'Orion Mall Parking', 'Dr Rajkumar Rd, Rajajinagar, Bangalore', 'Bangalore', 13.0107, 77.5556, 25, 40.00, 2.00, 15),
(6, 'UB City Parking', 'Vittal Mallya Rd, Bangalore', 'Bangalore', 12.9716, 77.5972, 10, 100.00, 5.00, 10),
(7, 'Amanora Mall Parking', 'Hadapsar, Pune', 'Pune', 18.5124, 73.9310, 16, 30.00, 2.00, 15),
(8, 'Inorbit Mall Parking', 'HITEC City, Hyderabad', 'Hyderabad', 17.4325, 78.3827, 22, 40.00, 2.00, 15);

-- Slots for Phoenix Mall Parking (Lot 1)
INSERT INTO slots (id, parking_lot_id, slot_number, floor_level, slot_type, is_active) VALUES
(1, 1, 'A-01', 'G', 'REGULAR', true),
(2, 1, 'A-02', 'G', 'REGULAR', true),
(3, 1, 'A-03', 'G', 'COMPACT', true),
(4, 1, 'A-04', 'G', 'COMPACT', true),
(5, 1, 'A-05', 'G', 'LARGE', true),
(6, 1, 'B-01', '1', 'REGULAR', true),
(7, 1, 'B-02', '1', 'REGULAR', true),
(8, 1, 'B-03', '1', 'REGULAR', true),
(9, 1, 'B-04', '1', 'HANDICAP', true),
(10, 1, 'B-05', '1', 'COMPACT', true),
(11, 1, 'C-01', '2', 'REGULAR', true),
(12, 1, 'C-02', '2', 'REGULAR', true),
(13, 1, 'C-03', '2', 'LARGE', true),
(14, 1, 'C-04', '2', 'COMPACT', true),
(15, 1, 'C-05', '2', 'REGULAR', true);

-- Slots for BKC Parking Complex (Lot 2)
INSERT INTO slots (id, parking_lot_id, slot_number, floor_level, slot_type, is_active) VALUES
(16, 2, 'A-01', 'G', 'REGULAR', true),
(17, 2, 'A-02', 'G', 'REGULAR', true),
(18, 2, 'A-03', 'G', 'LARGE', true),
(19, 2, 'A-04', 'G', 'COMPACT', true),
(20, 2, 'B-01', '1', 'REGULAR', true),
(21, 2, 'B-02', '1', 'REGULAR', true),
(22, 2, 'B-03', '1', 'HANDICAP', true),
(23, 2, 'B-04', '1', 'REGULAR', true),
(24, 2, 'C-01', '2', 'REGULAR', true),
(25, 2, 'C-02', '2', 'COMPACT', true),
(26, 2, 'C-03', '2', 'REGULAR', true),
(27, 2, 'C-04', '2', 'LARGE', true);

-- Slots for Connaught Place Parking (Lot 3)
INSERT INTO slots (id, parking_lot_id, slot_number, floor_level, slot_type, is_active) VALUES
(28, 3, 'P-01', 'B1', 'REGULAR', true),
(29, 3, 'P-02', 'B1', 'REGULAR', true),
(30, 3, 'P-03', 'B1', 'COMPACT', true),
(31, 3, 'P-04', 'B1', 'REGULAR', true),
(32, 3, 'P-05', 'B1', 'LARGE', true),
(33, 3, 'P-06', 'B1', 'REGULAR', true),
(34, 3, 'P-07', 'B1', 'REGULAR', true),
(35, 3, 'P-08', 'B1', 'HANDICAP', true),
(36, 3, 'P-09', 'B1', 'COMPACT', true),
(37, 3, 'P-10', 'B1', 'REGULAR', true),
(38, 3, 'Q-01', 'B2', 'REGULAR', true),
(39, 3, 'Q-02', 'B2', 'REGULAR', true),
(40, 3, 'Q-03', 'B2', 'LARGE', true),
(41, 3, 'Q-04', 'B2', 'COMPACT', true),
(42, 3, 'Q-05', 'B2', 'REGULAR', true),
(43, 3, 'Q-06', 'B2', 'REGULAR', true),
(44, 3, 'Q-07', 'B2', 'REGULAR', true),
(45, 3, 'Q-08', 'B2', 'HANDICAP', true),
(46, 3, 'Q-09', 'B2', 'COMPACT', true),
(47, 3, 'Q-10', 'B2', 'REGULAR', true);

-- Slots for Saket Mall Parking (Lot 4)
INSERT INTO slots (id, parking_lot_id, slot_number, floor_level, slot_type, is_active) VALUES
(48, 4, 'S-01', 'G', 'REGULAR', true),
(49, 4, 'S-02', 'G', 'COMPACT', true),
(50, 4, 'S-03', 'G', 'REGULAR', true),
(51, 4, 'S-04', 'G', 'LARGE', true),
(52, 4, 'S-05', 'G', 'HANDICAP', true),
(53, 4, 'S-06', 'G', 'REGULAR', true),
(54, 4, 'T-01', '1', 'REGULAR', true),
(55, 4, 'T-02', '1', 'REGULAR', true),
(56, 4, 'T-03', '1', 'COMPACT', true),
(57, 4, 'T-04', '1', 'REGULAR', true),
(58, 4, 'T-05', '1', 'REGULAR', true),
(59, 4, 'T-06', '1', 'LARGE', true),
(60, 4, 'U-01', '2', 'REGULAR', true),
(61, 4, 'U-02', '2', 'REGULAR', true),
(62, 4, 'U-03', '2', 'COMPACT', true),
(63, 4, 'U-04', '2', 'REGULAR', true),
(64, 4, 'U-05', '2', 'HANDICAP', true),
(65, 4, 'U-06', '2', 'REGULAR', true);

-- Slots for Orion Mall Parking (Lot 5)
INSERT INTO slots (id, parking_lot_id, slot_number, floor_level, slot_type, is_active) VALUES
(66, 5, 'A-01', 'G', 'REGULAR', true),
(67, 5, 'A-02', 'G', 'REGULAR', true),
(68, 5, 'A-03', 'G', 'COMPACT', true),
(69, 5, 'A-04', 'G', 'LARGE', true),
(70, 5, 'A-05', 'G', 'REGULAR', true),
(71, 5, 'B-01', '1', 'REGULAR', true),
(72, 5, 'B-02', '1', 'COMPACT', true),
(73, 5, 'B-03', '1', 'REGULAR', true),
(74, 5, 'B-04', '1', 'REGULAR', true),
(75, 5, 'B-05', '1', 'HANDICAP', true),
(76, 5, 'C-01', '2', 'REGULAR', true),
(77, 5, 'C-02', '2', 'REGULAR', true),
(78, 5, 'C-03', '2', 'LARGE', true),
(79, 5, 'C-04', '2', 'REGULAR', true),
(80, 5, 'C-05', '2', 'COMPACT', true),
(81, 5, 'D-01', '3', 'REGULAR', true),
(82, 5, 'D-02', '3', 'REGULAR', true),
(83, 5, 'D-03', '3', 'REGULAR', true),
(84, 5, 'D-04', '3', 'COMPACT', true),
(85, 5, 'D-05', '3', 'REGULAR', true),
(86, 5, 'E-01', '4', 'LARGE', true),
(87, 5, 'E-02', '4', 'REGULAR', true),
(88, 5, 'E-03', '4', 'REGULAR', true),
(89, 5, 'E-04', '4', 'HANDICAP', true),
(90, 5, 'E-05', '4', 'REGULAR', true);

-- Slots for UB City Parking (Lot 6)
INSERT INTO slots (id, parking_lot_id, slot_number, floor_level, slot_type, is_active) VALUES
(91, 6, 'V-01', 'B1', 'REGULAR', true),
(92, 6, 'V-02', 'B1', 'REGULAR', true),
(93, 6, 'V-03', 'B1', 'LARGE', true),
(94, 6, 'V-04', 'B1', 'COMPACT', true),
(95, 6, 'V-05', 'B1', 'REGULAR', true),
(96, 6, 'W-01', 'B2', 'REGULAR', true),
(97, 6, 'W-02', 'B2', 'HANDICAP', true),
(98, 6, 'W-03', 'B2', 'REGULAR', true),
(99, 6, 'W-04', 'B2', 'COMPACT', true),
(100, 6, 'W-05', 'B2', 'REGULAR', true);

-- Slots for Amanora Mall Parking (Lot 7)
INSERT INTO slots (id, parking_lot_id, slot_number, floor_level, slot_type, is_active) VALUES
(101, 7, 'A-01', 'G', 'REGULAR', true),
(102, 7, 'A-02', 'G', 'COMPACT', true),
(103, 7, 'A-03', 'G', 'REGULAR', true),
(104, 7, 'A-04', 'G', 'LARGE', true),
(105, 7, 'B-01', '1', 'REGULAR', true),
(106, 7, 'B-02', '1', 'REGULAR', true),
(107, 7, 'B-03', '1', 'HANDICAP', true),
(108, 7, 'B-04', '1', 'COMPACT', true),
(109, 7, 'C-01', '2', 'REGULAR', true),
(110, 7, 'C-02', '2', 'REGULAR', true),
(111, 7, 'C-03', '2', 'REGULAR', true),
(112, 7, 'C-04', '2', 'LARGE', true),
(113, 7, 'D-01', '3', 'REGULAR', true),
(114, 7, 'D-02', '3', 'COMPACT', true),
(115, 7, 'D-03', '3', 'REGULAR', true),
(116, 7, 'D-04', '3', 'REGULAR', true);

-- Slots for Inorbit Mall Parking (Lot 8)
INSERT INTO slots (id, parking_lot_id, slot_number, floor_level, slot_type, is_active) VALUES
(117, 8, 'A-01', 'G', 'REGULAR', true),
(118, 8, 'A-02', 'G', 'REGULAR', true),
(119, 8, 'A-03', 'G', 'COMPACT', true),
(120, 8, 'A-04', 'G', 'LARGE', true),
(121, 8, 'A-05', 'G', 'REGULAR', true),
(122, 8, 'B-01', '1', 'REGULAR', true),
(123, 8, 'B-02', '1', 'REGULAR', true),
(124, 8, 'B-03', '1', 'COMPACT', true),
(125, 8, 'B-04', '1', 'HANDICAP', true),
(126, 8, 'B-05', '1', 'REGULAR', true),
(127, 8, 'C-01', '2', 'REGULAR', true),
(128, 8, 'C-02', '2', 'REGULAR', true),
(129, 8, 'C-03', '2', 'LARGE', true),
(130, 8, 'C-04', '2', 'REGULAR', true),
(131, 8, 'C-05', '2', 'COMPACT', true),
(132, 8, 'D-01', '3', 'REGULAR', true),
(133, 8, 'D-02', '3', 'REGULAR', true),
(134, 8, 'D-03', '3', 'REGULAR', true),
(135, 8, 'D-04', '3', 'HANDICAP', true),
(136, 8, 'D-05', '3', 'REGULAR', true),
(137, 8, 'D-06', '3', 'COMPACT', true),
(138, 8, 'D-07', '3', 'REGULAR', true);

-- Admin User (password: admin123)
INSERT INTO users (id, full_name, email, password_hash, phone, role) VALUES
(1, 'Smart Park Admin', 'admin@smartpark.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', '9999999999', 'ADMIN');
