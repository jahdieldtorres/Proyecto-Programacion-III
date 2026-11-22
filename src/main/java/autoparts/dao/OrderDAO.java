package autoparts.dao;

import autoparts.model.Order;
import java.util.List;

/**
 * Operaciones para guardar y consultar los pedidos de los clientes.
 *
 * Los servicios dependen de esta interfaz y no de cómo se guardan los datos:
 * OrderService usa OrderDAO y hoy la implementa OrderOracleDAO (Oracle).
 *
 * @author Héctor Guevara
 */
public interface OrderDAO {

    /**
     * Guarda un pedido nuevo con todas sus líneas. Se guarda todo o nada: si
     * una línea falla, el pedido tampoco queda guardado.
     *
     * @param order pedido a guardar (sin ID)
     * @return el identificador que asignó la base de datos
     */
    int save(Order order);

    /**
     * Busca un pedido por su identificador, con sus líneas.
     *
     * @param id identificador del pedido
     * @return el pedido, o null si no existe
     */
    Order findById(int id);

    /**
     * Devuelve los pedidos de un cliente, con sus líneas, del más reciente al
     * más antiguo.
     *
     * @param userId identificador del cliente
     * @return lista de pedidos (vacía si no tiene)
     */
    List<Order> findByUser(int userId);
}
