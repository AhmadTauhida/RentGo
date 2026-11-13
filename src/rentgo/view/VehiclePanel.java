package rentgo.view;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.time.Year;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.table.DefaultTableModel;

import rentgo.controller.VehicleController;
import rentgo.model.Bus;
import rentgo.model.Car;
import rentgo.model.Motorcycle;
import rentgo.model.Staff;
import rentgo.model.Vehicle;
import rentgo.model.VehicleStatus;
import rentgo.model.VehicleType;
import rentgo.util.CurrencyFormatter;
import rentgo.util.Session;

/** Vehicle management panel. */
public class VehiclePanel extends JPanel {
    private VehicleController controller;
    private final List<Vehicle> vehicles = new ArrayList<>();

    private final JTextField plateNumberField = new JTextField(14);
    private final JTextField brandField = new JTextField(14);
    private final JSpinner manufactureYearSpinner = new JSpinner(
            new SpinnerNumberModel(Year.now().getValue(), 1980, Year.now().getValue() + 1, 1));
    private final JTextField baseRateField = new JTextField(14);
    private final JComboBox<VehicleType> typeCombo = new JComboBox<>(VehicleType.values());
    private final JComboBox<VehicleStatus> statusCombo = new JComboBox<>(VehicleStatus.values());

    private final JSpinner passengerCapacitySpinner = new JSpinner(new SpinnerNumberModel(4, 1, 100, 1));
    private final JRadioButton manualRadio = new JRadioButton("Manual");
    private final JRadioButton automaticRadio = new JRadioButton("Otomatis");
    private final JPanel transmissionPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
    private final JSpinner engineCcSpinner = new JSpinner(new SpinnerNumberModel(150, 1, 5000, 1));
    private final JSpinner seatCountSpinner = new JSpinner(new SpinnerNumberModel(30, 1, 200, 1));
    private final JPanel specificFields = new JPanel(new GridBagLayout());

    private final JTextField searchField = new JTextField(14);
    private final JComboBox<VehicleType> searchTypeCombo = createTypeFilter();
    private final JComboBox<VehicleStatus> searchStatusCombo = createStatusFilter();

