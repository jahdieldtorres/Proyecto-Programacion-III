package autoparts.dao.oracle;

import autoparts.dao.CompatibilityDAO;
import autoparts.exception.PersistenceException;
import autoparts.util.DatabaseConnection;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

/**
 * Guarda y consulta qué componentes son compatibles con qué vehículos
 * llamando a las funciones del paquete PL/SQL PKG_COMPATIBILITY
 * (database/05a_paquetes_catalogo.sql). La tabla COMPATIBILITY tiene como
 * llave primaria el par componente + vehículo.
 *
 * Las consultas devuelven un cursor con una columna de ID; enlazar y desenlazar
 * devuelven las filas afectadas.
 *
 * Implementa la misma interfaz {@link CompatibilityDAO} que
 * CompatibilityFileDAO. Sigue el patrón de {@link UserOracleDAO}.
 *
 * @author Álvaro Aramendiz
 */
public class CompatibilityOracleDAO implements CompatibilityDAO {

    private static final String CALL_VEHICLES_OF_COMPONENT =
            "{ ? = call PKG_COMPATIBILITY.LIST_VEHICLE_IDS(?) }";

    private static final String CALL_COMPONENTS_OF_VEHICLE =
            "{ ? = call PKG_COMPATIBILITY.LIST_COMPONENT_IDS(?) }";

    private static final String CALL_LINK = "{ ? = call PKG_COMPATIBILITY.LINK_COMPATIBILITY(?, ?) }";

    private static final String CALL_UNLINK = "{ ? = call PKG_COMPATIBILITY.UNLINK_COMPATIBILITY(?, ?) }";

    @Override
    public List<Integer> listVehicleIds(int componentId) {
        return listIds(CALL_VEHICLES_OF_COMPONENT, componentId,
                "No se pudieron leer los vehículos compatibles.");
    }

    @Override
    public List<Integer> listComponentIds(int vehicleId) {
        return listIds(CALL_COMPONENTS_OF_VEHICLE, vehicleId,
                "No se pudieron leer los componentes compatibles.");
    }

    /**
     * Enlaza un componente con un vehículo. Si ya estaban enlazados, Oracle
     * rechaza la fila repetida (llave primaria).
     */
    @Override
    public void link(int componentId, int vehicleId) {
        execute(CALL_LINK, componentId, vehicleId, "No se pudo registrar la compatibilidad.");
    }

    @Override
    public void unlink(int componentId, int vehicleId) {
        execute(CALL_UNLINK, componentId, vehicleId, "No se pudo quitar la compatibilidad.");
    }

    /**
     * Llama una función de consulta con un solo parámetro que devuelve un
     * cursor de una columna de números.
     */
    private List<Integer> listIds(String function, int value, String errorMessage) {
        List<Integer> ids = new ArrayList<>();
        try (Connection connection = DatabaseConnection.getConnection();
                CallableStatement call = connection.prepareCall(function)) {
            call.registerOutParameter(1, Types.REF_CURSOR);
            call.setInt(2, value);
            call.execute();
            try (ResultSet result = (ResultSet) call.getObject(1)) {
                while (result.next()) {
                    ids.add(result.getInt(1));
                }
            }
        } catch (SQLException e) {
            throw new PersistenceException(errorMessage, e);
        }
        return ids;
    }

    /**
     * Llama a la función de enlazar o desenlazar, que recibe (componente,
     * vehículo) y devuelve las filas afectadas.
     */
    private void execute(String function, int componentId, int vehicleId, String errorMessage) {
        try (Connection connection = DatabaseConnection.getConnection();
                CallableStatement call = connection.prepareCall(function)) {
            call.registerOutParameter(1, Types.NUMERIC); // filas afectadas
            call.setInt(2, componentId);
            call.setInt(3, vehicleId);
            call.execute();
        } catch (SQLException e) {
            throw new PersistenceException(errorMessage, e);
        }
    }
}
