package rentgo.dao;

import java.util.List;
import rentgo.model.Vehicle;
import rentgo.model.VehicleStatus;
import rentgo.model.VehicleType;

/** Persistence contract placeholder for vehicles. */
public class VehicleDAO implements CrudDAO<Vehicle> {
    private static UnsupportedOperationException todo() { return new UnsupportedOperationException("TODO(Anggota 2)"); }
    public void add(Vehicle obj) { throw todo(); } public void update(Vehicle obj) { throw todo(); } public void delete(int id) { throw todo(); } public Vehicle findById(int id) { throw todo(); } public List<Vehicle> findAll() { throw todo(); }
    public List<Vehicle> search(String keyword, VehicleType type, VehicleStatus status) { throw todo(); }
    public List<Vehicle> findAvailable() { throw todo(); }
    public void updateStatus(int id, VehicleStatus status) { throw todo(); }
}