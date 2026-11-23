package autoparts.dao.oracle;

import autoparts.dao.InventoryImageDAO;
import autoparts.exception.PersistenceException;
import autoparts.model.InventoryImage;
import autoparts.util.DatabaseConnection;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

/**
 * Guarda y consulta los registros de las fotos de las ofertas de inventario
 * llamando a las funciones del paquete PL/SQL PKG_INVENTORY_IMAGES
 * (database/05a_paquetes_catalogo.sql).
 *
 * Regla que cuida el paquete: una oferta tiene como máximo una foto
 * principal. Al guardar o actualizar una foto como principal, las otras de la
 * misma oferta se desmarcan dentro de la misma llamada, así que el DAO no
 * necesita manejar la transacción: la conexión confirma al terminar la
 * llamada y Oracle deshace todo si algo falla.
 *
 *
 * @author Álvaro Aramendiz
 */
public class InventoryImageOracleDAO implements InventoryImageDAO {

    // Consultas: el primer ? es el cursor que devuelve la función
    private static final String CALL_LIST_BY_INVENTORY = "{ ? = call PKG_INVENTORY_IMAGES.LIST_BY_INVENTORY(?) }";
    private static final String CALL_FIND_MAIN = "{ ? = call PKG_INVENTORY_IMAGES.FIND_MAIN(?) }";
    private static final String CALL_FIND_BY_ID = "{ ? = call PKG_INVENTORY_IMAGES.FIND_BY_ID(?) }";

    // Cambios: el primer ? es el número que devuelve la función
    // (INSERT_IMAGE: el ID nuevo; UPDATE_IMAGE y DELETE_IMAGE: las filas cambiadas)
    private static final String CALL_INSERT = "{ ? = call PKG_INVENTORY_IMAGES.INSERT_IMAGE(?, ?, ?, ?, ?) }";
    private static final String CALL_UPDATE = "{ ? = call PKG_INVENTORY_IMAGES.UPDATE_IMAGE(?, ?, ?, ?, ?) }";
    private static final String CALL_DELETE = "{ ? = call PKG_INVENTORY_IMAGES.DELETE_IMAGE(?) }";

    @Override
    public List<InventoryImage> listByInventory(int inventoryId) {
        List<InventoryImage> images = new ArrayList<>();
        try (Connection connection = DatabaseConnection.getConnection();
                CallableStatement call = connection.prepareCall(CALL_LIST_BY_INVENTORY)) {
            call.registerOutParameter(1, Types.REF_CURSOR);
            call.setInt(2, inventoryId);
            call.execute();
            try (ResultSet result = (ResultSet) call.getObject(1)) {
                while (result.next()) {
                    images.add(mapRow(result));
                }
            }
        } catch (SQLException e) {
            throw new PersistenceException("No se pudieron leer las fotos de la oferta.", e);
        }
        return images;
    }

    @Override
    public InventoryImage findMain(int inventoryId) {
        try (Connection connection = DatabaseConnection.getConnection();
                CallableStatement call = connection.prepareCall(CALL_FIND_MAIN)) {
            call.registerOutParameter(1, Types.REF_CURSOR);
            call.setInt(2, inventoryId);
            return readOne(call);
        } catch (SQLException e) {
            throw new PersistenceException("No se pudo leer la foto principal de la oferta.", e);
        }
    }

    @Override
    public InventoryImage findById(int id) {
        try (Connection connection = DatabaseConnection.getConnection();
                CallableStatement call = connection.prepareCall(CALL_FIND_BY_ID)) {
            call.registerOutParameter(1, Types.REF_CURSOR);
            call.setInt(2, id);
            return readOne(call);
        } catch (SQLException e) {
            throw new PersistenceException("No se pudo buscar la foto.", e);
        }
    }

    /**
     * Guarda una foto nueva con PKG_INVENTORY_IMAGES.INSERT_IMAGE. El ID lo
     * genera Oracle (IDENTITY), la función lo devuelve y se le asigna al
     * objeto. Si es la principal, la función desmarca las otras de la oferta.
     */
    @Override
    public void save(InventoryImage image) {
        try (Connection connection = DatabaseConnection.getConnection();
                CallableStatement call = connection.prepareCall(CALL_INSERT)) {
            call.registerOutParameter(1, Types.NUMERIC); // ID nuevo
            call.setInt(2, image.getInventoryId());
            call.setString(3, image.getFileName());
            call.setString(4, image.getCaption());
            call.setString(5, image.isMain() ? "S" : "N");
            call.setInt(6, image.getSortOrder());
            call.execute();
            image.setId(call.getInt(1));
        } catch (SQLException e) {
            throw new PersistenceException("No se pudo guardar la foto de la oferta.", e);
        }
    }

    /**
     * Actualiza la foto con PKG_INVENTORY_IMAGES.UPDATE_IMAGE. La función
     * devuelve cuántas filas cambió; si es 0, la foto no existe. Si pasa a ser
     * la principal, la función desmarca las otras de la oferta.
     */
    @Override
    public void update(InventoryImage image) {
        try (Connection connection = DatabaseConnection.getConnection();
                CallableStatement call = connection.prepareCall(CALL_UPDATE)) {
            call.registerOutParameter(1, Types.NUMERIC); // filas cambiadas
            call.setInt(2, image.getId());
            call.setString(3, image.getFileName());
            call.setString(4, image.getCaption());
            call.setString(5, image.isMain() ? "S" : "N");
            call.setInt(6, image.getSortOrder());
            call.execute();
            if (call.getInt(1) == 0) {
                throw new PersistenceException("No existe la foto que se quiere actualizar.");
            }
        } catch (SQLException e) {
            throw new PersistenceException("No se pudo actualizar la foto de la oferta.", e);
        }
    }

    @Override
    public void delete(int id) {
        try (Connection connection = DatabaseConnection.getConnection();
                CallableStatement call = connection.prepareCall(CALL_DELETE)) {
            call.registerOutParameter(1, Types.NUMERIC); // filas borradas
            call.setInt(2, id);
            call.execute();
        } catch (SQLException e) {
            throw new PersistenceException("No se pudo eliminar la foto de la oferta.", e);
        }
    }

    /**
     * Ejecuta una función de consulta que devuelve como máximo una foto.
     * Quien la llama ya registró el cursor en el primer ? y puso el parámetro
     * de entrada con su tipo.
     *
     * @param call la llamada lista para ejecutar
     * @return la foto encontrada, o null si no existe
     */
    private InventoryImage readOne(CallableStatement call) throws SQLException {
        call.execute();
        try (ResultSet result = (ResultSet) call.getObject(1)) {
            return result.next() ? mapRow(result) : null;
        }
    }

    /**
     * Convierte la fila actual del resultado en un objeto InventoryImage. Los
     * nombres de columna son los que devuelve el cursor del paquete.
     */
    private InventoryImage mapRow(ResultSet result) throws SQLException {
        return new InventoryImage(
                result.getInt("ID"),
                result.getInt("INVENTORY_ID"),
                result.getString("FILE_NAME"),
                result.getString("CAPTION"),
                "S".equals(result.getString("IS_MAIN")),
                result.getInt("SORT_ORDER"));
    }
}
