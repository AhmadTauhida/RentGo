package rentgo.view;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.LayoutManager;
import java.awt.RenderingHints;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;

/** Shared look-and-feel helpers for the view layer (buttons, tables, cards). Colors live in {@link Theme}. */
final class UiStyle {
    static final Color PAGE = Theme.PAGE;
    static final Color CARD = Theme.CARD;
    static final Color BORDER = Theme.BORDER;
    static final Color TEXT = Theme.TEXT;
    static final Color TEXT_MUTED = Theme.TEXT_MUTED;
    static final int ARC = 16;
    static final int LABEL_WIDTH = 115;

    private UiStyle() {
    }

    /** Main action button (filled deep green). */
    static void primary(JButton button) {
        button.putClientProperty("FlatLaf.style",
                "background: " + Theme.PRIMARY_HEX + "; foreground: #ffffff; font: bold; "
                + "hoverBackground: " + Theme.PRIMARY_HOVER_HEX + "; "
                + "pressedBackground: " + Theme.PRIMARY_DARK_HEX);
    }

    /** Destructive action button (delete, cancel). */
    static void danger(JButton button) {
        button.putClientProperty("FlatLaf.style",
                "background: " + Theme.DANGER_HEX + "; foreground: #ffffff; font: bold; "
                + "hoverBackground: " + Theme.DANGER_HOVER_HEX + "; pressedBackground: #8A281E");
    }

    /** Light green button for secondary actions. */
    static void accent(JButton button) {
        button.putClientProperty("FlatLaf.style",
                "background: " + Theme.ACCENT_HEX + "; foreground: " + Theme.PRIMARY_DARK_HEX + "; font: bold; "
                + "hoverBackground: #7FC78C; pressedBackground: #6FBB7D");
    }

    /** Hint text shown inside an empty text field. */
    static void placeholder(JTextField field, String text) {
        field.putClientProperty("JTextField.placeholderText", text);
    }

    /** Shows a year without a thousands separator (2026 instead of 2,026). */
    static void plainYear(JSpinner spinner) {
        spinner.setEditor(new JSpinner.NumberEditor(spinner, "#"));
    }

    /** Adds a "label + field" row to a GridBagLayout form. */
    static void formRow(JPanel form, int row, String label, Component field) {
        JLabel labelComponent = new JLabel(label);
        labelComponent.setPreferredSize(new Dimension(LABEL_WIDTH, labelComponent.getPreferredSize().height));
        GridBagConstraints labelConstraints = new GridBagConstraints();
        labelConstraints.gridx = 0;
        labelConstraints.gridy = row;
        labelConstraints.anchor = GridBagConstraints.NORTHWEST;
        labelConstraints.insets = new Insets(10, 0, 6, 8);
        form.add(labelComponent, labelConstraints);

        GridBagConstraints fieldConstraints = new GridBagConstraints();
        fieldConstraints.gridx = 1;
        fieldConstraints.gridy = row;
        fieldConstraints.weightx = 1;
        fieldConstraints.fill = GridBagConstraints.HORIZONTAL;
        fieldConstraints.insets = new Insets(6, 0, 6, 0);
        form.add(field, fieldConstraints);
    }

    /** Adds a full-width note row (for example a hint) to a GridBagLayout form. */
    static void formNote(JPanel form, int row, JComponent note) {
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.gridx = 0;
        constraints.gridy = row;
        constraints.gridwidth = 2;
        constraints.anchor = GridBagConstraints.WEST;
        constraints.insets = new Insets(4, 0, 4, 0);
        form.add(note, constraints);
    }

    /** Adds a stretchy empty row so the fields stay at the top instead of floating in the middle. */
    static void formFiller(JPanel form, int row) {
        GridBagConstraints filler = new GridBagConstraints();
        filler.gridx = 0;
        filler.gridy = row;
        filler.gridwidth = 2;
        filler.weighty = 1;
        JPanel fillerPanel = new JPanel();
        fillerPanel.setOpaque(false);
        form.add(fillerPanel, filler);
    }

    static JLabel pageTitle(String text) {
        JLabel label = new JLabel(text);
        label.setFont(label.getFont().deriveFont(Font.BOLD, 22f));
        label.setForeground(Theme.PRIMARY_DARK);
        return label;
    }

    static JLabel sectionTitle(String text) {
        JLabel label = new JLabel(text);
        label.setFont(label.getFont().deriveFont(Font.BOLD, 16f));
        label.setForeground(Theme.PRIMARY_DARK);
        return label;
    }

    static JLabel mutedLabel(String text) {
        JLabel label = new JLabel(text);
        label.setForeground(TEXT_MUTED);
        return label;
    }

    /** Applies the common table look: bone-white background, taller rows, bold left-aligned header. */
    static void table(JTable table) {
        table.setFillsViewportHeight(true);
        table.setBackground(Theme.CARD);
        table.setRowHeight(34);
        table.setShowVerticalLines(false);
        table.setShowHorizontalLines(true);
        table.setGridColor(BORDER);
        table.setIntercellSpacing(new Dimension(0, 1));

        JTableHeader header = table.getTableHeader();
        header.setReorderingAllowed(false);
        header.setBackground(Theme.HEADER);
        header.setForeground(Theme.PRIMARY_DARK);
        header.setFont(header.getFont().deriveFont(Font.BOLD));
        TableCellRenderer base = header.getDefaultRenderer();
        header.setDefaultRenderer((tbl, value, selected, focused, row, column) -> {
            Component component = base.getTableCellRendererComponent(
                    tbl, value, selected, focused, row, column);
            if (component instanceof JLabel) {
                ((JLabel) component).setHorizontalAlignment(SwingConstants.LEFT);
            }
            return component;
        });
    }

    /** Right-aligned cells, for money columns. */
    static TableCellRenderer rightRenderer() {
        DefaultTableCellRenderer renderer = new DefaultTableCellRenderer();
        renderer.setHorizontalAlignment(SwingConstants.RIGHT);
        return renderer;
    }

    /** Colors status text: green (AVAILABLE, COMPLETED), amber (RENTED, ACTIVE), red (MAINTENANCE, CANCELLED). */
    static TableCellRenderer statusRenderer() {
        return new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                    boolean isSelected, boolean hasFocus, int row, int column) {
                super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                setFont(getFont().deriveFont(Font.BOLD));
                if (!isSelected) {
                    setForeground(statusColor(value));
                }
                return this;
            }
        };
    }

    private static Color statusColor(Object value) {
        String name = value instanceof Enum ? ((Enum<?>) value).name() : String.valueOf(value);
        switch (name) {
            case "AVAILABLE":
            case "COMPLETED":
                return Theme.SUCCESS;
            case "RENTED":
            case "ACTIVE":
                return Theme.WARNING;
            case "MAINTENANCE":
            case "CANCELLED":
                return Theme.DANGER;
            default:
                return TEXT;
        }
    }

    /** Bone-white rounded panel with a thin border, used to group a form or a table. */
    static final class Card extends JPanel {
        Card(LayoutManager layout) {
            super(layout);
            setOpaque(false);
            setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(CARD);
            g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, ARC, ARC);
            g2.setColor(BORDER);
            g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, ARC, ARC);
            g2.dispose();
            super.paintComponent(g);
        }
    }
}