package autoparts.service;

import autoparts.dao.OrderDAO;
import autoparts.exception.InvalidDataException;
import autoparts.model.Cart;
import autoparts.model.Order;
import autoparts.model.User;
import java.util.List;

/**
 * Reglas de negocio de los pedidos: confirmar el carrito del cliente y
 * consultar sus pedidos.
 *
 * Aquí se valida (carrito vacío, usuario sin sesión) y se arma el pedido a
 * partir del carrito. Guardarlo en la base es trabajo del {@link OrderDAO}.
 * Este servicio tampoco resta el stock: lo hace el disparador de la base.
 *
 * @author Héctor Guevara
 */
public class OrderService {

    private final OrderDAO orderDAO;

    /**
     * Crea el servicio con su DAO.
     *
     * @param orderDAO acceso a datos de los pedidos
     */
    public OrderService(OrderDAO orderDAO) {
        this.orderDAO = orderDAO;
    }

    /**
     * Confirma el pedido del cliente que inició sesión con las líneas del
     * carrito. Si se guarda bien, el pedido recibe su ID y el carrito queda
     * vacío. Si falla (por ejemplo, sin stock), el carrito no cambia para que
     * el cliente pueda corregir la cantidad.
     *
     * @param cart carrito a confirmar
     * @return el pedido guardado, con su identificador
     * @throws InvalidDataException si el carrito está vacío o no hay sesión
     * @throws autoparts.exception.PersistenceException si no hay stock suficiente o falla la base
     */
    public Order checkout(Cart cart) {
        if (cart == null || cart.isEmpty()) {
            throw new InvalidDataException("El carrito está vacío.");
        }
        User user = Session.getUser();
        if (user == null) {
            throw new InvalidDataException("Debes iniciar sesión para confirmar el pedido.");
        }
        Order order = cart.toOrder(user.getId());
        int id = orderDAO.save(order);
        // Estas dos líneas solo se ejecutan si save no lanzó excepción.
        order.setId(id);
        cart.clear();
        return order;
    }

    /**
     * Devuelve los pedidos de un cliente, del más reciente al más antiguo.
     *
     * @param userId identificador del cliente
     * @return lista de pedidos con sus líneas (vacía si no tiene)
     */
    public List<Order> findByUser(int userId) {
        return orderDAO.findByUser(userId);
    }
}
