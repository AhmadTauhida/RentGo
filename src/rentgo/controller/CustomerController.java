package rentgo.controller;

import java.util.ArrayList;
import java.util.List;
import rentgo.dao.CustomerDAO;
import rentgo.exception.DatabaseException;
import rentgo.exception.InvalidDataException;
import rentgo.model.Customer;
import rentgo.util.Validator;
import rentgo.view.CustomerPanel;

/** Coordinates customer CRUD. */
public class CustomerController {
    private final CustomerPanel view;
    private final CustomerDAO dao;
    private final List<Customer> current = new ArrayList<>();

    public CustomerController(CustomerPanel view, CustomerDAO dao) {
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

    public void addCustomer() {
        try {
            dao.add(readForm(0));
            view.showInfo("Pelanggan berhasil ditambahkan");
            view.clearForm();
            loadData();
        } catch (InvalidDataException | DatabaseException e) {
            view.showError(e.getMessage());
        }
    }

    public void updateCustomer() {
        Customer selected = view.getSelectedCustomer();
        if (selected == null) {
            view.showError("Pilih pelanggan terlebih dahulu");
            return;
        }
        try {
            dao.update(readForm(selected.getId()));
            view.showInfo("Pelanggan berhasil diubah");
            loadData();
        } catch (InvalidDataException | DatabaseException e) {
            view.showError(e.getMessage());
        }
    }

    public void deleteCustomer() {
        Customer selected = view.getSelectedCustomer();
        if (selected == null) {
            view.showError("Pilih pelanggan terlebih dahulu");
            return;
        }
        if (!view.confirm("Hapus pelanggan " + selected.getName() + "?")) {
            return;
        }
        try {
            dao.delete(selected.getId());
            view.showInfo("Pelanggan berhasil dihapus");
            view.clearForm();
            loadData();
        } catch (InvalidDataException | DatabaseException e) {
            view.showError(e.getMessage());
        }
    }

    public void search() {
        try {
            current.clear();
            current.addAll(dao.search(view.getSearchKeyword()));
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

    private Customer readForm(int id) {
        String name = Validator.requireNotBlank(view.getNameField(), "Nama");
        String phoneNumber = view.getPhoneNumber();
        Validator.validatePhone(phoneNumber);
        String nationalId = Validator.requireNotBlank(view.getNationalId(), "NIK");
        Validator.validateNationalId(nationalId);
        String address = Validator.requireNotBlank(view.getAddress(), "Alamat");
        return new Customer(id, name, phoneNumber, nationalId, address);
    }
}
