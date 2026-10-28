package autoparts.dao;

import autoparts.model.Category;
import java.util.List;

/**
 * Interfaz que define las operaciones de acceso a datos para las categorías.
 * 
 * @author ALVARO
 */
public interface CategoryDAO {
    
    /**
     * Devuelve una lista con todos los registros de categorías.
     * @return Lista de objetos Category
     */
    List<Category> listAll();
    
    /**
     * Busca una categoría por su identificador único.
     * @param id El identificador de la categoría
     * @return El objeto Category encontrado, o null en caso contrario
     */
    Category findById(int id);
    
    /**
     * Guarda un nuevo registro de categoría.
     * @param category El objeto Category a guardar
     */
    void save(Category category);
    
    /**
     * Actualiza un registro de categoría existente.
     * @param category El objeto Category con los valores actualizados
     */
    void update(Category category);
    
    /**
     * Elimina un registro de categoría por su identificador único.
     * @param id El identificador de la categoría a eliminar
     */
    void delete(int id);
    
}
