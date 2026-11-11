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

        check("Car 350.000, 7 hari, tanpa sopir", 2_205_000, car.calculateRent(7));
        check("Car 350.000, 3 hari, dengan sopir", 1_500_000, car.calculateRent(3, true));
        check("Car 6 hari tanpa diskon", 2_100_000, car.calculateRent(6));
        check("Car 7 hari diskon 10%", 2_205_000, car.calculateRent(7));
        check("Motorcycle 100.000, 7 hari", 595_000, motorcycle.calculateRent(7));
        check("Bus 1.500.000, 2 hari (sopir wajib)", 3_600_000, bus.calculateRent(2, false));

        check("Rental Car 7 hari", 2_205_000, rental(customer, car, staff, start, 7, false).calculateTotalCost());
        check("Rental Car 3 hari + sopir", 1_500_000, rental(customer, car, staff, start, 3, true).calculateTotalCost());
        check("Rental Motorcycle 7 hari", 595_000, rental(customer, motorcycle, staff, start, 7, false).calculateTotalCost());
        check("Rental Bus 2 hari", 3_600_000, rental(customer, bus, staff, start, 2, false).calculateTotalCost());

        expectInvalid("calculateRent(0)", () -> car.calculateRent(0));
        expectInvalid("calculateRent(-1)", () -> car.calculateRent(-1));
        expectInvalid("setBaseRate(-1)", () -> car.setBaseRate(-1));
        expectInvalid("plat kosong", () -> car.setPlateNumber(" "));
        expectInvalid("tahun 1900", () -> car.setManufactureYear(1900));
        expectInvalid("NIK bukan 16 digit", () -> customer.setNationalId("123"));
        expectInvalid("dueDate sebelum startDate", () -> rental(customer, car, staff, start, -2, false));

        Rental sameDay = new Rental(customer, car, staff, start, start, false);
        check("startDate == dueDate => 1 hari", 1, sameDay.getRentalDays());

        Rental lateRental = rental(customer, car, staff, start, 2, false);
        VehicleReturn lateReturn = new VehicleReturn(lateRental, start.plusDays(4), VehicleCondition.GOOD);
        check("Denda Car 350.000 terlambat 2 hari", 1_050_000, lateReturn.calculateLateFee());

        VehicleReturn onTime = new VehicleReturn(lateRental, lateRental.getDueDate(), VehicleCondition.GOOD);
        check("Denda tepat waktu", 0, onTime.calculateLateFee());
        VehicleReturn early = new VehicleReturn(lateRental, lateRental.getDueDate().minusDays(1), VehicleCondition.GOOD);
        check("Denda lebih awal", 0, early.calculateLateFee());

        List<Vehicle> vehicles = List.of(car, motorcycle, bus);
        double[] expected = {1_750_000, 500_000, 9_000_000};
        for (int i = 0; i < vehicles.size(); i++) {
            check("Polimorfik " + vehicles.get(i).getType(), expected[i], vehicles.get(i).calculateRent(5));
        }

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

    private static void expectInvalid(String name, Runnable action) {
        try {
            action.run();
            throw new AssertionError(name + ": expected InvalidDataException");
        } catch (InvalidDataException e) {
            passed++;
            System.out.println(name + ": OK (" + e.getMessage() + ")");
        }
    }
}
