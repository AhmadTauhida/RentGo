package rentgo.view;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
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

/** Staff account management panel. */
public class UserManagementPanel extends JPanel {
    private UserManagementController controller;
    private final List<Staff> staffMembers = new ArrayList<>();

    private final JTextField nameField = new JTextField(14);
    private final JTextField usernameField = new JTextField(14);
    private final JTextField phoneNumberField = new JTextField(14);
    private final JPasswordField passwordField = new JPasswordField(14);
    private final JComboBox<Role> roleCombo = new JComboBox<>(Role.values());

    private final DefaultTableModel tableModel = new DefaultTableModel(
            new Object[] {"ID", "Nama", "Username", "Telepon", "Role"}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final JTable staffTable = new JTable(tableModel);

    private final JButton addButton = new JButton("Tambah");
    private final JButton updateButton = new JButton("Ubah");
    private final JButton deleteButton = new JButton("Hapus");
    private final JButton clearButton = new JButton("Bersihkan");

    public UserManagementPanel() {
        super(new BorderLayout(8, 8));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        buildForm();
        buildTable();
    }

    public void setController(UserManagementController value) {
        controller = value;
    }

    public void setTableData(List<Staff> data) {
        staffMembers.clear();
        staffMembers.addAll(data);
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
    }

    public void setForm(Staff staff) {
        if (staff == null) {
            clearForm();
            return;
        }
        nameField.setText(staff.getName());
        usernameField.setText(staff.getUsername());
        phoneNumberField.setText(staff.getPhoneNumber());
        passwordField.setText("");
        roleCombo.setSelectedItem(staff.getRole());
    }

    public void clearForm() {
        nameField.setText("");
        usernameField.setText("");
        phoneNumberField.setText("");
        passwordField.setText("");
        roleCombo.setSelectedIndex(0);
        staffTable.clearSelection();
    }

    public Staff getSelectedStaff() {
        int index = getSelectedIndex();
        return index >= 0 && index < staffMembers.size() ? staffMembers.get(index) : null;
    }

    public String getNameField() {
        return nameField.getText().trim();
    }

    public String getUsernameField() {
        return usernameField.getText().trim();
    }

    public String getPhoneNumber() {
        return phoneNumberField.getText().trim();
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

    public int getSelectedIndex() {
        return staffTable.getSelectedRow();
    }

    private void buildForm() {
        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(BorderFactory.createTitledBorder("Form Akun"));
        addRow(form, 0, "Nama", nameField);
        addRow(form, 1, "Username", usernameField);
        addRow(form, 2, "Telepon", phoneNumberField);
        addRow(form, 3, "Password", passwordField);
        addRow(form, 4, "Role", roleCombo);

        GridBagConstraints hintConstraints = new GridBagConstraints();
        hintConstraints.gridx = 0;
        hintConstraints.gridy = 5;
        hintConstraints.gridwidth = 2;
        hintConstraints.anchor = GridBagConstraints.WEST;
        hintConstraints.insets = new Insets(0, 4, 4, 4);
        form.add(new JLabel("Kosongkan password saat mengubah akun."), hintConstraints);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
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

        GridBagConstraints actionConstraints = new GridBagConstraints();
        actionConstraints.gridx = 0;
        actionConstraints.gridy = 6;
        actionConstraints.gridwidth = 2;
        actionConstraints.weightx = 1;
        actionConstraints.fill = GridBagConstraints.HORIZONTAL;
        actionConstraints.insets = new Insets(8, 4, 4, 4);
        form.add(actions, actionConstraints);

        add(form, BorderLayout.WEST);
    }

    private void buildTable() {
        staffTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        staffTable.setFillsViewportHeight(true);
        staffTable.getSelectionModel().addListSelectionListener(event -> {
            if (!event.getValueIsAdjusting() && controller != null) {
                controller.selectRow(getSelectedIndex());
            }
        });
        add(new JScrollPane(staffTable), BorderLayout.CENTER);
    }

    private void addRow(JPanel form, int row, String label, Component component) {
        GridBagConstraints labelConstraints = new GridBagConstraints();
        labelConstraints.gridx = 0;
        labelConstraints.gridy = row;
        labelConstraints.anchor = GridBagConstraints.WEST;
        labelConstraints.insets = new Insets(4, 4, 4, 8);
        form.add(new JLabel(label), labelConstraints);

        GridBagConstraints fieldConstraints = new GridBagConstraints();
        fieldConstraints.gridx = 1;
        fieldConstraints.gridy = row;
        fieldConstraints.weightx = 1;
        fieldConstraints.fill = GridBagConstraints.HORIZONTAL;
        fieldConstraints.insets = new Insets(4, 0, 4, 4);
        form.add(component, fieldConstraints);
    }
}
