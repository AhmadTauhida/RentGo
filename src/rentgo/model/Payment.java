package rentgo.model;

import java.time.LocalDateTime;
import rentgo.exception.InvalidDataException;
import rentgo.util.CurrencyFormatter;

/** A payment recorded for a rental. */
public class Payment implements PrintableReceipt {
    private int id;
    private Rental rental;
    private PaymentMethod method;
    private double amount;
    private LocalDateTime paidAt;
    private String note;
    public Payment() { }
    public Payment(Rental rental, PaymentMethod method, double amount, LocalDateTime paidAt, String note) { this(0, rental, method, amount, paidAt, note); }
    public Payment(int id, Rental rental, PaymentMethod method, double amount, LocalDateTime paidAt, String note) { setId(id); setRental(rental); setMethod(method); setAmount(amount); setPaidAt(paidAt); setNote(note); }
    public int getId() { return id; }
    public void setId(int value) { if (value < 0) throw new InvalidDataException("ID tidak valid"); id = value; }
    public Rental getRental() { return rental; }
    public void setRental(Rental value) { if (value == null) throw new InvalidDataException("Rental wajib dipilih"); rental = value; }
    public PaymentMethod getMethod() { return method; }
    public void setMethod(PaymentMethod value) { if (value == null) throw new InvalidDataException("Metode pembayaran wajib dipilih"); method = value; }
    public double getAmount() { return amount; }
    public void setAmount(double value) { if (!Double.isFinite(value) || value <= 0) throw new InvalidDataException("Jumlah pembayaran harus lebih dari nol"); amount = value; }
    public LocalDateTime getPaidAt() { return paidAt; }
    public void setPaidAt(LocalDateTime value) { paidAt = value == null ? LocalDateTime.now() : value; }
    public String getNote() { return note; }
    public void setNote(String value) { note = value; }
    @Override public String printReceipt() {
        return "PEMBAYARAN\n"
                + "Metode: " + method + "\n"
                + "Jumlah: " + CurrencyFormatter.formatRupiah(amount) + "\n"
                + "Waktu: " + paidAt + "\n";
    }
}