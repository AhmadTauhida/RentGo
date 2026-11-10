-- RentGo PostgreSQL schema.
-- Run once on the Neon database before loading seed data.

CREATE TABLE staff (
  id            INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  name          VARCHAR(100) NOT NULL,
  phone_number  VARCHAR(20),
  username      VARCHAR(50)  NOT NULL UNIQUE,
  password_hash VARCHAR(255) NOT NULL,
  role          VARCHAR(10)  NOT NULL DEFAULT 'STAFF' CHECK (role IN ('ADMIN', 'STAFF'))
);

CREATE TABLE customer (
  id           INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  name         VARCHAR(100) NOT NULL,
  phone_number VARCHAR(20),
  national_id  CHAR(16)     NOT NULL UNIQUE,
  address      TEXT         NOT NULL
);

CREATE TABLE vehicle (
  id                 INT           GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  vehicle_type       VARCHAR(12)   NOT NULL CHECK (vehicle_type IN ('CAR', 'MOTORCYCLE', 'BUS')),
  plate_number       VARCHAR(15)   NOT NULL UNIQUE,
  brand              VARCHAR(50)   NOT NULL,
  manufacture_year   SMALLINT      NOT NULL,
  base_rate          NUMERIC(12,2) NOT NULL CHECK (base_rate >= 0),
  status             VARCHAR(12)   NOT NULL DEFAULT 'AVAILABLE' CHECK (status IN ('AVAILABLE', 'RENTED', 'MAINTENANCE')),
  passenger_capacity INT,
  transmission       VARCHAR(10),
  engine_cc          INT,
  seat_count         INT
);

CREATE TABLE rental (
  id          INT           GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  customer_id INT           NOT NULL REFERENCES customer(id),
  vehicle_id  INT           NOT NULL REFERENCES vehicle(id),
  staff_id    INT           NOT NULL REFERENCES staff(id),
  start_date  DATE          NOT NULL,
  due_date    DATE          NOT NULL,
  with_driver BOOLEAN       NOT NULL DEFAULT FALSE,
  total_cost  NUMERIC(14,2) NOT NULL,
  status      VARCHAR(12)   NOT NULL DEFAULT 'ACTIVE' CHECK (status IN ('ACTIVE', 'COMPLETED', 'CANCELLED')),
  CONSTRAINT chk_rental_dates CHECK (due_date >= start_date)
);

CREATE TABLE payment (
  id        INT           GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  rental_id INT           NOT NULL REFERENCES rental(id),
  method    VARCHAR(10)   NOT NULL CHECK (method IN ('CASH', 'TRANSFER', 'EWALLET')),
  amount    NUMERIC(14,2) NOT NULL CHECK (amount > 0),
  paid_at   TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  note      VARCHAR(100)
);

CREATE TABLE vehicle_return (
  id                  INT           GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  rental_id           INT           NOT NULL UNIQUE REFERENCES rental(id),
  actual_return_date  DATE          NOT NULL,
  vehicle_condition   VARCHAR(15)   NOT NULL DEFAULT 'GOOD' CHECK (vehicle_condition IN ('GOOD', 'MINOR_DAMAGE', 'MAJOR_DAMAGE')),
  late_fee            NUMERIC(14,2) NOT NULL DEFAULT 0 CHECK (late_fee >= 0)
);
