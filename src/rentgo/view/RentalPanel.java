package rentgo.view;

import java.util.Date;
import java.util.List;
import javax.swing.JPanel;
import rentgo.controller.RentalController;
import rentgo.model.Customer;
import rentgo.model.PaymentMethod;
import rentgo.model.Rental;
import rentgo.model.Vehicle;

/** Rental transaction panel. UI is completed in M5; API is ready for controllers. */
public class RentalPanel extends JPanel {
    public void setController(RentalController controller) { }
    public void setCustomerOptions(List<Customer> customers) { }
    public void setVehicleOptions(List<Vehicle> vehicles) { }
    public void setPaymentMethods(PaymentMethod[] methods) { }
    public void setTableData(List<Rental> rentals) { }
    public void setTotalCost(String value) { }
    public void showReceipt(String text) { }
    public void resetForm() { }
    public Customer getSelectedCustomer() { return null; }
    public Vehicle getSelectedVehicle() { return null; }
    public Rental getSelectedRental() { return null; }
    public Date getStartDate() { return null; }
    public Date getDueDate() { return null; }
    public boolean isWithDriver() { return false; }
    public PaymentMethod getPaymentMethod() { return null; }
    public void showError(String message) { }
    public void showInfo(String message) { }
    public boolean confirm(String message) { return false; }
    public int getSelectedIndex() { return -1; }
}
