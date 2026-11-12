# Referensi Implementasi Lanjutan RentGo

> Dokumen referensi kode, bukan source code aktif. Implementasikan per fitur
> sambil menyesuaikan dengan branch masing-masing. Sumber kebenaran tetap
> `.ai/prd/PRD.md`.

## 1. Kondisi Kode Saat Ini

| Lapisan | Sudah ada | Masih stub |
|---|---|---|
| `model` | 10 kelas + `PrintableReceipt` + 6 enum | - |
| `exception` | 3 exception | - |
| `util` | `Validator`, `CurrencyFormatter`, `PasswordHasher`, `Session` | - |
| `dao` | `DatabaseConnection`, `CrudDAO<T>` | semua DAO konkret |
| `controller` | - | semua controller |
| `view` | `LoginFrame` | `MainFrame`, 6 panel |
| `Main` | FlatLaf + `LoginFrame` | cek DB, `ensureDefaultAdmin`, wiring |
| SQL | `schema.sql`, `seed.sql`, `seed_transactions.sql` | - |
| Test | `RentalCostTest` | `DaoConsoleTest` |

Urutan implementasi yang disarankan:

1. `DatabaseConnection.translate(...)`.
2. `StaffDAO` + `LoginController` + update `Main`.
3. `VehicleDAO` + `VehicleController` + `VehiclePanel` + `MainFrame`.
4. `CustomerDAO` + `CustomerController` + `CustomerPanel`.
5. `RentalDAO` + `PaymentDAO` + `RentalController` + `RentalPanel`.
6. `VehicleReturnDAO` + `VehicleReturnController` + `VehicleReturnPanel`.
7. `ReportDAO` + `ReportController` + `ReportPanel`.
8. `StaffDAO` CRUD + `UserManagementController` + panel (P2).
9. `DaoConsoleTest` dan uji skenario PRD §17.

## 2. Perubahan Pendukung

### 2.1 Penerjemah SQLException

Tambahkan di `DatabaseConnection`:

```java
public static DatabaseException translate(SQLException cause) {
    String state = cause.getSQLState();
    if ("23505".equals(state)) {
        return new DatabaseException("Data sudah terdaftar (duplikat). Periksa kembali isian Anda.", cause);
    }
    if ("23503".equals(state)) {
        return new DatabaseException("Data masih dipakai transaksi lain sehingga tidak dapat dihapus.", cause);
    }
    return new DatabaseException("Operasi database gagal. Periksa koneksi dan data yang dikirim.", cause);
}
```

Ubah `databaseError` privat menjadi delegasi:

```java
private DatabaseException databaseError(SQLException cause) {
    return translate(cause);
}
```

### 2.2 Aturan koneksi

`DatabaseConnection` memakai satu koneksi bersama. Jangan menutup `Connection`
lewat try-with-resources. Gunakan try-with-resources hanya untuk
`PreparedStatement` dan `ResultSet`.

```java
Connection connection = DatabaseConnection.getInstance().getConnection();
try (PreparedStatement statement = connection.prepareStatement(sql)) {
    // ...
}
```

### 2.3 Build

```bash
rm -rf build/classes
mkdir -p build/classes
javac -encoding UTF-8 -cp "lib/*" -d build/classes $(find src test -name "*.java")
java -cp "build/classes:lib/*" rentgo.model.RentalCostTest
```

## 3. Lapisan DAO

Pola umum: pakai `PreparedStatement`, bungkus `SQLException` dengan
`DatabaseConnection.translate(...)`, `findById` mengembalikan `null` bila tidak
ada, dan operasi multi-tabel memakai transaksi dengan pemulihan `autoCommit`.

### 3.1 VehicleDAO

