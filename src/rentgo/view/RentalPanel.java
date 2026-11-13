package rentgo.view;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.ListSelectionModel;
import javax.swing.SpinnerDateModel;
import javax.swing.table.DefaultTableModel;

import rentgo.controller.RentalController;
import rentgo.model.Bus;
import rentgo.model.Customer;
import rentgo.model.PaymentMethod;
import rentgo.model.Rental;
import rentgo.model.Vehicle;
import rentgo.util.CurrencyFormatter;

/** Rental transaction panel. */
public class RentalPanel extends JPanel {
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd-MM-yyyy");

    private RentalController controller;
    private final List<Rental> rentals = new ArrayList<>();

    private final JComboBox<Customer> customerCombo = new JComboBox<>();
    private final JComboBox<Vehicle> vehicleCombo = new JComboBox<>();
    private final JSpinner startDateSpinner = new JSpinner(new SpinnerDateModel());
    private final JSpinner dueDateSpinner = new JSpinner(new SpinnerDateModel());
    private final JCheckBox withDriverCheckBox = new JCheckBox("Dengan sopir");
    private final JComboBox<PaymentMethod> paymentMethodCombo = new JComboBox<>();
    private final JLabel totalLabel = new JLabel("-");

    private final DefaultTableModel tableModel = new DefaultTableModel(
            new Object[] {"ID", "Pelanggan", "Kendaraan", "Tanggal Sewa", "Tanggal Kembali", "Total", "Status"}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final JTable rentalTable = new JTable(tableModel);

    private final JButton saveButton = new JButton("Simpan");
    private final JButton updateButton = new JButton("Ubah");
    private final JButton cancelButton = new JButton("Batalkan");
    private final JButton receiptButton = new JButton("Cetak Struk");

    public RentalPanel() {
        super(new BorderLayout(8, 8));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        startDateSpinner.setEditor(new JSpinner.DateEditor(startDateSpinner, "dd-MM-yyyy"));
        dueDateSpinner.setEditor(new JSpinner.DateEditor(dueDateSpinner, "dd-MM-yyyy"));
        startDateSpinner.setValue(today());
        dueDateSpinner.setValue(addDays(today(), 1));

        buildForm();
        buildTable();

        customerCombo.addActionListener(event -> calculateTotal());
        vehicleCombo.addActionListener(event -> updateDriverCheckbox());
        paymentMethodCombo.addActionListener(event -> calculateTotal());
        withDriverCheckBox.addActionListener(event -> calculateTotal());
        startDateSpinner.addChangeListener(event -> calculateTotal());
        dueDateSpinner.addChangeListener(event -> calculateTotal());
    }

    public void setController(RentalController value) {
        controller = value;
    }

    public void setCustomerOptions(List<Customer> customers) {
        customerCombo.removeAllItems();
        for (Customer customer : customers) {
            customerCombo.addItem(customer);
        }
    }

    public void setVehicleOptions(List<Vehicle> vehicles) {
        vehicleCombo.removeAllItems();
        for (Vehicle vehicle : vehicles) {
            vehicleCombo.addItem(vehicle);
        }
        updateDriverCheckbox();
    }

    public void setPaymentMethods(PaymentMethod[] methods) {
        paymentMethodCombo.removeAllItems();
        for (PaymentMethod method : methods) {
            paymentMethodCombo.addItem(method);
        }
    }

    public void setTableData(List<Rental> data) {
        rentals.clear();
        rentals.addAll(data);
        tableModel.setRowCount(0);
        for (Rental rental : data) {
            tableModel.addRow(new Object[] {
                    rental.getId(),
                    rental.getCustomer().getName(),
                    rental.getVehicle().getPlateNumber(),
                    rental.getStartDate().format(DATE_FORMAT),
                    rental.getDueDate().format(DATE_FORMAT),
                    CurrencyFormatter.formatRupiah(rental.getTotalCost()),
                    rental.getStatus()
            });
        }
    }

    public void setTotalCost(String value) {
        totalLabel.setText(value);
    }

    public void showReceipt(String text) {
        JTextArea area = new JTextArea(text);
        area.setEditable(false);
        area.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));
        area.setRows(12);
        area.setColumns(40);
        JOptionPane.showMessageDialog(this, new JScrollPane(area),
                "Struk Transaksi", JOptionPane.INFORMATION_MESSAGE);
    }

    public void resetForm() {
        startDateSpinner.setValue(today());
        dueDateSpinner.setValue(addDays(today(), 1));
        withDriverCheckBox.setSelected(false);
        withDriverCheckBox.setEnabled(true);
        if (customerCombo.getItemCount() > 0) {
            customerCombo.setSelectedIndex(0);
        }
        if (vehicleCombo.getItemCount() > 0) {
            vehicleCombo.setSelectedIndex(0);
        }
        if (paymentMethodCombo.getItemCount() > 0) {
            paymentMethodCombo.setSelectedIndex(0);
        }
        rentalTable.clearSelection();
        totalLabel.setText("-");
    }

    public Customer getSelectedCustomer() {
        return (Customer) customerCombo.getSelectedItem();
    }

    public Vehicle getSelectedVehicle() {
        return (Vehicle) vehicleCombo.getSelectedItem();
    }

    public Rental getSelectedRental() {
        int index = getSelectedIndex();
        return index >= 0 && index < rentals.size() ? rentals.get(index) : null;
    }

    public Date getStartDate() {
        return (Date) startDateSpinner.getValue();
    }

    public Date getDueDate() {
        return (Date) dueDateSpinner.getValue();
    }

    public boolean isWithDriver() {
        return withDriverCheckBox.isSelected();
    }

    public PaymentMethod getPaymentMethod() {
        return (PaymentMethod) paymentMethodCombo.getSelectedItem();
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
        return rentalTable.getSelectedRow();
    }

    private void buildForm() {
        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(BorderFactory.createTitledBorder("Form Transaksi"));
        addRow(form, 0, "Pelanggan", customerCombo);
        addRow(form, 1, "Kendaraan", vehicleCombo);
        addRow(form, 2, "Tanggal sewa", startDateSpinner);
        addRow(form, 3, "Tanggal kembali", dueDateSpinner);

        GridBagConstraints checkConstraints = new GridBagConstraints();
        checkConstraints.gridx = 0;
        checkConstraints.gridy = 4;
        checkConstraints.gridwidth = 2;
        checkConstraints.anchor = GridBagConstraints.WEST;
        checkConstraints.insets = new Insets(4, 4, 4, 4);
        form.add(withDriverCheckBox, checkConstraints);

        addRow(form, 5, "Metode pembayaran", paymentMethodCombo);

        GridBagConstraints captionConstraints = new GridBagConstraints();
        captionConstraints.gridx = 0;
        captionConstraints.gridy = 6;
        captionConstraints.anchor = GridBagConstraints.WEST;
        captionConstraints.insets = new Insets(4, 4, 4, 8);
        form.add(new JLabel("Total biaya"), captionConstraints);

        totalLabel.setFont(totalLabel.getFont().deriveFont(Font.BOLD, 15f));
        GridBagConstraints totalConstraints = new GridBagConstraints();
        totalConstraints.gridx = 1;
        totalConstraints.gridy = 6;
        totalConstraints.anchor = GridBagConstraints.WEST;
        totalConstraints.insets = new Insets(4, 0, 4, 4);
        form.add(totalLabel, totalConstraints);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        saveButton.addActionListener(event -> {
            if (controller != null) controller.saveRental();
        });
        updateButton.addActionListener(event -> {
            if (controller != null) controller.updateRental();
        });
        cancelButton.addActionListener(event -> {
            if (controller != null) controller.cancelRental();
        });
        receiptButton.addActionListener(event -> {
            if (controller != null) controller.printReceipt();
        });
        actions.add(saveButton);
        actions.add(updateButton);
        actions.add(cancelButton);
        actions.add(receiptButton);

        GridBagConstraints actionConstraints = new GridBagConstraints();
        actionConstraints.gridx = 0;
        actionConstraints.gridy = 7;
        actionConstraints.gridwidth = 2;
        actionConstraints.weightx = 1;
        actionConstraints.fill = GridBagConstraints.HORIZONTAL;
        actionConstraints.insets = new Insets(8, 4, 4, 4);
        form.add(actions, actionConstraints);

        add(form, BorderLayout.NORTH);
    }

    private void buildTable() {
        rentalTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        rentalTable.setFillsViewportHeight(true);
        add(new JScrollPane(rentalTable), BorderLayout.CENTER);
    }

    private void updateDriverCheckbox() {
        boolean bus = getSelectedVehicle() instanceof Bus;
        if (bus) {
            withDriverCheckBox.setSelected(true);
            withDriverCheckBox.setEnabled(false);
        } else {
            if (!withDriverCheckBox.isEnabled()) {
                withDriverCheckBox.setSelected(false);
            }
            withDriverCheckBox.setEnabled(true);
        }
        calculateTotal();
    }

    private void calculateTotal() {
        if (controller != null) {
            controller.calculateTotal();
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

    private static Date today() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTime();
    }

    private static Date addDays(Date date, int days) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.DAY_OF_MONTH, days);
        return calendar.getTime();
    }
}
