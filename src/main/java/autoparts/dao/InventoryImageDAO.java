package autoparts.dao;

import autoparts.model.InventoryImage;
import java.util.List;

/**
 * Interfaz que define las operaciones de acceso a datos para las fotos de las
 * ofertas de inventario.
 *
 * Solo maneja los registros (el nombre del archivo, el texto explicativo y si
 * es la principal); copiar o borrar el archivo de la foto es trabajo de
 * ImageStorage.
 *
 * @author Álvaro Aramendiz
 */
public interface InventoryImageDAO {

    /**
     * Devuelve las fotos de una oferta: primero la principal y después las
     * demás por su orden.
     *
     * @param inventoryId El identificador de la oferta de inventario
     * @return Lista de objetos InventoryImage; vacía si la oferta no tiene fotos
     */
    List<InventoryImage> listByInventory(int inventoryId);

    /**
     * Devuelve la foto que se muestra de la oferta: la marcada como principal
     * o, si ninguna lo está, la primera por orden.
     *
     * @param inventoryId El identificador de la oferta de inventario
     * @return El objeto InventoryImage, o null si la oferta no tiene fotos
     */
    InventoryImage findMain(int inventoryId);

    /**
     * Busca una foto por su identificador único.
     *
     * @param id El identificador de la foto
     * @return El objeto InventoryImage encontrado, o null en caso contrario
     */
    InventoryImage findById(int id);

    /**
     * Guarda una foto nueva y le asigna su identificador. Si es la principal,
     * las demás fotos de la misma oferta dejan de serlo.
     *
     * @param image El objeto InventoryImage a guardar
     */
    void save(InventoryImage image);

    /**
     * Actualiza el nombre de archivo, el texto, el orden y si es la principal.
     * Si pasa a ser la principal, las demás fotos de la misma oferta dejan de serlo.
     *
     * @param image El objeto InventoryImage con los valores actualizados
     */
    void update(InventoryImage image);

    /**
     * Elimina el registro de una foto. No borra el archivo del disco.
     *
     * @param id El identificador de la foto a eliminar
     */
    void delete(int id);
}
