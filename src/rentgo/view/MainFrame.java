package rentgo.view;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.util.HashMap;
import java.util.Map;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JToggleButton;
import javax.swing.SwingConstants;

import rentgo.controller.LoginController;
import rentgo.model.Staff;
import rentgo.util.Session;

/** Main window with sidebar navigation, card layout, and status bar. */
public class MainFrame extends JFrame {
    public static final String VEHICLE = "VEHICLE";
    public static final String CUSTOMER = "CUSTOMER";
    public static final String RENTAL = "RENTAL";
    public static final String RETURN = "RETURN";
    public static final String REPORT = "REPORT";
    public static final String USERS = "USERS";

    private static final Color SIDEBAR = Theme.PRIMARY_DARK;
    private static final String NAV_STYLE =
            "background: " + Theme.PRIMARY_DARK_HEX + "; foreground: #DCEFDD; "
            + "hoverBackground: " + Theme.PRIMARY_HEX + "; pressedBackground: #2E8B43; "
            + "selectedBackground: " + Theme.ACCENT_HEX + "; selectedForeground: " + Theme.PRIMARY_DARK_HEX + "; "
            + "arc: 10; borderWidth: 0; focusWidth: 0; margin: 10,16,10,16";
    private static final String LOGOUT_STYLE =
            "background: " + Theme.PRIMARY_DARK_HEX + "; foreground: " + Theme.ACCENT_LIGHT_HEX + "; "
            + "hoverBackground: " + Theme.PRIMARY_HEX + "; pressedBackground: #2E8B43; "
            + "arc: 10; borderWidth: 0; focusWidth: 0; margin: 10,16,10,16";

    private final CardLayout cardLayout = new CardLayout();
    private final JPanel cards = new JPanel(cardLayout);
    private final ButtonGroup navGroup = new ButtonGroup();
    private final Map<String, JToggleButton> navButtons = new HashMap<>();

    public MainFrame() {
        super("RentGo");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(1000, 650));
        setSize(1200, 760);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        buildCards();
        add(buildSidebar(), BorderLayout.WEST);

        JPanel content = new JPanel(new BorderLayout());
        content.add(cards, BorderLayout.CENTER);
        content.add(buildStatusBar(), BorderLayout.SOUTH);
        add(content, BorderLayout.CENTER);

        showCard(VEHICLE);
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

    private JPanel buildSidebar() {
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBackground(SIDEBAR);
        sidebar.setBorder(BorderFactory.createEmptyBorder(24, 12, 16, 12));
        sidebar.setPreferredSize(new Dimension(200, 0));

        JLabel brand = new JLabel("<html>Rent<span style='color:" + Theme.ACCENT_HEX + "'>Go</span></html>");
        brand.setForeground(Color.WHITE);
        brand.setFont(brand.getFont().deriveFont(Font.BOLD, 24f));
        brand.setAlignmentX(Component.LEFT_ALIGNMENT);
        brand.setBorder(BorderFactory.createEmptyBorder(0, 16, 0, 0));
        sidebar.add(brand);

        JLabel tagline = new JLabel("Rental Kendaraan");
        tagline.setForeground(Theme.PRIMARY_SOFT);
        tagline.setAlignmentX(Component.LEFT_ALIGNMENT);
        tagline.setBorder(BorderFactory.createEmptyBorder(0, 16, 0, 0));
        sidebar.add(tagline);
        sidebar.add(Box.createVerticalStrut(24));

        addSection(sidebar, "DATA");
        sidebar.add(navButton("Kendaraan", VEHICLE));
        sidebar.add(navButton("Pelanggan", CUSTOMER));

        addSection(sidebar, "TRANSAKSI");
        sidebar.add(navButton("Penyewaan", RENTAL));
        sidebar.add(navButton("Pengembalian", RETURN));

        addSection(sidebar, "ANALISIS");
        sidebar.add(navButton("Laporan", REPORT));

        Staff staff = Session.getStaff();
        if (staff != null && staff.isAdmin()) {
            addSection(sidebar, "AKUN");
            sidebar.add(navButton("Pengguna", USERS));
        }

        sidebar.add(Box.createVerticalGlue());
        sidebar.add(logoutButton());
        return sidebar;
    }

    private void addSection(JPanel sidebar, String title) {
        JLabel label = new JLabel(title);
        label.setForeground(new Color(0x8FD19B));
        label.setFont(label.getFont().deriveFont(Font.BOLD, 11f));
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        label.setBorder(BorderFactory.createEmptyBorder(14, 16, 6, 0));
        sidebar.add(label);
    }

    private JToggleButton navButton(String label, String card) {
        JToggleButton button = new JToggleButton(label);
        button.putClientProperty("FlatLaf.style", NAV_STYLE);
        button.setHorizontalAlignment(SwingConstants.LEFT);
        button.setFocusable(false);
        button.setAlignmentX(Component.LEFT_ALIGNMENT);
        button.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        button.addActionListener(event -> showCard(card));
        navGroup.add(button);
        navButtons.put(card, button);
        return button;
    }

    private JButton logoutButton() {
        JButton button = new JButton("Logout");
        button.putClientProperty("FlatLaf.style", LOGOUT_STYLE);
        button.setHorizontalAlignment(SwingConstants.LEFT);
        button.setFocusable(false);
        button.setAlignmentX(Component.LEFT_ALIGNMENT);
        button.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        button.addActionListener(event -> LoginController.logout());
        return button;
    }

    private JPanel buildStatusBar() {
        Staff staff = Session.getStaff();
        String text = staff == null
                ? "Belum masuk"
                : "Masuk sebagai " + staff.getName() + " (" + staff.getRole() + ")";

        JLabel label = new JLabel(text);
        label.setForeground(UiStyle.TEXT_MUTED);

        JPanel bar = new JPanel(new BorderLayout());
        bar.setBackground(Theme.CARD);
        bar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, UiStyle.BORDER),
                BorderFactory.createEmptyBorder(6, 16, 6, 16)));
        bar.add(label, BorderLayout.WEST);
        return bar;
    }

    public void showCard(String name) {
        cardLayout.show(cards, name);
        JToggleButton button = navButtons.get(name);
        if (button != null) {
            button.setSelected(true);
        }
    }
}