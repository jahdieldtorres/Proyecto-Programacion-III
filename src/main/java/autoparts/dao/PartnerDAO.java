package autoparts.dao;

import autoparts.model.Partner;

import java.util.List;
import java.util.Optional;


public interface PartnerDAO {

 
    int nextId();

    void save(Partner partner);

    
    boolean update(Partner partner);

 
    Optional<Partner> findById(int id);


    Optional<Partner> findByTaxId(String taxId);

    List<Partner> findAll();
}