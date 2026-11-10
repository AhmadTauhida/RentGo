package rentgo.model;

import java.time.LocalDate;

/** Console checks for the pricing and late-fee acceptance scenarios. */
public final class RentalCostTest {
    private RentalCostTest() { }

    public static void main(String[] args) {
        Customer customer = new Customer("Demo Customer", "081234567890", "1234567890123456", "Semarang");
        Staff staff = new Staff("Demo Staff", "081234567891", "tester", "hash", Role.STAFF);
        Car car = new Car("H 1000 AA", "Toyota", 2024, 350_000, VehicleStatus.AVAILABLE, 5, "AUTOMATIC");
        Motorcycle motorcycle = new Motorcycle("H 2000 AA", "Honda", 2024, 100_000, VehicleStatus.AVAILABLE, 150);
        Bus bus = new Bus("H 3000 AA", "Isuzu", 2024, 1_500_000, VehicleStatus.AVAILABLE, 30);

        Rental carSevenDays = rental(customer, car, staff, 7, false);
        check("Car 7 hari", 2_205_000, carSevenDays.calculateTotalCost());
        Rental carWithDriver = rental(customer, car, staff, 3, true);
        check("Car dengan sopir", 1_500_000, carWithDriver.calculateTotalCost());
        Rental motorcycleSevenDays = rental(customer, motorcycle, staff, 7, false);
        check("Motor 7 hari", 595_000, motorcycleSevenDays.calculateTotalCost());
        Rental busTwoDays = rental(customer, bus, staff, 2, false);
        check("Bus 2 hari", 3_600_000, busTwoDays.calculateTotalCost());

        Rental lateRental = new Rental(customer, car, staff, LocalDate.of(2026, 11, 1), LocalDate.of(2026, 11, 3), false);
        VehicleReturn vehicleReturn = new VehicleReturn(lateRental, LocalDate.of(2026, 11, 5), VehicleCondition.GOOD);
        check("Denda terlambat 2 hari", 1_050_000, vehicleReturn.calculateLateFee());
        System.out.println("RentalCostTest passed");
    }

    private static Rental rental(Customer customer, Vehicle vehicle, Staff staff, int days, boolean withDriver) {
        return new Rental(customer, vehicle, staff, LocalDate.of(2026, 11, 1), LocalDate.of(2026, 11, 1).plusDays(days), withDriver);
    }

    private static void check(String name, double expected, double actual) {
        if (Math.abs(expected - actual) > 0.001) throw new AssertionError(name + ": expected " + expected + ", got " + actual);
        System.out.println(name + ": OK");
    }
}
