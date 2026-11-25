package autoparts.dao.oracle;

import autoparts.dao.AppointmentDAO;
import autoparts.exception.PersistenceException;
import autoparts.model.Appointment;
import autoparts.model.AppointmentStatus;
import autoparts.util.DatabaseConnection;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Guarda y consulta las citas con los talleres llamando a las funciones del
 * paquete PL/SQL PKG_APPOINTMENTS (database/06d_paquetes_citas.sql).
 *
 * Todo es una función y el resultado llega por el primer ? de la llamada: un
 * cursor en las consultas, el ID nuevo en el INSERT y las filas cambiadas en
 * el UPDATE. Sigue el patrón de {@link FeeOracleDAO}. Los errores de SQL se
 * convierten en {@link PersistenceException}.
 *
 * @author Héctor Guevara
 */
public class AppointmentOracleDAO implements AppointmentDAO {

    private static final String CALL_FIND_BY_ID = "{ ? = call PKG_APPOINTMENTS.FIND_BY_ID(?) }";
    private static final String CALL_LIST_BY_USER = "{ ? = call PKG_APPOINTMENTS.LIST_BY_USER(?) }";
    private static final String CALL_INSERT = "{ ? = call PKG_APPOINTMENTS.INSERT_APPOINTMENT(?, ?, ?, ?) }";
    private static final String CALL_UPDATE_STATUS = "{ ? = call PKG_APPOINTMENTS.UPDATE_STATUS(?, ?) }";

    /**
     * Guarda una cita nueva con PKG_APPOINTMENTS.INSERT_APPOINTMENT. El ID lo
     * genera Oracle (IDENTITY), la función lo devuelve y se le asigna al objeto
     * recibido.
     */
    @Override
    public void save(Appointment appointment) {
        try (Connection connection = DatabaseConnection.getConnection();
                CallableStatement call = connection.prepareCall(CALL_INSERT)) {
            call.registerOutParameter(1, Types.NUMERIC); // ID nuevo
            call.setInt(2, appointment.getUserId());
            call.setInt(3, appointment.getServiceId());
            call.setTimestamp(4, Timestamp.valueOf(appointment.getDateTime()));
            call.setString(5, appointment.getStatus().name());
            call.execute();
            appointment.setId(call.getInt(1));
        } catch (SQLException e) {
            throw new PersistenceException("No se pudo guardar la cita.", e);
        }
    }

    @Override
    public Optional<Appointment> findById(int id) {
        try (Connection connection = DatabaseConnection.getConnection();
                CallableStatement call = connection.prepareCall(CALL_FIND_BY_ID)) {
            call.registerOutParameter(1, Types.REF_CURSOR);
            call.setInt(2, id);
            call.execute();
            try (ResultSet result = (ResultSet) call.getObject(1)) {
                return result.next() ? Optional.of(mapRow(result)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw new PersistenceException("No se pudo buscar la cita.", e);
        }
    }

    @Override
    public List<Appointment> findByUser(int userId) {
        List<Appointment> appointments = new ArrayList<>();
        try (Connection connection = DatabaseConnection.getConnection();
                CallableStatement call = connection.prepareCall(CALL_LIST_BY_USER)) {
            call.registerOutParameter(1, Types.REF_CURSOR);
            call.setInt(2, userId);
            call.execute();
            try (ResultSet result = (ResultSet) call.getObject(1)) {
                while (result.next()) {
                    appointments.add(mapRow(result));
                }
            }
        } catch (SQLException e) {
            throw new PersistenceException("No se pudieron leer las citas.", e);
        }
        return appointments;
    }

    /**
     * Cambia el estado con PKG_APPOINTMENTS.UPDATE_STATUS. La función devuelve
     * cuántas filas cambió; si es 0, la cita no existe.
     */
    @Override
    public boolean updateStatus(int id, AppointmentStatus status) {
        try (Connection connection = DatabaseConnection.getConnection();
                CallableStatement call = connection.prepareCall(CALL_UPDATE_STATUS)) {
            call.registerOutParameter(1, Types.NUMERIC); // filas cambiadas
            call.setInt(2, id);
            call.setString(3, status.name());
            call.execute();
            return call.getInt(1) > 0;
        } catch (SQLException e) {
            throw new PersistenceException("No se pudo actualizar la cita.", e);
        }
    }

    /**
     * Convierte la fila actual del resultado en un objeto Appointment.
     */
    private Appointment mapRow(ResultSet result) throws SQLException {
        return new Appointment(
                result.getInt("ID"),
                result.getInt("USER_ID"),
                result.getInt("SERVICE_ID"),
                result.getTimestamp("APPOINTMENT_DATE").toLocalDateTime(),
                AppointmentStatus.valueOf(result.getString("STATUS")));
    }
}
