package rentgo.model;

import rentgo.exception.InvalidDataException;

/** A bus whose rental always includes a driver. */
public class Bus extends Vehicle {
    public static final double DRIVER_FEE_PER_DAY = 300_000;
    private int seatCount;
    public Bus() { }
    public Bus(String plateNumber, String brand, int manufactureYear, double baseRate, VehicleStatus status, int seatCount) { this(0, plateNumber, brand, manufactureYear, baseRate, status, seatCount); }
    public Bus(int id, String plateNumber, String brand, int manufactureYear, double baseRate, VehicleStatus status, int seatCount) { super(id, plateNumber, brand, manufactureYear, baseRate, status); setSeatCount(seatCount); }
    public int getSeatCount() { return seatCount; }
    public void setSeatCount(int value) { if (value < 1) throw new InvalidDataException("Jumlah kursi tidak valid"); seatCount = value; }
    @Override public double calculateRent(int days) { validateDays(days); return getBaseRate() * days + DRIVER_FEE_PER_DAY * days; }
    @Override public double calculateRent(int days, boolean withDriver) { return calculateRent(days); }
    @Override public VehicleType getType() { return VehicleType.BUS; }
}