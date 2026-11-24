package autoparts.dao.oracle;

import autoparts.dao.OrderDAO;
import autoparts.exception.PersistenceException;
import autoparts.model.Order;
import autoparts.model.OrderItem;
import autoparts.model.OrderStatus;
import autoparts.util.DatabaseConnection;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

/**
 * Guarda y consulta los pedidos llamando a las funciones del paquete PL/SQL
 * PKG_ORDERS (database/05b_paquetes_negocio.sql).
 *
 * Todo es una función y el resultado llega por el primer ? de la llamada: un
 * cursor en las consultas y el ID nuevo en los INSERT.
 *
 * Cada pedido ocupa dos tablas: ORDERS (el pedido) y ORDER_ITEMS (una fila por
 * línea). Cada función del paquete toca una sola tabla, así que guardar sigue
 * haciéndose aquí en una transacción (commit y rollback los maneja Java, el
 * paquete no confirma): o se guardan el pedido y todas sus líneas, o no se
 * guarda nada. Sigue el patrón de {@link ComponentOracleDAO}.
 *
 * Este DAO no resta el stock: lo hace el disparador TRG_ORDER_ITEMS_STOCK al
 * insertar cada línea (database/03_disparadores.sql). Si no alcanza el stock,
 * el disparador responde con el error 20001 y aquí se convierte en un mensaje
 * claro para el cliente.
 *
 * @author Héctor Guevara
 */
public class OrderOracleDAO implements OrderDAO {

    /** Código que recibe Java cuando el disparador responde ORA-20001 (sin stock). */
    private static final int NOT_ENOUGH_STOCK = 20001;

    private static final String CALL_FIND_BY_ID = "{ ? = call PKG_ORDERS.FIND_BY_ID(?) }";
    private static final String CALL_LIST_BY_USER = "{ ? = call PKG_ORDERS.LIST_BY_USER(?) }";
    private static final String CALL_LIST_ITEMS = "{ ? = call PKG_ORDERS.LIST_ITEMS(?) }";
    private static final String CALL_INSERT_ORDER = "{ ? = call PKG_ORDERS.INSERT_ORDER(?, ?, ?, ?) }";
    private static final String CALL_INSERT_ITEM = "{ ? = call PKG_ORDERS.INSERT_ORDER_ITEM(?, ?, ?, ?, ?) }";

    /**
     * Guarda el pedido con una transacción: llama a INSERT_ORDER, toma el ID
     * que devuelve la función y llama a INSERT_ORDER_ITEM por cada línea con la
     * misma conexión. Si algo falla, se deshace todo. El objeto recibido no se
     * modifica: el servicio es quien le pone el ID que devuelve este método.
     *
     * @throws PersistenceException si no hay stock suficiente o falla la base
     */
    @Override
    public int save(Order order) {
        try (Connection connection = DatabaseConnection.getConnection()) {
            connection.setAutoCommit(false);
            try {
                int orderId = insertOrder(connection, order);
                for (OrderItem item : order.getItems()) {
                    insertItem(connection, orderId, item);
                }
                connection.commit();
                return orderId;
            } catch (SQLException | RuntimeException e) {
                rollback(connection);
                throw e;
            }
        } catch (SQLException e) {
            if (e.getErrorCode() == NOT_ENOUGH_STOCK) {
                throw new PersistenceException(
                        "No hay stock suficiente para uno de los repuestos del carrito.", e);
            }
            throw new PersistenceException("No se pudo guardar el pedido.", e);
        }
    }

    @Override
    public Order findById(int id) {
        try (Connection connection = DatabaseConnection.getConnection();
                CallableStatement call = connection.prepareCall(CALL_FIND_BY_ID)) {
            call.registerOutParameter(1, Types.REF_CURSOR);
            call.setInt(2, id);
            call.execute();
            Order order;
            try (ResultSet result = (ResultSet) call.getObject(1)) {
                if (!result.next()) {
                    return null;
                }
                order = mapRow(result);
            }
            loadItems(connection, order);
            return order;
        } catch (SQLException e) {
            throw new PersistenceException("No se pudo buscar el pedido.", e);
        }
    }

