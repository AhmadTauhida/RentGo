package rentgo.model;

import rentgo.exception.InvalidDataException;

/** A car with optional driver pricing. */
public class Car extends Vehicle {
    public static final int DISCOUNT_THRESHOLD_DAYS = 7;
    public static final double DISCOUNT_RATE = 0.10;
    public static final double DRIVER_FEE_PER_DAY = 150_000;
    private int passengerCapacity;
    private String transmission;
    public Car() { }
    public Car(String plateNumber, String brand, int manufactureYear, double baseRate, VehicleStatus status, int passengerCapacity, String transmission) { this(0, plateNumber, brand, manufactureYear, baseRate, status, passengerCapacity, transmission); }
    public Car(int id, String plateNumber, String brand, int manufactureYear, double baseRate, VehicleStatus status, int passengerCapacity, String transmission) { super(id, plateNumber, brand, manufactureYear, baseRate, status); setPassengerCapacity(passengerCapacity); setTransmission(transmission); }
    public int getPassengerCapacity() { return passengerCapacity; }
    public void setPassengerCapacity(int value) { if (value < 1) throw new InvalidDataException("Kapasitas penumpang tidak valid"); passengerCapacity = value; }
    public String getTransmission() { return transmission; }
    public void setTransmission(String value) { if (value == null || !(value.equals("MANUAL") || value.equals("AUTOMATIC"))) throw new InvalidDataException("Transmisi tidak valid"); transmission = value; }
    @Override public double calculateRent(int days) { validateDays(days); double total = getBaseRate() * days; return days >= DISCOUNT_THRESHOLD_DAYS ? total * (1 - DISCOUNT_RATE) : total; }
    @Override public double calculateRent(int days, boolean withDriver) { return calculateRent(days) + (withDriver ? DRIVER_FEE_PER_DAY * days : 0); }
    @Override public VehicleType getType() { return VehicleType.CAR; }
}