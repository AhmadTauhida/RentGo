package rentgo.model;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import rentgo.exception.InvalidDataException;

/** A vehicle rental transaction. */
public class Rental implements PrintableReceipt {
    private int id;
    private Customer customer;
    private Vehicle vehicle;
    private Staff staff;
    private LocalDate startDate;
    private LocalDate dueDate;
    private boolean withDriver;
    private double totalCost;
    private RentalStatus status;
    public Rental() { }
    public Rental(Customer customer, Vehicle vehicle, Staff staff, LocalDate startDate, LocalDate dueDate, boolean withDriver) { this(0, customer, vehicle, staff, startDate, dueDate, withDriver, 0, RentalStatus.ACTIVE); }
    public Rental(int id, Customer customer, Vehicle vehicle, Staff staff, LocalDate startDate, LocalDate dueDate, boolean withDriver, double totalCost, RentalStatus status) { setId(id); setCustomer(customer); setVehicle(vehicle); setStaff(staff); setStartDate(startDate); setDueDate(dueDate); setWithDriver(withDriver); setTotalCost(totalCost); setStatus(status); }
    public int getId() { return id; }
    public void setId(int value) { if (value < 0) throw new InvalidDataException("ID tidak valid"); id = value; }
    public Customer getCustomer() { return customer; }
    public void setCustomer(Customer value) { if (value == null) throw new InvalidDataException("Pelanggan wajib dipilih"); customer = value; }
    public Vehicle getVehicle() { return vehicle; }
    public void setVehicle(Vehicle value) { if (value == null) throw new InvalidDataException("Kendaraan wajib dipilih"); vehicle = value; }
    public Staff getStaff() { return staff; }
    public void setStaff(Staff value) { if (value == null) throw new InvalidDataException("Petugas wajib dipilih"); staff = value; }
    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate value) { if (value == null) throw new InvalidDataException("Tanggal sewa wajib diisi"); if (dueDate != null && value.isAfter(dueDate)) throw new InvalidDataException("Tanggal sewa tidak boleh setelah tanggal kembali"); startDate = value; }
    public LocalDate getDueDate() { return dueDate; }
    public void setDueDate(LocalDate value) { if (value == null) throw new InvalidDataException("Tanggal kembali wajib diisi"); if (startDate != null && value.isBefore(startDate)) throw new InvalidDataException("Tanggal kembali tidak boleh sebelum tanggal sewa"); dueDate = value; }
    public boolean isWithDriver() { return withDriver; }
    public void setWithDriver(boolean value) { withDriver = value; }
    public double getTotalCost() { return totalCost; }
    public void setTotalCost(double value) { if (!Double.isFinite(value) || value < 0) throw new InvalidDataException("Total biaya tidak valid"); totalCost = value; }
    public RentalStatus getStatus() { return status; }
    public void setStatus(RentalStatus value) { if (value == null) throw new InvalidDataException("Status transaksi wajib dipilih"); status = value; }
    public int getRentalDays() { return Math.max(1, (int) ChronoUnit.DAYS.between(startDate, dueDate)); }
    public double calculateTotalCost() { totalCost = vehicle.calculateRent(getRentalDays(), withDriver); return totalCost; }
    @Override public String printReceipt() { return "STRUK RENTGO\nPelanggan: " + customer.getName() + "\nKendaraan: " + vehicle + "\nTanggal: " + startDate + " s.d. " + dueDate + "\nLama sewa: " + getRentalDays() + " hari\nTotal: Rp" + String.format("%,.0f", totalCost).replace(',', '.') + "\n"; }
    @Override public String toString() { return "#" + id + " - " + vehicle + " (" + startDate + ")"; }
}