```java
package rentgo.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

import rentgo.exception.InvalidDataException;
import rentgo.model.Bus;
import rentgo.model.Car;
import rentgo.model.Motorcycle;
import rentgo.model.Vehicle;
import rentgo.model.VehicleStatus;
import rentgo.model.VehicleType;

/** JDBC persistence for vehicles. */
public class VehicleDAO implements CrudDAO<Vehicle> {
    private final DatabaseConnection database = DatabaseConnection.getInstance();

    @Override
    public void add(Vehicle vehicle) {
        String sql = "INSERT INTO vehicle "
                + "(vehicle_type, plate_number, brand, manufacture_year, base_rate, status, "
                + "passenger_capacity, transmission, engine_cc, seat_count) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        Connection connection = database.getConnection();
        try (PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            bind(statement, vehicle);
            statement.executeUpdate();
            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (keys.next()) {
                    vehicle.setId(keys.getInt(1));
                }
            }
        } catch (SQLException e) {
            throw DatabaseConnection.translate(e);
        }
    }

    @Override
    public void update(Vehicle vehicle) {
        String sql = "UPDATE vehicle SET vehicle_type = ?, plate_number = ?, brand = ?, "
                + "manufacture_year = ?, base_rate = ?, status = ?, passenger_capacity = ?, "
                + "transmission = ?, engine_cc = ?, seat_count = ? WHERE id = ?";
        Connection connection = database.getConnection();
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            bind(statement, vehicle);
            statement.setInt(11, vehicle.getId());
            if (statement.executeUpdate() == 0) {
                throw new InvalidDataException("Kendaraan tidak ditemukan");
            }
        } catch (SQLException e) {
            throw DatabaseConnection.translate(e);
        }
    }

    @Override
    public void delete(int id) {
        Vehicle vehicle = findById(id);
        if (vehicle == null) {
            throw new InvalidDataException("Kendaraan tidak ditemukan");
        }
        if (vehicle.getStatus() == VehicleStatus.RENTED) {
            throw new InvalidDataException("Kendaraan sedang disewa dan tidak dapat dihapus");
        }
        Connection connection = database.getConnection();
        try (PreparedStatement statement = connection.prepareStatement("DELETE FROM vehicle WHERE id = ?")) {
            statement.setInt(1, id);
            statement.executeUpdate();
        } catch (SQLException e) {
            throw DatabaseConnection.translate(e);
        }
    }

    @Override
    public Vehicle findById(int id) {
        Connection connection = database.getConnection();
        try (PreparedStatement statement = connection.prepareStatement("SELECT * FROM vehicle WHERE id = ?")) {
            statement.setInt(1, id);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() ? map(resultSet) : null;
            }
        } catch (SQLException e) {
            throw DatabaseConnection.translate(e);
        }
    }

    @Override
    public List<Vehicle> findAll() {
        return query("SELECT * FROM vehicle ORDER BY id");
    }

    public List<Vehicle> search(String keyword, VehicleType type, VehicleStatus status) {
        StringBuilder sql = new StringBuilder("SELECT * FROM vehicle WHERE 1 = 1");
        List<Object> parameters = new ArrayList<>();
        if (keyword != null && !keyword.isBlank()) {
            sql.append(" AND (LOWER(plate_number) LIKE ? OR LOWER(brand) LIKE ?)");
            String pattern = "%" + keyword.trim().toLowerCase() + "%";
            parameters.add(pattern);
            parameters.add(pattern);
        }
        if (type != null) {
            sql.append(" AND vehicle_type = ?");
            parameters.add(type.name());
        }
        if (status != null) {
            sql.append(" AND status = ?");
            parameters.add(status.name());
        }
        sql.append(" ORDER BY id");
        return query(sql.toString(), parameters.toArray());
    }

    public List<Vehicle> findAvailable() {
        return query("SELECT * FROM vehicle WHERE status = 'AVAILABLE' ORDER BY id");
    }

    public void updateStatus(int id, VehicleStatus status) {
        Connection connection = database.getConnection();
        try (PreparedStatement statement = connection.prepareStatement(
                "UPDATE vehicle SET status = ? WHERE id = ?")) {
            statement.setString(1, status.name());
            statement.setInt(2, id);
            if (statement.executeUpdate() == 0) {
                throw new InvalidDataException("Kendaraan tidak ditemukan");
            }
        } catch (SQLException e) {
            throw DatabaseConnection.translate(e);
        }
    }

    private List<Vehicle> query(String sql, Object... parameters) {
        List<Vehicle> vehicles = new ArrayList<>();
        Connection connection = database.getConnection();
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            for (int index = 0; index < parameters.length; index++) {
                statement.setObject(index + 1, parameters[index]);
            }
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    vehicles.add(map(resultSet));
                }
            }
        } catch (SQLException e) {
            throw DatabaseConnection.translate(e);
        }
        return vehicles;
    }

    private void bind(PreparedStatement statement, Vehicle vehicle) throws SQLException {
        statement.setString(1, vehicle.getType().name());
        statement.setString(2, vehicle.getPlateNumber());
        statement.setString(3, vehicle.getBrand());
        statement.setInt(4, vehicle.getManufactureYear());
        statement.setDouble(5, vehicle.getBaseRate());
        statement.setString(6, vehicle.getStatus().name());
        if (vehicle instanceof Car) {
            Car car = (Car) vehicle;
            statement.setInt(7, car.getPassengerCapacity());
            statement.setString(8, car.getTransmission());
            statement.setNull(9, Types.INTEGER);
            statement.setNull(10, Types.INTEGER);
        } else if (vehicle instanceof Motorcycle) {
            Motorcycle motorcycle = (Motorcycle) vehicle;
            statement.setNull(7, Types.INTEGER);
            statement.setNull(8, Types.VARCHAR);
            statement.setInt(9, motorcycle.getEngineCc());
            statement.setNull(10, Types.INTEGER);
        } else if (vehicle instanceof Bus) {
            Bus bus = (Bus) vehicle;
            statement.setNull(7, Types.INTEGER);
            statement.setNull(8, Types.VARCHAR);
            statement.setNull(9, Types.INTEGER);
            statement.setInt(10, bus.getSeatCount());
        } else {
            throw new InvalidDataException("Jenis kendaraan tidak dikenal");
        }
    }

    private Vehicle map(ResultSet resultSet) throws SQLException {
        int id = resultSet.getInt("id");
        String plateNumber = resultSet.getString("plate_number");
        String brand = resultSet.getString("brand");
        int manufactureYear = resultSet.getInt("manufacture_year");
        double baseRate = resultSet.getDouble("base_rate");
        VehicleStatus status = VehicleStatus.fromString(resultSet.getString("status"));
        VehicleType type = VehicleType.fromString(resultSet.getString("vehicle_type"));
        switch (type) {
            case CAR:
                return new Car(id, plateNumber, brand, manufactureYear, baseRate, status,
                        resultSet.getInt("passenger_capacity"), resultSet.getString("transmission"));
            case MOTORCYCLE:
                return new Motorcycle(id, plateNumber, brand, manufactureYear, baseRate, status,
                        resultSet.getInt("engine_cc"));
            case BUS:
                return new Bus(id, plateNumber, brand, manufactureYear, baseRate, status,
                        resultSet.getInt("seat_count"));
            default:
                throw new SQLException("Jenis kendaraan tidak dikenal: " + type);
        }
    }
}
```

### 3.2 StaffDAO

Bagian penting: `ensureDefaultAdmin()` membuat `admin/admin123` hanya bila tabel
`staff` kosong.

```java
public Staff findByUsername(String username) {
    Connection connection = database.getConnection();
    try (PreparedStatement statement = connection.prepareStatement(
            "SELECT * FROM staff WHERE username = ?")) {
        statement.setString(1, username);
        try (ResultSet resultSet = statement.executeQuery()) {
            return resultSet.next() ? map(resultSet) : null;
        }
    } catch (SQLException e) {
        throw DatabaseConnection.translate(e);
    }
}

public void ensureDefaultAdmin() {
    Connection connection = database.getConnection();
    try (PreparedStatement count = connection.prepareStatement("SELECT COUNT(*) FROM staff");
         ResultSet resultSet = count.executeQuery()) {
        resultSet.next();
        if (resultSet.getInt(1) > 0) {
            return;
        }
    } catch (SQLException e) {
        throw DatabaseConnection.translate(e);
    }
    add(new Staff("Administrator", "", "admin",
            PasswordHasher.hash("admin123".toCharArray()), Role.ADMIN));
}
```

Method `add/update/delete/findById/findAll` mengikuti pola yang sama dengan
`VehicleDAO`, memakai kolom `name, phone_number, username, password_hash, role`.

### 3.3 CustomerDAO

Method CRUD mengikuti pola `VehicleDAO`, dengan kolom
`name, phone_number, national_id, address`.

```java
public List<Customer> search(String nameOrNationalId) {
    if (nameOrNationalId == null || nameOrNationalId.isBlank()) {
        return findAll();
    }
    String pattern = "%" + nameOrNationalId.trim().toLowerCase() + "%";
    return query("SELECT * FROM customer WHERE LOWER(name) LIKE ? OR national_id LIKE ? ORDER BY name",
            new Object[] {pattern, pattern});
}

public Map<String, Customer> mapByNationalId() {
    Map<String, Customer> customers = new LinkedHashMap<>();
    for (Customer customer : findAll()) {
        customers.put(customer.getNationalId(), customer);
    }
    return customers;
}
```

### 3.4 RentalDAO

`saveRental` wajib atomik: cek status kendaraan di dalam transaksi, insert
rental, ubah status kendaraan, insert payment, lalu commit.

