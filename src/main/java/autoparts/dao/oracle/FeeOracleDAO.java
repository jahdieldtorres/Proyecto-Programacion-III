package autoparts.dao.oracle;

import autoparts.dao.FeeDAO;
import autoparts.exception.PersistenceException;
import autoparts.model.Fee;
import autoparts.model.Status;
import autoparts.util.DatabaseConnection;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Guarda y consulta las tarifas de los servicios de los talleres llamando a
 * las funciones del paquete PL/SQL PKG_FEES
 * (database/05b_paquetes_negocio.sql).
 *
 * Todo es una función y el resultado llega por el primer ? de la llamada: un
 * cursor en las consultas, el ID nuevo en el INSERT y las filas cambiadas en
 * el UPDATE.
 *
 * Implementa la misma interfaz {@link FeeDAO} que FeeFileDAO, por eso
 * FeeService no cambia: solo se cambia el DAO en AppServices. Sigue el patrón
 * de {@link UserOracleDAO}.
 *
 * @author Héctor Guevara
 */
public class FeeOracleDAO implements FeeDAO {

    private static final String CALL_LIST_ALL = "{ ? = call PKG_FEES.LIST_ALL }";
    private static final String CALL_FIND_BY_ID = "{ ? = call PKG_FEES.FIND_BY_ID(?) }";
    private static final String CALL_LIST_BY_SERVICE = "{ ? = call PKG_FEES.LIST_BY_SERVICE(?) }";
    private static final String CALL_INSERT = "{ ? = call PKG_FEES.INSERT_FEE(?, ?, ?, ?) }";
    private static final String CALL_UPDATE = "{ ? = call PKG_FEES.UPDATE_FEE(?, ?, ?, ?, ?) }";

    /**
     * Con Oracle el ID lo genera la base (columna IDENTITY) al guardar, así que
     * no se puede calcular antes. Devuelve 0 como valor provisorio:
     * {@link #save(Fee)} reemplaza ese 0 por el ID real en el mismo objeto.
     *
     * @return siempre 0
     */
    @Override
    public int nextId() {
        return 0;
    }

    /**
     * Guarda una tarifa nueva con PKG_FEES.INSERT_FEE. El ID lo genera Oracle
     * (IDENTITY), la función lo devuelve y se le asigna al objeto recibido,
     * igual que hace FeeFileDAO.
     */
    @Override
    public void save(Fee fee) {
        try (Connection connection = DatabaseConnection.getConnection();
                CallableStatement call = connection.prepareCall(CALL_INSERT)) {
            call.registerOutParameter(1, Types.NUMERIC); // ID nuevo
            call.setInt(2, fee.getServiceId());
            call.setInt(3, fee.getAmount());
            call.setDate(4, Date.valueOf(fee.getStartDate()));
            call.setString(5, fee.getStatus().name());
            call.execute();
            fee.setId(call.getInt(1));
        } catch (SQLException e) {
            throw new PersistenceException("No se pudo guardar la tarifa.", e);
        }
    }

    /**
     * Actualiza con PKG_FEES.UPDATE_FEE. La función devuelve cuántas filas
     * cambió; si es 0, la tarifa no existe.
     *
     * @return true si la tarifa existía y se actualizó
     */
    @Override
    public boolean update(Fee fee) {
        try (Connection connection = DatabaseConnection.getConnection();
                CallableStatement call = connection.prepareCall(CALL_UPDATE)) {
            call.registerOutParameter(1, Types.NUMERIC); // filas cambiadas
            call.setInt(2, fee.getId());
            call.setInt(3, fee.getServiceId());
            call.setInt(4, fee.getAmount());
            call.setDate(5, Date.valueOf(fee.getStartDate()));
            call.setString(6, fee.getStatus().name());
            call.execute();
            return call.getInt(1) > 0;
        } catch (SQLException e) {
            throw new PersistenceException("No se pudo actualizar la tarifa.", e);
        }
    }

    @Override
    public Optional<Fee> findById(int id) {
        try (Connection connection = DatabaseConnection.getConnection();
                CallableStatement call = connection.prepareCall(CALL_FIND_BY_ID)) {
            call.registerOutParameter(1, Types.REF_CURSOR);
            call.setInt(2, id);
            call.execute();
            try (ResultSet result = (ResultSet) call.getObject(1)) {
                return result.next() ? Optional.of(mapRow(result)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw new PersistenceException("No se pudo buscar la tarifa.", e);
        }
    }

    @Override
    public List<Fee> findByService(int serviceId) {
        List<Fee> fees = new ArrayList<>();
        try (Connection connection = DatabaseConnection.getConnection();
                CallableStatement call = connection.prepareCall(CALL_LIST_BY_SERVICE)) {
            call.registerOutParameter(1, Types.REF_CURSOR);
            call.setInt(2, serviceId);
            call.execute();
            try (ResultSet result = (ResultSet) call.getObject(1)) {
                while (result.next()) {
                    fees.add(mapRow(result));
                }
            }
        } catch (SQLException e) {
            throw new PersistenceException("No se pudieron leer las tarifas.", e);
        }
        return fees;
    }

    @Override
    public List<Fee> findAll() {
        List<Fee> fees = new ArrayList<>();
        try (Connection connection = DatabaseConnection.getConnection();
                CallableStatement call = connection.prepareCall(CALL_LIST_ALL)) {
            call.registerOutParameter(1, Types.REF_CURSOR);
            call.execute();
            try (ResultSet result = (ResultSet) call.getObject(1)) {
                while (result.next()) {
                    fees.add(mapRow(result));
                }
            }
        } catch (SQLException e) {
            throw new PersistenceException("No se pudieron leer las tarifas.", e);
        }
        return fees;
    }

    /**
     * Convierte la fila actual del resultado en un objeto Fee.
     */
    private Fee mapRow(ResultSet result) throws SQLException {
        return new Fee(
                result.getInt("ID"),
                result.getInt("SERVICE_ID"),
                result.getInt("AMOUNT"),
                result.getDate("START_DATE").toLocalDate(),
                Status.valueOf(result.getString("STATUS")));
    }
}
