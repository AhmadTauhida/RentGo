package rentgo.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import rentgo.exception.InvalidDataException;
import rentgo.model.Role;
import rentgo.model.Staff;
import rentgo.util.PasswordHasher;

/** JDBC persistence for staff accounts. */
public class StaffDAO implements CrudDAO<Staff> {
    private final DatabaseConnection database = DatabaseConnection.getInstance();

    @Override
    public void add(Staff staff) {
        String sql = "INSERT INTO staff (name, phone_number, username, password_hash, role) "
                + "VALUES (?, ?, ?, ?, ?)";
        Connection connection = database.getConnection();
        try (PreparedStatement statement = connection.prepareStatement(
                sql, Statement.RETURN_GENERATED_KEYS)) {
            bind(statement, staff);
            statement.executeUpdate();
            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (keys.next()) {
                    staff.setId(keys.getInt(1));
                }
            }
        } catch (SQLException e) {
            throw DatabaseConnection.translate(e);
        }
    }

    @Override
    public void update(Staff staff) {
        String sql = "UPDATE staff SET name = ?, phone_number = ?, username = ?, "
                + "password_hash = ?, role = ? WHERE id = ?";
        Connection connection = database.getConnection();
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            bind(statement, staff);
            statement.setInt(6, staff.getId());
            if (statement.executeUpdate() == 0) {
                throw new InvalidDataException("Akun staff tidak ditemukan");
            }
        } catch (SQLException e) {
            throw DatabaseConnection.translate(e);
        }
    }

    @Override
    public void delete(int id) {
        Connection connection = database.getConnection();
        try (PreparedStatement statement = connection.prepareStatement(
                "DELETE FROM staff WHERE id = ?")) {
            statement.setInt(1, id);
            if (statement.executeUpdate() == 0) {
                throw new InvalidDataException("Akun staff tidak ditemukan");
            }
        } catch (SQLException e) {
            throw DatabaseConnection.translate(e);
        }
    }

    @Override
    public Staff findById(int id) {
        Connection connection = database.getConnection();
        try (PreparedStatement statement = connection.prepareStatement(
                "SELECT * FROM staff WHERE id = ?")) {
            statement.setInt(1, id);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() ? map(resultSet) : null;
            }
        } catch (SQLException e) {
            throw DatabaseConnection.translate(e);
        }
    }

    @Override
    public List<Staff> findAll() {
        List<Staff> staffMembers = new ArrayList<>();
        Connection connection = database.getConnection();
        try (PreparedStatement statement = connection.prepareStatement(
                "SELECT * FROM staff ORDER BY id");
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                staffMembers.add(map(resultSet));
            }
        } catch (SQLException e) {
            throw DatabaseConnection.translate(e);
        }
        return staffMembers;
    }

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

    private void bind(PreparedStatement statement, Staff staff) throws SQLException {
        statement.setString(1, staff.getName());
        statement.setString(2, staff.getPhoneNumber());
        statement.setString(3, staff.getUsername());
        statement.setString(4, staff.getPasswordHash());
        statement.setString(5, staff.getRole().name());
    }

    private Staff map(ResultSet resultSet) throws SQLException {
        return new Staff(
                resultSet.getInt("id"),
                resultSet.getString("name"),
                resultSet.getString("phone_number"),
                resultSet.getString("username"),
                resultSet.getString("password_hash"),
                Role.fromString(resultSet.getString("role")));
    }
}