```java
public void saveRental(Rental rental, Payment payment) {
    Connection connection = database.getConnection();
    boolean originalAutoCommit = true;
    try {
        originalAutoCommit = connection.getAutoCommit();
        connection.setAutoCommit(false);

        VehicleStatus status = readVehicleStatus(connection, rental.getVehicle().getId());
        if (status != VehicleStatus.AVAILABLE) {
            throw new VehicleUnavailableException("Kendaraan tidak tersedia untuk disewa");
        }

        RentalStatus rentalStatus = rental.getStatus() == null ? RentalStatus.ACTIVE : rental.getStatus();
        String rentalSql = "INSERT INTO rental "
                + "(customer_id, vehicle_id, staff_id, start_date, due_date, with_driver, total_cost, status) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement statement = connection.prepareStatement(
                rentalSql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setInt(1, rental.getCustomer().getId());
            statement.setInt(2, rental.getVehicle().getId());
            statement.setInt(3, rental.getStaff().getId());
            statement.setObject(4, rental.getStartDate());
            statement.setObject(5, rental.getDueDate());
            statement.setBoolean(6, rental.isWithDriver());
            statement.setDouble(7, rental.getTotalCost());
            statement.setString(8, rentalStatus.name());
            statement.executeUpdate();
            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (keys.next()) {
                    rental.setId(keys.getInt(1));
                }
            }
        }

        try (PreparedStatement statement = connection.prepareStatement(
                "UPDATE vehicle SET status = 'RENTED' WHERE id = ? AND status = 'AVAILABLE'")) {
            statement.setInt(1, rental.getVehicle().getId());
            if (statement.executeUpdate() != 1) {
                throw new VehicleUnavailableException("Kendaraan baru saja disewa oleh petugas lain");
            }
        }

        if (payment != null) {
            payment.setRental(rental);
            String paymentSql = "INSERT INTO payment (rental_id, method, amount, paid_at, note) "
                    + "VALUES (?, ?, ?, ?, ?)";
            try (PreparedStatement statement = connection.prepareStatement(
                    paymentSql, Statement.RETURN_GENERATED_KEYS)) {
                statement.setInt(1, rental.getId());
                statement.setString(2, payment.getMethod().name());
                statement.setDouble(3, payment.getAmount());
                statement.setObject(4, payment.getPaidAt());
                statement.setString(5, payment.getNote());
                statement.executeUpdate();
                try (ResultSet keys = statement.getGeneratedKeys()) {
                    if (keys.next()) {
                        payment.setId(keys.getInt(1));
                    }
                }
            }
        }

        connection.commit();
    } catch (SQLException e) {
        rollback(connection, e);
        throw DatabaseConnection.translate(e);
    } catch (RuntimeException e) {
        rollback(connection, e);
        throw e;
    } finally {
        restoreAutoCommit(connection, originalAutoCommit);
    }
}

public void cancel(int rentalId) {
    Connection connection = database.getConnection();
    boolean originalAutoCommit = true;
    try {
        originalAutoCommit = connection.getAutoCommit();
        connection.setAutoCommit(false);

        int vehicleId;
        try (PreparedStatement statement = connection.prepareStatement(
                "SELECT vehicle_id FROM rental WHERE id = ? AND status = 'ACTIVE'")) {
            statement.setInt(1, rentalId);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    throw new InvalidDataException("Hanya transaksi aktif yang dapat dibatalkan");
                }
                vehicleId = resultSet.getInt(1);
            }
        }

        try (PreparedStatement statement = connection.prepareStatement(
                "UPDATE rental SET status = 'CANCELLED' WHERE id = ?")) {
            statement.setInt(1, rentalId);
            statement.executeUpdate();
        }
        try (PreparedStatement statement = connection.prepareStatement(
                "UPDATE vehicle SET status = 'AVAILABLE' WHERE id = ?")) {
            statement.setInt(1, vehicleId);
            statement.executeUpdate();
        }

        connection.commit();
    } catch (SQLException e) {
        rollback(connection, e);
        throw DatabaseConnection.translate(e);
    } catch (RuntimeException e) {
        rollback(connection, e);
        throw e;
    } finally {
        restoreAutoCommit(connection, originalAutoCommit);
    }
}

public void update(Rental rental) {
    String sql = "UPDATE rental SET start_date = ?, due_date = ?, with_driver = ?, total_cost = ? "
            + "WHERE id = ? AND status = 'ACTIVE'";
    Connection connection = database.getConnection();
    try (PreparedStatement statement = connection.prepareStatement(sql)) {
        statement.setObject(1, rental.getStartDate());
        statement.setObject(2, rental.getDueDate());
        statement.setBoolean(3, rental.isWithDriver());
        statement.setDouble(4, rental.getTotalCost());
        statement.setInt(5, rental.getId());
        if (statement.executeUpdate() == 0) {
            throw new InvalidDataException("Hanya transaksi aktif yang dapat diubah");
        }
    } catch (SQLException e) {
        throw DatabaseConnection.translate(e);
    }
}

public void add(Rental rental) {
    throw new UnsupportedOperationException("Gunakan saveRental(rental, payment)");
}

public void delete(int id) {
    throw new UnsupportedOperationException("Gunakan cancel(rentalId)");
}

public List<Rental> findActive() {
    return query("SELECT * FROM rental WHERE status = 'ACTIVE' ORDER BY start_date, id");
}

public List<Rental> findHistory() {
    return query("SELECT * FROM rental WHERE status <> 'ACTIVE' ORDER BY start_date DESC, id DESC");
}

private List<Rental> query(String sql) {
    List<Rental> rentals = new ArrayList<>();
    Connection connection = database.getConnection();
    try (PreparedStatement statement = connection.prepareStatement(sql);
         ResultSet resultSet = statement.executeQuery()) {
        while (resultSet.next()) {
            rentals.add(map(resultSet));
        }
    } catch (SQLException e) {
        throw DatabaseConnection.translate(e);
    }
    return rentals;
}

private Rental map(ResultSet resultSet) throws SQLException {
    Customer customer = customerDAO.findById(resultSet.getInt("customer_id"));
    Vehicle vehicle = vehicleDAO.findById(resultSet.getInt("vehicle_id"));
    Staff staff = staffDAO.findById(resultSet.getInt("staff_id"));
    return new Rental(
            resultSet.getInt("id"), customer, vehicle, staff,
            resultSet.getObject("start_date", LocalDate.class),
            resultSet.getObject("due_date", LocalDate.class),
            resultSet.getBoolean("with_driver"),
            resultSet.getDouble("total_cost"),
            RentalStatus.fromString(resultSet.getString("status")));
}

private VehicleStatus readVehicleStatus(Connection connection, int vehicleId) throws SQLException {
    try (PreparedStatement statement = connection.prepareStatement(
            "SELECT status FROM vehicle WHERE id = ?")) {
        statement.setInt(1, vehicleId);
        try (ResultSet resultSet = statement.executeQuery()) {
            if (!resultSet.next()) {
                throw new InvalidDataException("Kendaraan tidak ditemukan");
            }
            return VehicleStatus.fromString(resultSet.getString(1));
        }
    }
}

private void rollback(Connection connection, Throwable failure) {
    try {
        connection.rollback();
    } catch (SQLException rollbackError) {
        failure.addSuppressed(rollbackError);
    }
}

private void restoreAutoCommit(Connection connection, boolean autoCommit) {
    try {
        connection.setAutoCommit(autoCommit);
    } catch (SQLException ignored) {
        // Koneksi sudah tidak dapat dipakai; error utama tetap dilempar.
    }
}
```

Field pendukung di `RentalDAO`:

```java
private final DatabaseConnection database = DatabaseConnection.getInstance();
private final CustomerDAO customerDAO = new CustomerDAO();
private final VehicleDAO vehicleDAO = new VehicleDAO();
private final StaffDAO staffDAO = new StaffDAO();
```

`findById` dan `findAll` memakai `query`/pola yang sama. `map` memuat relasi
lewat DAO lain agar objek `Rental` lengkap.

### 3.5 PaymentDAO

Pola CRUD sama dengan `VehicleDAO`, ditambah query per rental.

