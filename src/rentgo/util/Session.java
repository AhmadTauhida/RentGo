package rentgo.util;

import rentgo.model.Staff;

/** Holds the current authenticated staff member. */
public final class Session {
    private static Staff staff;
    private Session() { }
    public static Staff getStaff() { return staff; }
    public static void setStaff(Staff value) { staff = value; }
    public static void clear() { staff = null; }
}