package rentgo.model;

import rentgo.exception.InvalidDataException;

/** Common identity data for customers and staff. */
public abstract class Person {
    private int id;
    private String name;
    private String phoneNumber;

    protected Person() { }
    protected Person(int id, String name, String phoneNumber) {
        setId(id);
        setName(name);
        setPhoneNumber(phoneNumber);
    }
    public int getId() { return id; }
    public void setId(int id) { if (id < 0) throw new InvalidDataException("ID tidak valid"); this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { if (name == null || name.isBlank()) throw new InvalidDataException("Nama wajib diisi"); this.name = name.trim(); }
    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) {
        if (phoneNumber == null || phoneNumber.isBlank()) { this.phoneNumber = ""; return; }
        if (!phoneNumber.matches("[+0-9]{8,15}")) throw new InvalidDataException("Nomor telepon tidak valid");
        this.phoneNumber = phoneNumber;
    }
    public abstract String getDisplayInfo();
}