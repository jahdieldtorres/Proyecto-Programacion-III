package autoparts.dao.oracle;

import autoparts.dao.InventoryDAO;
import autoparts.exception.PersistenceException;
import autoparts.model.Condition;
import autoparts.model.Inventory;
import autoparts.model.Status;
import autoparts.util.DatabaseConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Guarda y consulta el inventario de los proveedores (sus ofertas) en la tabla
 * INVENTORY de la base de datos Oracle.
 *
 * Implementa la misma interfaz {@link InventoryDAO} que InventoryFileDAO, por
 * eso InventoryService y ComparatorService no cambian: solo se cambia el DAO
 * en AppServices. Sigue el mismo patrón de UserOracleDAO.
 *
 * Este DAO no descuenta el stock de los pedidos: eso lo hace el disparador
 * TRG_ORDER_ITEMS_STOCK de la base (database/03_disparadores.sql). Tampoco
 * guarda el historial de precios: lo hace TRG_INVENTORY_PRICE.
 *
 * @author Héctor Guevara
 */
public class InventoryOracleDAO implements InventoryDAO {

    private static final String SELECT_INVENTORY =
            "SELECT ID, SUPPLIER_ID, COMPONENT_ID, PRICE, STOCK, SHIPPING_COST, "
            + "WARRANTY_MONTHS, CONDITION, STATUS FROM INVENTORY";

    private static final String FIND_ALL = SELECT_INVENTORY + " ORDER BY ID";

    private static final String FIND_BY_ID = SELECT_INVENTORY + " WHERE ID = ?";

    private static final String FIND_BY_SUPPLIER = SELECT_INVENTORY + " WHERE SUPPLIER_ID = ? ORDER BY ID";

    private static final String FIND_BY_COMPONENT = SELECT_INVENTORY + " WHERE COMPONENT_ID = ? ORDER BY ID";

    private static final String INSERT =
            "INSERT INTO INVENTORY (SUPPLIER_ID, COMPONENT_ID, PRICE, STOCK, SHIPPING_COST, "
            + "WARRANTY_MONTHS, CONDITION, STATUS) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

    private static final String UPDATE =
            "UPDATE INVENTORY SET SUPPLIER_ID = ?, COMPONENT_ID = ?, PRICE = ?, STOCK = ?, "
            + "SHIPPING_COST = ?, WARRANTY_MONTHS = ?, CONDITION = ?, STATUS = ? WHERE ID = ?";

    /**
     * Con Oracle el ID lo genera la base (columna IDENTITY) al guardar, así que
     * no se puede calcular antes. Devuelve 0 como valor provisorio:
     * {@link #save(Inventory)} reemplaza ese 0 por el ID real en el mismo objeto.
     *
     * @return siempre 0
     */
    @Override
    public int nextId() {
        return 0;
    }

    /**
     * Guarda una existencia nueva. Después de guardar, la existencia recibida
     * queda con el ID que le asignó Oracle.
     */
    @Override
    public void save(Inventory item) {
        try (Connection connection = DatabaseConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(INSERT, new String[]{"ID"})) {
            statement.setInt(1, item.getSupplierId());
            statement.setInt(2, item.getComponentId());
            statement.setInt(3, item.getPrice());
            statement.setInt(4, item.getStock());
            statement.setInt(5, item.getShippingCost());
            statement.setInt(6, item.getWarrantyMonths());
            statement.setString(7, item.getCondition().name());
            statement.setString(8, item.getStatus().name());
            statement.executeUpdate();

            try (ResultSet keys = statement.getGeneratedKeys()) {
                keys.next();
                item.setId(keys.getInt(1));
            }
        } catch (SQLException e) {
            throw new PersistenceException("No se pudo guardar la oferta del inventario.", e);
        }
    }

    /**
     * Actualiza todos los datos de la existencia. Si el precio cambia, el
     * disparador TRG_INVENTORY_PRICE guarda el precio viejo y el nuevo.
     *
     * @return true si la existencia existía y se actualizó
     */
    @Override
    public boolean update(Inventory item) {
        try (Connection connection = DatabaseConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(UPDATE)) {
            statement.setInt(1, item.getSupplierId());
            statement.setInt(2, item.getComponentId());
            statement.setInt(3, item.getPrice());
            statement.setInt(4, item.getStock());
            statement.setInt(5, item.getShippingCost());
            statement.setInt(6, item.getWarrantyMonths());
            statement.setString(7, item.getCondition().name());
            statement.setString(8, item.getStatus().name());
            statement.setInt(9, item.getId());
            return statement.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new PersistenceException("No se pudo actualizar la oferta del inventario.", e);
        }
    }

    @Override
    public Optional<Inventory> findById(int id) {
        List<Inventory> items = findList(FIND_BY_ID, id);
        return items.isEmpty() ? Optional.empty() : Optional.of(items.get(0));
    }

    @Override
    public List<Inventory> findAll() {
        return findList(FIND_ALL);
    }

    @Override
    public List<Inventory> findBySupplier(int supplierId) {
        return findList(FIND_BY_SUPPLIER, supplierId);
    }

    @Override
    public List<Inventory> findByComponent(int componentId) {
        return findList(FIND_BY_COMPONENT, componentId);
    }

    /**
     * Ejecuta una consulta de inventario y devuelve todas las filas como objetos.
     *
     * @param sql    consulta con tantos ? como valores en params
     * @param params valores de los parámetros, en orden (puede no haber ninguno)
     */
    private List<Inventory> findList(String sql, Object... params) {
        List<Inventory> items = new ArrayList<>();
        try (Connection connection = DatabaseConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            for (int i = 0; i < params.length; i++) {
                statement.setObject(i + 1, params[i]);
            }
            try (ResultSet result = statement.executeQuery()) {
                while (result.next()) {
                    items.add(mapRow(result));
                }
            }
        } catch (SQLException e) {
            throw new PersistenceException("No se pudo leer el inventario.", e);
        }
        return items;
    }

    /**
     * Convierte la fila actual del resultado en un objeto Inventory.
     */
    private Inventory mapRow(ResultSet result) throws SQLException {
        return new Inventory(
                result.getInt("ID"),
                result.getInt("SUPPLIER_ID"),
                result.getInt("COMPONENT_ID"),
                result.getInt("PRICE"),
                result.getInt("STOCK"),
                result.getInt("SHIPPING_COST"),
                result.getInt("WARRANTY_MONTHS"),
                Condition.valueOf(result.getString("CONDITION")),
                Status.valueOf(result.getString("STATUS")));
    }
}