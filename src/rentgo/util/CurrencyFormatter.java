package rentgo.util;

import java.text.NumberFormat;
import java.util.Locale;

/** Formats and parses Indonesian currency values. */
public final class CurrencyFormatter {
    private CurrencyFormatter() { }
    public static String formatRupiah(double value) { return NumberFormat.getCurrencyInstance(Locale.forLanguageTag("id-ID")).format(value).replace("Rp", "Rp").replace(",00", ""); }
    public static double parseRupiah(String value) { return Double.parseDouble(value.replace("Rp", "").replace(".", "").replace(",", ".").trim()); }
}