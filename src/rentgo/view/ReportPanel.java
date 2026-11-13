package rentgo.view;

import java.util.List;
import javax.swing.JPanel;
import rentgo.controller.ReportController;

/** Report panel. UI is completed in M5; API is ready for controllers. */
public class ReportPanel extends JPanel {
    public void setController(ReportController controller) { }
    public void setRevenueData(List<Object[]> data) { }
    public void setTopVehiclesData(List<Object[]> data) { }
    public String getPeriod() { return "ALL"; }
    public void showError(String message) { }
    public void showInfo(String message) { }
}
