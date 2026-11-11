package rentgo;

import javax.swing.SwingUtilities;
import com.formdev.flatlaf.FlatLightLaf;
import rentgo.view.LoginFrame;

/** Starts the RentGo desktop application. */
public final class Main {
    private Main() { }
    public static void main(String[] args) {
        FlatLightLaf.setup();
        SwingUtilities.invokeLater(() -> new LoginFrame().setVisible(true));
    }
}