```java
public List<Payment> findByRentalId(int rentalId) {
    List<Payment> payments = new ArrayList<>();
    Connection connection = database.getConnection();
    try (PreparedStatement statement = connection.prepareStatement(
            "SELECT * FROM payment WHERE rental_id = ? ORDER BY paid_at")) {
        statement.setInt(1, rentalId);
        try (ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                payments.add(map(resultSet));
            }
        }
    } catch (SQLException e) {
        throw DatabaseConnection.translate(e);
    }
    return payments;
}

private Payment map(ResultSet resultSet) throws SQLException {
    Rental rental = new RentalDAO().findById(resultSet.getInt("rental_id"));
    return new Payment(
            resultSet.getInt("id"), rental,
            PaymentMethod.fromString(resultSet.getString("method")),
            resultSet.getDouble("amount"),
            resultSet.getObject("paid_at", LocalDateTime.class),
            resultSet.getString("note"));
}
```

`add/update/delete/findById/findAll` memakai kolom
`rental_id, method, amount, paid_at, note`.

### 3.6 VehicleReturnDAO

Transaksi pengembalian: insert `vehicle_return`, ubah rental menjadi
`COMPLETED`, ubah kendaraan menjadi `AVAILABLE`.

```java
public void saveReturn(VehicleReturn vehicleReturn) {
    Rental rental = vehicleReturn.getRental();
    if (rental == null || rental.getId() == 0) {
        throw new InvalidDataException("Transaksi sewa wajib dipilih");
    }

    Connection connection = database.getConnection();
    boolean originalAutoCommit = true;
    try {
        originalAutoCommit = connection.getAutoCommit();
        connection.setAutoCommit(false);

        try (PreparedStatement statement = connection.prepareStatement(
                "SELECT status FROM rental WHERE id = ?")) {
            statement.setInt(1, rental.getId());
            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    throw new InvalidDataException("Transaksi sewa tidak ditemukan");
                }
                if (RentalStatus.fromString(resultSet.getString(1)) != RentalStatus.ACTIVE) {
                    throw new InvalidDataException("Hanya transaksi aktif yang dapat dikembalikan");
                }
            }
        }

        String sql = "INSERT INTO vehicle_return "
                + "(rental_id, actual_return_date, vehicle_condition, late_fee) VALUES (?, ?, ?, ?)";
        try (PreparedStatement statement = connection.prepareStatement(
                sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setInt(1, rental.getId());
            statement.setObject(2, vehicleReturn.getActualReturnDate());
            statement.setString(3, vehicleReturn.getCondition().name());
            statement.setDouble(4, vehicleReturn.getLateFee());
            statement.executeUpdate();
            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (keys.next()) {
                    vehicleReturn.setId(keys.getInt(1));
                }
            }
        }

        try (PreparedStatement statement = connection.prepareStatement(
                "UPDATE rental SET status = 'COMPLETED' WHERE id = ?")) {
            statement.setInt(1, rental.getId());
            statement.executeUpdate();
        }
        try (PreparedStatement statement = connection.prepareStatement(
                "UPDATE vehicle SET status = 'AVAILABLE' WHERE id = ?")) {
            statement.setInt(1, rental.getVehicle().getId());
            statement.executeUpdate();
        }

        // P2: pembayaran denda. Hapus blok ini bila fitur belum dikerjakan.
        if (vehicleReturn.getLateFee() > 0) {
            try (PreparedStatement statement = connection.prepareStatement(
                    "INSERT INTO payment (rental_id, method, amount, paid_at, note) "
                    + "VALUES (?, 'CASH', ?, CURRENT_TIMESTAMP, 'LATE_FEE')")) {
                statement.setInt(1, rental.getId());
                statement.setDouble(2, vehicleReturn.getLateFee());
                statement.executeUpdate();
            }
        }

        connection.commit();
    } catch (SQLException e) {
        rollback(connection, e);
        throw DatabaseConnection.translate(e);
    } catch (RuntimeException e) {
        rollback(connection, e);
        throw e;
    } finally {
        restoreAutoCommit(connection, originalAutoCommit);
    }
}
```

`findByRentalId` membaca `vehicle_return` lalu memuat `Rental` lewat
`new RentalDAO().findById(rentalId)`. Helper `rollback` dan `restoreAutoCommit`
sama seperti `RentalDAO`.

### 3.7 ReportDAO

```java
public List<Object[]> revenue(LocalDate from, LocalDate to) {
    StringBuilder sql = new StringBuilder(
            "SELECT p.paid_at::date AS day, COUNT(DISTINCT p.rental_id) AS transaction_count, "
            + "COALESCE(SUM(p.amount), 0) AS total FROM payment p WHERE 1 = 1");
    List<Object> parameters = new ArrayList<>();
    if (from != null) {
        sql.append(" AND p.paid_at::date >= ?");
        parameters.add(from);
    }
    if (to != null) {
        sql.append(" AND p.paid_at::date <= ?");
        parameters.add(to);
    }
    sql.append(" GROUP BY p.paid_at::date ORDER BY p.paid_at::date");

    List<Object[]> rows = new ArrayList<>();
    Connection connection = database.getConnection();
    try (PreparedStatement statement = connection.prepareStatement(sql.toString())) {
        for (int index = 0; index < parameters.size(); index++) {
            statement.setObject(index + 1, parameters.get(index));
        }
        try (ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                rows.add(new Object[] {
                        resultSet.getObject("day", LocalDate.class),
                        resultSet.getLong("transaction_count"),
                        resultSet.getBigDecimal("total").doubleValue()
                });
            }
        }
    } catch (SQLException e) {
        throw DatabaseConnection.translate(e);
    }
    return rows;
}

public List<Object[]> topVehicles(int limit) {
    String sql = "SELECT v.plate_number, v.brand, v.vehicle_type, COUNT(r.id) AS rental_count "
            + "FROM rental r JOIN vehicle v ON v.id = r.vehicle_id "
            + "WHERE r.status <> 'CANCELLED' "
            + "GROUP BY v.plate_number, v.brand, v.vehicle_type "
            + "ORDER BY rental_count DESC, v.plate_number LIMIT ?";
    List<Object[]> rows = new ArrayList<>();
    Connection connection = database.getConnection();
    try (PreparedStatement statement = connection.prepareStatement(sql)) {
        statement.setInt(1, limit);
        try (ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                rows.add(new Object[] {
                        resultSet.getString("plate_number"),
                        resultSet.getString("brand"),
                        VehicleType.fromString(resultSet.getString("vehicle_type")),
                        resultSet.getLong("rental_count")
                });
            }
        }
    } catch (SQLException e) {
        throw DatabaseConnection.translate(e);
    }
    return rows;
}
```

## 4. Controller

### 4.1 LoginController

