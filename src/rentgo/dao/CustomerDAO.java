package rentgo.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import rentgo.exception.InvalidDataException;
import rentgo.model.Customer;

/** JDBC persistence for customers. */
public class CustomerDAO implements CrudDAO<Customer> {
    private final DatabaseConnection database = DatabaseConnection.getInstance();

    @Override
    public void add(Customer customer) {
        String sql = "INSERT INTO customer (name, phone_number, national_id, address) "
                + "VALUES (?, ?, ?, ?)";
        Connection connection = database.getConnection();
        try (PreparedStatement statement = connection.prepareStatement(
                sql, Statement.RETURN_GENERATED_KEYS)) {
            bind(statement, customer);
            statement.executeUpdate();
            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (keys.next()) {
                    customer.setId(keys.getInt(1));
                }
            }
        } catch (SQLException e) {
            throw DatabaseConnection.translate(e);
        }
    }

    @Override
    public void update(Customer customer) {
        String sql = "UPDATE customer SET name = ?, phone_number = ?, national_id = ?, "
                + "address = ? WHERE id = ?";
        Connection connection = database.getConnection();
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            bind(statement, customer);
            statement.setInt(5, customer.getId());
            if (statement.executeUpdate() == 0) {
                throw new InvalidDataException("Pelanggan tidak ditemukan");
            }
        } catch (SQLException e) {
            throw DatabaseConnection.translate(e);
        }
    }

    @Override
    public void delete(int id) {
        Connection connection = database.getConnection();
        try (PreparedStatement statement = connection.prepareStatement(
                "DELETE FROM customer WHERE id = ?")) {
            statement.setInt(1, id);
            if (statement.executeUpdate() == 0) {
                throw new InvalidDataException("Pelanggan tidak ditemukan");
            }
        } catch (SQLException e) {
            throw DatabaseConnection.translate(e);
        }
    }

    @Override
    public Customer findById(int id) {
        Connection connection = database.getConnection();
        try (PreparedStatement statement = connection.prepareStatement(
                "SELECT * FROM customer WHERE id = ?")) {
            statement.setInt(1, id);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() ? map(resultSet) : null;
            }
        } catch (SQLException e) {
            throw DatabaseConnection.translate(e);
        }
    }

    @Override
    public List<Customer> findAll() {
        return query("SELECT * FROM customer ORDER BY name");
    }

    public List<Customer> search(String nameOrNationalId) {
        if (nameOrNationalId == null || nameOrNationalId.isBlank()) {
            return findAll();
        }
        String pattern = "%" + nameOrNationalId.trim().toLowerCase() + "%";
        return query("SELECT * FROM customer WHERE LOWER(name) LIKE ? OR national_id LIKE ? "
                + "ORDER BY name", pattern, pattern);
    }

    public Map<String, Customer> mapByNationalId() {
        Map<String, Customer> customers = new LinkedHashMap<>();
        for (Customer customer : findAll()) {
            customers.put(customer.getNationalId(), customer);
        }
        return customers;
    }

    private List<Customer> query(String sql, Object... parameters) {
        List<Customer> customers = new ArrayList<>();
        Connection connection = database.getConnection();
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            for (int index = 0; index < parameters.length; index++) {
                statement.setObject(index + 1, parameters[index]);
            }
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    customers.add(map(resultSet));
                }
            }
        } catch (SQLException e) {
            throw DatabaseConnection.translate(e);
        }
        return customers;
    }

    private void bind(PreparedStatement statement, Customer customer) throws SQLException {
        statement.setString(1, customer.getName());
        statement.setString(2, customer.getPhoneNumber());
        statement.setString(3, customer.getNationalId());
        statement.setString(4, customer.getAddress());
    }

    private Customer map(ResultSet resultSet) throws SQLException {
        return new Customer(
                resultSet.getInt("id"),
                resultSet.getString("name"),
                resultSet.getString("phone_number"),
                resultSet.getString("national_id"),
                resultSet.getString("address"));
    }
}
