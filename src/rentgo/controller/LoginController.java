package rentgo.controller;

import java.awt.Window;
import java.util.Arrays;
import javax.swing.SwingUtilities;
import rentgo.dao.StaffDAO;
import rentgo.exception.DatabaseException;
import rentgo.model.Staff;
import rentgo.util.PasswordHasher;
import rentgo.util.Session;
import rentgo.view.LoginFrame;
import rentgo.view.MainFrame;

/** Handles authentication and logout. */
public class LoginController {
    private final LoginFrame view;
    private final StaffDAO staffDAO;

    public LoginController(LoginFrame view, StaffDAO staffDAO) {
        this.view = view;
        this.staffDAO = staffDAO;
    }

    public void login() {
        char[] password = view.getPassword();
        try {
            String username = view.getUsername();
            if (username.isBlank() || password.length == 0) {
                view.showError("Username dan password wajib diisi");
                return;
            }
            Staff staff = staffDAO.findByUsername(username);
            if (staff == null || !PasswordHasher.verify(password, staff.getPasswordHash())) {
                view.showError("Username atau password salah");
                return;
            }
            Session.setStaff(staff);
            view.dispose();
            new MainFrame().setVisible(true);
        } catch (DatabaseException e) {
            view.showError(e.getMessage());
        } finally {
            Arrays.fill(password, '\0');
        }
    }

    public void cancel() {
        view.dispose();
        System.exit(0);
    }

    public static void logout() {
        Session.clear();
        for (Window window : Window.getWindows()) {
            window.dispose();
        }
        SwingUtilities.invokeLater(() -> {
            LoginFrame frame = new LoginFrame();
            LoginController controller = new LoginController(frame, new StaffDAO());
            frame.setController(controller);
            frame.setVisible(true);
        });
    }
}
