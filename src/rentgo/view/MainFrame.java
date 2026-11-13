package rentgo.view;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Dimension;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;

import rentgo.controller.LoginController;
import rentgo.model.Staff;
import rentgo.util.Session;

/** Main window with menu, card layout, and status bar. */
public class MainFrame extends JFrame {
    public static final String VEHICLE = "VEHICLE";
    public static final String CUSTOMER = "CUSTOMER";
    public static final String RENTAL = "RENTAL";
    public static final String RETURN = "RETURN";
    public static final String REPORT = "REPORT";
    public static final String USERS = "USERS";

    private final CardLayout cardLayout = new CardLayout();
    private final JPanel cards = new JPanel(cardLayout);

    public MainFrame() {
        super("RentGo");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(1000, 650));
        setSize(1200, 760);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        add(cards, BorderLayout.CENTER);
        buildCards();
        buildMenuBar();
        add(buildStatusBar(), BorderLayout.SOUTH);
    }

    private void buildCards() {
        cards.add(new VehiclePanel(), VEHICLE);
        cards.add(new CustomerPanel(), CUSTOMER);
        cards.add(new RentalPanel(), RENTAL);
        cards.add(new VehicleReturnPanel(), RETURN);
        cards.add(new ReportPanel(), REPORT);

        Staff staff = Session.getStaff();
        if (staff != null && staff.isAdmin()) {
            cards.add(new UserManagementPanel(), USERS);
        }
    }

    private void buildMenuBar() {
        JMenuBar menuBar = new JMenuBar();

        JMenu data = new JMenu("Data");
        data.add(cardItem("Kendaraan", VEHICLE));
        data.add(cardItem("Pelanggan", CUSTOMER));
        menuBar.add(data);

        JMenu transaction = new JMenu("Transaksi");
        transaction.add(cardItem("Penyewaan", RENTAL));
        transaction.add(cardItem("Pengembalian", RETURN));
        menuBar.add(transaction);

        menuBar.add(cardItem("Laporan", REPORT));

        Staff staff = Session.getStaff();
        if (staff != null && staff.isAdmin()) {
            JMenu account = new JMenu("Akun");
            account.add(cardItem("Pengguna", USERS));
            account.add(logoutItem());
            menuBar.add(account);
        } else {
            JMenu file = new JMenu("Berkas");
            file.add(logoutItem());
            menuBar.add(file);
        }

        setJMenuBar(menuBar);
    }

    private JMenuItem cardItem(String label, String card) {
        JMenuItem item = new JMenuItem(label);
        item.addActionListener(event -> showCard(card));
        return item;
    }

    private JMenuItem logoutItem() {
        JMenuItem item = new JMenuItem("Logout");
        item.addActionListener(event -> LoginController.logout());
        return item;
    }

    private JLabel buildStatusBar() {
        Staff staff = Session.getStaff();
        String text = staff == null
                ? "Belum masuk"
                : "Masuk sebagai " + staff.getName() + " (" + staff.getRole() + ")";
        return new JLabel("  " + text);
    }

    public void showCard(String name) {
        cardLayout.show(cards, name);
    }
}
