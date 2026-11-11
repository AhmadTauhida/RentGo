package rentgo.dao;

import java.util.List;
import rentgo.model.Payment;
import rentgo.model.Rental;

/** Persistence contract placeholder for rental transactions. */
public class RentalDAO implements CrudDAO<Rental> {
    private static UnsupportedOperationException todo() { return new UnsupportedOperationException("TODO(Anggota 2)"); }
    public void add(Rental obj) { throw todo(); } public void update(Rental obj) { throw todo(); } public void delete(int id) { throw todo(); } public Rental findById(int id) { throw todo(); } public List<Rental> findAll() { throw todo(); }
    public void saveRental(Rental rental, Payment payment) { throw todo(); } public void cancel(int rentalId) { throw todo(); } public List<Rental> findActive() { throw todo(); } public List<Rental> findHistory() { throw todo(); }
}