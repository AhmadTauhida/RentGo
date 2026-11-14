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
    private final JTextField phoneField = new JTextField(14);
    private final JTextArea addressArea = new JTextArea(4, 14);

    private final JTextField searchField = new JTextField(20);

    private final DefaultTableModel tableModel = new DefaultTableModel(
            new Object[] {"ID", "Nama", "NIK", "Telepon", "Alamat"}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final JTable customerTable = new JTable(tableModel);
    private final JLabel countLabel = UiStyle.mutedLabel("0 pelanggan");

    private final JButton addButton = new JButton("Tambah");
    private final JButton updateButton = new JButton("Ubah");
    private final JButton deleteButton = new JButton("Hapus");
    private final JButton clearButton = new JButton("Bersihkan");

    public CustomerPanel() {
        super(new BorderLayout(16, 16));
        setBackground(UiStyle.PAGE);
        setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));

        addressArea.setLineWrap(true);
        addressArea.setWrapStyleWord(true);
        addressArea.setBorder(BorderFactory.createEmptyBorder(6, 8, 6, 8));

        UiStyle.placeholder(nameField, "Nama lengkap");
        UiStyle.placeholder(nationalIdField, "16 digit NIK");
        UiStyle.placeholder(phoneField, "Contoh: 081234567890");
        UiStyle.placeholder(searchField, "Cari nama atau NIK");
        addressArea.putClientProperty("JTextArea.placeholderText", "Alamat lengkap");

        buildHeader();
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
        countLabel.setText(data.size() + " pelanggan");
    }

    public void setForm(Customer customer) {
        if (customer == null) {
            clearForm();
            return;
        }
        nameField.setText(customer.getName());
        nationalIdField.setText(customer.getNationalId());
        phoneField.setText(customer.getPhoneNumber());
        addressArea.setText(customer.getAddress());
    }

    public void clearForm() {
        nameField.setText("");
        nationalIdField.setText("");
        phoneField.setText("");
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
        return phoneField.getText().trim();
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

    private void buildHeader() {
        JPanel header = new JPanel(new BorderLayout(16, 0));
        header.setOpaque(false);
        header.add(UiStyle.pageTitle("Data Pelanggan"), BorderLayout.WEST);

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
        card.add(UiStyle.sectionTitle("Form Pelanggan"), BorderLayout.NORTH);

        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        UiStyle.formRow(form, 0, "Nama", nameField);
        UiStyle.formRow(form, 1, "NIK", nationalIdField);
        UiStyle.formRow(form, 2, "Telepon", phoneField);
        UiStyle.formRow(form, 3, "Alamat", new JScrollPane(addressArea));
        UiStyle.formFiller(form, 4);
        card.add(form, BorderLayout.CENTER);

        JPanel actions = new JPanel(new GridLayout(2, 2, 8, 8));
        actions.setOpaque(false);
        UiStyle.primary(addButton);
        UiStyle.danger(deleteButton);
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
        card.add(actions, BorderLayout.SOUTH);

        add(card, BorderLayout.WEST);
    }

    private void buildTable() {
        customerTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        UiStyle.table(customerTable);
        customerTable.getColumnModel().getColumn(0).setMaxWidth(60);
        customerTable.getSelectionModel().addListSelectionListener(event -> {
            if (!event.getValueIsAdjusting() && controller != null) {
                controller.selectRow(getSelectedIndex());
            }
        });

        UiStyle.Card card = new UiStyle.Card(new BorderLayout(0, 12));
        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);
        top.add(UiStyle.sectionTitle("Daftar Pelanggan"), BorderLayout.WEST);
        top.add(countLabel, BorderLayout.EAST);
        card.add(top, BorderLayout.NORTH);

        JScrollPane scrollPane = new JScrollPane(customerTable);
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