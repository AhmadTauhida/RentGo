package rentgo.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import rentgo.exception.InvalidDataException;
import rentgo.model.Payment;
import rentgo.model.PaymentMethod;
import rentgo.model.Rental;

/** JDBC persistence for rental payments. */
public class PaymentDAO implements CrudDAO<Payment> {
    private final DatabaseConnection database = DatabaseConnection.getInstance();
    private final RentalDAO rentalDAO = new RentalDAO();

    @Override
    public void add(Payment payment) {
        String sql = "INSERT INTO payment (rental_id, method, amount, paid_at, note) "
                + "VALUES (?, ?, ?, ?, ?)";
        Connection connection = database.getConnection();
        try (PreparedStatement statement = connection.prepareStatement(
                sql, Statement.RETURN_GENERATED_KEYS)) {
            bind(statement, payment);
            statement.executeUpdate();
            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (keys.next()) {
                    payment.setId(keys.getInt(1));
                }
            }
        } catch (SQLException e) {
            throw DatabaseConnection.translate(e);
        }
    }

    @Override
    public void update(Payment payment) {
        String sql = "UPDATE payment SET rental_id = ?, method = ?, amount = ?, "
                + "paid_at = ?, note = ? WHERE id = ?";
        Connection connection = database.getConnection();
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            bind(statement, payment);
            statement.setInt(6, payment.getId());
            if (statement.executeUpdate() == 0) {
                throw new InvalidDataException("Pembayaran tidak ditemukan");
            }
        } catch (SQLException e) {
            throw DatabaseConnection.translate(e);
        }
    }

    @Override
    public void delete(int id) {
        Connection connection = database.getConnection();
        try (PreparedStatement statement = connection.prepareStatement(
                "DELETE FROM payment WHERE id = ?")) {
            statement.setInt(1, id);
            if (statement.executeUpdate() == 0) {
                throw new InvalidDataException("Pembayaran tidak ditemukan");
            }
        } catch (SQLException e) {
            throw DatabaseConnection.translate(e);
        }
    }

    @Override
    public Payment findById(int id) {
        Connection connection = database.getConnection();
        try (PreparedStatement statement = connection.prepareStatement(
                "SELECT * FROM payment WHERE id = ?")) {
            statement.setInt(1, id);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() ? map(resultSet) : null;
            }
        } catch (SQLException e) {
            throw DatabaseConnection.translate(e);
        }
    }

    @Override
    public List<Payment> findAll() {
        List<Payment> payments = new ArrayList<>();
        Connection connection = database.getConnection();
        try (PreparedStatement statement = connection.prepareStatement(
                "SELECT * FROM payment ORDER BY id");
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                payments.add(map(resultSet));
            }
        } catch (SQLException e) {
            throw DatabaseConnection.translate(e);
        }
        return payments;
    }

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

    private void bind(PreparedStatement statement, Payment payment) throws SQLException {
        Rental rental = payment.getRental();
        if (rental == null || rental.getId() == 0) {
            throw new InvalidDataException("Transaksi sewa pembayaran wajib dipilih");
        }
        statement.setInt(1, rental.getId());
        statement.setString(2, payment.getMethod().name());
        statement.setDouble(3, payment.getAmount());
        statement.setObject(4, payment.getPaidAt());
        statement.setString(5, payment.getNote());
    }

    private Payment map(ResultSet resultSet) throws SQLException {
        Rental rental = rentalDAO.findById(resultSet.getInt("rental_id"));
        return new Payment(
                resultSet.getInt("id"), rental,
                PaymentMethod.fromString(resultSet.getString("method")),
                resultSet.getDouble("amount"),
                resultSet.getObject("paid_at", LocalDateTime.class),
                resultSet.getString("note"));
    }
}
