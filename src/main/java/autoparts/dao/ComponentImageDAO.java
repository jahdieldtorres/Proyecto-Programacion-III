package autoparts.dao;

import autoparts.model.ComponentImage;
import java.util.List;

/**
 * Interfaz que define las operaciones de acceso a datos para las fotos de los componentes.
 *
 * Solo maneja los registros (el nombre del archivo y si es la principal);
 * copiar o borrar el archivo de la foto es trabajo de ImageStorage.
 *
 * @author ALVARO
 */
public interface ComponentImageDAO {

    /**
     * Devuelve las fotos de un componente: primero la principal y después las
     * demás por su orden.
     *
     * @param componentId El identificador del componente
     * @return Lista de objetos ComponentImage; vacía si no tiene fotos
     */
    List<ComponentImage> listByComponent(int componentId);

    /**
     * Devuelve la foto que se muestra del componente: la marcada como
     * principal o, si ninguna lo está, la primera por orden.
     *
     * @param componentId El identificador del componente
     * @return El objeto ComponentImage, o null si el componente no tiene fotos
     */
    ComponentImage findMain(int componentId);

    /**
     * Busca una foto por su identificador único.
     *
     * @param id El identificador de la foto
     * @return El objeto ComponentImage encontrado, o null en caso contrario
     */
    ComponentImage findById(int id);

    /**
     * Guarda una foto nueva y le asigna su identificador. Si es la principal,
     * las demás fotos del mismo componente dejan de serlo.
     *
     * @param image El objeto ComponentImage a guardar
     */
    void save(ComponentImage image);

    /**
     * Actualiza el nombre de archivo, el orden y si es la principal. Si pasa a
     * ser la principal, las demás fotos del mismo componente dejan de serlo.
     *
     * @param image El objeto ComponentImage con los valores actualizados
     */
    void update(ComponentImage image);

    /**
     * Elimina el registro de una foto. No borra el archivo del disco.
     *
     * @param id El identificador de la foto a eliminar
     */
    void delete(int id);
}
