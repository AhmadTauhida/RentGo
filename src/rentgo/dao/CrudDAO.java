package rentgo.dao;

import java.util.List;

/** Generic CRUD contract for persistence implementations. */
public interface CrudDAO<T> {
    void add(T obj);
    void update(T obj);
    void delete(int id);
    T findById(int id);
    List<T> findAll();
}