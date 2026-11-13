package rentgo.view;

import java.util.List;
import javax.swing.JPanel;
import rentgo.controller.UserManagementController;
import rentgo.model.Role;
import rentgo.model.Staff;

/** Staff management panel. UI is completed in M5; API is ready for controllers. */
public class UserManagementPanel extends JPanel {
    public void setController(UserManagementController controller) { }
    public void setTableData(List<Staff> staffMembers) { }
    public void setForm(Staff staff) { }
    public void clearForm() { }
    public Staff getSelectedStaff() { return null; }
    public String getNameField() { return ""; }
    public String getUsernameField() { return ""; }
    public String getPhoneNumber() { return ""; }
    public char[] getPassword() { return new char[0]; }
    public Role getRole() { return null; }
    public void showError(String message) { }
    public void showInfo(String message) { }
    public boolean confirm(String message) { return false; }
}
