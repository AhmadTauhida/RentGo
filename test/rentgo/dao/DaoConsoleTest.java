package rentgo.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Year;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import rentgo.exception.DatabaseException;
import rentgo.exception.InvalidDataException;
import rentgo.exception.VehicleUnavailableException;
import rentgo.model.Bus;
import rentgo.model.Car;
import rentgo.model.Customer;
import rentgo.model.Motorcycle;
import rentgo.model.Payment;
import rentgo.model.PaymentMethod;
import rentgo.model.Rental;
import rentgo.model.RentalStatus;
import rentgo.model.Role;
import rentgo.model.Staff;
import rentgo.model.Vehicle;
import rentgo.model.VehicleCondition;
import rentgo.model.VehicleReturn;
import rentgo.model.VehicleStatus;
import rentgo.model.VehicleType;
import rentgo.util.CurrencyFormatter;
import rentgo.util.PasswordHasher;
import rentgo.util.Session;
import rentgo.util.Validator;

/** Console checks for DAO and utility layers. */
public final class DaoConsoleTest {
    private static int passed;
    private static int failed;
    private static int sequence;

    private DaoConsoleTest() { }

    public static void main(String[] args) {
        System.out.println("== Util tests ==");
        testCurrencyFormatter();
        testValidator();
        testPasswordHasher();
        testSession();
        System.out.println("== Database error tests ==");
        testDatabaseTranslate();
        System.out.printf("%nUtil result: %d passed, %d failed%n", passed, failed);

        System.out.println("\n== DAO tests ==");
        if (databaseAvailable()) {
            try {
                testStaffDAO();
                testVehicleDAO();
                testCustomerDAO();
                testRentalLifecycle();
            } catch (RuntimeException e) {
                failed++;
                System.out.println("[FAIL] DAO test berhenti: " + e.getMessage());
            }
        }

        System.out.printf("%nTotal result: %d passed, %d failed%n", passed, failed);
        if (failed > 0) {
            throw new AssertionError(failed + " test(s) failed");
        }
    }

    private static void testCurrencyFormatter() {
        check("formatRupiah(350000)", "Rp350.000", CurrencyFormatter.formatRupiah(350_000));
        check("parseRupiah(Rp350.000)", 350_000.0, CurrencyFormatter.parseRupiah("Rp350.000"));
        check("parseRupiah(1.500.000,50)", 1_500_000.5, CurrencyFormatter.parseRupiah("1.500.000,50"));
        expectInvalid("parseRupiah(kosong)", () -> CurrencyFormatter.parseRupiah(" "));
        expectInvalid("parseRupiah(abc)", () -> CurrencyFormatter.parseRupiah("abc"));
    }

    private static void testValidator() {
        check("requireNotBlank", "Budi", Validator.requireNotBlank("  Budi  ", "Nama"));
        expectInvalid("requireNotBlank kosong", () -> Validator.requireNotBlank(" ", "Nama"));
        check("parsePositiveDouble", 350_000.0, Validator.parsePositiveDouble("350000", "Tarif"));
        expectInvalid("parsePositiveDouble nol", () -> Validator.parsePositiveDouble("0", "Tarif"));
        expectInvalid("parsePositiveDouble teks", () -> Validator.parsePositiveDouble("abc", "Tarif"));
        check("parseInt", 7, Validator.parseInt("7", "Lama sewa"));
        expectInvalid("parseInt teks", () -> Validator.parseInt("7hari", "Lama sewa"));
        Validator.validateNationalId("3273010101010001");
        expectInvalid("NIK 15 digit", () -> Validator.validateNationalId("327301010101000"));
        Validator.validatePhone("081234567890");
        expectInvalid("telepon tidak valid", () -> Validator.validatePhone("08-1234"));
        Validator.validatePlate("H 1234 AB");
        expectInvalid("plat kosong", () -> Validator.validatePlate(""));
        Validator.validateYear(Year.now().getValue());
        expectInvalid("tahun 1900", () -> Validator.validateYear(1900));
    }

    private static void testPasswordHasher() {
        char[] password = "admin123".toCharArray();
        String first = PasswordHasher.hash(password);
        String second = PasswordHasher.hash(password);
        check("hash berbeda tiap panggilan", false, first.equals(second));
        check("verify hash pertama", true, PasswordHasher.verify(password, first));
        check("verify password salah", false, PasswordHasher.verify("salah".toCharArray(), first));
        check("verify format rusak", false, PasswordHasher.verify(password, "bukan-hash"));
    }

