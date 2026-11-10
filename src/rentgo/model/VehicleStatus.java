package rentgo.model;

/** Vehicle availability states. */
public enum VehicleStatus {
    AVAILABLE("Tersedia"), RENTED("Disewa"), MAINTENANCE("Perawatan");
    private final String label;
    VehicleStatus(String label) { this.label = label; }
    public String getLabel() { return label; }
    @Override public String toString() { return label; }
    public static VehicleStatus fromString(String value) { return valueOf(value.toUpperCase()); }
}