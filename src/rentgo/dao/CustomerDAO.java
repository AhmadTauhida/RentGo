package rentgo.dao;

import java.util.List;
import java.util.Map;
import rentgo.model.Customer;

/** Persistence contract placeholder for customers. */
public class CustomerDAO implements CrudDAO<Customer> {
    private static UnsupportedOperationException todo() { return new UnsupportedOperationException("TODO(Anggota 2)"); }
    public void add(Customer obj) { throw todo(); } public void update(Customer obj) { throw todo(); } public void delete(int id) { throw todo(); } public Customer findById(int id) { throw todo(); } public List<Customer> findAll() { throw todo(); }
    public List<Customer> search(String nameOrNationalId) { throw todo(); }
    public Map<String, Customer> mapByNationalId() { throw todo(); }
}