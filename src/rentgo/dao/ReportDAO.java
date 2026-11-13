package rentgo.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import rentgo.model.VehicleType;

/** Read-only aggregate queries for reports. */
public class ReportDAO {
    private final DatabaseConnection database = DatabaseConnection.getInstance();

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
}