    private static void testSession() {
        Staff staff = new Staff(1, "Administrator", "081234567890", "admin",
                PasswordHasher.hash("admin123".toCharArray()), Role.ADMIN);
        Session.clear();
        check("Session kosong", null, Session.getStaff());
        Session.setStaff(staff);
        check("Session terisi", staff, Session.getStaff());
        Session.clear();
        check("Session dibersihkan", null, Session.getStaff());
    }

    private static void testDatabaseTranslate() {
        DatabaseException duplicate = DatabaseConnection.translate(new SQLException("dup", "23505"));
        check("translate UNIQUE", true, duplicate.getMessage().contains("sudah terdaftar"));
        check("translate UNIQUE cause", true, duplicate.getCause() instanceof SQLException);

        DatabaseException foreignKey = DatabaseConnection.translate(new SQLException("fk", "23503"));
        check("translate FK", true, foreignKey.getMessage().contains("masih dipakai"));

        DatabaseException other = DatabaseConnection.translate(new SQLException("putus", "08006"));
        check("translate umum", true, other.getMessage().contains("Periksa internet"));
    }

    private static void testStaffDAO() {
        StaffDAO dao = new StaffDAO();
        Staff admin = dao.findByUsername("admin");
        if (admin == null) {
            dao.ensureDefaultAdmin();
            admin = dao.findByUsername("admin");
        }
        check("Staff admin tersedia", true, admin != null);
        if (admin == null) {
            return;
        }
        check("Staff admin role", Role.ADMIN, admin.getRole());
        check("Staff admin password", true,
                PasswordHasher.verify("admin123".toCharArray(), admin.getPasswordHash()));

        int before = dao.findAll().size();
        dao.ensureDefaultAdmin();
        check("ensureDefaultAdmin idempotent", before, dao.findAll().size());

        int id = 0;
        try {
            Staff staff = new Staff("Petugas Uji", "081234567890", uniqueUsername(),
                    PasswordHasher.hash("uji12345".toCharArray()), Role.STAFF);
            dao.add(staff);
            id = staff.getId();
            check("Staff add id", true, id > 0);
            check("Staff findById", staff.getUsername(), dao.findById(id).getUsername());

            staff.setName("Petugas Diubah");
            dao.update(staff);
            check("Staff update", "Petugas Diubah", dao.findById(id).getName());

            dao.delete(id);
            check("Staff delete", null, dao.findById(id));
            id = 0;
        } finally {
            deleteStaffQuietly(id);
        }
    }

    private static void testVehicleDAO() {
        VehicleDAO dao = new VehicleDAO();
        int carId = 0;
        int motorcycleId = 0;
        int busId = 0;
        try {
            Car car = new Car(uniquePlate(), "Toyota Uji", Year.now().getValue(), 350_000,
                    VehicleStatus.AVAILABLE, 7, "AUTOMATIC");
            dao.add(car);
            carId = car.getId();
            check("Vehicle add Car id", true, carId > 0);

            Vehicle loaded = dao.findById(carId);
            check("Vehicle findById Car", VehicleType.CAR, loaded.getType());
            check("Vehicle passengerCapacity", 7, ((Car) loaded).getPassengerCapacity());

            car.setBrand("Toyota Diubah");
            dao.update(car);
            check("Vehicle update", "Toyota Diubah", dao.findById(carId).getBrand());

            check("Vehicle search keyword", true, containsVehicle(dao.search("Diubah", null, null), carId));
            check("Vehicle search type+status", true,
                    containsVehicle(dao.search(null, VehicleType.CAR, VehicleStatus.AVAILABLE), carId));
            check("Vehicle search filter type", false,
                    containsVehicle(dao.search(null, VehicleType.MOTORCYCLE, null), carId));
            check("Vehicle findAvailable", true, containsVehicle(dao.findAvailable(), carId));

            dao.updateStatus(carId, VehicleStatus.MAINTENANCE);
            check("Vehicle updateStatus", VehicleStatus.MAINTENANCE,
                    dao.findById(carId).getStatus());
            dao.updateStatus(carId, VehicleStatus.AVAILABLE);

            Motorcycle motorcycle = new Motorcycle(uniquePlate(), "Yamaha Uji",
                    Year.now().getValue(), 100_000, VehicleStatus.AVAILABLE, 160);
            dao.add(motorcycle);
            motorcycleId = motorcycle.getId();
            check("Vehicle add Motorcycle", VehicleType.MOTORCYCLE,
                    dao.findById(motorcycleId).getType());
            check("Vehicle engineCc", 160,
                    ((Motorcycle) dao.findById(motorcycleId)).getEngineCc());

            Bus bus = new Bus(uniquePlate(), "Isuzu Uji", Year.now().getValue(), 1_500_000,
                    VehicleStatus.AVAILABLE, 30);
            dao.add(bus);
            busId = bus.getId();
            check("Vehicle add Bus", VehicleType.BUS, dao.findById(busId).getType());
            check("Vehicle seatCount", 30, ((Bus) dao.findById(busId)).getSeatCount());

            expectDatabase("Vehicle plat duplikat", "sudah terdaftar",
                    () -> dao.add(new Car(car.getPlateNumber(), "Duplikat",
                            Year.now().getValue(), 100_000, VehicleStatus.AVAILABLE, 5, "MANUAL")));
        } finally {
            deleteVehicleQuietly(busId);
            deleteVehicleQuietly(motorcycleId);
            deleteVehicleQuietly(carId);
        }
    }

