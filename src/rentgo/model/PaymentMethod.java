package rentgo.model;

/** Payment methods supported by RentGo. */
public enum PaymentMethod {
    CASH("Tunai"), TRANSFER("Transfer"), EWALLET("Dompet digital");
    private final String label;
    PaymentMethod(String label) { this.label = label; }
    public String getLabel() { return label; }
    @Override public String toString() { return label; }
    public static PaymentMethod fromString(String value) { return valueOf(value.toUpperCase()); }
}