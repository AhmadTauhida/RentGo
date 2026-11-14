package rentgo.view;

import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagLayout;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.geom.Arc2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.RoundRectangle2D;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

import rentgo.controller.LoginController;

/** Login window: a deep green card with pill-shaped light green fields on a bone-white page. */
public class LoginFrame extends JFrame {
    private static final int FIELD_WIDTH = 380;
    private static final String FIELD_STYLE =
            "arc: 999; background: " + Theme.FIELD_GREEN_HEX + "; foreground: #FFFFFF; "
            + "borderColor: " + Theme.FIELD_GREEN_HEX + "; focusedBorderColor: #FFFFFF; "
            + "focusWidth: 0; margin: 4,22,4,16; placeholderForeground: #E3F3E6";
    private static final String LOGIN_BUTTON_STYLE =
            "arc: 999; background: #FFFFFF; foreground: " + Theme.PRIMARY_DARK_HEX + "; font: bold +2; "
            + "hoverBackground: #EAF6EC; pressedBackground: #D3EBD8; borderWidth: 0; focusWidth: 0";

    private LoginController controller;

    private final JTextField usernameField = new JTextField();
    private final JPasswordField passwordField = new JPasswordField();
    private final JButton loginButton = new JButton("Login");

    public LoginFrame() {
        super("RentGo - Login");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(520, 580));
        setSize(720, 640);
        setLocationRelativeTo(null);

        JPanel page = new JPanel(new GridBagLayout());
        page.setBackground(Theme.PAGE);
        page.add(buildCard());
        add(page);

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowOpened(WindowEvent event) {
                usernameField.requestFocusInWindow();
            }
        });
    }

    public void setController(LoginController value) {
        controller = value;
    }

    public String getUsername() {
        return usernameField.getText().trim();
    }

    public char[] getPassword() {
        return passwordField.getPassword();
    }

    public void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Gagal Masuk", JOptionPane.ERROR_MESSAGE);
    }

    public void showInfo(String message) {
        JOptionPane.showMessageDialog(this, message, "Informasi", JOptionPane.INFORMATION_MESSAGE);
    }

    private JPanel buildCard() {
        JPanel card = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Theme.PRIMARY_DARK);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 32, 32);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        card.setOpaque(false);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(BorderFactory.createEmptyBorder(40, 40, 40, 40));

        JLabel title = new JLabel("Login");
        title.setFont(title.getFont().deriveFont(Font.BOLD, 44f));
        title.setForeground(Color.WHITE);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);

        styleField(usernameField, "Username", new UserIcon());
        styleField(passwordField, "Password", new LockIcon());
        usernameField.addActionListener(event -> login());
        passwordField.addActionListener(event -> login());

        JLabel forgot = new JLabel("Lupa password?");
        forgot.setForeground(Color.WHITE);
        forgot.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        forgot.setAlignmentX(Component.LEFT_ALIGNMENT);
        forgot.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent event) {
                showInfo("Hubungi Admin untuk mengatur ulang password Anda.");
            }
        });

        loginButton.putClientProperty("FlatLaf.style", LOGIN_BUTTON_STYLE);
        loginButton.setAlignmentX(Component.LEFT_ALIGNMENT);
        loginButton.setPreferredSize(new Dimension(FIELD_WIDTH, 52));
        loginButton.setMaximumSize(new Dimension(Integer.MAX_VALUE, 52));
        loginButton.addActionListener(event -> login());

        card.add(title);
        card.add(Box.createVerticalStrut(36));
        card.add(usernameField);
        card.add(Box.createVerticalStrut(22));
        card.add(passwordField);
        card.add(Box.createVerticalStrut(14));
        card.add(forgot);
        card.add(Box.createVerticalStrut(14));
        card.add(loginButton);
        return card;
    }

    private void styleField(JTextField field, String placeholder, Icon icon) {
        field.putClientProperty("FlatLaf.style", FIELD_STYLE);
        field.putClientProperty("JTextField.placeholderText", placeholder);
        field.putClientProperty("JTextField.trailingIcon", icon);
        field.setCaretColor(Color.WHITE);
        field.setFont(field.getFont().deriveFont(16f));
        field.setAlignmentX(Component.LEFT_ALIGNMENT);
        field.setPreferredSize(new Dimension(FIELD_WIDTH, 54));
        field.setMaximumSize(new Dimension(Integer.MAX_VALUE, 54));
    }

    private void login() {
        if (controller != null) {
            controller.login();
        }
    }

    /** Small white user silhouette drawn with Java2D (no image files needed). */
    private static final class UserIcon implements Icon {
        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(Color.WHITE);
            g2.fill(new Ellipse2D.Float(x + 6, y + 1, 8, 8));
            g2.fill(new Arc2D.Float(x + 1, y + 11, 18, 18, 0, 180, Arc2D.CHORD));
            g2.dispose();
        }

        @Override
        public int getIconWidth() {
            return 20;
        }

        @Override
        public int getIconHeight() {
            return 20;
        }
    }

    /** Small white padlock drawn with Java2D. */
    private static final class LockIcon implements Icon {
        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(Color.WHITE);
            g2.fill(new RoundRectangle2D.Float(x + 3, y + 9, 14, 10, 4, 4));
            g2.setStroke(new java.awt.BasicStroke(2f));
            g2.draw(new Arc2D.Float(x + 6, y + 2, 8, 10, 0, 180, Arc2D.OPEN));
            g2.drawLine(x + 6, y + 7, x + 6, y + 10);
            g2.drawLine(x + 14, y + 7, x + 14, y + 10);
            g2.dispose();
        }

        @Override
        public int getIconWidth() {
            return 20;
        }

        @Override
        public int getIconHeight() {
            return 20;
        }
    }
}