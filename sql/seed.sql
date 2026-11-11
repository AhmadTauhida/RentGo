-- RentGo demo master data. Run after schema.sql.
-- Safe to run before login. Transaction examples are in seed_transactions.sql.
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
