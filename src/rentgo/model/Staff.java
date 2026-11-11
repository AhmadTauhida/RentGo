package rentgo.model;

import rentgo.exception.InvalidDataException;

/** A RentGo application user. */
public class Staff extends Person {
    private String username;
    private String passwordHash;
    private Role role;
    public Staff() { }
    public Staff(String name, String phoneNumber, String username, String passwordHash, Role role) { this(0, name, phoneNumber, username, passwordHash, role); }
    public Staff(int id, String name, String phoneNumber, String username, String passwordHash, Role role) { super(id, name, phoneNumber); setUsername(username); setPasswordHash(passwordHash); setRole(role); }
    public String getUsername() { return username; }
    public void setUsername(String value) { if (value == null || !value.matches("\\S{3,50}")) throw new InvalidDataException("Username tidak valid"); username = value; }
    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String value) { if (value == null || value.isBlank()) throw new InvalidDataException("Password hash wajib diisi"); passwordHash = value; }
    public Role getRole() { return role; }
    public void setRole(Role value) { if (value == null) throw new InvalidDataException("Peran wajib dipilih"); role = value; }
    public boolean isAdmin() { return role == Role.ADMIN; }
    @Override public String getDisplayInfo() { return getName() + " (" + role + ")"; }
    @Override public String toString() { return getDisplayInfo(); }
}