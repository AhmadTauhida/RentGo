package rentgo.model;

import java.time.Year;
import rentgo.exception.InvalidDataException;

/** Base type for vehicles with polymorphic rental pricing. */
public abstract class Vehicle {
    private int id;
    private String plateNumber;
    private String brand;
    private int manufactureYear;
    private double baseRate;
    private VehicleStatus status;
    protected Vehicle() { }
    protected Vehicle(int id, String plateNumber, String brand, int manufactureYear, double baseRate, VehicleStatus status) {
        setId(id);
        setPlateNumber(plateNumber);
        setBrand(brand);
        setManufactureYear(manufactureYear);
        setBaseRate(baseRate);
        setStatus(status);
    }
    public int getId() { return id; }
    public void setId(int value) { if (value < 0) throw new InvalidDataException("ID tidak valid"); id = value; }
    public String getPlateNumber() { return plateNumber; }
    public void setPlateNumber(String value) { if (value == null || value.isBlank()) throw new InvalidDataException("Plat nomor wajib diisi"); plateNumber = value.trim(); }
    public String getBrand() { return brand; }
    public void setBrand(String value) { if (value == null || value.isBlank()) throw new InvalidDataException("Merek wajib diisi"); brand = value.trim(); }
    public int getManufactureYear() { return manufactureYear; }
    public void setManufactureYear(int value) { if (value < 1980 || value > Year.now().getValue() + 1) throw new InvalidDataException("Tahun produksi tidak valid"); manufactureYear = value; }
    public double getBaseRate() { return baseRate; }
    public void setBaseRate(double value) { if (!Double.isFinite(value) || value < 0) throw new InvalidDataException("Tarif dasar tidak boleh negatif"); baseRate = value; }
    public VehicleStatus getStatus() { return status; }
    public void setStatus(VehicleStatus value) { if (value == null) throw new InvalidDataException("Status wajib dipilih"); status = value; }
    public boolean isAvailable() { return status == VehicleStatus.AVAILABLE; }
    protected void validateDays(int days) { if (days < 1) throw new InvalidDataException("Lama sewa minimal 1 hari"); }
    public abstract double calculateRent(int days);
    public double calculateRent(int days, boolean withDriver) { return calculateRent(days); }
    public abstract VehicleType getType();
    @Override public String toString() { return plateNumber + " - " + brand; }
}