    private static void testCustomerDAO() {
        CustomerDAO dao = new CustomerDAO();
        int id = 0;
        try {
            String name = "Pelanggan Uji " + uniqueDigits().substring(10);
            Customer customer = new Customer(name, "081234567891", uniqueNationalId(), "Semarang");
            dao.add(customer);
            id = customer.getId();
            check("Customer add id", true, id > 0);
            check("Customer findById", name, dao.findById(id).getName());
            check("Customer search name", true, containsCustomer(dao.search(name), id));
            check("Customer search NIK", true,
                    containsCustomer(dao.search(customer.getNationalId()), id));

            Map<String, Customer> byNationalId = dao.mapByNationalId();
            check("Customer mapByNationalId", true,
                    byNationalId.containsKey(customer.getNationalId()));

            customer.setAddress("Salatiga");
            dao.update(customer);
            check("Customer update", "Salatiga", dao.findById(id).getAddress());

            dao.delete(id);
            check("Customer delete", null, dao.findById(id));
            id = 0;
        } finally {
            deleteCustomerQuietly(id);
        }
    }

    private static void testRentalLifecycle() {
        CustomerDAO customerDAO = new CustomerDAO();
        VehicleDAO vehicleDAO = new VehicleDAO();
        StaffDAO staffDAO = new StaffDAO();
        RentalDAO rentalDAO = new RentalDAO();
        PaymentDAO paymentDAO = new PaymentDAO();
        VehicleReturnDAO returnDAO = new VehicleReturnDAO();
        ReportDAO reportDAO = new ReportDAO();

        int customerId = 0;
        int activeVehicleId = 0;
        int returnVehicleId = 0;
        int rollbackVehicleId = 0;
        try {
            Staff admin = staffDAO.findByUsername("admin");
            if (admin == null) {
                staffDAO.ensureDefaultAdmin();
                admin = staffDAO.findByUsername("admin");
            }
            check("Rental staff admin tersedia", true, admin != null);
            if (admin == null) {
                return;
            }

            Customer customer = new Customer("Penyewa Uji " + uniqueDigits().substring(10),
                    "081234567892", uniqueNationalId(), "Semarang");
            customerDAO.add(customer);
            customerId = customer.getId();

            Car activeCar = new Car(uniquePlate(), "Avanza Uji", Year.now().getValue(), 350_000,
                    VehicleStatus.AVAILABLE, 7, "AUTOMATIC");
            vehicleDAO.add(activeCar);
            activeVehicleId = activeCar.getId();

            LocalDate today = LocalDate.now();
            Rental rental = new Rental(customer, activeCar, admin, today, today.plusDays(2), false);
            rental.setTotalCost(rental.calculateTotalCost());
            Payment payment = new Payment(rental, PaymentMethod.CASH, rental.getTotalCost(),
                    LocalDateTime.now(), "TEST");
            rentalDAO.saveRental(rental, payment);
            check("Rental save id", true, rental.getId() > 0);
            check("Payment save id", true, payment.getId() > 0);
            check("Rental status kendaraan", VehicleStatus.RENTED,
                    vehicleDAO.findById(activeVehicleId).getStatus());
            check("Payment findByRentalId", 1,
                    paymentDAO.findByRentalId(rental.getId()).size());
            check("Rental findActive", true,
                    containsRental(rentalDAO.findActive(), rental.getId()));

            Rental loaded = rentalDAO.findById(rental.getId());
            check("Rental objek pelanggan", customer.getId(), loaded.getCustomer().getId());
            check("Rental objek kendaraan", activeVehicleId, loaded.getVehicle().getId());
            check("Rental objek staff", admin.getId(), loaded.getStaff().getId());

            Rental updated = rentalDAO.findById(rental.getId());
            updated.setDueDate(today.plusDays(3));
            updated.setTotalCost(updated.calculateTotalCost());
            rentalDAO.update(updated);
            check("Rental update", today.plusDays(3),
                    rentalDAO.findById(rental.getId()).getDueDate());

            check("Report revenue", true,
                    hasRevenue(reportDAO.revenue(today.minusDays(1), today.plusDays(1))));
            check("Report topVehicles", true,
                    containsTopVehicle(reportDAO.topVehicles(50), activeCar.getPlateNumber()));

            int before = rentalDAO.findAll().size();
            Rental duplicate = new Rental(customer, activeCar, admin, today,
                    today.plusDays(1), false);
            duplicate.setTotalCost(duplicate.calculateTotalCost());
            Payment duplicatePayment = new Payment(duplicate, PaymentMethod.CASH,
                    duplicate.getTotalCost(), LocalDateTime.now(), "TEST");
            expectVehicleUnavailable("Rental kendaraan RENTED ditolak",
                    () -> rentalDAO.saveRental(duplicate, duplicatePayment));
            check("Rental gagal tidak menambah baris", before, rentalDAO.findAll().size());
            check("Status tetap RENTED", VehicleStatus.RENTED,
                    vehicleDAO.findById(activeVehicleId).getStatus());

            rentalDAO.cancel(rental.getId());
            check("Rental cancel status", RentalStatus.CANCELLED,
                    rentalDAO.findById(rental.getId()).getStatus());
            check("Rental cancel kendaraan", VehicleStatus.AVAILABLE,
                    vehicleDAO.findById(activeVehicleId).getStatus());
            check("Rental findHistory", true,
                    containsRental(rentalDAO.findHistory(), rental.getId()));
            expectInvalid("Rental update setelah cancel", () -> {
                Rental cancelled = rentalDAO.findById(rental.getId());
                cancelled.setDueDate(cancelled.getDueDate().plusDays(1));
                rentalDAO.update(cancelled);
            });
            expectInvalid("Rental cancel kedua", () -> rentalDAO.cancel(rental.getId()));

            int customerFkId = customerId;
            int activeVehicleFkId = activeVehicleId;
            expectDatabase("Customer FK ditolak", "masih dipakai",
                    () -> customerDAO.delete(customerFkId));
            expectDatabase("Vehicle FK ditolak", "masih dipakai",
                    () -> vehicleDAO.delete(activeVehicleFkId));

            Car returnCar = new Car(uniquePlate(), "Brio Uji", Year.now().getValue(), 350_000,
                    VehicleStatus.AVAILABLE, 5, "MANUAL");
            vehicleDAO.add(returnCar);
            returnVehicleId = returnCar.getId();
            Rental returnRental = new Rental(customer, returnCar, admin, today,
                    today.plusDays(2), false);
            returnRental.setTotalCost(returnRental.calculateTotalCost());
            rentalDAO.saveRental(returnRental, new Payment(returnRental, PaymentMethod.TRANSFER,
                    returnRental.getTotalCost(), LocalDateTime.now(), "TEST"));

            VehicleReturn vehicleReturn = new VehicleReturn(returnRental,
                    today.plusDays(4), VehicleCondition.GOOD);
            vehicleReturn.setLateFee(vehicleReturn.calculateLateFee());
            check("Denda terlambat 2 hari", 1_050_000.0, vehicleReturn.getLateFee());
            returnDAO.saveReturn(vehicleReturn);
            check("Return save id", true, vehicleReturn.getId() > 0);
            check("Return rental COMPLETED", RentalStatus.COMPLETED,
                    rentalDAO.findById(returnRental.getId()).getStatus());
            check("Return kendaraan AVAILABLE", VehicleStatus.AVAILABLE,
                    vehicleDAO.findById(returnVehicleId).getStatus());

            VehicleReturn loadedReturn = returnDAO.findByRentalId(returnRental.getId());
            check("Return findByRentalId", returnRental.getId(),
                    loadedReturn.getRental().getId());
            check("Return lateFee tersimpan", 1_050_000.0, loadedReturn.getLateFee());
            expectInvalid("Return kedua ditolak", () -> {
                VehicleReturn again = new VehicleReturn(
                        rentalDAO.findById(returnRental.getId()),
                        today.plusDays(5), VehicleCondition.GOOD);
                returnDAO.saveReturn(again);
            });

            Car rollbackCar = new Car(uniquePlate(), "Ertiga Uji", Year.now().getValue(), 400_000,
                    VehicleStatus.AVAILABLE, 7, "MANUAL");
            vehicleDAO.add(rollbackCar);
            rollbackVehicleId = rollbackCar.getId();
            Rental badRental = new Rental(customer, rollbackCar, admin, today,
                    today.plusDays(1), false);
            badRental.setTotalCost(badRental.calculateTotalCost());
            Payment badPayment = new Payment(badRental, PaymentMethod.CASH,
                    badRental.getTotalCost(), LocalDateTime.now(), "X".repeat(120));
            expectDatabase("Rental rollback saat payment gagal", null,
                    () -> rentalDAO.saveRental(badRental, badPayment));
            check("Rollback tidak menyimpan rental", 0, countRentals(rollbackVehicleId));
            check("Rollback tidak menyimpan payment", 0,
                    countPaymentsForVehicle(rollbackVehicleId));
            check("Rollback kendaraan AVAILABLE", VehicleStatus.AVAILABLE,
                    vehicleDAO.findById(rollbackVehicleId).getStatus());
        } finally {
            deleteRentalsQuietly(activeVehicleId);
            deleteRentalsQuietly(returnVehicleId);
            deleteRentalsQuietly(rollbackVehicleId);
            deleteVehicleQuietly(activeVehicleId);
            deleteVehicleQuietly(returnVehicleId);
            deleteVehicleQuietly(rollbackVehicleId);
            deleteCustomerQuietly(customerId);
        }
    }

