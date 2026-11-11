package rentgo.model;

import rentgo.exception.InvalidDataException;

/** A customer who rents vehicles. */
public class Customer extends Person {
    private String nationalId;
    private String address;
    public Customer() { }
    public Customer(String name, String phoneNumber, String nationalId, String address) { this(0, name, phoneNumber, nationalId, address); }
    public Customer(int id, String name, String phoneNumber, String nationalId, String address) { super(id, name, phoneNumber); setNationalId(nationalId); setAddress(address); }
    public String getNationalId() { return nationalId; }
    public void setNationalId(String value) { if (value == null || !value.matches("\\d{16}")) throw new InvalidDataException("NIK harus 16 digit"); nationalId = value; }
    public String getAddress() { return address; }
    public void setAddress(String value) { if (value == null || value.isBlank()) throw new InvalidDataException("Alamat wajib diisi"); address = value.trim(); }
    @Override public String getDisplayInfo() { return getName() + " (" + nationalId + ")"; }
    @Override public String toString() { return getDisplayInfo(); }
}