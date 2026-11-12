package rentgo.model;

import java.time.LocalDate;
import java.util.List;
import rentgo.exception.InvalidDataException;

/** Console checks for the pricing and late-fee acceptance scenarios. */
public final class RentalCostTest {
    private static int passed;

    private RentalCostTest() { }

    public static void main(String[] args) {
        Customer customer = new Customer("Demo Customer", "081234567890", "1234567890123456", "Semarang");
        Staff staff = new Staff("Demo Staff", "081234567891", "tester", "hash", Role.STAFF);
        Car car = new Car("H 1000 AA", "Toyota", 2024, 350_000, VehicleStatus.AVAILABLE, 5, "AUTOMATIC");
        Motorcycle motorcycle = new Motorcycle("H 2000 AA", "Honda", 2024, 100_000, VehicleStatus.AVAILABLE, 150);
        Bus bus = new Bus("H 3000 AA", "Isuzu", 2024, 1_500_000, VehicleStatus.AVAILABLE, 30);
        LocalDate start = LocalDate.of(2026, 11, 1);

        check("Car 7 hari", 2_205_000, rental(customer, car, staff, start, 7, false).calculateTotalCost());
        check("Car 6 hari tanpa diskon", 2_100_000, rental(customer, car, staff, start, 6, false).calculateTotalCost());
        check("Car 3 hari dengan sopir", 1_500_000, rental(customer, car, staff, start, 3, true).calculateTotalCost());
        check("Motor 7 hari", 595_000, rental(customer, motorcycle, staff, start, 7, false).calculateTotalCost());
        check("Motor 7 hari mengabaikan sopir", 595_000, rental(customer, motorcycle, staff, start, 7, true).calculateTotalCost());
        check("Bus 2 hari tanpa flag sopir", 3_600_000, rental(customer, bus, staff, start, 2, false).calculateTotalCost());
        check("Bus 2 hari dengan flag sopir", 3_600_000, rental(customer, bus, staff, start, 2, true).calculateTotalCost());

        Rental sameDay = new Rental(customer, car, staff, start, start, false);
        check("Tanggal sama = 1 hari", 1, sameDay.getRentalDays());

        Rental lateRental = new Rental(customer, car, staff, start, start.plusDays(2), false);
        check("Denda terlambat 2 hari", 1_050_000,
                new VehicleReturn(lateRental, start.plusDays(4), VehicleCondition.GOOD).calculateLateFee());
        check("Denda tepat waktu", 0,
                new VehicleReturn(lateRental, start.plusDays(2), VehicleCondition.GOOD).calculateLateFee());
        check("Denda kembali lebih awal", 0,
                new VehicleReturn(lateRental, start.plusDays(1), VehicleCondition.GOOD).calculateLateFee());

        checkInvalid("calculateRent(0)", () -> car.calculateRent(0));
        checkInvalid("calculateRent(-1)", () -> motorcycle.calculateRent(-1));
        checkInvalid("setBaseRate(-1)", () -> car.setBaseRate(-1));
        checkInvalid("plat kosong", () -> car.setPlateNumber(" "));
        checkInvalid("tahun 1900", () -> car.setManufactureYear(1900));
        checkInvalid("NIK bukan 16 digit", () -> customer.setNationalId("123"));
        checkInvalid("dueDate sebelum startDate", () -> sameDay.setDueDate(start.minusDays(1)));
        checkInvalid("payment nol", () -> new Payment(lateRental, PaymentMethod.CASH, 0, null, "RENTAL"));
        checkInvalid("username kosong", () -> staff.setUsername(" "));

        List<Vehicle> vehicles = List.of(car, motorcycle, bus);
        for (Vehicle vehicle : vehicles) {
            vehicle.calculateRent(5);
        }
        check("Polimorfisme tanpa cabang tipe", true,
                car.calculateRent(5) != motorcycle.calculateRent(5)
                        && motorcycle.calculateRent(5) != bus.calculateRent(5));

        System.out.println("RentalCostTest passed: " + passed + " checks");
    }

    private static Rental rental(Customer customer, Vehicle vehicle, Staff staff,
                                 LocalDate start, int days, boolean withDriver) {
        return new Rental(customer, vehicle, staff, start, start.plusDays(days), withDriver);
    }

    private static void check(String name, double expected, double actual) {
        if (Math.abs(expected - actual) > 0.001) {
            throw new AssertionError(name + ": expected " + expected + ", got " + actual);
        }
        passed++;
        System.out.println(name + ": OK");
    }

    private static void check(String name, int expected, int actual) {
        if (expected != actual) {
            throw new AssertionError(name + ": expected " + expected + ", got " + actual);
        }
        passed++;
        System.out.println(name + ": OK");
    }

    private static void check(String name, boolean expected, boolean actual) {
        if (expected != actual) {
            throw new AssertionError(name + ": expected " + expected + ", got " + actual);
        }
        passed++;
        System.out.println(name + ": OK");
    }

    private static void checkInvalid(String name, Runnable action) {
        try {
            action.run();
            throw new AssertionError(name + ": expected InvalidDataException");
        } catch (InvalidDataException e) {
            passed++;
            System.out.println(name + ": OK (" + e.getMessage() + ")");
        }
    }
}
