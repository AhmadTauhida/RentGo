package rentgo.util;

import java.time.Year;
import rentgo.exception.InvalidDataException;

/** Shared validation and parsing helpers for controller input. */
public final class Validator {
    private Validator() { }

    public static String requireNotBlank(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new InvalidDataException(fieldName + " wajib diisi");
        }
        return value.trim();
    }

    public static double parsePositiveDouble(String text, String fieldName) {
        String raw = requireNotBlank(text, fieldName);
        try {
            double value = Double.parseDouble(raw);
            if (!Double.isFinite(value) || value <= 0) {
                throw new InvalidDataException(fieldName + " harus lebih dari nol");
            }
            return value;
        } catch (NumberFormatException e) {
            throw new InvalidDataException(fieldName + " harus berupa angka", e);
        }
    }

    public static int parseInt(String text, String fieldName) {
        String raw = requireNotBlank(text, fieldName);
        try {
            return Integer.parseInt(raw);
        } catch (NumberFormatException e) {
            throw new InvalidDataException(fieldName + " harus berupa bilangan bulat", e);
        }
    }

    public static void validateNationalId(String value) {
        if (value == null || !value.matches("\\d{16}")) {
            throw new InvalidDataException("NIK harus 16 digit");
        }
    }

    public static void validatePhone(String value) {
        if (value == null || value.isBlank()) {
            return;
        }
        if (!value.matches("[+0-9]{8,15}")) {
            throw new InvalidDataException("Nomor telepon harus 8-15 digit angka");
        }
    }

    public static void validatePlate(String value) {
        requireNotBlank(value, "Plat nomor");
    }

    public static void validateYear(int value) {
        int maxYear = Year.now().getValue() + 1;
        if (value < 1980 || value > maxYear) {
            throw new InvalidDataException("Tahun produksi harus antara 1980 dan " + maxYear);
        }
    }
}
