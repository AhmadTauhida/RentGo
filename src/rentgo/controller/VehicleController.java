package rentgo.controller;

import java.util.ArrayList;
import java.util.List;
import rentgo.dao.VehicleDAO;
import rentgo.exception.DatabaseException;
import rentgo.exception.InvalidDataException;
import rentgo.model.Bus;
import rentgo.model.Car;
import rentgo.model.Motorcycle;
import rentgo.model.Vehicle;
import rentgo.model.VehicleStatus;
import rentgo.model.VehicleType;
import rentgo.util.Validator;
import rentgo.view.VehiclePanel;

/** Coordinates vehicle CRUD. */
public class VehicleController {
    private final VehiclePanel view;
    private final VehicleDAO dao;
    private final List<Vehicle> current = new ArrayList<>();

    public VehicleController(VehiclePanel view, VehicleDAO dao) {
        this.view = view;
        this.dao = dao;
    }

    public void loadData() {
        try {
            current.clear();
            current.addAll(dao.findAll());
            view.setTableData(current);
        } catch (DatabaseException e) {
            view.showError(e.getMessage());
        }
    }

    public void addVehicle() {
        try {
            dao.add(readForm(0));
            view.showInfo("Kendaraan berhasil ditambahkan");
            view.clearForm();
            loadData();
        } catch (InvalidDataException | DatabaseException e) {
            view.showError(e.getMessage());
        }
    }

    public void updateVehicle() {
        Vehicle selected = view.getSelectedVehicle();
        if (selected == null) {
            view.showError("Pilih kendaraan terlebih dahulu");
            return;
        }
        try {
            dao.update(readForm(selected.getId()));
            view.showInfo("Kendaraan berhasil diubah");
            loadData();
        } catch (InvalidDataException | DatabaseException e) {
            view.showError(e.getMessage());
        }
    }

    public void deleteVehicle() {
        Vehicle selected = view.getSelectedVehicle();
        if (selected == null) {
            view.showError("Pilih kendaraan terlebih dahulu");
            return;
        }
        if (!view.confirm("Hapus kendaraan " + selected.getPlateNumber() + "?")) {
            return;
        }
        try {
            dao.delete(selected.getId());
            view.showInfo("Kendaraan berhasil dihapus");
            view.clearForm();
            loadData();
        } catch (InvalidDataException | DatabaseException e) {
            view.showError(e.getMessage());
        }
    }

    public void search() {
        try {
            current.clear();
            current.addAll(dao.search(view.getSearchKeyword(),
                    view.getSearchType(), view.getSearchStatus()));
            view.setTableData(current);
        } catch (DatabaseException e) {
            view.showError(e.getMessage());
        }
    }

    public void clearForm() {
        view.clearForm();
    }

    public void selectRow(int index) {
        if (index >= 0 && index < current.size()) {
            view.setForm(current.get(index));
        }
    }

    private Vehicle readForm(int id) {
        String plateNumber = Validator.requireNotBlank(view.getPlateNumber(), "Plat nomor");
        String brand = Validator.requireNotBlank(view.getBrand(), "Merek");
        int year = Validator.parseInt(view.getManufactureYear(), "Tahun produksi");
        Validator.validateYear(year);
        double baseRate = parseBaseRate(view.getBaseRate());
        VehicleType type = view.getVehicleType();
        VehicleStatus status = view.getVehicleStatus();
        if (type == null) {
            throw new InvalidDataException("Jenis kendaraan wajib dipilih");
        }
        if (status == null) {
            throw new InvalidDataException("Status kendaraan wajib dipilih");
        }
        switch (type) {
            case CAR:
                return new Car(id, plateNumber, brand, year, baseRate, status,
                        Validator.parseInt(view.getPassengerCapacity(), "Kapasitas penumpang"),
                        view.getTransmission());
            case MOTORCYCLE:
                return new Motorcycle(id, plateNumber, brand, year, baseRate, status,
                        Validator.parseInt(view.getEngineCc(), "Kapasitas mesin"));
            case BUS:
                return new Bus(id, plateNumber, brand, year, baseRate, status,
                        Validator.parseInt(view.getSeatCount(), "Jumlah kursi"));
            default:
                throw new InvalidDataException("Jenis kendaraan tidak dikenal");
        }
    }

    private double parseBaseRate(String text) {
        try {
            double value = Double.parseDouble(Validator.requireNotBlank(text, "Tarif dasar")
                    .replace(".", "").replace(",", "."));
            if (value < 0) {
                throw new InvalidDataException("Tarif dasar tidak boleh negatif");
            }
            return value;
        } catch (NumberFormatException e) {
            throw new InvalidDataException("Tarif dasar harus berupa angka", e);
        }
    }
}
