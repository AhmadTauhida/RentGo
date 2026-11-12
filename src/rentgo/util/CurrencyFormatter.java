package rentgo.util;

import java.text.NumberFormat;
import java.util.Locale;
import rentgo.exception.InvalidDataException;

/** Formats and parses Indonesian rupiah values. */
public final class CurrencyFormatter {
    private static final Locale INDONESIAN = Locale.forLanguageTag("id-ID");

    private CurrencyFormatter() { }

    public static String formatRupiah(double value) {
        NumberFormat format = NumberFormat.getNumberInstance(INDONESIAN);
        format.setMinimumFractionDigits(0);
        format.setMaximumFractionDigits(0);
        return "Rp" + format.format(value);
    }

    public static double parseRupiah(String value) {
        if (value == null || value.isBlank()) {
            throw new InvalidDataException("Jumlah uang wajib diisi");
        }
        String clean = value.trim();
        if (clean.regionMatches(true, 0, "Rp", 0, 2)) {
            clean = clean.substring(2).trim();
        }
        clean = clean.replace(".", "").replace(',', '.').trim();
        try {
            return Double.parseDouble(clean);
        } catch (NumberFormatException e) {
            throw new InvalidDataException("Jumlah uang tidak valid", e);
        }
    }
}
