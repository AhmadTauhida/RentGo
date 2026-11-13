package rentgo.view;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

import rentgo.controller.ReportController;
import rentgo.util.CurrencyFormatter;

/** Report panel. */
public class ReportPanel extends JPanel {
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd-MM-yyyy");
    private static final String[] PERIOD_LABELS = {
            "Hari ini", "7 hari terakhir", "Bulan ini", "Tahun ini", "Semua"
    };
    private static final String[] PERIOD_CODES = {
            "TODAY", "LAST_7", "THIS_MONTH", "THIS_YEAR", "ALL"
    };

    private ReportController controller;

    private final JComboBox<String> periodCombo = new JComboBox<>(PERIOD_LABELS);
    private final DefaultTableModel revenueModel = new DefaultTableModel(
            new Object[] {"Tanggal", "Jumlah Transaksi", "Pendapatan"}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final JTable revenueTable = new JTable(revenueModel);
    private final DefaultTableModel topVehiclesModel = new DefaultTableModel(
            new Object[] {"Plat Nomor", "Merek", "Jenis", "Jumlah Sewa"}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final JTable topVehiclesTable = new JTable(topVehiclesModel);
    private final JLabel totalLabel = new JLabel("Total pendapatan: -");

    public ReportPanel() {
        super(new BorderLayout(8, 8));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        buildPeriodBar();
        buildTables();
        periodCombo.addActionListener(event -> loadReport());
    }

    public void setController(ReportController value) {
        controller = value;
    }

    public void setRevenueData(List<Object[]> data) {
        revenueModel.setRowCount(0);
        double total = 0;
        for (Object[] row : data) {
            LocalDate day = (LocalDate) row[0];
            long count = (Long) row[1];
            double amount = (Double) row[2];
            total += amount;
            revenueModel.addRow(new Object[] {
                    day.format(DATE_FORMAT),
                    count,
                    CurrencyFormatter.formatRupiah(amount)
            });
        }
        totalLabel.setText("Total pendapatan: " + CurrencyFormatter.formatRupiah(total));
    }

    public void setTopVehiclesData(List<Object[]> data) {
        topVehiclesModel.setRowCount(0);
        for (Object[] row : data) {
            topVehiclesModel.addRow(new Object[] {row[0], row[1], row[2], row[3]});
        }
    }

    public String getPeriod() {
        int index = periodCombo.getSelectedIndex();
        return index >= 0 && index < PERIOD_CODES.length ? PERIOD_CODES[index] : "ALL";
    }

    public void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Kesalahan", JOptionPane.ERROR_MESSAGE);
    }

    public void showInfo(String message) {
        JOptionPane.showMessageDialog(this, message, "Informasi", JOptionPane.INFORMATION_MESSAGE);
    }

    private void buildPeriodBar() {
        JPanel periodBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        periodBar.add(new JLabel("Periode"));
        periodBar.add(periodCombo);
        add(periodBar, BorderLayout.NORTH);
    }

    private void buildTables() {
        revenueTable.setFillsViewportHeight(true);
        topVehiclesTable.setFillsViewportHeight(true);

        JScrollPane revenueScroll = new JScrollPane(revenueTable);
        revenueScroll.setBorder(BorderFactory.createTitledBorder("Pendapatan"));
        JScrollPane topScroll = new JScrollPane(topVehiclesTable);
        topScroll.setBorder(BorderFactory.createTitledBorder("Kendaraan Terlaris"));

        JPanel tables = new JPanel(new GridLayout(2, 1, 8, 8));
        tables.add(revenueScroll);
        tables.add(topScroll);
        add(tables, BorderLayout.CENTER);

        JPanel footer = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        footer.add(totalLabel);
        add(footer, BorderLayout.SOUTH);
    }

    private void loadReport() {
        if (controller != null) {
            controller.loadReport();
        }
    }
}
