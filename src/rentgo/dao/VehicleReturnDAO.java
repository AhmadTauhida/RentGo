package rentgo.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import rentgo.exception.InvalidDataException;
import rentgo.model.Rental;
import rentgo.model.RentalStatus;
import rentgo.model.VehicleCondition;
import rentgo.model.VehicleReturn;

/** JDBC persistence for vehicle returns. */
public class VehicleReturnDAO {
    private final DatabaseConnection database = DatabaseConnection.getInstance();
    private final RentalDAO rentalDAO = new RentalDAO();

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
                        throw new InvalidDataException(
                                "Hanya transaksi aktif yang dapat dikembalikan");
                    }
                }
            }

            String sql = "INSERT INTO vehicle_return "
                    + "(rental_id, actual_return_date, vehicle_condition, late_fee) "
                    + "VALUES (?, ?, ?, ?)";
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

    public VehicleReturn findByRentalId(int rentalId) {
        Connection connection = database.getConnection();
        try (PreparedStatement statement = connection.prepareStatement(
                "SELECT * FROM vehicle_return WHERE rental_id = ?")) {
            statement.setInt(1, rentalId);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() ? map(resultSet) : null;
            }
        } catch (SQLException e) {
            throw DatabaseConnection.translate(e);
        }
    }

    private VehicleReturn map(ResultSet resultSet) throws SQLException {
        Rental rental = rentalDAO.findById(resultSet.getInt("rental_id"));
        return new VehicleReturn(
                resultSet.getInt("id"), rental,
                resultSet.getObject("actual_return_date", LocalDate.class),
                VehicleCondition.fromString(resultSet.getString("vehicle_condition")),
                resultSet.getDouble("late_fee"));
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
