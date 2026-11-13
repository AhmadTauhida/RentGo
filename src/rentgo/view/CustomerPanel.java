package rentgo.view;

import java.util.List;
import javax.swing.JPanel;
import rentgo.controller.CustomerController;
import rentgo.model.Customer;

/** Customer management panel. UI is completed in M5; API is ready for controllers. */
public class CustomerPanel extends JPanel {
    public void setController(CustomerController controller) { }
    public void setTableData(List<Customer> customers) { }
    public void setForm(Customer customer) { }
    public void clearForm() { }
    public Customer getSelectedCustomer() { return null; }
    public String getNameField() { return ""; }
    public String getNationalId() { return ""; }
    public String getPhoneNumber() { return ""; }
    public String getAddress() { return ""; }
    public String getSearchKeyword() { return ""; }
    public void showError(String message) { }
    public void showInfo(String message) { }
    public boolean confirm(String message) { return false; }
    public int getSelectedIndex() { return -1; }
}
