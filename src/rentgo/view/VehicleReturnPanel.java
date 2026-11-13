package rentgo.view;

import java.util.Date;
import java.util.List;
import javax.swing.JPanel;
import rentgo.controller.VehicleReturnController;
import rentgo.model.Rental;
import rentgo.model.VehicleCondition;

/** Vehicle return panel. UI is completed in M5; API is ready for controllers. */
public class VehicleReturnPanel extends JPanel {
    public void setController(VehicleReturnController controller) { }
    public void setRentalOptions(List<Rental> rentals) { }
    public void setLateFeeInfo(int lateDays, String lateFeeText) { }
    public void resetForm() { }
    public Rental getSelectedRental() { return null; }
    public Date getActualReturnDate() { return null; }
    public VehicleCondition getCondition() { return null; }
    public void showError(String message) { }
    public void showInfo(String message) { }
}
