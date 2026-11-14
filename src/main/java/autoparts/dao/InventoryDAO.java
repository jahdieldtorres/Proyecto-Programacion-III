package autoparts.dao;

import autoparts.model.Inventory;

import java.util.List;
import java.util.Optional;

/**
 * Acceso a datos del inventario de los proveedores.
 *
 * Los servicios dependen de esta interfaz y no de cómo se guardan los datos:
 * hoy la implementa InventoryFileDAO (archivos planos) y después podrá
 * implementarla una clase para base de datos sin cambiar el servicio.
 *
 * @author Héctor Guevara
 */
public interface InventoryDAO {

    /**
     * Calcula el siguiente identificador libre.
     *
     * @return identificador para una existencia nueva
     */
    int nextId();

    /**
     * Guarda una existencia nueva.
     *
     * @param item existencia a guardar
     */
    void save(Inventory item);

    /**
     * Reemplaza los datos de una existencia ya guardada.
     *
     * @param item existencia con los datos nuevos
     * @return true si la existencia existía y se actualizó
     */
    boolean update(Inventory item);

    /**
     * Busca una existencia por su identificador.
     *
     * @param id identificador de la existencia
     * @return la existencia, o vacío si no existe
     */
    Optional<Inventory> findById(int id);

    /**
     * Devuelve todas las existencias, activas o no.
     *
     * @return lista de existencias
     */
    List<Inventory> findAll();

    /**
     * Devuelve las existencias de un proveedor, activas o no.
     *
     * @param supplierId identificador del proveedor
     * @return lista de existencias de ese proveedor
     */
    List<Inventory> findBySupplier(int supplierId);

    /**
     * Devuelve las existencias de un componente en todos los proveedores,
     * activas o no.
     *
     * @param componentId identificador del componente
     * @return lista de existencias de ese componente
     */
    List<Inventory> findByComponent(int componentId);
}