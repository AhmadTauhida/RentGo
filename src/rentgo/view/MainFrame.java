package rentgo.view;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Dimension;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;

import rentgo.controller.CustomerController;
import rentgo.controller.LoginController;
import rentgo.controller.RentalController;
import rentgo.controller.ReportController;
import rentgo.controller.UserManagementController;
import rentgo.controller.VehicleController;
import rentgo.controller.VehicleReturnController;
import rentgo.dao.CustomerDAO;
import rentgo.dao.PaymentDAO;
import rentgo.dao.RentalDAO;
import rentgo.dao.ReportDAO;
import rentgo.dao.StaffDAO;
import rentgo.dao.VehicleDAO;
import rentgo.dao.VehicleReturnDAO;
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
    private final Map<String, Runnable> loaders = new HashMap<>();

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
        openCard(VEHICLE);
    }

    private void buildCards() {
        VehiclePanel vehiclePanel = new VehiclePanel();
        VehicleController vehicleController = new VehicleController(vehiclePanel, new VehicleDAO());
        vehiclePanel.setController(vehicleController);
        cards.add(vehiclePanel, VEHICLE);
        loaders.put(VEHICLE, vehicleController::loadData);

        CustomerPanel customerPanel = new CustomerPanel();
        CustomerController customerController = new CustomerController(customerPanel, new CustomerDAO());
        customerPanel.setController(customerController);
        cards.add(customerPanel, CUSTOMER);
        loaders.put(CUSTOMER, customerController::loadData);

        RentalPanel rentalPanel = new RentalPanel();
        RentalController rentalController = new RentalController(rentalPanel,
                new RentalDAO(), new CustomerDAO(), new VehicleDAO(), new PaymentDAO());
        rentalPanel.setController(rentalController);
        cards.add(rentalPanel, RENTAL);
        loaders.put(RENTAL, () -> {
            rentalController.loadOptions();
            rentalController.loadHistory();
        });

        VehicleReturnPanel returnPanel = new VehicleReturnPanel();
        VehicleReturnController returnController = new VehicleReturnController(
                returnPanel, new RentalDAO(), new VehicleReturnDAO());
        returnPanel.setController(returnController);
        cards.add(returnPanel, RETURN);
        loaders.put(RETURN, returnController::loadActiveRentals);

        ReportPanel reportPanel = new ReportPanel();
        ReportController reportController = new ReportController(reportPanel, new ReportDAO());
        reportPanel.setController(reportController);
        cards.add(reportPanel, REPORT);
        loaders.put(REPORT, reportController::loadReport);

        Staff staff = Session.getStaff();
        if (staff != null && staff.isAdmin()) {
            UserManagementPanel usersPanel = new UserManagementPanel();
            UserManagementController usersController = new UserManagementController(usersPanel, new StaffDAO());
            usersPanel.setController(usersController);
            cards.add(usersPanel, USERS);
            loaders.put(USERS, usersController::loadData);
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
        item.addActionListener(event -> openCard(card));
        return item;
    }

    private void openCard(String card) {
        showCard(card);
        Runnable loader = loaders.get(card);
        if (loader != null) {
            loader.run();
        }
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
