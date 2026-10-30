package autoparts.dao;

import autoparts.model.Inventory;

import java.util.List;

/**
 * Acceso a datos del inventario de los proveedores.
 *
 * @author Héctor Guevara
 */
public interface InventoryDAO {

    /**
     * Devuelve todas las existencias, activas o no.
     *
     * @return lista de existencias
     */
    List<Inventory> findAll();

    /**
     * Devuelve las existencias de un componente en todos los proveedores,
     * activas o no.
     *
     * @param componentId identificador del componente
     * @return lista de existencias de ese componente
     */
    List<Inventory> findByComponent(int componentId);
}