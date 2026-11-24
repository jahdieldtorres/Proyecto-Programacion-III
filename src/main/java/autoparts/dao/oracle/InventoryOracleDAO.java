package autoparts.dao.oracle;

import autoparts.dao.InventoryDAO;
import autoparts.exception.PersistenceException;
import autoparts.model.Condition;
import autoparts.model.Inventory;
import autoparts.model.Status;
import autoparts.util.DatabaseConnection;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Guarda y consulta el inventario de los proveedores (sus ofertas) llamando a
 * las funciones del paquete PL/SQL PKG_INVENTORY
 * (database/05b_paquetes_negocio.sql).
 *
 * Todo es una función y el resultado llega por el primer ? de la llamada: un
 * cursor en las consultas, el ID nuevo en el INSERT y las filas cambiadas en
 * el UPDATE.
 *
 * Implementa la misma interfaz {@link InventoryDAO} que InventoryFileDAO, por
 * eso InventoryService y ComparatorService no cambian: solo se cambia el DAO
 * en AppServices. Sigue el patrón de {@link UserOracleDAO}.
 *
 * Este DAO no descuenta el stock de los pedidos: eso lo hace el disparador
 * TRG_ORDER_ITEMS_STOCK de la base (database/03_disparadores.sql). Tampoco
 * guarda el historial de precios: lo hace TRG_INVENTORY_PRICE.
 *
 * @author Héctor Guevara
 */
public class InventoryOracleDAO implements InventoryDAO {

    private static final String CALL_LIST_ALL = "{ ? = call PKG_INVENTORY.LIST_ALL }";
    private static final String CALL_FIND_BY_ID = "{ ? = call PKG_INVENTORY.FIND_BY_ID(?) }";
    private static final String CALL_LIST_BY_SUPPLIER = "{ ? = call PKG_INVENTORY.LIST_BY_SUPPLIER(?) }";
    private static final String CALL_LIST_BY_COMPONENT = "{ ? = call PKG_INVENTORY.LIST_BY_COMPONENT(?) }";
    private static final String CALL_INSERT = "{ ? = call PKG_INVENTORY.INSERT_INVENTORY(?, ?, ?, ?, ?, ?, ?, ?) }";
    private static final String CALL_UPDATE = "{ ? = call PKG_INVENTORY.UPDATE_INVENTORY(?, ?, ?, ?, ?, ?, ?, ?, ?) }";

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
     * Guarda una existencia nueva con PKG_INVENTORY.INSERT_INVENTORY. El ID lo
     * genera Oracle (IDENTITY), la función lo devuelve y se le asigna al objeto
     * recibido, igual que hace InventoryFileDAO.
     */
    @Override
    public void save(Inventory item) {
        try (Connection connection = DatabaseConnection.getConnection();
                CallableStatement call = connection.prepareCall(CALL_INSERT)) {
            call.registerOutParameter(1, Types.NUMERIC); // ID nuevo
            call.setInt(2, item.getSupplierId());
            call.setInt(3, item.getComponentId());
            call.setInt(4, item.getPrice());
            call.setInt(5, item.getStock());
            call.setInt(6, item.getShippingCost());
            call.setInt(7, item.getWarrantyMonths());
            call.setString(8, item.getCondition().name());
            call.setString(9, item.getStatus().name());
            call.execute();
            item.setId(call.getInt(1));
        } catch (SQLException e) {
            throw new PersistenceException("No se pudo guardar la oferta del inventario.", e);
        }
    }

    /**
     * Actualiza todos los datos de la existencia con PKG_INVENTORY.UPDATE_INVENTORY.
     * La función devuelve cuántas filas cambió; si es 0, la existencia no existe.
     * Si el precio cambia, el disparador TRG_INVENTORY_PRICE guarda el precio
     * viejo y el nuevo.
     *
     * @return true si la existencia existía y se actualizó
     */
    @Override
    public boolean update(Inventory item) {
        try (Connection connection = DatabaseConnection.getConnection();
                CallableStatement call = connection.prepareCall(CALL_UPDATE)) {
            call.registerOutParameter(1, Types.NUMERIC); // filas cambiadas
            call.setInt(2, item.getId());
            call.setInt(3, item.getSupplierId());
            call.setInt(4, item.getComponentId());
            call.setInt(5, item.getPrice());
            call.setInt(6, item.getStock());
            call.setInt(7, item.getShippingCost());
            call.setInt(8, item.getWarrantyMonths());
            call.setString(9, item.getCondition().name());
            call.setString(10, item.getStatus().name());
            call.execute();
            return call.getInt(1) > 0;
        } catch (SQLException e) {
            throw new PersistenceException("No se pudo actualizar la oferta del inventario.", e);
        }
    }

    @Override
    public Optional<Inventory> findById(int id) {
        try (Connection connection = DatabaseConnection.getConnection();
                CallableStatement call = connection.prepareCall(CALL_FIND_BY_ID)) {
            call.registerOutParameter(1, Types.REF_CURSOR);
            call.setInt(2, id);
            call.execute();
            try (ResultSet result = (ResultSet) call.getObject(1)) {
                return result.next() ? Optional.of(mapRow(result)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw new PersistenceException("No se pudo leer el inventario.", e);
        }
    }

    @Override
    public List<Inventory> findAll() {
        List<Inventory> items = new ArrayList<>();
        try (Connection connection = DatabaseConnection.getConnection();
                CallableStatement call = connection.prepareCall(CALL_LIST_ALL)) {
            call.registerOutParameter(1, Types.REF_CURSOR);
            call.execute();
            try (ResultSet result = (ResultSet) call.getObject(1)) {
                while (result.next()) {
                    items.add(mapRow(result));
                }
            }
        } catch (SQLException e) {
            throw new PersistenceException("No se pudo leer el inventario.", e);
        }
        return items;
    }

    @Override
    public List<Inventory> findBySupplier(int supplierId) {
        return listBy(CALL_LIST_BY_SUPPLIER, supplierId);
    }

    @Override
    public List<Inventory> findByComponent(int componentId) {
        return listBy(CALL_LIST_BY_COMPONENT, componentId);
    }

    /**
     * Llama una función de consulta con un solo parámetro numérico (el ID del
     * proveedor o del componente) que devuelve un cursor de existencias.
     */
    private List<Inventory> listBy(String function, int value) {
        List<Inventory> items = new ArrayList<>();
        try (Connection connection = DatabaseConnection.getConnection();
                CallableStatement call = connection.prepareCall(function)) {
            call.registerOutParameter(1, Types.REF_CURSOR);
            call.setInt(2, value);
            call.execute();
            try (ResultSet result = (ResultSet) call.getObject(1)) {
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