```java
package rentgo.controller;

import java.awt.Window;
import java.util.Arrays;

import javax.swing.SwingUtilities;

import rentgo.dao.StaffDAO;
import rentgo.exception.DatabaseException;
import rentgo.model.Staff;
import rentgo.util.PasswordHasher;
import rentgo.util.Session;
import rentgo.view.LoginFrame;
import rentgo.view.MainFrame;

/** Handles authentication and logout. */
public class LoginController {
    private final LoginFrame view;
    private final StaffDAO staffDAO;

    public LoginController(LoginFrame view, StaffDAO staffDAO) {
        this.view = view;
        this.staffDAO = staffDAO;
    }

    public void login() {
        char[] password = view.getPassword();
        try {
            String username = view.getUsername();
            if (username.isBlank() || password.length == 0) {
                view.showError("Username dan password wajib diisi");
                return;
            }
            Staff staff = staffDAO.findByUsername(username);
            if (staff == null || !PasswordHasher.verify(password, staff.getPasswordHash())) {
                view.showError("Username atau password salah");
                return;
            }
            Session.setStaff(staff);
            view.dispose();
            new MainFrame().setVisible(true);
        } catch (DatabaseException e) {
            view.showError(e.getMessage());
        } finally {
            Arrays.fill(password, '\0');
        }
    }

    public void cancel() {
        view.dispose();
        System.exit(0);
    }

    public static void logout() {
        Session.clear();
        for (Window window : Window.getWindows()) {
            window.dispose();
        }
        SwingUtilities.invokeLater(() -> {
            LoginFrame frame = new LoginFrame();
            LoginController controller = new LoginController(frame, new StaffDAO());
            frame.setController(controller);
            frame.setVisible(true);
        });
    }
}
```

Pesan error login dibuat sama untuk username tidak ada dan password salah agar
tidak membocorkan keberadaan akun.

### 4.2 VehicleController

```java
package rentgo.controller;

import java.util.ArrayList;
import java.util.List;

import rentgo.dao.VehicleDAO;
import rentgo.exception.DatabaseException;
import rentgo.exception.InvalidDataException;
import rentgo.model.Bus;
import rentgo.model.Car;
import rentgo.model.Motorcycle;
import rentgo.model.Vehicle;
import rentgo.model.VehicleStatus;
import rentgo.model.VehicleType;
import rentgo.util.Validator;
import rentgo.view.VehiclePanel;

/** Coordinates vehicle CRUD. */
public class VehicleController {
    private final VehiclePanel view;
    private final VehicleDAO dao;
    private final List<Vehicle> current = new ArrayList<>();

    public VehicleController(VehiclePanel view, VehicleDAO dao) {
        this.view = view;
        this.dao = dao;
    }

    public void loadData() {
        try {
            current.clear();
            current.addAll(dao.findAll());
            view.setTableData(current);
        } catch (DatabaseException e) {
            view.showError(e.getMessage());
        }
    }

    public void addVehicle() {
        try {
            dao.add(readForm(0));
            view.showInfo("Kendaraan berhasil ditambahkan");
            view.clearForm();
            loadData();
        } catch (InvalidDataException | DatabaseException e) {
            view.showError(e.getMessage());
        }
    }

    public void updateVehicle() {
        Vehicle selected = view.getSelectedVehicle();
        if (selected == null) {
            view.showError("Pilih kendaraan terlebih dahulu");
            return;
        }
        try {
            dao.update(readForm(selected.getId()));
            view.showInfo("Kendaraan berhasil diubah");
            loadData();
        } catch (InvalidDataException | DatabaseException e) {
            view.showError(e.getMessage());
        }
    }

    public void deleteVehicle() {
        Vehicle selected = view.getSelectedVehicle();
        if (selected == null) {
            view.showError("Pilih kendaraan terlebih dahulu");
            return;
        }
        if (!view.confirm("Hapus kendaraan " + selected.getPlateNumber() + "?")) {
            return;
        }
        try {
            dao.delete(selected.getId());
            view.showInfo("Kendaraan berhasil dihapus");
            view.clearForm();
            loadData();
        } catch (InvalidDataException | DatabaseException e) {
            view.showError(e.getMessage());
        }
    }

    public void search() {
        try {
            current.clear();
            current.addAll(dao.search(view.getSearchKeyword(), view.getSearchType(), view.getSearchStatus()));
            view.setTableData(current);
        } catch (DatabaseException e) {
            view.showError(e.getMessage());
        }
    }

    public void clearForm() {
        view.clearForm();
    }

    public void selectRow(int index) {
        if (index >= 0 && index < current.size()) {
            view.setForm(current.get(index));
        }
    }

    private Vehicle readForm(int id) {
        String plateNumber = Validator.requireNotBlank(view.getPlateNumber(), "Plat nomor");
        String brand = Validator.requireNotBlank(view.getBrand(), "Merek");
        int year = Validator.parseInt(view.getManufactureYear(), "Tahun produksi");
        Validator.validateYear(year);
        double baseRate = parseBaseRate(view.getBaseRate());
        VehicleType type = view.getVehicleType();
        VehicleStatus status = view.getVehicleStatus();
        if (type == null) {
            throw new InvalidDataException("Jenis kendaraan wajib dipilih");
        }
        if (status == null) {
            throw new InvalidDataException("Status kendaraan wajib dipilih");
        }
        switch (type) {
            case CAR:
                return new Car(id, plateNumber, brand, year, baseRate, status,
                        Validator.parseInt(view.getPassengerCapacity(), "Kapasitas penumpang"),
                        view.getTransmission());
            case MOTORCYCLE:
                return new Motorcycle(id, plateNumber, brand, year, baseRate, status,
                        Validator.parseInt(view.getEngineCc(), "Kapasitas mesin"));
            case BUS:
                return new Bus(id, plateNumber, brand, year, baseRate, status,
                        Validator.parseInt(view.getSeatCount(), "Jumlah kursi"));
            default:
                throw new InvalidDataException("Jenis kendaraan tidak dikenal");
        }
    }

    private double parseBaseRate(String text) {
        try {
            double value = Double.parseDouble(Validator.requireNotBlank(text, "Tarif dasar")
                    .replace(".", "").replace(",", "."));
            if (value < 0) {
                throw new InvalidDataException("Tarif dasar tidak boleh negatif");
            }
            return value;
        } catch (NumberFormatException e) {
            throw new InvalidDataException("Tarif dasar harus berupa angka", e);
        }
    }
}
```

### 4.3 CustomerController

Pola sama dengan `VehicleController`: simpan daftar `current`, panggil
`dao.findAll/search`, lalu `view.setTableData(current)`. `readForm`:

```java
private Customer readForm(int id) {
    String name = Validator.requireNotBlank(view.getNameField(), "Nama");
    String phoneNumber = view.getPhoneNumber();
    Validator.validatePhone(phoneNumber);
    String nationalId = Validator.requireNotBlank(view.getNationalId(), "NIK");
    Validator.validateNationalId(nationalId);
    String address = Validator.requireNotBlank(view.getAddress(), "Alamat");
    return new Customer(id, name, phoneNumber, nationalId, address);
}
```

Method publik: `loadData`, `addCustomer`, `updateCustomer`, `deleteCustomer`,
`search`, `clearForm`, `selectRow`. Semua menangkap
`InvalidDataException | DatabaseException` dan memanggil `view.showError`.

### 4.4 RentalController

