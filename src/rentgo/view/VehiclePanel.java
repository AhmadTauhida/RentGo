package rentgo.view;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.time.Year;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
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
    private static final int LABEL_WIDTH = 115;

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

    private final JTextField searchField = new JTextField(20);
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
    private final JLabel countLabel = UiStyle.mutedLabel("0 kendaraan");

    private final JButton addButton = new JButton("Tambah");
    private final JButton updateButton = new JButton("Ubah");
    private final JButton deleteButton = new JButton("Hapus");
    private final JButton clearButton = new JButton("Bersihkan");

    public VehiclePanel() {
        super(new BorderLayout(16, 16));
        setBackground(UiStyle.PAGE);
        setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));

        ButtonGroup transmissionGroup = new ButtonGroup();
        transmissionGroup.add(manualRadio);
        transmissionGroup.add(automaticRadio);
        manualRadio.setSelected(true);
        manualRadio.setOpaque(false);
        automaticRadio.setOpaque(false);
        transmissionPanel.setOpaque(false);
        transmissionPanel.add(manualRadio);
        transmissionPanel.add(Box.createHorizontalStrut(12));
        transmissionPanel.add(automaticRadio);
        specificFields.setOpaque(false);

        UiStyle.plainYear(manufactureYearSpinner);
        UiStyle.placeholder(plateNumberField, "Contoh: H 1234 AB");
        UiStyle.placeholder(brandField, "Contoh: Toyota Avanza");
        UiStyle.placeholder(baseRateField, "Contoh: 350000");
        UiStyle.placeholder(searchField, "Cari plat nomor atau merek");

        buildHeader();
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
        countLabel.setText(data.size() + " kendaraan");
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

    private void buildHeader() {
        JPanel header = new JPanel(new BorderLayout(16, 0));
        header.setOpaque(false);
        header.add(UiStyle.pageTitle("Data Kendaraan"), BorderLayout.WEST);

        JPanel searchBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        searchBar.setOpaque(false);
        searchBar.add(searchField);
        searchBar.add(searchTypeCombo);
        searchBar.add(searchStatusCombo);

        JButton searchButton = new JButton("Cari");
        UiStyle.primary(searchButton);
        searchButton.addActionListener(event -> search());
        searchBar.add(searchButton);
        header.add(searchBar, BorderLayout.EAST);

        searchField.addActionListener(event -> search());
        searchTypeCombo.addActionListener(event -> search());
        searchStatusCombo.addActionListener(event -> search());
        add(header, BorderLayout.NORTH);
    }

    private void buildForm() {
        UiStyle.Card card = new UiStyle.Card(new BorderLayout(0, 12));
        card.setPreferredSize(new Dimension(350, 0));
        card.add(UiStyle.sectionTitle("Form Kendaraan"), BorderLayout.NORTH);

        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
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
        form.add(specificFields, specificConstraints);

        // Filler row: pushes all fields to the top instead of centering them vertically.
        GridBagConstraints filler = new GridBagConstraints();
        filler.gridx = 0;
        filler.gridy = 7;
        filler.gridwidth = 2;
        filler.weighty = 1;
        JPanel fillerPanel = new JPanel();
        fillerPanel.setOpaque(false);
        form.add(fillerPanel, filler);
        card.add(form, BorderLayout.CENTER);

        JPanel actions = new JPanel(new GridLayout(2, 2, 8, 8));
        actions.setOpaque(false);
        UiStyle.primary(addButton);
        UiStyle.danger(deleteButton);
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
        card.add(actions, BorderLayout.SOUTH);

        add(card, BorderLayout.WEST);
    }

    private void buildTable() {
        vehicleTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        UiStyle.table(vehicleTable);
        vehicleTable.getColumnModel().getColumn(0).setMaxWidth(60);
        vehicleTable.getColumnModel().getColumn(3).setMaxWidth(80);
        vehicleTable.getColumnModel().getColumn(4).setCellRenderer(UiStyle.rightRenderer());
        vehicleTable.getColumnModel().getColumn(6).setCellRenderer(UiStyle.statusRenderer());
        vehicleTable.getSelectionModel().addListSelectionListener(event -> {
            if (!event.getValueIsAdjusting() && controller != null) {
                controller.selectRow(getSelectedIndex());
            }
        });

        UiStyle.Card card = new UiStyle.Card(new BorderLayout(0, 12));
        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);
        top.add(UiStyle.sectionTitle("Daftar Kendaraan"), BorderLayout.WEST);
        top.add(countLabel, BorderLayout.EAST);
        card.add(top, BorderLayout.NORTH);

        JScrollPane scrollPane = new JScrollPane(vehicleTable);
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

    private void updateSpecificFields() {
        specificFields.removeAll();
        VehicleType type = getVehicleType();
        if (type != null) {
            switch (type) {
                case CAR:
                    addSpecificRow(0, "Penumpang", passengerCapacitySpinner);
                    addSpecificRow(1, "Transmisi", transmissionPanel);
                    break;
                case MOTORCYCLE:
                    addSpecificRow(0, "Mesin (CC)", engineCcSpinner);
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
        addLabeled(form, row, label, component, new Insets(6, 0, 6, 8), new Insets(6, 0, 6, 0));
    }

    private void addSpecificRow(int row, String label, Component component) {
        addLabeled(specificFields, row, label, component, new Insets(6, 0, 6, 8), new Insets(6, 0, 6, 0));
    }

    private void addLabeled(JPanel target, int row, String label, Component component,
            Insets labelInsets, Insets fieldInsets) {
        JLabel labelComponent = new JLabel(label);
        labelComponent.setPreferredSize(new Dimension(LABEL_WIDTH, labelComponent.getPreferredSize().height));
        GridBagConstraints labelConstraints = new GridBagConstraints();
        labelConstraints.gridx = 0;
        labelConstraints.gridy = row;
        labelConstraints.anchor = GridBagConstraints.WEST;
        labelConstraints.insets = labelInsets;
        target.add(labelComponent, labelConstraints);

        GridBagConstraints fieldConstraints = new GridBagConstraints();
        fieldConstraints.gridx = 1;
        fieldConstraints.gridy = row;
        fieldConstraints.weightx = 1;
        fieldConstraints.fill = GridBagConstraints.HORIZONTAL;
        fieldConstraints.insets = fieldInsets;
        target.add(component, fieldConstraints);
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