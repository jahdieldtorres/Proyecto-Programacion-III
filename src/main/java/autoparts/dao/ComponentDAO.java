package autoparts.dao;

import autoparts.model.Component;
import java.util.List;

/**
 *
 * @author ALVARO
 */
public interface ComponentDAO {
    
    /**
     * Devuelve una lista con todos los registros de componentes.
     * @return Lista de objetos Component
     */
    List<Component> listAll();
    
    /**
     * Busca un componente por su identificador único.
     * @param id El identificador del componente
     * @return El objeto Component encontrado, o null en caso contrario
     */
    Component findById(Long id);
    
    /**
     * Guarda un nuevo registro de componente.
     * @param component El objeto Component a guardar
     */
    void save(Component component);
    
    /**
     * Actualiza un registro de componente existente.
     * @param component El objeto Component con los valores actualizados
     */
    void update(Component component);
    
    /**
     * Elimina un registro de componente por su identificador único.
     * @param id El identificador del componente a eliminar
     */
    void delete(Long id);
    
}