    private static boolean databaseAvailable() {
        try {
            DatabaseConnection.getInstance().getConnection();
            return true;
        } catch (DatabaseException e) {
            failed++;
            System.out.println("[FAIL] database tidak tersedia -> " + e.getMessage());
            return false;
        }
    }

    private static String uniqueDigits() {
        long value = (System.currentTimeMillis() % 100_000_000_000_000L) + (++sequence);
        return String.format("%014d", value);
    }

    private static String uniqueNationalId() {
        return "99" + uniqueDigits();
    }

    private static String uniquePlate() {
        return "T" + uniqueDigits().substring(5);
    }

    private static String uniqueUsername() {
        return "uji" + uniqueDigits().substring(6);
    }

    private static boolean containsVehicle(List<Vehicle> vehicles, int id) {
        for (Vehicle vehicle : vehicles) {
            if (vehicle.getId() == id) {
                return true;
            }
        }
        return false;
    }

    private static boolean containsCustomer(List<Customer> customers, int id) {
        for (Customer customer : customers) {
            if (customer.getId() == id) {
                return true;
            }
        }
        return false;
    }

    private static boolean containsRental(List<Rental> rentals, int id) {
        for (Rental rental : rentals) {
            if (rental.getId() == id) {
                return true;
            }
        }
        return false;
    }

