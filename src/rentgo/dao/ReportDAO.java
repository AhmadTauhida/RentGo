package rentgo.dao;

import java.time.LocalDate;
import java.util.List;

/** Persistence contract placeholder for reports. */
public class ReportDAO {
    private static UnsupportedOperationException todo() { return new UnsupportedOperationException("TODO(Anggota 2)"); }
    public List<Object[]> revenue(LocalDate from, LocalDate to) { throw todo(); }
    public List<Object[]> topVehicles(int limit) { throw todo(); }
}