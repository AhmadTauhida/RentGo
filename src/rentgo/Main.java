package rentgo;

import java.awt.Font;
import java.awt.Insets;
import java.util.Collections;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import com.formdev.flatlaf.FlatLaf;
import com.formdev.flatlaf.FlatLightLaf;

import rentgo.controller.LoginController;
import rentgo.dao.DatabaseConnection;
import rentgo.dao.StaffDAO;
import rentgo.exception.DatabaseException;
import rentgo.view.LoginFrame;
import rentgo.view.Theme;

/** Starts the RentGo desktop application. */
public final class Main {
    private Main() {
    }

    public static void main(String[] args) {
        configureTheme();
        FlatLightLaf.setup();
        SwingUtilities.invokeLater(Main::start);
    }

    /** Global look: deep green accent, bone-white surfaces, rounded components, roomier tables, slim scrollbars. */
    private static void configureTheme() {
        FlatLaf.setGlobalExtraDefaults(Collections.singletonMap("@accentColor", Theme.PRIMARY_HEX));

        // One sans-serif font for the whole app (login included).
        UIManager.put("defaultFont", new Font(Font.SANS_SERIF, Font.PLAIN, 14));

        UIManager.put("Component.arc", 12);
        UIManager.put("Button.arc", 12);
        UIManager.put("TextComponent.arc", 10);
        UIManager.put("Component.focusWidth", 1);

        UIManager.put("ScrollBar.width", 12);
        UIManager.put("ScrollBar.thumbArc", 999);
        UIManager.put("ScrollBar.trackArc", 999);
        UIManager.put("ScrollBar.thumbInsets", new Insets(2, 2, 2, 2));

        UIManager.put("Table.cellMargins", new Insets(4, 10, 4, 10));
        UIManager.put("TableHeader.cellMargins", new Insets(6, 10, 6, 10));
        UIManager.put("TableHeader.height", 38);
        UIManager.put("Table.background", Theme.CARD);
        UIManager.put("Table.alternateRowColor", Theme.ALT_ROW);
        UIManager.put("Table.gridColor", Theme.BORDER);
        UIManager.put("Table.selectionBackground", Theme.ACCENT_LIGHT);
        UIManager.put("Table.selectionForeground", Theme.PRIMARY_DARK);
        UIManager.put("TableHeader.background", Theme.HEADER);
        UIManager.put("TableHeader.foreground", Theme.PRIMARY_DARK);

        // Bone-white surfaces (also applies to dialogs).
        UIManager.put("Panel.background", Theme.PAGE);
        UIManager.put("OptionPane.background", Theme.PAGE);
    }

    private static void start() {
        try {
            DatabaseConnection.getInstance().getConnection();
            new StaffDAO().ensureDefaultAdmin();

            LoginFrame frame = new LoginFrame();
            LoginController controller = new LoginController(frame, new StaffDAO());
            frame.setController(controller);
            frame.setVisible(true);
        } catch (DatabaseException e) {
            JOptionPane.showMessageDialog(null, e.getMessage(),
                    "Koneksi Database", JOptionPane.ERROR_MESSAGE);
        }
    }
}