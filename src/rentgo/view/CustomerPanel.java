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
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableModel;

import rentgo.controller.CustomerController;
import rentgo.model.Customer;

/** Customer management panel. */
public class CustomerPanel extends JPanel {
    private CustomerController controller;
    private final List<Customer> customers = new ArrayList<>();

    private final JTextField nameField = new JTextField(14);
    private final JTextField nationalIdField = new JTextField(14);
    private final JTextField phoneNumberField = new JTextField(14);
    private final JTextArea addressArea = new JTextArea(3, 14);
    private final JTextField searchField = new JTextField(14);

    private final DefaultTableModel tableModel = new DefaultTableModel(
            new Object[] {"ID", "Nama", "NIK", "Telepon", "Alamat"}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final JTable customerTable = new JTable(tableModel);

    private final JButton addButton = new JButton("Tambah");
    private final JButton updateButton = new JButton("Ubah");
    private final JButton deleteButton = new JButton("Hapus");
    private final JButton clearButton = new JButton("Bersihkan");

    public CustomerPanel() {
        super(new BorderLayout(8, 8));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        buildSearchBar();
        buildForm();
        buildTable();
    }

    public void setController(CustomerController value) {
        controller = value;
    }

    public void setTableData(List<Customer> data) {
        customers.clear();
        customers.addAll(data);
        tableModel.setRowCount(0);
        for (Customer customer : data) {
            tableModel.addRow(new Object[] {
                    customer.getId(),
                    customer.getName(),
                    customer.getNationalId(),
                    customer.getPhoneNumber(),
                    customer.getAddress()
            });
        }
    }

    public void setForm(Customer customer) {
        if (customer == null) {
            clearForm();
            return;
        }
        nameField.setText(customer.getName());
        nationalIdField.setText(customer.getNationalId());
        phoneNumberField.setText(customer.getPhoneNumber());
        addressArea.setText(customer.getAddress());
    }

    public void clearForm() {
        nameField.setText("");
        nationalIdField.setText("");
        phoneNumberField.setText("");
        addressArea.setText("");
        customerTable.clearSelection();
    }

    public Customer getSelectedCustomer() {
        int index = getSelectedIndex();
        return index >= 0 && index < customers.size() ? customers.get(index) : null;
    }

    public String getNameField() {
        return nameField.getText().trim();
    }

    public String getNationalId() {
        return nationalIdField.getText().trim();
    }

    public String getPhoneNumber() {
        return phoneNumberField.getText().trim();
    }

    public String getAddress() {
        return addressArea.getText().trim();
    }

    public String getSearchKeyword() {
        return searchField.getText().trim();
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
        return customerTable.getSelectedRow();
    }

    private void buildSearchBar() {
        JPanel searchBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        searchBar.add(new JLabel("Cari"));
        searchBar.add(searchField);

        JButton searchButton = new JButton("Cari");
        searchButton.addActionListener(event -> search());
        searchBar.add(searchButton);

        searchField.addActionListener(event -> search());
        add(searchBar, BorderLayout.NORTH);
    }

    private void buildForm() {
        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(BorderFactory.createTitledBorder("Form Pelanggan"));
        addRow(form, 0, "Nama", nameField);
        addRow(form, 1, "NIK", nationalIdField);
        addRow(form, 2, "Telepon", phoneNumberField);

        addressArea.setLineWrap(true);
        addressArea.setWrapStyleWord(true);
        addRow(form, 3, "Alamat", new JScrollPane(addressArea));

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        addButton.addActionListener(event -> {
            if (controller != null) controller.addCustomer();
        });
        updateButton.addActionListener(event -> {
            if (controller != null) controller.updateCustomer();
        });
        deleteButton.addActionListener(event -> {
            if (controller != null) controller.deleteCustomer();
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
        actionConstraints.gridy = 4;
        actionConstraints.gridwidth = 2;
        actionConstraints.weightx = 1;
        actionConstraints.fill = GridBagConstraints.HORIZONTAL;
        actionConstraints.insets = new Insets(8, 4, 4, 4);
        form.add(actions, actionConstraints);

        add(form, BorderLayout.WEST);
    }

    private void buildTable() {
        customerTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        customerTable.setFillsViewportHeight(true);
        customerTable.getSelectionModel().addListSelectionListener(event -> {
            if (!event.getValueIsAdjusting() && controller != null) {
                controller.selectRow(getSelectedIndex());
            }
        });
        add(new JScrollPane(customerTable), BorderLayout.CENTER);
    }

    private void search() {
        if (controller != null) {
            controller.search();
        }
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
