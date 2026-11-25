package autoparts.dao.oracle;

import autoparts.dao.WorkshopServiceDAO;
import autoparts.exception.PersistenceException;
import autoparts.model.Status;
import autoparts.model.WorkshopService;
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
 * Consulta los servicios de los talleres llamando a las funciones del paquete
 * PL/SQL PKG_SERVICES (database/06c_paquetes_servicios.sql).
 *
 * Todo es una función y el cursor llega por el primer ? de la llamada. Sigue el
 * patrón de {@link FeeOracleDAO}. Los errores de SQL se convierten en
 * {@link PersistenceException}.
 *
 * @author Héctor Guevara
 */
public class WorkshopServiceOracleDAO implements WorkshopServiceDAO {

    private static final String CALL_LIST_ALL = "{ ? = call PKG_SERVICES.LIST_ALL }";
    private static final String CALL_FIND_BY_ID = "{ ? = call PKG_SERVICES.FIND_BY_ID(?) }";
    private static final String CALL_LIST_BY_WORKSHOP = "{ ? = call PKG_SERVICES.LIST_BY_WORKSHOP(?) }";

    @Override
    public Optional<WorkshopService> findById(int id) {
        try (Connection connection = DatabaseConnection.getConnection();
                CallableStatement call = connection.prepareCall(CALL_FIND_BY_ID)) {
            call.registerOutParameter(1, Types.REF_CURSOR);
            call.setInt(2, id);
            call.execute();
            try (ResultSet result = (ResultSet) call.getObject(1)) {
                return result.next() ? Optional.of(mapRow(result)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw new PersistenceException("No se pudo buscar el servicio.", e);
        }
    }

    @Override
    public List<WorkshopService> findByWorkshop(int workshopId) {
        List<WorkshopService> services = new ArrayList<>();
        try (Connection connection = DatabaseConnection.getConnection();
                CallableStatement call = connection.prepareCall(CALL_LIST_BY_WORKSHOP)) {
            call.registerOutParameter(1, Types.REF_CURSOR);
            call.setInt(2, workshopId);
            call.execute();
            try (ResultSet result = (ResultSet) call.getObject(1)) {
                while (result.next()) {
                    services.add(mapRow(result));
                }
            }
        } catch (SQLException e) {
            throw new PersistenceException("No se pudieron leer los servicios del taller.", e);
        }
        return services;
    }

    @Override
    public List<WorkshopService> findAll() {
        List<WorkshopService> services = new ArrayList<>();
        try (Connection connection = DatabaseConnection.getConnection();
                CallableStatement call = connection.prepareCall(CALL_LIST_ALL)) {
            call.registerOutParameter(1, Types.REF_CURSOR);
            call.execute();
            try (ResultSet result = (ResultSet) call.getObject(1)) {
                while (result.next()) {
                    services.add(mapRow(result));
                }
            }
        } catch (SQLException e) {
            throw new PersistenceException("No se pudieron leer los servicios.", e);
        }
        return services;
    }

    /**
     * Convierte la fila actual del resultado en un objeto WorkshopService.
     */
    private WorkshopService mapRow(ResultSet result) throws SQLException {
        return new WorkshopService(
                result.getInt("ID"),
                result.getInt("WORKSHOP_ID"),
                result.getString("NAME"),
                result.getString("DESCRIPTION"),
                Status.valueOf(result.getString("STATUS")));
    }
}
