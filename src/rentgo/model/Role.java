package rentgo.model;

/** Application roles. */
public enum Role {
    ADMIN("Admin"), STAFF("Staff");
    private final String label;
    Role(String label) { this.label = label; }
    public String getLabel() { return label; }
    @Override public String toString() { return label; }
    public static Role fromString(String value) { return valueOf(value.toUpperCase()); }
}