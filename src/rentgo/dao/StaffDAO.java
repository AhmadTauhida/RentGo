package rentgo.dao;

import java.util.List;
import rentgo.model.Staff;

/** Persistence contract placeholder for staff accounts. */
public class StaffDAO implements CrudDAO<Staff> {
    private static UnsupportedOperationException todo() { return new UnsupportedOperationException("TODO(Anggota 2)"); }
    public void add(Staff obj) { throw todo(); } public void update(Staff obj) { throw todo(); } public void delete(int id) { throw todo(); } public Staff findById(int id) { throw todo(); } public List<Staff> findAll() { throw todo(); }
    public Staff findByUsername(String username) { throw todo(); }
    public void ensureDefaultAdmin() { throw todo(); }
}