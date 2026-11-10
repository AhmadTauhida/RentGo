package rentgo.model;

/** Rental lifecycle states. */
public enum RentalStatus {
    ACTIVE("Aktif"), COMPLETED("Selesai"), CANCELLED("Dibatalkan");
    private final String label;
    RentalStatus(String label) { this.label = label; }
    public String getLabel() { return label; }
    @Override public String toString() { return label; }
    public static RentalStatus fromString(String value) { return valueOf(value.toUpperCase()); }
}