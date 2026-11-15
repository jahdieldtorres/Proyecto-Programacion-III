package autoparts.dao.oracle;

import autoparts.dao.AffiliationDAO;
import autoparts.exception.PersistenceException;
import autoparts.model.Affiliation;
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
 * Guarda y consulta las afiliaciones entre proveedores y talleres en la tabla
 * AFFILIATIONS de la base de datos Oracle.
 *
 * Implementa la misma interfaz {@link AffiliationDAO} que AffiliationFileDAO,
 * por eso AffiliationService no cambia: solo se cambia el DAO en AppServices.
 * Sigue el mismo patrón de UserOracleDAO.
 *
 * @author Héctor Guevara
 */
public class AffiliationOracleDAO implements AffiliationDAO {

    private static final String SELECT_AFFILIATIONS =
            "SELECT ID, SUPPLIER_ID, WORKSHOP_ID, START_DATE, STATUS FROM AFFILIATIONS";

    private static final String FIND_ALL = SELECT_AFFILIATIONS + " ORDER BY ID";

    private static final String FIND_BY_ID = SELECT_AFFILIATIONS + " WHERE ID = ?";

    private static final String FIND_BY_WORKSHOP = SELECT_AFFILIATIONS + " WHERE WORKSHOP_ID = ? ORDER BY ID";

    private static final String FIND_BY_SUPPLIER = SELECT_AFFILIATIONS + " WHERE SUPPLIER_ID = ? ORDER BY ID";

    private static final String INSERT =
            "INSERT INTO AFFILIATIONS (SUPPLIER_ID, WORKSHOP_ID, START_DATE, STATUS) VALUES (?, ?, ?, ?)";

    private static final String UPDATE =
            "UPDATE AFFILIATIONS SET SUPPLIER_ID = ?, WORKSHOP_ID = ?, START_DATE = ?, STATUS = ? WHERE ID = ?";

    /**
     * Con Oracle el ID lo genera la base (columna IDENTITY) al guardar, así que
     * no se puede calcular antes. Devuelve 0 como valor provisorio:
     * {@link #save(Affiliation)} reemplaza ese 0 por el ID real en el mismo objeto.
     *
     * @return siempre 0
     */
    @Override
    public int nextId() {
        return 0;
    }

    /**
     * Guarda una afiliación nueva. Después de guardar, la afiliación recibida
     * queda con el ID que le asignó Oracle.
     */
    @Override
    public void save(Affiliation affiliation) {
        try (Connection connection = DatabaseConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(INSERT, new String[]{"ID"})) {
            statement.setInt(1, affiliation.getSupplierId());
            statement.setInt(2, affiliation.getWorkshopId());
            statement.setDate(3, Date.valueOf(affiliation.getStartDate()));
            statement.setString(4, affiliation.getStatus().name());
            statement.executeUpdate();

            try (ResultSet keys = statement.getGeneratedKeys()) {
                keys.next();
                affiliation.setId(keys.getInt(1));
            }
        } catch (SQLException e) {
            throw new PersistenceException("No se pudo guardar la afiliación.", e);
        }
    }

    /**
     * @return true si la afiliación existía y se actualizó
     */
    @Override
    public boolean update(Affiliation affiliation) {
        try (Connection connection = DatabaseConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(UPDATE)) {
            statement.setInt(1, affiliation.getSupplierId());
            statement.setInt(2, affiliation.getWorkshopId());
            statement.setDate(3, Date.valueOf(affiliation.getStartDate()));
            statement.setString(4, affiliation.getStatus().name());
            statement.setInt(5, affiliation.getId());
            return statement.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new PersistenceException("No se pudo actualizar la afiliación.", e);
        }
    }

    @Override
    public Optional<Affiliation> findById(int id) {
        List<Affiliation> affiliations = findList(FIND_BY_ID, id);
        return affiliations.isEmpty() ? Optional.empty() : Optional.of(affiliations.get(0));
    }

    @Override
    public List<Affiliation> findByWorkshop(int workshopId) {
        return findList(FIND_BY_WORKSHOP, workshopId);
    }

    @Override
    public List<Affiliation> findBySupplier(int supplierId) {
        return findList(FIND_BY_SUPPLIER, supplierId);
    }

    @Override
    public List<Affiliation> findAll() {
        return findList(FIND_ALL);
    }

    /**
     * Ejecuta una consulta de afiliaciones y devuelve todas las filas como objetos.
     *
     * @param sql    consulta con tantos ? como valores en params
     * @param params valores de los parámetros, en orden (puede no haber ninguno)
     */
    private List<Affiliation> findList(String sql, Object... params) {
        List<Affiliation> affiliations = new ArrayList<>();
        try (Connection connection = DatabaseConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            for (int i = 0; i < params.length; i++) {
                statement.setObject(i + 1, params[i]);
            }
            try (ResultSet result = statement.executeQuery()) {
                while (result.next()) {
                    affiliations.add(mapRow(result));
                }
            }
        } catch (SQLException e) {
            throw new PersistenceException("No se pudieron leer las afiliaciones.", e);
        }
        return affiliations;
    }

    /**
     * Convierte la fila actual del resultado en un objeto Affiliation.
     */
    private Affiliation mapRow(ResultSet result) throws SQLException {
        return new Affiliation(
                result.getInt("ID"),
                result.getInt("SUPPLIER_ID"),
                result.getInt("WORKSHOP_ID"),
                result.getDate("START_DATE").toLocalDate(),
                Status.valueOf(result.getString("STATUS")));
    }
}