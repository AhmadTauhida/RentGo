package rentgo.dao;

import java.util.List;
import rentgo.model.Payment;

/** Persistence contract placeholder for payments. */
public class PaymentDAO implements CrudDAO<Payment> {
    private static UnsupportedOperationException todo() { return new UnsupportedOperationException("TODO(Anggota 2)"); }
    public void add(Payment obj) { throw todo(); } public void update(Payment obj) { throw todo(); } public void delete(int id) { throw todo(); } public Payment findById(int id) { throw todo(); } public List<Payment> findAll() { throw todo(); }
    public List<Payment> findByRentalId(int rentalId) { throw todo(); }
}