```java
package rentgo.controller;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import rentgo.dao.CustomerDAO;
import rentgo.dao.PaymentDAO;
import rentgo.dao.RentalDAO;
import rentgo.dao.VehicleDAO;
import rentgo.exception.DatabaseException;
import rentgo.exception.InvalidDataException;
import rentgo.exception.VehicleUnavailableException;
import rentgo.model.Customer;
import rentgo.model.Payment;
import rentgo.model.PaymentMethod;
import rentgo.model.Rental;
import rentgo.model.RentalStatus;
import rentgo.model.Staff;
import rentgo.model.Vehicle;
import rentgo.util.CurrencyFormatter;
import rentgo.util.Session;
import rentgo.view.RentalPanel;

/** Coordinates rental creation, update, cancellation, and receipts. */
public class RentalController {
    private final RentalPanel view;
    private final RentalDAO rentalDAO;
    private final CustomerDAO customerDAO;
    private final VehicleDAO vehicleDAO;
    private final PaymentDAO paymentDAO;
    private final List<Rental> current = new ArrayList<>();

    public RentalController(RentalPanel view, RentalDAO rentalDAO, CustomerDAO customerDAO,
                            VehicleDAO vehicleDAO, PaymentDAO paymentDAO) {
        this.view = view;
        this.rentalDAO = rentalDAO;
        this.customerDAO = customerDAO;
        this.vehicleDAO = vehicleDAO;
        this.paymentDAO = paymentDAO;
    }

    public void loadOptions() {
        try {
            view.setCustomerOptions(customerDAO.findAll());
            view.setVehicleOptions(vehicleDAO.findAvailable());
            view.setPaymentMethods(PaymentMethod.values());
            calculateTotal();
        } catch (DatabaseException e) {
            view.showError(e.getMessage());
        }
    }

    public void calculateTotal() {
        try {
            Rental rental = buildRental(0);
            view.setTotalCost(CurrencyFormatter.formatRupiah(rental.calculateTotalCost()));
        } catch (InvalidDataException e) {
            view.setTotalCost("-");
        }
    }

    public void saveRental() {
        try {
            Rental rental = buildRental(0);
            rental.calculateTotalCost();
            PaymentMethod method = view.getPaymentMethod();
            if (method == null) {
                throw new InvalidDataException("Metode pembayaran wajib dipilih");
            }
            Payment payment = new Payment(rental, method, rental.getTotalCost(),
                    LocalDateTime.now(), "RENTAL");
            rentalDAO.saveRental(rental, payment);
            view.showInfo("Transaksi berhasil disimpan");
            view.resetForm();
            loadOptions();
            loadHistory();
        } catch (InvalidDataException | VehicleUnavailableException | DatabaseException e) {
            view.showError(e.getMessage());
        }
    }

    public void updateRental() {
        Rental selected = view.getSelectedRental();
        if (selected == null) {
            view.showError("Pilih transaksi terlebih dahulu");
            return;
        }
        try {
            Rental rental = buildRental(selected.getId());
            rental.calculateTotalCost();
            rentalDAO.update(rental);
            view.showInfo("Transaksi berhasil diubah");
            loadHistory();
        } catch (InvalidDataException | DatabaseException e) {
            view.showError(e.getMessage());
        }
    }

    public void cancelRental() {
        Rental selected = view.getSelectedRental();
        if (selected == null) {
            view.showError("Pilih transaksi terlebih dahulu");
            return;
        }
        if (!view.confirm("Batalkan transaksi #" + selected.getId() + "?")) {
            return;
        }
        try {
            rentalDAO.cancel(selected.getId());
            view.showInfo("Transaksi berhasil dibatalkan");
            loadOptions();
            loadHistory();
        } catch (InvalidDataException | DatabaseException e) {
            view.showError(e.getMessage());
        }
    }

    public void loadHistory() {
        try {
            current.clear();
            current.addAll(rentalDAO.findHistory());
            view.setTableData(current);
        } catch (DatabaseException e) {
            view.showError(e.getMessage());
        }
    }

    public void printReceipt() {
        Rental selected = view.getSelectedRental();
        if (selected == null) {
            view.showError("Pilih transaksi terlebih dahulu");
            return;
        }
        view.showReceipt(selected.printReceipt());
    }

    public void selectRow(int index) {
        // Riwayat bersifat read-only; tidak perlu memuat ke form.
    }

    private Rental buildRental(int id) {
        Customer customer = view.getSelectedCustomer();
        if (customer == null) {
            throw new InvalidDataException("Pelanggan wajib dipilih");
        }
        Vehicle vehicle = view.getSelectedVehicle();
        if (vehicle == null) {
            throw new InvalidDataException("Kendaraan wajib dipilih");
        }
        Staff staff = Session.getStaff();
        if (staff == null) {
            throw new InvalidDataException("Sesi login tidak ditemukan");
        }
        return new Rental(id, customer, vehicle, staff,
                toLocalDate(view.getStartDate()), toLocalDate(view.getDueDate()),
                view.isWithDriver(), 0, RentalStatus.ACTIVE);
    }

    private LocalDate toLocalDate(Date date) {
        if (date == null) {
            throw new InvalidDataException("Tanggal wajib diisi");
        }
        return Instant.ofEpochMilli(date.getTime())
                .atZone(ZoneId.systemDefault())
                .toLocalDate();
    }
}
```

Untuk Bus, `withDriver` tidak perlu dipaksa di controller. `Bus.calculateRent`
sudah selalu menambahkan biaya sopir, dan `Rental.calculateTotalCost` memanggil
method polimorfik tersebut.

### 4.5 VehicleReturnController

```java
public void calculateLateFee() {
    try {
        Rental rental = view.getSelectedRental();
        if (rental == null) {
            view.setLateFeeInfo(0, "-");
            return;
        }
        VehicleReturn vehicleReturn = new VehicleReturn(
                rental, toLocalDate(view.getActualReturnDate()), view.getCondition());
        int lateDays = vehicleReturn.calculateLateDays();
        double lateFee = vehicleReturn.calculateLateFee();
        view.setLateFeeInfo(lateDays, CurrencyFormatter.formatRupiah(lateFee));
    } catch (InvalidDataException e) {
        view.setLateFeeInfo(0, "-");
    }
}

public void saveReturn() {
    try {
        Rental rental = view.getSelectedRental();
        if (rental == null) {
            throw new InvalidDataException("Transaksi aktif wajib dipilih");
        }
        VehicleReturn vehicleReturn = new VehicleReturn(
                rental, toLocalDate(view.getActualReturnDate()), view.getCondition());
        vehicleReturn.calculateLateFee();
        returnDAO.saveReturn(vehicleReturn);
        view.showInfo("Pengembalian kendaraan berhasil dicatat");
        view.resetForm();
        loadActiveRentals();
    } catch (InvalidDataException | DatabaseException e) {
        view.showError(e.getMessage());
    }
}
```

`loadActiveRentals` memanggil `rentalDAO.findActive()` lalu
`view.setRentalOptions(...)`. Konversi tanggal sama dengan `RentalController`.

### 4.6 ReportController

```java
public void loadReport() {
    try {
        LocalDate[] range = range(view.getPeriod());
        view.setRevenueData(dao.revenue(range[0], range[1]));
        view.setTopVehiclesData(dao.topVehicles(5));
    } catch (DatabaseException e) {
        view.showError(e.getMessage());
    }
}

private LocalDate[] range(String period) {
    LocalDate today = LocalDate.now();
    if ("TODAY".equals(period)) {
        return new LocalDate[] {today, today};
    }
    if ("LAST_7".equals(period)) {
        return new LocalDate[] {today.minusDays(6), today};
    }
    if ("THIS_MONTH".equals(period)) {
        return new LocalDate[] {today.withDayOfMonth(1), today};
    }
    if ("THIS_YEAR".equals(period)) {
        return new LocalDate[] {today.withDayOfYear(1), today};
    }
    return new LocalDate[] {null, null};
}
```

