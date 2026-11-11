package rentgo.model;

/** Vehicle condition recorded at return. */
public enum VehicleCondition {
    GOOD("Baik"), MINOR_DAMAGE("Kerusakan ringan"), MAJOR_DAMAGE("Kerusakan berat");
    private final String label;
    VehicleCondition(String label) { this.label = label; }
    public String getLabel() { return label; }
    @Override public String toString() { return label; }
    public static VehicleCondition fromString(String value) { return valueOf(value.toUpperCase()); }
}