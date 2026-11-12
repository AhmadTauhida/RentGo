package rentgo.dao;

import java.io.IOException;
import java.io.Reader;
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
    private static final Path CONFIG_PATH = Path.of("config", "db.properties");
    private static final String CONFIG_HINT =
            "Periksa internet, kredensial/URL di config/db.properties, dan status database Neon.";

    private Connection connection;

    private DatabaseConnection() { }

    public static DatabaseConnection getInstance() {
        return INSTANCE;
    }

    public synchronized Connection getConnection() {
        try {
            if (connection == null || connection.isClosed() || !connection.isValid(2)) {
                connection = connectWithRetry();
            }
            return connection;
        } catch (SQLException e) {
            throw translate(e);
        }
    }

    public synchronized void close() {
        if (connection == null) {
            return;
        }
        try {
            connection.close();
        } catch (SQLException e) {
            throw translate(e);
        } finally {
            connection = null;
        }
    }

    /** Translates SQLState violations into friendly Indonesian messages. */
    public static DatabaseException translate(SQLException cause) {
        String state = cause == null ? null : cause.getSQLState();
        if ("23505".equals(state)) {
            return new DatabaseException(
                    "Data sudah terdaftar (duplikat). Periksa kembali isian Anda.", cause);
        }
        if ("23503".equals(state)) {
            return new DatabaseException(
                    "Data masih dipakai transaksi lain sehingga tidak dapat dihapus.", cause);
        }
        return new DatabaseException("Operasi database gagal. " + CONFIG_HINT, cause);
    }

    private Connection connectWithRetry() {
        Properties properties = loadConfig();
        SQLException failure = null;
        for (int attempt = 1; attempt <= 2; attempt++) {
            try {
                Class.forName("org.postgresql.Driver");
                return DriverManager.getConnection(
                        properties.getProperty("db.url"),
                        properties.getProperty("db.user"),
                        properties.getProperty("db.password"));
            } catch (ClassNotFoundException e) {
                throw new DatabaseException(
                        "Driver PostgreSQL tidak ditemukan. Periksa lib/postgresql-42.7.14.jar.", e);
            } catch (SQLException e) {
                failure = e;
            }
        }
        throw new DatabaseException("Database tidak dapat diakses. " + CONFIG_HINT, failure);
    }

    private Properties loadConfig() {
        if (!Files.isReadable(CONFIG_PATH)) {
            throw new DatabaseException(
                    "File config/db.properties tidak ditemukan. Salin dari "
                    + "config/db.properties.example lalu isi kredensial. " + CONFIG_HINT);
        }
        Properties properties = new Properties();
        try (Reader reader = Files.newBufferedReader(CONFIG_PATH)) {
            properties.load(reader);
        } catch (IOException e) {
            throw new DatabaseException(
                    "File config/db.properties tidak dapat dibaca. " + CONFIG_HINT, e);
        }
        for (String key : new String[] {"db.url", "db.user", "db.password"}) {
            String value = properties.getProperty(key);
            if (value == null || value.isBlank()) {
                throw new DatabaseException(
                        "Konfigurasi " + key + " belum diisi di config/db.properties. " + CONFIG_HINT);
            }
        }
        return properties;
    }
}
