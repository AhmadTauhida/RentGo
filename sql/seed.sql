-- RentGo demo data. Run after schema.sql.
-- Staff accounts are created by StaffDAO.ensureDefaultAdmin(), not seeded here.

INSERT INTO vehicle (vehicle_type, plate_number, brand, manufacture_year, base_rate, status, passenger_capacity, transmission)
VALUES
  ('CAR', 'H 1001 AA', 'Toyota Avanza', 2023, 350000, 'AVAILABLE', 7, 'AUTOMATIC'),
  ('CAR', 'H 1002 AA', 'Honda Brio', 2022, 350000, 'AVAILABLE', 5, 'MANUAL'),
  ('CAR', 'H 1003 AA', 'Suzuki Ertiga', 2024, 400000, 'AVAILABLE', 7, 'MANUAL')
ON CONFLICT (plate_number) DO NOTHING;

INSERT INTO vehicle (vehicle_type, plate_number, brand, manufacture_year, base_rate, status, engine_cc)
VALUES
  ('MOTORCYCLE', 'H 2001 AA', 'Honda PCX', 2023, 100000, 'AVAILABLE', 160),
  ('MOTORCYCLE', 'H 2002 AA', 'Yamaha NMAX', 2022, 110000, 'AVAILABLE', 155),
  ('MOTORCYCLE', 'H 2003 AA', 'Honda Vario', 2024, 90000, 'MAINTENANCE', 125)
ON CONFLICT (plate_number) DO NOTHING;

INSERT INTO vehicle (vehicle_type, plate_number, brand, manufacture_year, base_rate, status, seat_count)
VALUES ('BUS', 'H 3001 AA', 'Isuzu Elf', 2021, 1500000, 'AVAILABLE', 30)
ON CONFLICT (plate_number) DO NOTHING;

INSERT INTO customer (name, phone_number, national_id, address)
VALUES
  ('Budi Santoso', '081234567801', '3273010101010001', 'Semarang'),
  ('Siti Aminah', '081234567802', '3273010101010002', 'Kendal'),
  ('Agus Wijaya', '081234567803', '3273010101010003', 'Demak'),
  ('Dewi Lestari', '081234567804', '3273010101010004', 'Ungaran'),
  ('Rina Kartika', '081234567805', '3273010101010005', 'Salatiga')
ON CONFLICT (national_id) DO NOTHING;

-- Transaction examples are inserted only after at least one staff account exists.
-- This keeps password hashing in Java while allowing the seed to be run after login setup.
INSERT INTO rental (customer_id, vehicle_id, staff_id, start_date, due_date, with_driver, total_cost, status)
SELECT c.id, v.id, s.id, DATE '2026-11-01', DATE '2026-11-03', FALSE, 630000, 'COMPLETED'
FROM customer c, vehicle v, staff s
WHERE c.national_id = '3273010101010001' AND v.plate_number = 'H 1002 AA' AND s.username = 'admin'
  AND NOT EXISTS (SELECT 1 FROM rental r WHERE r.vehicle_id = v.id AND r.start_date = DATE '2026-11-01');

INSERT INTO payment (rental_id, method, amount, note)
SELECT r.id, 'TRANSFER', r.total_cost, 'RENTAL'
FROM rental r
WHERE r.start_date = DATE '2026-11-01' AND NOT EXISTS (SELECT 1 FROM payment p WHERE p.rental_id = r.id);

INSERT INTO vehicle_return (rental_id, actual_return_date, vehicle_condition, late_fee)
SELECT r.id, DATE '2026-11-05', 'GOOD', 1050000
FROM rental r
WHERE r.start_date = DATE '2026-11-01' AND NOT EXISTS (SELECT 1 FROM vehicle_return vr WHERE vr.rental_id = r.id);

UPDATE vehicle
SET status = 'AVAILABLE'
WHERE plate_number = 'H 1002 AA';

INSERT INTO rental (customer_id, vehicle_id, staff_id, start_date, due_date, with_driver, total_cost, status)
SELECT c.id, v.id, s.id, DATE '2026-11-10', DATE '2026-11-12', FALSE, 630000, 'ACTIVE'
FROM customer c, vehicle v, staff s
WHERE c.national_id = '3273010101010002' AND v.plate_number = 'H 1001 AA' AND s.username = 'admin'
  AND NOT EXISTS (SELECT 1 FROM rental r WHERE r.vehicle_id = v.id AND r.status = 'ACTIVE');

INSERT INTO payment (rental_id, method, amount, note)
SELECT r.id, 'CASH', r.total_cost, 'RENTAL'
FROM rental r
WHERE r.status = 'ACTIVE' AND NOT EXISTS (SELECT 1 FROM payment p WHERE p.rental_id = r.id);

UPDATE vehicle
SET status = 'RENTED'
WHERE plate_number = 'H 1001 AA'
  AND EXISTS (SELECT 1 FROM rental r WHERE r.vehicle_id = vehicle.id AND r.status = 'ACTIVE');
