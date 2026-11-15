package rentgo.view;

import java.awt.Color;

/** Central color palette: deep green, light green, and bone white. Change colors here only. */
public final class Theme {
    // Deep green (main brand color; sidebar and login card)
    public static final String PRIMARY_HEX = "#237A35";
    public static final String PRIMARY_HOVER_HEX = "#1C682B";
    public static final String PRIMARY_DARK_HEX = "#185A24";
    public static final String PRIMARY_SOFT_HEX = "#A9DDB3";

    // Light green (accent; login fields, active menu, small highlights)
    public static final String ACCENT_HEX = "#8FD19B";
    public static final String ACCENT_LIGHT_HEX = "#DDF0DF";
    public static final String FIELD_GREEN_HEX = "#52A55F";

    // Danger
    public static final String DANGER_HEX = "#C0392B";
    public static final String DANGER_HOVER_HEX = "#A32E22";

    public static final Color PRIMARY = Color.decode(PRIMARY_HEX);
    public static final Color PRIMARY_DARK = Color.decode(PRIMARY_DARK_HEX);
    public static final Color PRIMARY_SOFT = Color.decode(PRIMARY_SOFT_HEX);
    public static final Color ACCENT = Color.decode(ACCENT_HEX);
    public static final Color ACCENT_LIGHT = Color.decode(ACCENT_LIGHT_HEX);
    public static final Color FIELD_GREEN = Color.decode(FIELD_GREEN_HEX);

    // Light green background for the login page
    public static final Color MINT = new Color(0xD3EDD8);

    // Bone white surfaces
    public static final Color PAGE = new Color(0xF4F2EA);
    public static final Color CARD = new Color(0xFCFBF7);
    public static final Color HEADER = new Color(0xEEEBE0);
    public static final Color ALT_ROW = new Color(0xF8F7F1);
    public static final Color BORDER = new Color(0xE2DED0);

    // Text
    public static final Color TEXT = new Color(0x1E2B22);
    public static final Color TEXT_MUTED = new Color(0x66736B);

    // Status colors
    public static final Color SUCCESS = PRIMARY;
    public static final Color WARNING = new Color(0xB7791F);
    public static final Color DANGER = Color.decode(DANGER_HEX);

    private Theme() {
    }
}