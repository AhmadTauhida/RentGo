package rentgo.view;

import java.awt.Dimension;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;

import com.formdev.flatlaf.FlatLightLaf;

import rentgo.model.Bus;
import rentgo.model.Car;
import rentgo.model.Customer;
import rentgo.model.Motorcycle;
import rentgo.model.Rental;
import rentgo.model.RentalStatus;
import rentgo.model.Role;
import rentgo.model.Staff;
import rentgo.model.Vehicle;
import rentgo.model.VehicleStatus;
import rentgo.model.VehicleType;
import rentgo.util.Session;

/** Previews every panel with dummy data, without touching the database. */
public final class ViewPreview {
    private ViewPreview() {
    }

    public static void main(String[] args) {
        FlatLightLaf.setup();
        SwingUtilities.invokeLater(ViewPreview::show);
    }

    private static void show() {
        Staff admin = new Staff(1, "Admin Demo", "081234567890", "admin", "dummy-hash", Role.ADMIN);
        Session.setStaff(admin);

        List<Customer> customers = dummyCustomers();
        List<Vehicle> vehicles = dummyVehicles();
        List<Rental> rentals = dummyRentals(customers, vehicles, admin);

        JTabbedPane tabs = new JTabbedPane();

        VehiclePanel vehiclePanel = new VehiclePanel();
        vehiclePanel.setTableData(vehicles);
        tabs.addTab("Kendaraan", vehiclePanel);

        CustomerPanel customerPanel = new CustomerPanel();
        customerPanel.setTableData(customers);
        tabs.addTab("Pelanggan", customerPanel);

        RentalPanel rentalPanel = new RentalPanel();
        rentalPanel.setCustomerOptions(customers);
        rentalPanel.setVehicleOptions(vehicles);
        rentalPanel.setTableData(rentals);
        tabs.addTab("Penyewaan", rentalPanel);

        VehicleReturnPanel returnPanel = new VehicleReturnPanel();
        returnPanel.setRentalOptions(rentals);
        returnPanel.setLateFeeInfo(2, "Rp150.000");
        tabs.addTab("Pengembalian", returnPanel);

        ReportPanel reportPanel = new ReportPanel();
        reportPanel.setRevenueData(List.of(
                new Object[] {LocalDate.now().minusDays(1), 2L, 700000.0},
                new Object[] {LocalDate.now(), 3L, 1050000.0}));
        reportPanel.setTopVehiclesData(List.of(
                new Object[] {"B 1234 XYZ", "Toyota", VehicleType.CAR, 3L},
                new Object[] {"D 5678 ABC", "Honda", VehicleType.MOTORCYCLE, 2L}));
        tabs.addTab("Laporan", reportPanel);

        UserManagementPanel usersPanel = new UserManagementPanel();
        usersPanel.setTableData(List.of(
                admin,
                new Staff(2, "Staff Demo", "081298765432", "staff", "dummy-hash", Role.STAFF)));
        tabs.addTab("Pengguna", usersPanel);

        JFrame frame = new JFrame("RentGo - Pratinjau UI");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setMinimumSize(new Dimension(1000, 650));
        frame.setSize(1200, 760);
        frame.setLocationRelativeTo(null);
        frame.setContentPane(tabs);
        frame.setVisible(true);
    }

    private static List<Customer> dummyCustomers() {
        List<Customer> customers = new ArrayList<>();
        customers.add(new Customer(1, "Budi Santoso", "081234567890", "3174010101010001", "Jl. Merdeka No. 1"));
        customers.add(new Customer(2, "Siti Aminah", "081298765432", "3174010101010002", "Jl. Sudirman No. 2"));
        return customers;
    }

    private static List<Vehicle> dummyVehicles() {
        List<Vehicle> vehicles = new ArrayList<>();
        vehicles.add(new Car(1, "B 1234 XYZ", "Toyota", 2022, 350000, VehicleStatus.AVAILABLE, 5, "AUTOMATIC"));
        vehicles.add(new Motorcycle(2, "D 5678 ABC", "Honda", 2021, 100000, VehicleStatus.RENTED, 150));
        vehicles.add(new Bus(3, "E 9012 DEF", "Hino", 2020, 1200000, VehicleStatus.MAINTENANCE, 40));
        return vehicles;
    }

    private static List<Rental> dummyRentals(List<Customer> customers, List<Vehicle> vehicles, Staff staff) {
        List<Rental> rentals = new ArrayList<>();
        Rental active = new Rental(1, customers.get(0), vehicles.get(0), staff,
                LocalDate.now().minusDays(3), LocalDate.now().plusDays(2), true, 0, RentalStatus.ACTIVE);
        active.calculateTotalCost();
        rentals.add(active);
        Rental completed = new Rental(2, customers.get(1), vehicles.get(1), staff,
                LocalDate.now().minusDays(10), LocalDate.now().minusDays(7), false, 0, RentalStatus.COMPLETED);
        completed.calculateTotalCost();
        rentals.add(completed);
        return rentals;
    }
}
