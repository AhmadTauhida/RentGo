package rentgo.view;

import java.util.List;
import javax.swing.JPanel;
import rentgo.controller.VehicleController;
import rentgo.model.Vehicle;
import rentgo.model.VehicleStatus;
import rentgo.model.VehicleType;

/** Vehicle management panel. UI is completed in M5; API is ready for controllers. */
public class VehiclePanel extends JPanel {
    public void setController(VehicleController controller) { }
    public void setTableData(List<Vehicle> vehicles) { }
    public void setForm(Vehicle vehicle) { }
    public void clearForm() { }
    public Vehicle getSelectedVehicle() { return null; }
    public String getPlateNumber() { return ""; }
    public String getBrand() { return ""; }
    public String getManufactureYear() { return ""; }
    public String getBaseRate() { return ""; }
    public String getPassengerCapacity() { return ""; }
    public String getTransmission() { return ""; }
    public String getEngineCc() { return ""; }
    public String getSeatCount() { return ""; }
    public VehicleType getVehicleType() { return null; }
    public VehicleStatus getVehicleStatus() { return null; }
    public String getSearchKeyword() { return ""; }
    public VehicleType getSearchType() { return null; }
    public VehicleStatus getSearchStatus() { return null; }
    public void showError(String message) { }
    public void showInfo(String message) { }
    public boolean confirm(String message) { return false; }
    public int getSelectedIndex() { return -1; }
}
