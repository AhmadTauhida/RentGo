package rentgo.view;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableModel;

import rentgo.controller.UserManagementController;
import rentgo.model.Role;
import rentgo.model.Staff;

/** Staff account management panel (Admin only). */
public class UserManagementPanel extends JPanel {
    private UserManagementController controller;
    private final List<Staff> accounts = new ArrayList<>();

    private final JTextField nameField = new JTextField(14);
    private final JTextField usernameField = new JTextField(14);
    private final JTextField phoneField = new JTextField(14);
    private final JPasswordField passwordField = new JPasswordField(14);
    private final JComboBox<Role> roleCombo = new JComboBox<>(Role.values());
    private final JTextField searchField = new JTextField(20);

    private final DefaultTableModel tableModel = new DefaultTableModel(
            new Object[] {"ID", "Nama", "Username", "Telepon", "Role"}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final JTable accountTable = new JTable(tableModel);
    private final JLabel countLabel = UiStyle.mutedLabel("0 akun");

    private final JButton addButton = new JButton("Tambah");
    private final JButton updateButton = new JButton("Ubah");
    private final JButton deleteButton = new JButton("Hapus");
    private final JButton clearButton = new JButton("Bersihkan");

    public UserManagementPanel() {
        super(new BorderLayout(16, 16));
        setBackground(UiStyle.PAGE);
        setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));

        UiStyle.placeholder(nameField, "Nama lengkap");
        UiStyle.placeholder(usernameField, "Minimal 3 karakter");
        UiStyle.placeholder(phoneField, "Contoh: 081234567890");
        UiStyle.placeholder(searchField, "Cari nama, username, atau telepon");
        passwordField.putClientProperty("JTextField.placeholderText", "Minimal 6 karakter");
        PasswordEye.attach(passwordField);

        buildHeader();
        buildForm();
        buildTable();
    }

    public void setController(UserManagementController value) {
        controller = value;
    }

    public void setTableData(List<Staff> data) {
        accounts.clear();
        accounts.addAll(data);
        tableModel.setRowCount(0);
        for (Staff staff : data) {
            tableModel.addRow(new Object[] {
                    staff.getId(),
                    staff.getName(),
                    staff.getUsername(),
                    staff.getPhoneNumber(),
                    staff.getRole()
            });
        }
        countLabel.setText(data.size() + " akun");
    }

    public void setForm(Staff staff) {
        if (staff == null) {
            clearForm();
            return;
        }
        nameField.setText(staff.getName());
        usernameField.setText(staff.getUsername());
        phoneField.setText(staff.getPhoneNumber());
        passwordField.setText("");
        roleCombo.setSelectedItem(staff.getRole());
    }

    public void clearForm() {
        nameField.setText("");
        usernameField.setText("");
        phoneField.setText("");
        passwordField.setText("");
        roleCombo.setSelectedItem(Role.STAFF);
        accountTable.clearSelection();
    }

    public Staff getSelectedStaff() {
        int index = getSelectedIndex();
        return index >= 0 && index < accounts.size() ? accounts.get(index) : null;
    }

    public String getNameField() {
        return nameField.getText().trim();
    }

    public String getUsernameField() {
        return usernameField.getText().trim();
    }

    public String getPhoneNumber() {
        return phoneField.getText().trim();
    }

    public char[] getPassword() {
        return passwordField.getPassword();
    }

    public Role getRole() {
        return (Role) roleCombo.getSelectedItem();
    }

    public void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Kesalahan", JOptionPane.ERROR_MESSAGE);
    }

    public void showInfo(String message) {
        JOptionPane.showMessageDialog(this, message, "Informasi", JOptionPane.INFORMATION_MESSAGE);
    }

    public boolean confirm(String message) {
        return JOptionPane.showConfirmDialog(this, message, "Konfirmasi",
                JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION;
    }

    public String getSearchKeyword() {
        return searchField.getText().trim();
    }

    public int getSelectedIndex() {
        return accountTable.getSelectedRow();
    }

    private void buildHeader() {
        JPanel header = new JPanel(new BorderLayout(16, 0));
        header.setOpaque(false);
        header.add(UiStyle.pageTitle("Kelola Akun"), BorderLayout.WEST);

        JPanel searchBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        searchBar.setOpaque(false);
        searchBar.add(searchField);

        JButton searchButton = new JButton("Cari");
        UiStyle.primary(searchButton);
        searchButton.addActionListener(event -> search());
        searchBar.add(searchButton);
        header.add(searchBar, BorderLayout.EAST);

        searchField.addActionListener(event -> search());
        add(header, BorderLayout.NORTH);
    }

    private void buildForm() {
        UiStyle.Card card = new UiStyle.Card(new BorderLayout(0, 12));
        card.setPreferredSize(new Dimension(350, 0));
        card.add(UiStyle.sectionTitle("Form Akun"), BorderLayout.NORTH);

        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        UiStyle.formRow(form, 0, "Nama", nameField);
        UiStyle.formRow(form, 1, "Username", usernameField);
        UiStyle.formRow(form, 2, "Telepon", phoneField);
        UiStyle.formRow(form, 3, "Password", passwordField);
        UiStyle.formRow(form, 4, "Role", roleCombo);
        UiStyle.formNote(form, 5, UiStyle.mutedLabel("Kosongkan password saat mengubah akun."));
        UiStyle.formFiller(form, 6);
        card.add(form, BorderLayout.CENTER);

        JPanel actions = new JPanel(new GridLayout(2, 2, 8, 8));
        actions.setOpaque(false);
        UiStyle.primary(addButton);
        UiStyle.danger(deleteButton);
        // NOTE: method names below follow the CustomerController pattern; rename if your
        // UserManagementController uses different names.
        addButton.addActionListener(event -> {
            if (controller != null) controller.addStaff();
        });
        updateButton.addActionListener(event -> {
            if (controller != null) controller.updateStaff();
        });
        deleteButton.addActionListener(event -> {
            if (controller != null) controller.deleteStaff();
        });
        clearButton.addActionListener(event -> {
            if (controller != null) controller.clearForm();
        });
        actions.add(addButton);
        actions.add(updateButton);
        actions.add(deleteButton);
        actions.add(clearButton);
        card.add(actions, BorderLayout.SOUTH);

        add(card, BorderLayout.WEST);
    }

    private void buildTable() {
        accountTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        UiStyle.table(accountTable);
        accountTable.getColumnModel().getColumn(0).setMaxWidth(60);
        accountTable.getSelectionModel().addListSelectionListener(event -> {
            if (!event.getValueIsAdjusting() && controller != null) {
                controller.selectRow(getSelectedIndex());
            }
        });

        UiStyle.Card card = new UiStyle.Card(new BorderLayout(0, 12));
        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);
        top.add(UiStyle.sectionTitle("Daftar Akun"), BorderLayout.WEST);
        top.add(countLabel, BorderLayout.EAST);
        card.add(top, BorderLayout.NORTH);

        JScrollPane scrollPane = new JScrollPane(accountTable);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setBackground(UiStyle.CARD);
        card.add(scrollPane, BorderLayout.CENTER);
        add(card, BorderLayout.CENTER);
    }

    private void search() {
        if (controller != null) {
            controller.search();
        }
    }
}