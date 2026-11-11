package rentgo.view;

import javax.swing.JFrame;

/** Main application frame placeholder. */
public class MainFrame extends JFrame {
    public static final String VEHICLE = "VEHICLE";
    public static final String CUSTOMER = "CUSTOMER";
    public static final String RENTAL = "RENTAL";
    public static final String RETURN = "RETURN";
    public static final String REPORT = "REPORT";
    public static final String USERS = "USERS";
    public MainFrame() { super("RentGo"); setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); setMinimumSize(new java.awt.Dimension(1000, 650)); }
    public void showCard(String name) { }
}