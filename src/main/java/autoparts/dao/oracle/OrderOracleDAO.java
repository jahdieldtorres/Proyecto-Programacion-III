package autoparts.dao.oracle;

import autoparts.dao.OrderDAO;
import autoparts.exception.PersistenceException;
import autoparts.model.Order;
import autoparts.model.OrderItem;
import autoparts.model.OrderStatus;
import autoparts.util.DatabaseConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

/**
 * Guarda y consulta los pedidos en Oracle.
 *
 * Cada pedido ocupa dos tablas: ORDERS (el pedido) y ORDER_ITEMS (una fila por
 * línea). Por eso guardar se hace en una transacción: o se guardan el pedido y
 * todas sus líneas, o no se guarda nada. Sigue el mismo patrón de
 * {@link ComponentOracleDAO}.
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

    private static final String INSERT_ORDER =
            "INSERT INTO ORDERS (USER_ID, ORDER_DATE, STATUS, TOTAL) VALUES (?, ?, ?, ?)";

    private static final String INSERT_ITEM =
            "INSERT INTO ORDER_ITEMS (ORDER_ID, INVENTORY_ID, QUANTITY, UNIT_PRICE, SHIPPING_COST) "
            + "VALUES (?, ?, ?, ?, ?)";

    private static final String SELECT_ORDERS =
            "SELECT ID, USER_ID, ORDER_DATE, STATUS FROM ORDERS";

    private static final String FIND_BY_ID = SELECT_ORDERS + " WHERE ID = ?";

    private static final String FIND_BY_USER = SELECT_ORDERS + " WHERE USER_ID = ? ORDER BY ID DESC";

    private static final String FIND_ITEMS =
            "SELECT ID, ORDER_ID, INVENTORY_ID, QUANTITY, UNIT_PRICE, SHIPPING_COST "
            + "FROM ORDER_ITEMS WHERE ORDER_ID = ? ORDER BY ID";

    /**
     * Guarda el pedido con una transacción: inserta en ORDERS, toma el ID que
     * genera Oracle e inserta cada línea en ORDER_ITEMS con la misma conexión.
     * Si algo falla, se deshace todo. El objeto recibido no se modifica: el
     * servicio es quien le pone el ID que devuelve este método.
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
                PreparedStatement statement = connection.prepareStatement(FIND_BY_ID)) {
            statement.setInt(1, id);
            try (ResultSet result = statement.executeQuery()) {
                if (!result.next()) {
                    return null;
                }
                Order order = mapRow(result);
                loadItems(connection, order);
                return order;
            }
        } catch (SQLException e) {
            throw new PersistenceException("No se pudo buscar el pedido.", e);
        }
    }

    @Override
    public List<Order> findByUser(int userId) {
        List<Order> orders = new ArrayList<>();
        try (Connection connection = DatabaseConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(FIND_BY_USER)) {
            statement.setInt(1, userId);
            try (ResultSet result = statement.executeQuery()) {
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
     * Inserta la fila de ORDERS (con el total calculado por el pedido) y
     * devuelve el ID que generó Oracle.
     */
    private int insertOrder(Connection connection, Order order) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(INSERT_ORDER, new String[]{"ID"})) {
            statement.setInt(1, order.getUserId());
            statement.setTimestamp(2, Timestamp.valueOf(order.getOrderDate()));
            statement.setString(3, order.getStatus().name());
            statement.setInt(4, order.getTotal());
            statement.executeUpdate();
            try (ResultSet keys = statement.getGeneratedKeys()) {
                keys.next();
                return keys.getInt(1);
            }
        }
    }

    /**
     * Inserta una línea en ORDER_ITEMS. Aquí el disparador revisa y resta el stock.
     */
    private void insertItem(Connection connection, int orderId, OrderItem item) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(INSERT_ITEM)) {
            statement.setInt(1, orderId);
            statement.setInt(2, item.getInventoryId());
            statement.setInt(3, item.getQuantity());
            statement.setInt(4, item.getUnitPrice());
            statement.setInt(5, item.getShippingCost());
            statement.executeUpdate();
        }
    }

    /**
     * Lee las líneas del pedido y las agrega a él.
     */
    private void loadItems(Connection connection, Order order) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(FIND_ITEMS)) {
            statement.setInt(1, order.getId());
            try (ResultSet result = statement.executeQuery()) {
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
