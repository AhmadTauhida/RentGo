package rentgo.controller;

import java.time.LocalDate;
import rentgo.dao.ReportDAO;
import rentgo.exception.DatabaseException;
import rentgo.view.ReportPanel;

/** Loads revenue and top-vehicle reports for the selected period. */
public class ReportController {
    private final ReportPanel view;
    private final ReportDAO dao;

    public ReportController(ReportPanel view, ReportDAO dao) {
        this.view = view;
        this.dao = dao;
    }

    public void loadReport() {
        try {
            LocalDate[] range = range(view.getPeriod());
            view.setRevenueData(dao.revenue(range[0], range[1]));
            view.setTopVehiclesData(dao.topVehicles(5));
        } catch (DatabaseException e) {
            view.showError(e.getMessage());
        }
    }

    private LocalDate[] range(String period) {
        LocalDate today = LocalDate.now();
        if ("TODAY".equals(period)) {
            return new LocalDate[] {today, today};
        }
        if ("LAST_7".equals(period)) {
            return new LocalDate[] {today.minusDays(6), today};
        }
        if ("THIS_MONTH".equals(period)) {
            return new LocalDate[] {today.withDayOfMonth(1), today};
        }
        if ("THIS_YEAR".equals(period)) {
            return new LocalDate[] {today.withDayOfYear(1), today};
        }
        return new LocalDate[] {null, null};
    }
}