### 4.7 UserManagementController (P2)

Pola CRUD sama dengan `CustomerController`, dengan tambahan hashing:

```java
char[] password = view.getPassword();
String hash = password.length == 0
        ? selected.getPasswordHash()
        : PasswordHasher.hash(password);
Staff staff = new Staff(selected.getId(),
        Validator.requireNotBlank(view.getNameField(), "Nama"),
        view.getPhoneNumber(),
        Validator.requireNotBlank(view.getUsernameField(), "Username"),
        hash,
        view.getRole());
```

Untuk akun baru, password wajib diisi (minimal 6 karakter) lalu di-hash.
Menu panel ini hanya ditampilkan untuk Admin.

## 5. API View yang Perlu Ditambahkan

Tambahkan method berikut ke stub panel masing-masing. Method yang sudah ada
(`showError`, `showInfo`, `confirm`, `getSelectedIndex`) tetap dipakai.

| Panel | Getter | Setter / aksi |
|---|---|---|
| `VehiclePanel` | `getPlateNumber`, `getBrand`, `getManufactureYear`, `getBaseRate`, `getVehicleType`, `getVehicleStatus`, `getPassengerCapacity`, `getTransmission`, `getEngineCc`, `getSeatCount`, `getSearchKeyword`, `getSearchType`, `getSearchStatus`, `getSelectedVehicle` | `setController`, `setTableData(List<Vehicle>)`, `setForm(Vehicle)`, `clearForm` |
| `CustomerPanel` | `getNameField`, `getNationalId`, `getPhoneNumber`, `getAddress`, `getSearchKeyword`, `getSelectedCustomer` | `setController`, `setTableData(List<Customer>)`, `setForm(Customer)`, `clearForm` |
| `RentalPanel` | `getSelectedCustomer`, `getSelectedVehicle`, `getStartDate`, `getDueDate`, `isWithDriver`, `getPaymentMethod`, `getSelectedRental` | `setController`, `setCustomerOptions`, `setVehicleOptions`, `setPaymentMethods`, `setTableData(List<Rental>)`, `setTotalCost(String)`, `showReceipt(String)`, `resetForm` |
| `VehicleReturnPanel` | `getSelectedRental`, `getActualReturnDate`, `getCondition` | `setController`, `setRentalOptions(List<Rental>)`, `setLateFeeInfo(int, String)`, `resetForm` |
| `ReportPanel` | `getPeriod` | `setController`, `setRevenueData(List<Object[]>)`, `setTopVehiclesData(List<Object[]>)` |
| `UserManagementPanel` | `getNameField`, `getUsernameField`, `getPhoneNumber`, `getPassword`, `getRole`, `getSelectedStaff` | `setController`, `setTableData(List<Staff>)`, `setForm(Staff)`, `clearForm` |

Konvensi:

- Getter teks mengembalikan `String`; controller mengubah ke angka/tanggal.
- Getter combo mengembalikan objek model/enum.
- Getter tanggal mengembalikan `java.util.Date` dari `JSpinner`.
- Setiap panel wajib punya `setController(...)` dan memasang listener yang
  memanggil method controller.

Contoh `setTableData` dan `showReceipt` untuk `RentalPanel`:

```java
public void setTableData(List<Rental> rentals) {
    tableModel.setRowCount(0);
    for (Rental rental : rentals) {
        tableModel.addRow(new Object[] {
                rental.getId(),
                rental.getCustomer().getName(),
                rental.getVehicle().getPlateNumber(),
                rental.getStartDate(),
                rental.getDueDate(),
                CurrencyFormatter.formatRupiah(rental.getTotalCost()),
                rental.getStatus()
        });
    }
}

public void showReceipt(String text) {
    JTextArea area = new JTextArea(text);
    area.setEditable(false);
    area.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));
    JOptionPane.showMessageDialog(this, new JScrollPane(area),
            "Struk Transaksi", JOptionPane.INFORMATION_MESSAGE);
}
```

## 6. Wiring MainFrame dan Main

### 6.1 MainFrame

Ganti stub `MainFrame` dengan kerangka berikut, lalu sesuaikan tampilan menu.

```java
package rentgo.view;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Dimension;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;

import rentgo.controller.CustomerController;
import rentgo.controller.LoginController;
import rentgo.controller.RentalController;
import rentgo.controller.ReportController;
import rentgo.controller.UserManagementController;
import rentgo.controller.VehicleController;
import rentgo.controller.VehicleReturnController;
import rentgo.dao.CustomerDAO;
import rentgo.dao.PaymentDAO;
import rentgo.dao.RentalDAO;
import rentgo.dao.ReportDAO;
import rentgo.dao.StaffDAO;
import rentgo.dao.VehicleDAO;
import rentgo.dao.VehicleReturnDAO;
import rentgo.model.Staff;
import rentgo.util.Session;

/** Main window with menu, card layout, and status bar. */
public class MainFrame extends JFrame {
    public static final String VEHICLE = "VEHICLE";
    public static final String CUSTOMER = "CUSTOMER";
    public static final String RENTAL = "RENTAL";
    public static final String RETURN = "RETURN";
    public static final String REPORT = "REPORT";
    public static final String USERS = "USERS";

    private final CardLayout cardLayout = new CardLayout();
    private final JPanel cards = new JPanel(cardLayout);
    private final Map<String, Runnable> loaders = new HashMap<>();

    public MainFrame() {
        super("RentGo");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(1000, 650));
        setLayout(new BorderLayout());
        add(cards, BorderLayout.CENTER);
        buildCards();
        buildMenu();
        buildStatusBar();
        pack();
        setLocationRelativeTo(null);
    }

    private void buildCards() {
        VehiclePanel vehiclePanel = new VehiclePanel();
        VehicleController vehicleController = new VehicleController(vehiclePanel, new VehicleDAO());
        vehiclePanel.setController(vehicleController);
        cards.add(vehiclePanel, VEHICLE);
        loaders.put(VEHICLE, vehicleController::loadData);

        CustomerPanel customerPanel = new CustomerPanel();
        CustomerController customerController = new CustomerController(customerPanel, new CustomerDAO());
        customerPanel.setController(customerController);
        cards.add(customerPanel, CUSTOMER);
        loaders.put(CUSTOMER, customerController::loadData);

        RentalPanel rentalPanel = new RentalPanel();
        RentalController rentalController = new RentalController(rentalPanel,
                new RentalDAO(), new CustomerDAO(), new VehicleDAO(), new PaymentDAO());
        rentalPanel.setController(rentalController);
        cards.add(rentalPanel, RENTAL);
        loaders.put(RENTAL, () -> {
            rentalController.loadOptions();
            rentalController.loadHistory();
        });

        VehicleReturnPanel returnPanel = new VehicleReturnPanel();
        VehicleReturnController returnController = new VehicleReturnController(
                returnPanel, new RentalDAO(), new VehicleReturnDAO());
        returnPanel.setController(returnController);
        cards.add(returnPanel, RETURN);
        loaders.put(RETURN, returnController::loadActiveRentals);

        ReportPanel reportPanel = new ReportPanel();
        ReportController reportController = new ReportController(reportPanel, new ReportDAO());
        reportPanel.setController(reportController);
        cards.add(reportPanel, REPORT);
        loaders.put(REPORT, reportController::loadReport);

        Staff staff = Session.getStaff();
        if (staff != null && staff.isAdmin()) {
            UserManagementPanel usersPanel = new UserManagementPanel();
            UserManagementController usersController = new UserManagementController(usersPanel, new StaffDAO());
            usersPanel.setController(usersController);
            cards.add(usersPanel, USERS);
            loaders.put(USERS, usersController::loadData);
        }
    }

    private void buildMenu() {
        JMenuBar menuBar = new JMenuBar();

        JMenu data = new JMenu("Data");
        data.add(menuItem("Kendaraan", VEHICLE));
        data.add(menuItem("Pelanggan", CUSTOMER));
        menuBar.add(data);

        JMenu transaction = new JMenu("Transaksi");
        transaction.add(menuItem("Penyewaan", RENTAL));
        transaction.add(menuItem("Pengembalian", RETURN));
        menuBar.add(transaction);

        menuBar.add(menuItem("Laporan", REPORT));

        JMenu account = new JMenu("Akun");
        Staff staff = Session.getStaff();
        if (staff != null && staff.isAdmin()) {
            account.add(menuItem("Pengguna", USERS));
        }
        JMenuItem logout = new JMenuItem("Logout");
        logout.addActionListener(event -> LoginController.logout());
        account.add(logout);
        menuBar.add(account);

        setJMenuBar(menuBar);
    }

    private JMenuItem menuItem(String label, String card) {
        JMenuItem item = new JMenuItem(label);
        item.addActionListener(event -> openCard(card));
        return item;
    }

    private void openCard(String card) {
        showCard(card);
        Runnable loader = loaders.get(card);
        if (loader != null) {
            loader.run();
        }
    }

    private void buildStatusBar() {
        Staff staff = Session.getStaff();
        String text = staff == null
                ? "Belum masuk"
                : "Masuk sebagai " + staff.getName() + " (" + staff.getRole() + ")";
        add(new JLabel("  " + text), BorderLayout.SOUTH);
    }

    public void showCard(String name) {
        cardLayout.show(cards, name);
    }
}
```

