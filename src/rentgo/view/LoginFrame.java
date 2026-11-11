package rentgo.view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import rentgo.controller.LoginController;

/** Login window for RentGo staff. */
public class LoginFrame extends JFrame {
    private final JTextField usernameField = new JTextField();
    private final JPasswordField passwordField = new JPasswordField();
    private LoginController controller;

    public LoginFrame() {
        super("RentGo - Masuk");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(460, 360));
        setSize(460, 360);
        setLocationRelativeTo(null);
        buildContent();
    }

    private void buildContent() {
        JPanel content = new JPanel(new BorderLayout(0, 18));
        content.setBorder(BorderFactory.createEmptyBorder(28, 42, 30, 42));
        content.setBackground(new Color(245, 247, 250));

        JLabel title = new JLabel("RentGo", SwingConstants.CENTER);
        title.setFont(title.getFont().deriveFont(Font.BOLD, 30f));
        title.setForeground(new Color(24, 58, 86));
        JLabel subtitle = new JLabel("Sistem Rental Kendaraan", SwingConstants.CENTER);
        subtitle.setForeground(new Color(90, 101, 112));
        JPanel heading = new JPanel(new BorderLayout(0, 4));
        heading.setOpaque(false);
        heading.add(title, BorderLayout.CENTER);
        heading.add(subtitle, BorderLayout.SOUTH);
        content.add(heading, BorderLayout.NORTH);

        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.anchor = GridBagConstraints.WEST;
        constraints.insets = new Insets(5, 0, 5, 12);
        form.add(new JLabel("Username"), constraints);
        constraints.gridx = 1;
        constraints.weightx = 1;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.insets = new Insets(5, 0, 5, 0);
        form.add(usernameField, constraints);
        constraints.gridx = 0;
        constraints.gridy = 1;
        constraints.weightx = 0;
        constraints.fill = GridBagConstraints.NONE;
        constraints.insets = new Insets(5, 0, 5, 12);
        form.add(new JLabel("Password"), constraints);
        constraints.gridx = 1;
        constraints.weightx = 1;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.insets = new Insets(5, 0, 5, 0);
        form.add(passwordField, constraints);
        content.add(form, BorderLayout.CENTER);

        JButton loginButton = new JButton("Masuk");
        JButton cancelButton = new JButton("Batal");
        loginButton.addActionListener(event -> {
            if (controller == null) showInfo("LoginController belum terhubung."); else controller.login();
        });
        cancelButton.addActionListener(event -> {
            if (controller == null) dispose(); else controller.cancel();
        });
        passwordField.addActionListener(event -> loginButton.doClick());
        JPanel actions = new JPanel(new BorderLayout(10, 0));
        actions.setOpaque(false);
        actions.add(cancelButton, BorderLayout.WEST);
        actions.add(loginButton, BorderLayout.EAST);
        content.add(actions, BorderLayout.SOUTH);
        setContentPane(content);
        getRootPane().setDefaultButton(loginButton);
    }

    public void setController(LoginController value) { controller = value; }
    public String getUsername() { return usernameField.getText().trim(); }
    public char[] getPassword() { return passwordField.getPassword(); }
    public void showError(String message) { JOptionPane.showMessageDialog(this, message, "Kesalahan", JOptionPane.ERROR_MESSAGE); }
    public void showInfo(String message) { JOptionPane.showMessageDialog(this, message, "Informasi", JOptionPane.INFORMATION_MESSAGE); }
}