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
        try (PreparedStatement statement = connection.prepareStatement(
                sql, Statement.RETURN_GENERATED_KEYS)) {
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
        try (PreparedStatement statement = connection.prepareStatement(
                "DELETE FROM vehicle WHERE id = ?")) {
            statement.setInt(1, id);
            statement.executeUpdate();
        } catch (SQLException e) {
            throw DatabaseConnection.translate(e);
        }
    }

    @Override
    public Vehicle findById(int id) {
        Connection connection = database.getConnection();
        try (PreparedStatement statement = connection.prepareStatement(
                "SELECT * FROM vehicle WHERE id = ?")) {
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
                        resultSet.getInt("passenger_capacity"),
                        resultSet.getString("transmission"));
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
