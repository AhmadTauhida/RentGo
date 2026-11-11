package rentgo.model;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import rentgo.exception.InvalidDataException;

/** Records the return of a rented vehicle and any late fee. */
public class VehicleReturn implements PrintableReceipt {
    public static final double LATE_FEE_MULTIPLIER = 1.5;
    private int id;
    private Rental rental;
    private LocalDate actualReturnDate;
    private VehicleCondition condition;
    private double lateFee;
    public VehicleReturn() { }
    public VehicleReturn(Rental rental, LocalDate actualReturnDate, VehicleCondition condition) { this(0, rental, actualReturnDate, condition, 0); }
    public VehicleReturn(int id, Rental rental, LocalDate actualReturnDate, VehicleCondition condition, double lateFee) { setId(id); setRental(rental); setActualReturnDate(actualReturnDate); setCondition(condition); setLateFee(lateFee); }
    public int getId() { return id; }
    public void setId(int value) { if (value < 0) throw new InvalidDataException("ID tidak valid"); id = value; }
    public Rental getRental() { return rental; }
    public void setRental(Rental value) { if (value == null) throw new InvalidDataException("Rental wajib dipilih"); rental = value; }
    public LocalDate getActualReturnDate() { return actualReturnDate; }
    public void setActualReturnDate(LocalDate value) { if (value == null) throw new InvalidDataException("Tanggal pengembalian wajib diisi"); actualReturnDate = value; }
    public VehicleCondition getCondition() { return condition; }
    public void setCondition(VehicleCondition value) { if (value == null) throw new InvalidDataException("Kondisi kendaraan wajib dipilih"); condition = value; }
    public double getLateFee() { return lateFee; }
    public void setLateFee(double value) { if (!Double.isFinite(value) || value < 0) throw new InvalidDataException("Denda tidak valid"); lateFee = value; }
    public int calculateLateDays() { return Math.max(0, (int) ChronoUnit.DAYS.between(rental.getDueDate(), actualReturnDate)); }
    public double calculateLateFee() { lateFee = calculateLateDays() * LATE_FEE_MULTIPLIER * rental.getVehicle().getBaseRate(); return lateFee; }
    @Override public String printReceipt() { return "PENGEMBALIAN\nKendaraan: " + rental.getVehicle() + "\nTerlambat: " + calculateLateDays() + " hari\nDenda: Rp" + String.format("%,.0f", lateFee).replace(',', '.') + "\n"; }
}