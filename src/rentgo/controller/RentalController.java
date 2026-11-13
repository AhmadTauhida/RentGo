package rentgo.controller;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import rentgo.dao.CustomerDAO;
import rentgo.dao.PaymentDAO;
import rentgo.dao.RentalDAO;
import rentgo.dao.VehicleDAO;
import rentgo.exception.DatabaseException;
import rentgo.exception.InvalidDataException;
import rentgo.exception.VehicleUnavailableException;
import rentgo.model.Customer;
import rentgo.model.Payment;
import rentgo.model.PaymentMethod;
import rentgo.model.Rental;
import rentgo.model.RentalStatus;
import rentgo.model.Staff;
import rentgo.model.Vehicle;
import rentgo.util.CurrencyFormatter;
import rentgo.util.Session;
import rentgo.view.RentalPanel;

/** Coordinates rental creation, update, cancellation, and receipts. */
public class RentalController {
    private final RentalPanel view;
    private final RentalDAO rentalDAO;
    private final CustomerDAO customerDAO;
    private final VehicleDAO vehicleDAO;
    private final PaymentDAO paymentDAO;
    private final List<Rental> current = new ArrayList<>();

    public RentalController(RentalPanel view, RentalDAO rentalDAO, CustomerDAO customerDAO,
                            VehicleDAO vehicleDAO, PaymentDAO paymentDAO) {
        this.view = view;
        this.rentalDAO = rentalDAO;
        this.customerDAO = customerDAO;
        this.vehicleDAO = vehicleDAO;
        this.paymentDAO = paymentDAO;
    }

    public void loadOptions() {
        try {
            view.setCustomerOptions(customerDAO.findAll());
            view.setVehicleOptions(vehicleDAO.findAvailable());
            view.setPaymentMethods(PaymentMethod.values());
            calculateTotal();
        } catch (DatabaseException e) {
            view.showError(e.getMessage());
        }
    }

    public void calculateTotal() {
        try {
            Rental rental = buildRental(0);
            view.setTotalCost(CurrencyFormatter.formatRupiah(rental.calculateTotalCost()));
        } catch (InvalidDataException e) {
            view.setTotalCost("-");
        }
    }

    public void saveRental() {
        try {
            Rental rental = buildRental(0);
            rental.calculateTotalCost();
            PaymentMethod method = view.getPaymentMethod();
            if (method == null) {
                throw new InvalidDataException("Metode pembayaran wajib dipilih");
            }
            Payment payment = new Payment(rental, method, rental.getTotalCost(),
                    LocalDateTime.now(), "RENTAL");
            rentalDAO.saveRental(rental, payment);
            view.showInfo("Transaksi berhasil disimpan");
            view.resetForm();
            loadOptions();
            loadHistory();
        } catch (InvalidDataException | VehicleUnavailableException | DatabaseException e) {
            view.showError(e.getMessage());
        }
    }

    public void updateRental() {
        Rental selected = view.getSelectedRental();
        if (selected == null) {
            view.showError("Pilih transaksi terlebih dahulu");
            return;
        }
        try {
            Rental rental = buildRental(selected.getId());
            rental.calculateTotalCost();
            rentalDAO.update(rental);
            view.showInfo("Transaksi berhasil diubah");
            loadHistory();
        } catch (InvalidDataException | DatabaseException e) {
            view.showError(e.getMessage());
        }
    }

    public void cancelRental() {
        Rental selected = view.getSelectedRental();
        if (selected == null) {
            view.showError("Pilih transaksi terlebih dahulu");
            return;
        }
        if (!view.confirm("Batalkan transaksi #" + selected.getId() + "?")) {
            return;
        }
        try {
            rentalDAO.cancel(selected.getId());
            view.showInfo("Transaksi berhasil dibatalkan");
            loadOptions();
            loadHistory();
        } catch (InvalidDataException | DatabaseException e) {
            view.showError(e.getMessage());
        }
    }

    public void loadHistory() {
        try {
            current.clear();
            current.addAll(rentalDAO.findHistory());
            view.setTableData(current);
        } catch (DatabaseException e) {
            view.showError(e.getMessage());
        }
    }

    public void printReceipt() {
        Rental selected = view.getSelectedRental();
        if (selected == null) {
            view.showError("Pilih transaksi terlebih dahulu");
            return;
        }
        view.showReceipt(selected.printReceipt());
    }

    public void selectRow(int index) {
        // Riwayat bersifat read-only; tidak perlu memuat ke form.
    }

    private Rental buildRental(int id) {
        Customer customer = view.getSelectedCustomer();
        if (customer == null) {
            throw new InvalidDataException("Pelanggan wajib dipilih");
        }
        Vehicle vehicle = view.getSelectedVehicle();
        if (vehicle == null) {
            throw new InvalidDataException("Kendaraan wajib dipilih");
        }
        Staff staff = Session.getStaff();
        if (staff == null) {
            throw new InvalidDataException("Sesi login tidak ditemukan");
        }
        return new Rental(id, customer, vehicle, staff,
                toLocalDate(view.getStartDate()), toLocalDate(view.getDueDate()),
                view.isWithDriver(), 0, RentalStatus.ACTIVE);
    }

    private LocalDate toLocalDate(Date date) {
        if (date == null) {
            throw new InvalidDataException("Tanggal wajib diisi");
        }
        return Instant.ofEpochMilli(date.getTime())
                .atZone(ZoneId.systemDefault())
                .toLocalDate();
    }
}
