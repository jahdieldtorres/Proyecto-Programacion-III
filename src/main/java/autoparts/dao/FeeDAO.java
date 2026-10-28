package autoparts.dao;

import autoparts.model.Fee;

import java.util.List;
import java.util.Optional;


public interface FeeDAO {

   
    int nextId();


    void save(Fee fee);

  
    boolean update(Fee fee);


    Optional<Fee> findById(int id);

 
    List<Fee> findByService(int serviceId);

    List<Fee> findAll();
}