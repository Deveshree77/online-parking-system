-- V3: Add vehicle-specific rates and categories
ALTER TABLE parking_lots ADD COLUMN rate_2_wheeler DECIMAL(10, 2);
ALTER TABLE parking_lots ADD COLUMN rate_4_wheeler DECIMAL(10, 2);

-- Set default rates based on existing rate_per_hour
-- 2-Wheeler: 50% of base rate, 4-Wheeler: 100% of base rate
UPDATE parking_lots SET rate_2_wheeler = rate_per_hour * 0.5, rate_4_wheeler = rate_per_hour;

-- Update existing slots to TWO_WHEELER or FOUR_WHEELER
-- COMPACT slots become TWO_WHEELER, others become FOUR_WHEELER
UPDATE slots SET slot_type = 'TWO_WHEELER' WHERE slot_type = 'COMPACT';
UPDATE slots SET slot_type = 'FOUR_WHEELER' WHERE slot_type IN ('REGULAR', 'LARGE', 'HANDICAP');
