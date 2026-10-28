package autoparts.dao;

import autoparts.model.Brand;
import java.util.List;

/**
 * 
 * @author ALVARO
 */
public interface BrandDAO {
    
    /**
     * Devuelve una lista con todos los registros de marcas.
     * @return Lista de objetos Brand
     */
    List<Brand> listAll();
    
    /**
     * Busca una marca por su identificador único.
     * @param id El identificador de la marca
     * @return El objeto Brand encontrado, o null en caso contrario
     */
    Brand findById(Long id);
    
    /**
     * Guarda un nuevo registro de marca.
     * @param brand El objeto Brand a guardar
     */
    void save(Brand brand);
    
    /**
     * Actualiza un registro de marca existente.
     * @param brand El objeto Brand con los valores actualizados
     */
    void update(Brand brand);
    
    /**
     * Elimina un registro de marca por su identificador único.
     * @param id El identificador de la marca a eliminar
     */
    void delete(Long id);
    
}
