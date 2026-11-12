-- RentGo demo transactions. Run after seed.sql and after the default admin exists.
-- Safe to rerun: every INSERT is guarded so existing rows are not duplicated.

DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM staff WHERE username = 'admin') THEN
        RAISE EXCEPTION 'Default admin account is missing. Start the app once so StaffDAO.ensureDefaultAdmin() creates admin/admin123, then rerun this script.';
    END IF;
END $$;

-- Completed late rental: Car 350000 x 2 days = 700000, late 2 days = 1050000.
INSERT INTO rental (customer_id, vehicle_id, staff_id, start_date, due_date, with_driver, total_cost, status)
SELECT c.id, v.id, s.id, DATE '2026-11-01', DATE '2026-11-03', FALSE, 700000, 'COMPLETED'
FROM customer c, vehicle v, staff s
WHERE c.national_id = '3273010101010001' AND v.plate_number = 'H 1002 AA' AND s.username = 'admin'
  AND NOT EXISTS (SELECT 1 FROM rental r WHERE r.vehicle_id = v.id AND r.start_date = DATE '2026-11-01');

INSERT INTO payment (rental_id, method, amount, note)
SELECT r.id, 'TRANSFER', r.total_cost, 'RENTAL'
FROM rental r
JOIN vehicle v ON v.id = r.vehicle_id
WHERE v.plate_number = 'H 1002 AA' AND r.start_date = DATE '2026-11-01'
  AND NOT EXISTS (SELECT 1 FROM payment p WHERE p.rental_id = r.id AND p.note = 'RENTAL');

INSERT INTO vehicle_return (rental_id, actual_return_date, vehicle_condition, late_fee)
SELECT r.id, DATE '2026-11-05', 'GOOD', 1050000
FROM rental r
JOIN vehicle v ON v.id = r.vehicle_id
WHERE v.plate_number = 'H 1002 AA' AND r.start_date = DATE '2026-11-01'
  AND NOT EXISTS (SELECT 1 FROM vehicle_return vr WHERE vr.rental_id = r.id);

UPDATE vehicle
SET status = 'AVAILABLE'
WHERE plate_number = 'H 1002 AA'
  AND NOT EXISTS (SELECT 1 FROM rental r WHERE r.vehicle_id = vehicle.id AND r.status = 'ACTIVE');

-- Active rental: Car 350000 x 2 days = 700000.
INSERT INTO rental (customer_id, vehicle_id, staff_id, start_date, due_date, with_driver, total_cost, status)
SELECT c.id, v.id, s.id, DATE '2026-11-10', DATE '2026-11-12', FALSE, 700000, 'ACTIVE'
FROM customer c, vehicle v, staff s
WHERE c.national_id = '3273010101010002' AND v.plate_number = 'H 1001 AA' AND s.username = 'admin'
  AND NOT EXISTS (SELECT 1 FROM rental r WHERE r.vehicle_id = v.id AND r.start_date = DATE '2026-11-10');

INSERT INTO payment (rental_id, method, amount, note)
SELECT r.id, 'CASH', r.total_cost, 'RENTAL'
FROM rental r
JOIN vehicle v ON v.id = r.vehicle_id
WHERE v.plate_number = 'H 1001 AA' AND r.start_date = DATE '2026-11-10'
  AND NOT EXISTS (SELECT 1 FROM payment p WHERE p.rental_id = r.id AND p.note = 'RENTAL');

UPDATE vehicle
SET status = 'RENTED'
WHERE plate_number = 'H 1001 AA'
  AND EXISTS (SELECT 1 FROM rental r WHERE r.vehicle_id = vehicle.id AND r.status = 'ACTIVE');
