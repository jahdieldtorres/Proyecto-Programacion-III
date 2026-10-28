package autoparts.dao;

import autoparts.model.Affiliation;

import java.util.List;
import java.util.Optional;


public interface AffiliationDAO {

  
    int nextId();


    void save(Affiliation affiliation);

 
    boolean update(Affiliation affiliation);

  
    Optional<Affiliation> findById(int id);

 
    List<Affiliation> findByWorkshop(int workshopId);


    List<Affiliation> findBySupplier(int supplierId);


    List<Affiliation> findAll();
}