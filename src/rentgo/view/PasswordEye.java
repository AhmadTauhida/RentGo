package rentgo.view;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Ellipse2D;
import javax.swing.Icon;
import javax.swing.JPasswordField;
import javax.swing.JToggleButton;

/** Adds an always-visible "show/hide password" eye button to a password field. */
public final class PasswordEye {
    private static final Color EYE_COLOR = new Color(0x66736B);
    private static final Color EYE_ACTIVE = new Color(0x237A35);

    private PasswordEye() { }

    public static void attach(JPasswordField field) {
        char defaultEcho = field.getEchoChar() == 0 ? '•' : field.getEchoChar();
        JToggleButton toggle = new JToggleButton(new EyeIcon(false));
        toggle.setSelectedIcon(new EyeIcon(true));
        toggle.setToolTipText("Tampilkan / sembunyikan password");
        toggle.setFocusable(false);
        toggle.setContentAreaFilled(false);
        toggle.setBorderPainted(false);
        toggle.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        toggle.addActionListener(event ->
                field.setEchoChar(toggle.isSelected() ? (char) 0 : defaultEcho));
        field.putClientProperty("JPasswordField.showRevealButton", Boolean.FALSE);
        field.putClientProperty("JTextField.trailingIcon", null);
        field.putClientProperty("JTextField.trailingComponent", toggle);
    }

    /** Eye icon, with a slash when the password is hidden. */
    private static final class EyeIcon implements Icon {
        private final boolean visible;

        EyeIcon(boolean visible) {
            this.visible = visible;
        }

        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setStroke(new BasicStroke(1.7f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2.setColor(visible ? EYE_ACTIVE : EYE_COLOR);
            g2.translate(x, y);
            g2.drawArc(1, 2, 18, 14, 0, 180);
            g2.drawArc(1, 4, 18, 10, 180, 180);
            g2.draw(new Ellipse2D.Double(7, 6, 6, 6));
            if (!visible) {
                g2.drawLine(3, 16, 17, 2);
            }
            g2.dispose();
        }

        @Override public int getIconWidth() { return 20; }
        @Override public int getIconHeight() { return 18; }
    }
}