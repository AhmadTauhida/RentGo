package rentgo;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import com.formdev.flatlaf.FlatLightLaf;

import rentgo.controller.LoginController;
import rentgo.dao.DatabaseConnection;
import rentgo.dao.StaffDAO;
import rentgo.exception.DatabaseException;
import rentgo.view.LoginFrame;

/** Starts the RentGo desktop application. */
public final class Main {
    private Main() {
    }

    public static void main(String[] args) {
        FlatLightLaf.setup();
        SwingUtilities.invokeLater(Main::start);
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