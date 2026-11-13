package rentgo.controller;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import rentgo.dao.StaffDAO;
import rentgo.exception.DatabaseException;
import rentgo.exception.InvalidDataException;
import rentgo.model.Role;
import rentgo.model.Staff;
import rentgo.util.PasswordHasher;
import rentgo.util.Validator;
import rentgo.view.UserManagementPanel;

/** Coordinates staff account CRUD for administrators. */
public class UserManagementController {
    private final UserManagementPanel view;
    private final StaffDAO dao;
    private final List<Staff> current = new ArrayList<>();

    public UserManagementController(UserManagementPanel view, StaffDAO dao) {
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

    public void addStaff() {
        try {
            dao.add(readForm(0, null));
            view.showInfo("Akun berhasil ditambahkan");
            view.clearForm();
            loadData();
        } catch (InvalidDataException | DatabaseException e) {
            view.showError(e.getMessage());
        }
    }

    public void updateStaff() {
        Staff selected = view.getSelectedStaff();
        if (selected == null) {
            view.showError("Pilih akun terlebih dahulu");
            return;
        }
        try {
            dao.update(readForm(selected.getId(), selected));
            view.showInfo("Akun berhasil diubah");
            loadData();
        } catch (InvalidDataException | DatabaseException e) {
            view.showError(e.getMessage());
        }
    }

    public void deleteStaff() {
        Staff selected = view.getSelectedStaff();
        if (selected == null) {
            view.showError("Pilih akun terlebih dahulu");
            return;
        }
        if (!view.confirm("Hapus akun " + selected.getUsername() + "?")) {
            return;
        }
        try {
            dao.delete(selected.getId());
            view.showInfo("Akun berhasil dihapus");
            view.clearForm();
            loadData();
        } catch (InvalidDataException | DatabaseException e) {
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

    private Staff readForm(int id, Staff selected) {
        char[] password = view.getPassword();
        try {
            String name = Validator.requireNotBlank(view.getNameField(), "Nama");
            String phoneNumber = view.getPhoneNumber();
            Validator.validatePhone(phoneNumber);
            String username = Validator.requireNotBlank(view.getUsernameField(), "Username");
            Role role = view.getRole();
            if (role == null) {
                throw new InvalidDataException("Role wajib dipilih");
            }
            String passwordHash;
            if (password.length == 0) {
                if (selected == null) {
                    throw new InvalidDataException("Password minimal 6 karakter");
                }
                passwordHash = selected.getPasswordHash();
            } else {
                if (password.length < 6) {
                    throw new InvalidDataException("Password minimal 6 karakter");
                }
                passwordHash = PasswordHasher.hash(password);
            }
            return new Staff(id, name, phoneNumber, username, passwordHash, role);
        } finally {
            Arrays.fill(password, '\0');
        }
    }
}
