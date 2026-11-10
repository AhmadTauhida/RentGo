package rentgo.model;

import rentgo.exception.InvalidDataException;

/** A motorcycle with long-rental discount pricing. */
public class Motorcycle extends Vehicle {
    public static final int DISCOUNT_THRESHOLD_DAYS = 7;
    public static final double DISCOUNT_RATE = 0.15;
    private int engineCc;
    public Motorcycle() { }
    public Motorcycle(String plateNumber, String brand, int manufactureYear, double baseRate, VehicleStatus status, int engineCc) { this(0, plateNumber, brand, manufactureYear, baseRate, status, engineCc); }
    public Motorcycle(int id, String plateNumber, String brand, int manufactureYear, double baseRate, VehicleStatus status, int engineCc) { super(id, plateNumber, brand, manufactureYear, baseRate, status); setEngineCc(engineCc); }
    public int getEngineCc() { return engineCc; }
    public void setEngineCc(int value) { if (value < 1) throw new InvalidDataException("Kapasitas mesin tidak valid"); engineCc = value; }
    @Override public double calculateRent(int days) { validateDays(days); double total = getBaseRate() * days; return days >= DISCOUNT_THRESHOLD_DAYS ? total * (1 - DISCOUNT_RATE) : total; }
    @Override public VehicleType getType() { return VehicleType.MOTORCYCLE; }
}