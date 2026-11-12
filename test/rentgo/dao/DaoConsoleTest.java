package rentgo.dao;

import java.sql.SQLException;
import java.time.Year;
import java.util.Objects;
import rentgo.exception.DatabaseException;
import rentgo.exception.InvalidDataException;
import rentgo.model.Role;
import rentgo.model.Staff;
import rentgo.util.CurrencyFormatter;
import rentgo.util.PasswordHasher;
import rentgo.util.Session;
import rentgo.util.Validator;

/** Console checks for DAO and utility layers. */
public final class DaoConsoleTest {
    private static int passed;
    private static int failed;

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
        if (failed > 0) {
            throw new AssertionError(failed + " util test(s) failed");
        }
        System.out.println("DAO checks will be added in the next steps.");
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
        }
    }
}
