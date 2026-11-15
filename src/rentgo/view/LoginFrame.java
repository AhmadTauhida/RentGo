package rentgo.view;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

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

/** Login window: simple centered form on a light green page. */
public class LoginFrame extends JFrame {
    private LoginController controller;

    private final JTextField usernameField = new JTextField(18);
    private final JPasswordField passwordField = new JPasswordField(18);
    private final JButton loginButton = new JButton("Masuk");
    private final JButton cancelButton = new JButton("Batal");

    public LoginFrame() {
        super("RentGo - Masuk");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(480, 460));
        setSize(560, 520);
        setLocationRelativeTo(null);

        JPanel page = new JPanel(new GridBagLayout());
        page.setBackground(Theme.MINT);
        page.add(buildCard());
        add(page);

        usernameField.addActionListener(event -> login());
        passwordField.addActionListener(event -> login());
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
        UiStyle.Card card = new UiStyle.Card(new GridBagLayout());
        card.setBorder(BorderFactory.createEmptyBorder(32, 36, 32, 36));

        JLabel title = new JLabel("RentGo", SwingConstants.CENTER);
        title.setFont(title.getFont().deriveFont(Font.BOLD, 32f));
        title.setForeground(Theme.PRIMARY_DARK);

        JLabel subtitle = new JLabel("Silakan masuk untuk melanjutkan", SwingConstants.CENTER);
        subtitle.setForeground(Theme.TEXT_MUTED);

        UiStyle.placeholder(usernameField, "Username");
        passwordField.putClientProperty("JTextField.placeholderText", "Password");
        // Eye icon at the right edge of the password field: click to show or hide the password.
        PasswordEye.attach(passwordField);

        UiStyle.primary(loginButton);
        UiStyle.accent(cancelButton);
        loginButton.setPreferredSize(new Dimension(130, 40));
        cancelButton.setPreferredSize(new Dimension(130, 40));
        loginButton.addActionListener(event -> login());
        cancelButton.addActionListener(event -> {
            if (controller != null) controller.cancel();
        });

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 0));
        buttons.setOpaque(false);
        buttons.add(loginButton);
        buttons.add(cancelButton);

        addFull(card, title, 0, new Insets(0, 0, 4, 0));
        addFull(card, subtitle, 1, new Insets(0, 0, 24, 0));
        addRow(card, 2, "Username", usernameField);
        addRow(card, 3, "Password", passwordField);
        addFull(card, buttons, 4, new Insets(20, 0, 0, 0));
        return card;
    }

    private void addFull(JPanel target, Component component, int row, Insets insets) {
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.gridx = 0;
        constraints.gridy = row;
        constraints.gridwidth = 2;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.insets = insets;
        target.add(component, constraints);
    }

    private void addRow(JPanel target, int row, String label, Component field) {
        GridBagConstraints labelConstraints = new GridBagConstraints();
        labelConstraints.gridx = 0;
        labelConstraints.gridy = row;
        labelConstraints.anchor = GridBagConstraints.WEST;
        labelConstraints.insets = new Insets(8, 0, 8, 14);
        target.add(new JLabel(label), labelConstraints);

        GridBagConstraints fieldConstraints = new GridBagConstraints();
        fieldConstraints.gridx = 1;
        fieldConstraints.gridy = row;
        fieldConstraints.weightx = 1;
        fieldConstraints.fill = GridBagConstraints.HORIZONTAL;
        fieldConstraints.insets = new Insets(8, 0, 8, 0);
        target.add(field, fieldConstraints);
    }

    private void login() {
        if (controller != null) {
            controller.login();
        }
    }
}