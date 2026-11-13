package rentgo.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import rentgo.exception.InvalidDataException;
import rentgo.exception.VehicleUnavailableException;
import rentgo.model.Bus;
import rentgo.model.Car;
import rentgo.model.Customer;
import rentgo.model.Motorcycle;
import rentgo.model.Payment;
import rentgo.model.Rental;
import rentgo.model.RentalStatus;
import rentgo.model.Role;
import rentgo.model.Staff;
import rentgo.model.Vehicle;
import rentgo.model.VehicleStatus;
import rentgo.model.VehicleType;

/** JDBC persistence for rental transactions. */
public class RentalDAO implements CrudDAO<Rental> {
    private static final String RENTAL_SELECT =
            "SELECT r.id AS rental_id, r.start_date, r.due_date, r.with_driver, "
            + "r.total_cost, r.status AS rental_status, "
            + "c.id AS customer_id, c.name AS customer_name, "
            + "c.phone_number AS customer_phone, c.national_id, c.address, "
            + "v.id AS vehicle_id, v.vehicle_type, v.plate_number, v.brand, "
            + "v.manufacture_year, v.base_rate, v.status AS vehicle_status, "
            + "v.passenger_capacity, v.transmission, v.engine_cc, v.seat_count, "
            + "s.id AS staff_id, s.name AS staff_name, s.phone_number AS staff_phone, "
            + "s.username, s.password_hash, s.role "
            + "FROM rental r "
            + "JOIN customer c ON c.id = r.customer_id "
            + "JOIN vehicle v ON v.id = r.vehicle_id "
            + "JOIN staff s ON s.id = r.staff_id";

    private final DatabaseConnection database = DatabaseConnection.getInstance();

    @Override
    public void add(Rental rental) {
        throw new UnsupportedOperationException("Gunakan saveRental(rental, payment)");
    }

    @Override
    public void update(Rental rental) {
        String sql = "UPDATE rental SET start_date = ?, due_date = ?, with_driver = ?, "
                + "total_cost = ? WHERE id = ? AND status = 'ACTIVE'";
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

    @Override
    public void delete(int id) {
        throw new UnsupportedOperationException("Gunakan cancel(rentalId)");
    }

    @Override
    public Rental findById(int id) {
        List<Rental> rentals = query(RENTAL_SELECT + " WHERE r.id = ?", id);
        return rentals.isEmpty() ? null : rentals.get(0);
    }

    @Override
    public List<Rental> findAll() {
        return query(RENTAL_SELECT + " ORDER BY r.id");
    }

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

            RentalStatus rentalStatus = rental.getStatus() == null
                    ? RentalStatus.ACTIVE : rental.getStatus();
            String rentalSql = "INSERT INTO rental "
                    + "(customer_id, vehicle_id, staff_id, start_date, due_date, with_driver, "
                    + "total_cost, status) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
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
                    throw new VehicleUnavailableException(
                            "Kendaraan baru saja disewa oleh petugas lain");
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
                        throw new InvalidDataException(
                                "Hanya transaksi aktif yang dapat dibatalkan");
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

    public List<Rental> findActive() {
        return query(RENTAL_SELECT + " WHERE r.status = 'ACTIVE' "
                + "ORDER BY r.start_date, r.id");
    }

    public List<Rental> findHistory() {
        return query(RENTAL_SELECT + " WHERE r.status <> 'ACTIVE' "
                + "ORDER BY r.start_date DESC, r.id DESC");
    }

    private List<Rental> query(String sql, Object... parameters) {
        List<Rental> rentals = new ArrayList<>();
        Connection connection = database.getConnection();
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            for (int index = 0; index < parameters.length; index++) {
                statement.setObject(index + 1, parameters[index]);
            }
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    rentals.add(map(resultSet));
                }
            }
        } catch (SQLException e) {
            throw DatabaseConnection.translate(e);
        }
        return rentals;
    }

    private Rental map(ResultSet resultSet) throws SQLException {
        Customer customer = new Customer(
                resultSet.getInt("customer_id"),
                resultSet.getString("customer_name"),
                resultSet.getString("customer_phone"),
                resultSet.getString("national_id"),
                resultSet.getString("address"));
        Staff staff = new Staff(
                resultSet.getInt("staff_id"),
                resultSet.getString("staff_name"),
                resultSet.getString("staff_phone"),
                resultSet.getString("username"),
                resultSet.getString("password_hash"),
                Role.fromString(resultSet.getString("role")));
        Vehicle vehicle = mapVehicle(resultSet);
        return new Rental(
                resultSet.getInt("rental_id"), customer, vehicle, staff,
                resultSet.getObject("start_date", LocalDate.class),
                resultSet.getObject("due_date", LocalDate.class),
                resultSet.getBoolean("with_driver"),
                resultSet.getDouble("total_cost"),
                RentalStatus.fromString(resultSet.getString("rental_status")));
    }

    private Vehicle mapVehicle(ResultSet resultSet) throws SQLException {
        int id = resultSet.getInt("vehicle_id");
        String plateNumber = resultSet.getString("plate_number");
        String brand = resultSet.getString("brand");
        int manufactureYear = resultSet.getInt("manufacture_year");
        double baseRate = resultSet.getDouble("base_rate");
        VehicleStatus status = VehicleStatus.fromString(resultSet.getString("vehicle_status"));
        switch (VehicleType.fromString(resultSet.getString("vehicle_type"))) {
            case CAR:
                return new Car(id, plateNumber, brand, manufactureYear, baseRate, status,
                        resultSet.getInt("passenger_capacity"),
                        resultSet.getString("transmission"));
            case MOTORCYCLE:
                return new Motorcycle(id, plateNumber, brand, manufactureYear, baseRate, status,
                        resultSet.getInt("engine_cc"));
            case BUS:
                return new Bus(id, plateNumber, brand, manufactureYear, baseRate, status,
                        resultSet.getInt("seat_count"));
            default:
                throw new SQLException("Jenis kendaraan tidak dikenal");
        }
    }

    private VehicleStatus readVehicleStatus(Connection connection, int vehicleId)
            throws SQLException {
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
            // The connection is unusable; the primary failure is rethrown by the caller.
        }
    }
}
