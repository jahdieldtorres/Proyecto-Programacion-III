package autoparts.dao.oracle;

import autoparts.dao.FeeDAO;
import autoparts.exception.PersistenceException;
import autoparts.model.Fee;
import autoparts.model.Status;
import autoparts.util.DatabaseConnection;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Guarda y consulta las tarifas de los servicios de los talleres en la tabla
 * FEES de la base de datos Oracle.
 *
 * Implementa la misma interfaz {@link FeeDAO} que FeeFileDAO, por eso
 * FeeService no cambia: solo se cambia el DAO en AppServices. Sigue el mismo
 * patrón de UserOracleDAO.
 *
 * @author Héctor Guevara
 */
public class FeeOracleDAO implements FeeDAO {

    private static final String SELECT_FEES =
            "SELECT ID, SERVICE_ID, AMOUNT, START_DATE, STATUS FROM FEES";

    private static final String FIND_ALL = SELECT_FEES + " ORDER BY ID";

    private static final String FIND_BY_ID = SELECT_FEES + " WHERE ID = ?";

    private static final String FIND_BY_SERVICE = SELECT_FEES + " WHERE SERVICE_ID = ? ORDER BY ID";

    private static final String INSERT =
            "INSERT INTO FEES (SERVICE_ID, AMOUNT, START_DATE, STATUS) VALUES (?, ?, ?, ?)";

    private static final String UPDATE =
            "UPDATE FEES SET SERVICE_ID = ?, AMOUNT = ?, START_DATE = ?, STATUS = ? WHERE ID = ?";

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
     * Guarda una tarifa nueva. Después de guardar, la tarifa recibida queda con
     * el ID que le asignó Oracle.
     */
    @Override
    public void save(Fee fee) {
        try (Connection connection = DatabaseConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(INSERT, new String[]{"ID"})) {
            statement.setInt(1, fee.getServiceId());
            statement.setInt(2, fee.getAmount());
            statement.setDate(3, Date.valueOf(fee.getStartDate()));
            statement.setString(4, fee.getStatus().name());
            statement.executeUpdate();

            try (ResultSet keys = statement.getGeneratedKeys()) {
                keys.next();
                fee.setId(keys.getInt(1));
            }
        } catch (SQLException e) {
            throw new PersistenceException("No se pudo guardar la tarifa.", e);
        }
    }

    /**
     * @return true si la tarifa existía y se actualizó
     */
    @Override
    public boolean update(Fee fee) {
        try (Connection connection = DatabaseConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(UPDATE)) {
            statement.setInt(1, fee.getServiceId());
            statement.setInt(2, fee.getAmount());
            statement.setDate(3, Date.valueOf(fee.getStartDate()));
            statement.setString(4, fee.getStatus().name());
            statement.setInt(5, fee.getId());
            return statement.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new PersistenceException("No se pudo actualizar la tarifa.", e);
        }
    }

    @Override
    public Optional<Fee> findById(int id) {
        List<Fee> fees = findList(FIND_BY_ID, id);
        return fees.isEmpty() ? Optional.empty() : Optional.of(fees.get(0));
    }

    @Override
    public List<Fee> findByService(int serviceId) {
        return findList(FIND_BY_SERVICE, serviceId);
    }

    @Override
    public List<Fee> findAll() {
        return findList(FIND_ALL);
    }

    /**
     * Ejecuta una consulta de tarifas y devuelve todas las filas como objetos.
     *
     * @param sql    consulta con tantos ? como valores en params
     * @param params valores de los parámetros, en orden (puede no haber ninguno)
     */
    private List<Fee> findList(String sql, Object... params) {
        List<Fee> fees = new ArrayList<>();
        try (Connection connection = DatabaseConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            for (int i = 0; i < params.length; i++) {
                statement.setObject(i + 1, params[i]);
            }
            try (ResultSet result = statement.executeQuery()) {
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