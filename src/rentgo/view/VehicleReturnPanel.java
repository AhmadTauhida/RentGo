package rentgo.view;

import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerDateModel;

import rentgo.controller.VehicleReturnController;
import rentgo.model.Rental;
import rentgo.model.VehicleCondition;

/** Vehicle return panel. */
public class VehicleReturnPanel extends JPanel {
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd-MM-yyyy");

    private VehicleReturnController controller;

    private final JComboBox<Rental> rentalCombo = new JComboBox<>();
    private final JSpinner actualReturnDateSpinner = new JSpinner(new SpinnerDateModel());
    private final JComboBox<VehicleCondition> conditionCombo = new JComboBox<>(VehicleCondition.values());
    private final JLabel rentalInfoLabel = new JLabel("Pilih transaksi aktif");
    private final JLabel lateDaysLabel = new JLabel("Hari terlambat: 0 hari");
    private final JLabel lateFeeLabel = new JLabel("Denda: -");
    private final JButton saveButton = new JButton("Simpan");

    public VehicleReturnPanel() {
        super(new GridBagLayout());
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        actualReturnDateSpinner.setEditor(new JSpinner.DateEditor(actualReturnDateSpinner, "dd-MM-yyyy"));
        actualReturnDateSpinner.setValue(new Date());
        rentalCombo.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value,
                    int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof Rental rental) {
                    setText("#" + rental.getId() + " - " + rental.getVehicle().getPlateNumber()
                            + " (" + rental.getDueDate().format(DATE_FORMAT) + ")");
                }
                return this;
            }
        });

        buildForm();

        rentalCombo.addActionListener(event -> {
            updateRentalInfo();
            calculateLateFee();
        });
        conditionCombo.addActionListener(event -> calculateLateFee());
        actualReturnDateSpinner.addChangeListener(event -> calculateLateFee());
    }

    public void setController(VehicleReturnController value) {
        controller = value;
    }

    public void setRentalOptions(List<Rental> rentals) {
        rentalCombo.removeAllItems();
        for (Rental rental : rentals) {
            rentalCombo.addItem(rental);
        }
        updateRentalInfo();
    }

    public void setLateFeeInfo(int lateDays, String lateFeeText) {
        lateDaysLabel.setText("Hari terlambat: " + lateDays + " hari");
        lateFeeLabel.setText("Denda: " + lateFeeText);
    }

    public void resetForm() {
        actualReturnDateSpinner.setValue(new Date());
        conditionCombo.setSelectedItem(VehicleCondition.GOOD);
        rentalCombo.setSelectedIndex(rentalCombo.getItemCount() > 0 ? 0 : -1);
        rentalInfoLabel.setText("Pilih transaksi aktif");
        setLateFeeInfo(0, "-");
    }

    public Rental getSelectedRental() {
        return (Rental) rentalCombo.getSelectedItem();
    }

    public Date getActualReturnDate() {
        return (Date) actualReturnDateSpinner.getValue();
    }

    public VehicleCondition getCondition() {
        return (VehicleCondition) conditionCombo.getSelectedItem();
    }

    public void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Kesalahan", JOptionPane.ERROR_MESSAGE);
    }

    public void showInfo(String message) {
        JOptionPane.showMessageDialog(this, message, "Informasi", JOptionPane.INFORMATION_MESSAGE);
    }

    private void buildForm() {
        addRow(0, "Transaksi aktif", rentalCombo);
        addRow(1, "Tanggal kembali", actualReturnDateSpinner);
        addRow(2, "Kondisi kendaraan", conditionCombo);
        addSpanning(3, rentalInfoLabel);
        addSpanning(4, lateDaysLabel);
        addSpanning(5, lateFeeLabel);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        saveButton.addActionListener(event -> {
            if (controller != null) controller.saveReturn();
        });
        actions.add(saveButton);
        addSpanning(6, actions);

        GridBagConstraints filler = new GridBagConstraints();
        filler.gridx = 0;
        filler.gridy = 7;
        filler.gridwidth = 2;
        filler.weighty = 1;
        filler.fill = GridBagConstraints.BOTH;
        add(new JPanel(), filler);
    }

    private void updateRentalInfo() {
        Rental rental = getSelectedRental();
        if (rental == null) {
            rentalInfoLabel.setText("Pilih transaksi aktif");
            return;
        }
        rentalInfoLabel.setText("Rencana kembali: " + rental.getDueDate().format(DATE_FORMAT)
                + " | Pelanggan: " + rental.getCustomer().getName()
                + " | Kendaraan: " + rental.getVehicle());
    }

    private void calculateLateFee() {
        if (controller != null) {
            controller.calculateLateFee();
        }
    }

    private void addRow(int row, String label, Component component) {
        GridBagConstraints labelConstraints = new GridBagConstraints();
        labelConstraints.gridx = 0;
        labelConstraints.gridy = row;
        labelConstraints.anchor = GridBagConstraints.WEST;
        labelConstraints.insets = new Insets(4, 4, 4, 8);
        add(new JLabel(label), labelConstraints);

        GridBagConstraints fieldConstraints = new GridBagConstraints();
        fieldConstraints.gridx = 1;
        fieldConstraints.gridy = row;
        fieldConstraints.weightx = 1;
        fieldConstraints.fill = GridBagConstraints.HORIZONTAL;
        fieldConstraints.insets = new Insets(4, 0, 4, 4);
        add(component, fieldConstraints);
    }

    private void addSpanning(int row, Component component) {
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.gridx = 0;
        constraints.gridy = row;
        constraints.gridwidth = 2;
        constraints.weightx = 1;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.insets = new Insets(4, 4, 4, 4);
        add(component, constraints);
    }
}