    private static boolean containsTopVehicle(List<Object[]> rows, String plateNumber) {
        for (Object[] row : rows) {
            if (plateNumber.equals(row[0])) {
                return true;
            }
        }
        return false;
    }

    private static boolean hasRevenue(List<Object[]> rows) {
        for (Object[] row : rows) {
            if (((Number) row[1]).longValue() > 0 && ((Number) row[2]).doubleValue() > 0) {
                return true;
            }
        }
        return false;
    }

    private static int countRentals(int vehicleId) {
        return count("SELECT COUNT(*) FROM rental WHERE vehicle_id = ?", vehicleId);
    }

    private static int countPaymentsForVehicle(int vehicleId) {
        return count("SELECT COUNT(*) FROM payment WHERE rental_id IN "
                + "(SELECT id FROM rental WHERE vehicle_id = ?)", vehicleId);
    }

    private static int count(String sql, int id) {
        try (PreparedStatement statement = DatabaseConnection.getInstance()
                .getConnection().prepareStatement(sql)) {
            statement.setInt(1, id);
            try (ResultSet resultSet = statement.executeQuery()) {
                resultSet.next();
                return resultSet.getInt(1);
            }
        } catch (SQLException e) {
            throw DatabaseConnection.translate(e);
        }
    }

    private static void deleteRentalsQuietly(int vehicleId) {
        if (vehicleId <= 0) {
            return;
        }
        try {
            Connection connection = DatabaseConnection.getInstance().getConnection();
            deleteByVehicleId(connection,
                    "DELETE FROM payment WHERE rental_id IN "
                    + "(SELECT id FROM rental WHERE vehicle_id = ?)", vehicleId);
            deleteByVehicleId(connection,
                    "DELETE FROM vehicle_return WHERE rental_id IN "
                    + "(SELECT id FROM rental WHERE vehicle_id = ?)", vehicleId);
            deleteByVehicleId(connection, "DELETE FROM rental WHERE vehicle_id = ?", vehicleId);
        } catch (SQLException | DatabaseException e) {
            System.out.println("[WARN] cleanup rental kendaraan " + vehicleId
                    + ": " + e.getMessage());
        }
    }