    private final DefaultTableModel tableModel = new DefaultTableModel(
            new Object[] {"ID", "Plat Nomor", "Merek", "Tahun", "Tarif", "Jenis", "Status"}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final JTable vehicleTable = new JTable(tableModel);

    private final JButton addButton = new JButton("Tambah");
    private final JButton updateButton = new JButton("Ubah");
    private final JButton deleteButton = new JButton("Hapus");
    private final JButton clearButton = new JButton("Bersihkan");

    public VehiclePanel() {
        super(new BorderLayout(8, 8));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        ButtonGroup transmissionGroup = new ButtonGroup();
        transmissionGroup.add(manualRadio);
        transmissionGroup.add(automaticRadio);
        manualRadio.setSelected(true);
        transmissionPanel.add(manualRadio);
        transmissionPanel.add(automaticRadio);

        buildSearchBar();
        buildForm();
        buildTable();

        typeCombo.addActionListener(event -> updateSpecificFields());
        applyRole();
        updateSpecificFields();
    }

    public void setController(VehicleController value) {
        controller = value;
    }

    public void setTableData(List<Vehicle> data) {
        vehicles.clear();
        vehicles.addAll(data);
        tableModel.setRowCount(0);
        for (Vehicle vehicle : data) {
            tableModel.addRow(new Object[] {
                    vehicle.getId(),
                    vehicle.getPlateNumber(),
                    vehicle.getBrand(),
                    vehicle.getManufactureYear(),
                    CurrencyFormatter.formatRupiah(vehicle.getBaseRate()),
                    vehicle.getType(),
                    vehicle.getStatus()
            });
        }
    }

    public void setForm(Vehicle vehicle) {
        if (vehicle == null) {
            clearForm();
            return;
        }
        plateNumberField.setText(vehicle.getPlateNumber());
        brandField.setText(vehicle.getBrand());
        manufactureYearSpinner.setValue(vehicle.getManufactureYear());
        baseRateField.setText(String.valueOf(vehicle.getBaseRate()));
        typeCombo.setSelectedItem(vehicle.getType());
        statusCombo.setSelectedItem(vehicle.getStatus());

        if (vehicle instanceof Car car) {
            passengerCapacitySpinner.setValue(car.getPassengerCapacity());
            if ("AUTOMATIC".equals(car.getTransmission())) {
                automaticRadio.setSelected(true);
            } else {
                manualRadio.setSelected(true);
            }
        } else if (vehicle instanceof Motorcycle motorcycle) {
            engineCcSpinner.setValue(motorcycle.getEngineCc());
        } else if (vehicle instanceof Bus bus) {
            seatCountSpinner.setValue(bus.getSeatCount());
        }
        updateSpecificFields();
    }

    public void clearForm() {
        plateNumberField.setText("");
        brandField.setText("");
        manufactureYearSpinner.setValue(Year.now().getValue());
        baseRateField.setText("");
        typeCombo.setSelectedItem(VehicleType.CAR);
        statusCombo.setSelectedItem(VehicleStatus.AVAILABLE);
        passengerCapacitySpinner.setValue(4);
        engineCcSpinner.setValue(150);
        seatCountSpinner.setValue(30);
        manualRadio.setSelected(true);
        vehicleTable.clearSelection();
        updateSpecificFields();
    }

    public Vehicle getSelectedVehicle() {
        int index = getSelectedIndex();
        return index >= 0 && index < vehicles.size() ? vehicles.get(index) : null;
    }

    public String getPlateNumber() {
        return plateNumberField.getText().trim();
    }

    public String getBrand() {
        return brandField.getText().trim();
    }

    public String getManufactureYear() {
        return String.valueOf(manufactureYearSpinner.getValue());
    }

    public String getBaseRate() {
        return baseRateField.getText().trim();
    }

    public String getPassengerCapacity() {
        return String.valueOf(passengerCapacitySpinner.getValue());
    }

    public String getTransmission() {
        return automaticRadio.isSelected() ? "AUTOMATIC" : "MANUAL";
    }

    public String getEngineCc() {
        return String.valueOf(engineCcSpinner.getValue());
    }

    public String getSeatCount() {
        return String.valueOf(seatCountSpinner.getValue());
    }

    public VehicleType getVehicleType() {
        return (VehicleType) typeCombo.getSelectedItem();
    }

    public VehicleStatus getVehicleStatus() {
        return (VehicleStatus) statusCombo.getSelectedItem();
    }

    public String getSearchKeyword() {
        return searchField.getText().trim();
    }

    public VehicleType getSearchType() {
        return (VehicleType) searchTypeCombo.getSelectedItem();
    }

    public VehicleStatus getSearchStatus() {
        return (VehicleStatus) searchStatusCombo.getSelectedItem();
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
        return vehicleTable.getSelectedRow();
    }

    private void buildSearchBar() {
        JPanel searchBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        searchBar.add(new JLabel("Cari"));
        searchBar.add(searchField);
        searchBar.add(new JLabel("Jenis"));
        searchBar.add(searchTypeCombo);
        searchBar.add(new JLabel("Status"));
        searchBar.add(searchStatusCombo);

        JButton searchButton = new JButton("Cari");
        searchButton.addActionListener(event -> search());
        searchBar.add(searchButton);

        searchField.addActionListener(event -> search());
        searchTypeCombo.addActionListener(event -> search());
        searchStatusCombo.addActionListener(event -> search());
        add(searchBar, BorderLayout.NORTH);
    }

    private void buildForm() {
        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(BorderFactory.createTitledBorder("Form Kendaraan"));
        addRow(form, 0, "Plat nomor", plateNumberField);
        addRow(form, 1, "Merek", brandField);
        addRow(form, 2, "Tahun produksi", manufactureYearSpinner);
        addRow(form, 3, "Tarif dasar (Rp)", baseRateField);
        addRow(form, 4, "Jenis", typeCombo);
        addRow(form, 5, "Status", statusCombo);

        GridBagConstraints specificConstraints = new GridBagConstraints();
        specificConstraints.gridx = 0;
        specificConstraints.gridy = 6;
        specificConstraints.gridwidth = 2;
        specificConstraints.weightx = 1;
        specificConstraints.fill = GridBagConstraints.HORIZONTAL;
        specificConstraints.insets = new Insets(4, 4, 4, 4);
        form.add(specificFields, specificConstraints);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        addButton.addActionListener(event -> {
            if (controller != null) controller.addVehicle();
        });
        updateButton.addActionListener(event -> {
            if (controller != null) controller.updateVehicle();
        });
        deleteButton.addActionListener(event -> {
            if (controller != null) controller.deleteVehicle();
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
        actionConstraints.gridy = 7;
        actionConstraints.gridwidth = 2;
        actionConstraints.weightx = 1;
        actionConstraints.fill = GridBagConstraints.HORIZONTAL;
        actionConstraints.insets = new Insets(8, 4, 4, 4);
        form.add(actions, actionConstraints);

        add(form, BorderLayout.WEST);
    }

    private void buildTable() {
        vehicleTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        vehicleTable.setFillsViewportHeight(true);
        vehicleTable.getSelectionModel().addListSelectionListener(event -> {
            if (!event.getValueIsAdjusting() && controller != null) {
                controller.selectRow(getSelectedIndex());
            }
        });
        add(new JScrollPane(vehicleTable), BorderLayout.CENTER);
    }

    private void search() {
        if (controller != null) {
            controller.search();
        }
    }

    private void updateSpecificFields() {
        specificFields.removeAll();
        VehicleType type = getVehicleType();
        if (type != null) {
            switch (type) {
                case CAR:
                    addSpecificRow(0, "Kapasitas penumpang", passengerCapacitySpinner);
                    addSpecificRow(1, "Transmisi", transmissionPanel);
                    break;
                case MOTORCYCLE:
                    addSpecificRow(0, "Kapasitas mesin (CC)", engineCcSpinner);
                    break;
                case BUS:
                    addSpecificRow(0, "Jumlah kursi", seatCountSpinner);
                    break;
                default:
                    break;
            }
        }
        specificFields.revalidate();
        specificFields.repaint();
    }

    private void applyRole() {
        Staff staff = Session.getStaff();
        if (staff != null && !staff.isAdmin()) {
            addButton.setEnabled(false);
            updateButton.setEnabled(false);
            deleteButton.setEnabled(false);
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

    private void addSpecificRow(int row, String label, Component component) {
        GridBagConstraints labelConstraints = new GridBagConstraints();
        labelConstraints.gridx = 0;
        labelConstraints.gridy = row;
        labelConstraints.anchor = GridBagConstraints.WEST;
        labelConstraints.insets = new Insets(4, 0, 4, 8);
        specificFields.add(new JLabel(label), labelConstraints);

        GridBagConstraints fieldConstraints = new GridBagConstraints();
        fieldConstraints.gridx = 1;
        fieldConstraints.gridy = row;
        fieldConstraints.weightx = 1;
        fieldConstraints.fill = GridBagConstraints.HORIZONTAL;
        fieldConstraints.insets = new Insets(4, 0, 4, 0);
        specificFields.add(component, fieldConstraints);
    }

    private static JComboBox<VehicleType> createTypeFilter() {
        JComboBox<VehicleType> combo = new JComboBox<>();
        combo.addItem(null);
        for (VehicleType type : VehicleType.values()) {
            combo.addItem(type);
        }
        combo.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value,
                    int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                setText(value == null ? "Semua jenis" : value.toString());
                return this;
            }
        });
        return combo;
    }

    private static JComboBox<VehicleStatus> createStatusFilter() {
        JComboBox<VehicleStatus> combo = new JComboBox<>();
        combo.addItem(null);
        for (VehicleStatus status : VehicleStatus.values()) {
            combo.addItem(status);
        }
        combo.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value,
                    int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                setText(value == null ? "Semua status" : value.toString());
                return this;
            }
        });
        return combo;
    }
}