    @Override
    public List<Order> findByUser(int userId) {
        List<Order> orders = new ArrayList<>();
        try (Connection connection = DatabaseConnection.getConnection();
                CallableStatement call = connection.prepareCall(CALL_LIST_BY_USER)) {
            call.registerOutParameter(1, Types.REF_CURSOR);
            call.setInt(2, userId);
            call.execute();
            try (ResultSet result = (ResultSet) call.getObject(1)) {
                while (result.next()) {
                    orders.add(mapRow(result));
                }
            }
            // Las líneas se leen después de cerrar el ResultSet de los pedidos.
            for (Order order : orders) {
                loadItems(connection, order);
            }
        } catch (SQLException e) {
            throw new PersistenceException("No se pudieron leer los pedidos.", e);
        }
        return orders;
    }

    /**
     * Llama a INSERT_ORDER (con el total calculado por el pedido) y devuelve el
     * ID que generó Oracle.
     */
    private int insertOrder(Connection connection, Order order) throws SQLException {
        try (CallableStatement call = connection.prepareCall(CALL_INSERT_ORDER)) {
            call.registerOutParameter(1, Types.NUMERIC); // ID nuevo
            call.setInt(2, order.getUserId());
            call.setTimestamp(3, Timestamp.valueOf(order.getOrderDate()));
            call.setString(4, order.getStatus().name());
            call.setInt(5, order.getTotal());
            call.execute();
            return call.getInt(1);
        }
    }

    /**
     * Llama a INSERT_ORDER_ITEM con una línea del pedido. Aquí el disparador
     * revisa y resta el stock. La función devuelve el ID de la línea, pero el
     * pedido no lo necesita.
     */
    private void insertItem(Connection connection, int orderId, OrderItem item) throws SQLException {
        try (CallableStatement call = connection.prepareCall(CALL_INSERT_ITEM)) {
            call.registerOutParameter(1, Types.NUMERIC); // ID nuevo de la línea
            call.setInt(2, orderId);
            call.setInt(3, item.getInventoryId());
            call.setInt(4, item.getQuantity());
            call.setInt(5, item.getUnitPrice());
            call.setInt(6, item.getShippingCost());
            call.execute();
        }
    }

    /**
     * Lee las líneas del pedido con LIST_ITEMS y las agrega a él.
     */
    private void loadItems(Connection connection, Order order) throws SQLException {
        try (CallableStatement call = connection.prepareCall(CALL_LIST_ITEMS)) {
            call.registerOutParameter(1, Types.REF_CURSOR);
            call.setInt(2, order.getId());
            call.execute();
            try (ResultSet result = (ResultSet) call.getObject(1)) {
                while (result.next()) {
                    order.addItem(mapItemRow(result));
                }
            }
        }
    }

    /**
     * Deshace la transacción. Es importante: Oracle confirma lo pendiente al
     * cerrar la conexión, así que sin rollback quedaría guardada a medias.
     */
    private void rollback(Connection connection) {
        try {
            connection.rollback();
        } catch (SQLException ignored) {
            // Si el rollback falla, la excepción original es la que importa.
        }
    }

    /**
     * Convierte la fila actual de ORDERS en un pedido sin líneas.
     */
    private Order mapRow(ResultSet result) throws SQLException {
        return new Order(result.getInt("ID"), result.getInt("USER_ID"),
                result.getTimestamp("ORDER_DATE").toLocalDateTime(),
                OrderStatus.valueOf(result.getString("STATUS")));
    }

    /**
     * Convierte la fila actual de ORDER_ITEMS en una línea de pedido.
     */
    private OrderItem mapItemRow(ResultSet result) throws SQLException {
        return new OrderItem(result.getInt("ID"), result.getInt("ORDER_ID"),
                result.getInt("INVENTORY_ID"), result.getInt("QUANTITY"),
                result.getInt("UNIT_PRICE"), result.getInt("SHIPPING_COST"));
    }
}