    private static void deleteVehicleQuietly(int id) {
        deleteQuietly("vehicle", id);
    }

    private static void deleteCustomerQuietly(int id) {
        deleteQuietly("customer", id);
    }

    private static void deleteStaffQuietly(int id) {
        deleteQuietly("staff", id);
    }

    private static void deleteQuietly(String table, int id) {
        if (id <= 0) {
            return;
        }
        try (PreparedStatement statement = DatabaseConnection.getInstance()
                .getConnection().prepareStatement("DELETE FROM " + table + " WHERE id = ?")) {
            statement.setInt(1, id);
            statement.executeUpdate();
        } catch (SQLException | DatabaseException e) {
            System.out.println("[WARN] cleanup " + table + " " + id + ": " + e.getMessage());
        }
    }

    private static void deleteByVehicleId(Connection connection, String sql, int vehicleId)
            throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, vehicleId);
            statement.executeUpdate();
        }
    }

    private static void check(String label, Object expected, Object actual) {
        if (Objects.equals(expected, actual)) {
            passed++;
            System.out.println("[PASS] " + label);
        } else {
            failed++;
            System.out.println("[FAIL] " + label + " (expected=" + expected + ", actual=" + actual + ")");
        }
    }

    private static void expectInvalid(String label, Runnable action) {
        try {
            action.run();
            failed++;
            System.out.println("[FAIL] " + label + " (tidak melempar InvalidDataException)");
        } catch (InvalidDataException e) {
            passed++;
            System.out.println("[PASS] " + label + " -> " + e.getMessage());
        } catch (RuntimeException e) {
            failed++;
            System.out.println("[FAIL] " + label + " (exception lain: "
                    + e.getClass().getSimpleName() + ": " + e.getMessage() + ")");
        }
    }

    private static void expectDatabase(String label, String messagePart, Runnable action) {
        try {
            action.run();
            failed++;
            System.out.println("[FAIL] " + label + " (tidak melempar DatabaseException)");
        } catch (DatabaseException e) {
            boolean matches = messagePart == null || e.getMessage().contains(messagePart);
            if (matches) {
                passed++;
                System.out.println("[PASS] " + label + " -> " + e.getMessage());
            } else {
                failed++;
                System.out.println("[FAIL] " + label + " (pesan tidak cocok: "
                        + e.getMessage() + ")");
            }
        } catch (RuntimeException e) {
            failed++;
            System.out.println("[FAIL] " + label + " (exception lain: "
                    + e.getClass().getSimpleName() + ": " + e.getMessage() + ")");
        }
    }

    private static void expectVehicleUnavailable(String label, Runnable action) {
        try {
            action.run();
            failed++;
            System.out.println("[FAIL] " + label + " (tidak melempar VehicleUnavailableException)");
        } catch (VehicleUnavailableException e) {
            passed++;
            System.out.println("[PASS] " + label + " -> " + e.getMessage());
        } catch (RuntimeException e) {
            failed++;
            System.out.println("[FAIL] " + label + " (exception lain: "
                    + e.getClass().getSimpleName() + ": " + e.getMessage() + ")");
        }
    }
}
