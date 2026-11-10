package rentgo.model;

/** Vehicle categories supported by RentGo. */
public enum VehicleType {
    CAR("Mobil"), MOTORCYCLE("Motor"), BUS("Bus");
    private final String label;
    VehicleType(String label) { this.label = label; }
    public String getLabel() { return label; }
    @Override public String toString() { return label; }
    public static VehicleType fromString(String value) { return valueOf(value.toUpperCase()); }
}