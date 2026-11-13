package rentgo.controller;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import rentgo.dao.RentalDAO;
import rentgo.dao.VehicleReturnDAO;
import rentgo.exception.DatabaseException;
import rentgo.exception.InvalidDataException;
import rentgo.model.Rental;
import rentgo.model.VehicleReturn;
import rentgo.util.CurrencyFormatter;
import rentgo.view.VehicleReturnPanel;

/** Coordinates vehicle returns and late-fee previews. */
public class VehicleReturnController {
    private final VehicleReturnPanel view;
    private final RentalDAO rentalDAO;
    private final VehicleReturnDAO returnDAO;

    public VehicleReturnController(VehicleReturnPanel view, RentalDAO rentalDAO,
                                   VehicleReturnDAO returnDAO) {
        this.view = view;
        this.rentalDAO = rentalDAO;
        this.returnDAO = returnDAO;
    }

    public void loadActiveRentals() {
        try {
            view.setRentalOptions(rentalDAO.findActive());
            calculateLateFee();
        } catch (DatabaseException e) {
            view.showError(e.getMessage());
        }
    }

    public void calculateLateFee() {
        try {
            Rental rental = view.getSelectedRental();
            if (rental == null) {
                view.setLateFeeInfo(0, "-");
                return;
            }
            VehicleReturn vehicleReturn = new VehicleReturn(rental,
                    toLocalDate(view.getActualReturnDate()), view.getCondition());
            view.setLateFeeInfo(vehicleReturn.calculateLateDays(),
                    CurrencyFormatter.formatRupiah(vehicleReturn.calculateLateFee()));
        } catch (InvalidDataException e) {
            view.setLateFeeInfo(0, "-");
        }
    }

    public void saveReturn() {
        try {
            Rental rental = view.getSelectedRental();
            if (rental == null) {
                throw new InvalidDataException("Transaksi aktif wajib dipilih");
            }
            VehicleReturn vehicleReturn = new VehicleReturn(rental,
                    toLocalDate(view.getActualReturnDate()), view.getCondition());
            vehicleReturn.calculateLateFee();
            returnDAO.saveReturn(vehicleReturn);
            view.showInfo("Pengembalian kendaraan berhasil dicatat");
            view.resetForm();
            loadActiveRentals();
        } catch (InvalidDataException | DatabaseException e) {
            view.showError(e.getMessage());
        }
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