Catatan: untuk mengikuti PRD §7.5 secara ketat, menu "Akun" boleh hanya
ditampilkan untuk Admin, tetapi sediakan jalan logout untuk Staff (misalnya menu
"Berkas" berisi Logout).

### 6.2 Main

```java
package rentgo;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import com.formdev.flatlaf.FlatLightLaf;

import rentgo.controller.LoginController;
import rentgo.dao.DatabaseConnection;
import rentgo.dao.StaffDAO;
import rentgo.exception.DatabaseException;
import rentgo.view.LoginFrame;

/** Starts the RentGo desktop application. */
public final class Main {
    private Main() {
    }

    public static void main(String[] args) {
        FlatLightLaf.setup();
        SwingUtilities.invokeLater(Main::start);
    }

    private static void start() {
        try {
            DatabaseConnection.getInstance().getConnection();
            new StaffDAO().ensureDefaultAdmin();

            LoginFrame frame = new LoginFrame();
            LoginController controller = new LoginController(frame, new StaffDAO());
            frame.setController(controller);
            frame.setVisible(true);
        } catch (DatabaseException e) {
            JOptionPane.showMessageDialog(null, e.getMessage(),
                    "Koneksi Database", JOptionPane.ERROR_MESSAGE);
        }
    }
}
```

## 7. DaoConsoleTest

```java
package rentgo.dao;

import rentgo.model.Staff;

/** Console checks for the DAO layer against the configured database. */
public final class DaoConsoleTest {
    private DaoConsoleTest() {
    }

    public static void main(String[] args) {
        StaffDAO staffDAO = new StaffDAO();
        VehicleDAO vehicleDAO = new VehicleDAO();
        CustomerDAO customerDAO = new CustomerDAO();
        RentalDAO rentalDAO = new RentalDAO();

        staffDAO.ensureDefaultAdmin();
        Staff admin = staffDAO.findByUsername("admin");
        System.out.println("Admin: " + (admin == null ? "tidak ada" : admin.getUsername()));
        System.out.println("Kendaraan: " + vehicleDAO.findAll().size());
        System.out.println("Kendaraan tersedia: " + vehicleDAO.findAvailable().size());
        System.out.println("Pelanggan: " + customerDAO.findAll().size());
        System.out.println("Rental aktif: " + rentalDAO.findActive().size());
        System.out.println("Riwayat rental: " + rentalDAO.findHistory().size());
    }
}
```

## 8. Checklist Akseptasi

- Login benar membuka `MainFrame`; password salah memberi pesan yang sama
  dengan username tidak dikenal.
- CRUD kendaraan dan pelanggan berjalan; hapus memakai konfirmasi.
- Menyewa kendaraan menghasilkan baris `rental` + `payment` dan status
  kendaraan menjadi `RENTED` dalam satu transaksi.
- Menyewa kendaraan `RENTED`/`MAINTENANCE` memunculkan pesan tidak tersedia.
- Pengembalian mengisi `vehicle_return`, status rental menjadi `COMPLETED`,
  kendaraan kembali `AVAILABLE`, denda sesuai `1.5 x tarif dasar x hari telat`.
- Hanya rental `ACTIVE` yang bisa diubah/dibatalkan.
- Pelanggan/kendaraan yang dipakai transaksi tidak bisa dihapus (pesan ramah).
- Staff tidak melihat menu Pengguna dan tombol CRUD kendaraan dinonaktifkan.
- Cetak struk menampilkan teks dari `Rental.printReceipt()`.
- Database mati atau kredensial salah menampilkan dialog, bukan stack trace.

## 9. Jebakan yang Sering Terjadi

- Menutup `Connection` bersama lewat try-with-resources.
- Lupa `connection.setAutoCommit(true)` di `finally`.
- Cek status kendaraan di luar transaksi sehingga rawan double-booking.
- Menaruh aturan tarif/denda di controller atau view (harus di model).
- Memakai `instanceof` untuk menghitung biaya (harus lewat
  `vehicle.calculateRent(...)`).
- Menyimpan password mentah; selalu gunakan `PasswordHasher` di controller
  atau `ensureDefaultAdmin`.
- Menelan `SQLException` tanpa menerjemahkannya menjadi `DatabaseException`.
- Menampilkan pesan login berbeda untuk username tidak ada dan password salah.

## 10. Catatan Branch

Branch `feature/model-oop` berisi hardening model dan perluasan
`RentalCostTest` yang belum di-merge ke `main`. Sebelum mulai integrasi,
gabungkan branch tersebut agar aturan validasi dan struk memakai versi terbaru.
Branch `feature/db-schema` berisi pemisahan seed master dan transaksi. Setelah
keduanya masuk `main`, lanjutkan dengan branch `feature/dao-controller` lalu
`feature/gui-view` sesuai urutan Gate di PRD.
