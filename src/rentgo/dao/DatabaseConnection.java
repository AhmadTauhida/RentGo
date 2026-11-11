package rentgo.dao;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;
import rentgo.exception.DatabaseException;

/** Singleton JDBC connection provider for the configured PostgreSQL database. */
public final class DatabaseConnection {
    private static final DatabaseConnection INSTANCE = new DatabaseConnection();
    private Connection connection;
    private DatabaseConnection() { }
    public static DatabaseConnection getInstance() { return INSTANCE; }
    public synchronized Connection getConnection() { try { if (connection == null || connection.isClosed() || !connection.isValid(2)) connection = connectWithRetry(); return connection; } catch (SQLException e) { throw databaseError(e); } }
    public synchronized void close() { if (connection != null) try { connection.close(); } catch (SQLException e) { throw databaseError(e); } finally { connection = null; } }
    private Connection connectWithRetry() { SQLException failure = null; for (int attempt = 0; attempt < 2; attempt++) { try { Class.forName("org.postgresql.Driver"); Properties properties = new Properties(); try (var reader = Files.newBufferedReader(Path.of("config/db.properties"))) { properties.load(reader); } return DriverManager.getConnection(properties.getProperty("db.url"), properties.getProperty("db.user"), properties.getProperty("db.password")); } catch (IOException | ClassNotFoundException | SQLException e) { if (e instanceof SQLException sqlException) failure = sqlException; } } throw databaseError(failure); }
    private DatabaseException databaseError(SQLException cause) { return new DatabaseException("Database tidak dapat diakses. Periksa internet, kredensial/URL di config/db.properties, dan status database Neon.", cause); }
}