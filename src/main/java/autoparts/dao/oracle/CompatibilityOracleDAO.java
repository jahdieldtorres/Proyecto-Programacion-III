package autoparts.dao.oracle;

import autoparts.dao.CompatibilityDAO;
import autoparts.exception.PersistenceException;
import autoparts.util.DatabaseConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Guarda y consulta qué componentes son compatibles con qué vehículos, en la
 * tabla COMPATIBILITY de Oracle (llave primaria: componente + vehículo).
 *
 * Implementa la misma interfaz {@link CompatibilityDAO} que
 * CompatibilityFileDAO. Sigue el patrón de {@link UserOracleDAO}.
 *
 * @author Alvaro Aramendiz
 */
public class CompatibilityOracleDAO implements CompatibilityDAO {

    private static final String VEHICLES_OF_COMPONENT =
            "SELECT VEHICLE_ID FROM COMPATIBILITY WHERE COMPONENT_ID = ? ORDER BY VEHICLE_ID";

    private static final String COMPONENTS_OF_VEHICLE =
            "SELECT COMPONENT_ID FROM COMPATIBILITY WHERE VEHICLE_ID = ? ORDER BY COMPONENT_ID";

    private static final String LINK =
            "INSERT INTO COMPATIBILITY (COMPONENT_ID, VEHICLE_ID) VALUES (?, ?)";

    private static final String UNLINK =
            "DELETE FROM COMPATIBILITY WHERE COMPONENT_ID = ? AND VEHICLE_ID = ?";

    @Override
    public List<Integer> listVehicleIds(int componentId) {
        return listIds(VEHICLES_OF_COMPONENT, componentId,
                "No se pudieron leer los vehículos compatibles.");
    }

    @Override
    public List<Integer> listComponentIds(int vehicleId) {
        return listIds(COMPONENTS_OF_VEHICLE, vehicleId,
                "No se pudieron leer los componentes compatibles.");
    }

    /**
     * Enlaza un componente con un vehículo. Si ya estaban enlazados, Oracle
     * rechaza la fila repetida (llave primaria).
     */
    @Override
    public void link(int componentId, int vehicleId) {
        execute(LINK, componentId, vehicleId, "No se pudo registrar la compatibilidad.");
    }

    @Override
    public void unlink(int componentId, int vehicleId) {
        execute(UNLINK, componentId, vehicleId, "No se pudo quitar la compatibilidad.");
    }

    /**
     * Ejecuta una consulta con un solo parámetro que devuelve una columna de números.
     */
    private List<Integer> listIds(String sql, int value, String errorMessage) {
        List<Integer> ids = new ArrayList<>();
        try (Connection connection = DatabaseConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, value);
            try (ResultSet result = statement.executeQuery()) {
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
     * Ejecuta un INSERT o DELETE que recibe (componente, vehículo).
     */
    private void execute(String sql, int componentId, int vehicleId, String errorMessage) {
        try (Connection connection = DatabaseConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, componentId);
            statement.setInt(2, vehicleId);
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new PersistenceException(errorMessage, e);
        }
    }
}