package autoparts.dao.oracle;

import autoparts.dao.AffiliationDAO;
import autoparts.exception.PersistenceException;
import autoparts.model.Affiliation;
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
 * Guarda y consulta las afiliaciones entre proveedores y talleres llamando a
 * las funciones del paquete PL/SQL PKG_AFFILIATIONS
 * (database/05b_paquetes_negocio.sql).
 *
 * Todo es una función y el resultado llega por el primer ? de la llamada: un
 * cursor en las consultas, el ID nuevo en el INSERT y las filas cambiadas en
 * el UPDATE.
 *
 * Implementa la misma interfaz {@link AffiliationDAO} que AffiliationFileDAO,
 * por eso AffiliationService no cambia: solo se cambia el DAO en AppServices.
 * Sigue el patrón de {@link UserOracleDAO}.
 *
 * @author Héctor Guevara
 */
public class AffiliationOracleDAO implements AffiliationDAO {

    private static final String CALL_LIST_ALL = "{ ? = call PKG_AFFILIATIONS.LIST_ALL }";
    private static final String CALL_FIND_BY_ID = "{ ? = call PKG_AFFILIATIONS.FIND_BY_ID(?) }";
    private static final String CALL_LIST_BY_WORKSHOP = "{ ? = call PKG_AFFILIATIONS.LIST_BY_WORKSHOP(?) }";
    private static final String CALL_LIST_BY_SUPPLIER = "{ ? = call PKG_AFFILIATIONS.LIST_BY_SUPPLIER(?) }";
    private static final String CALL_INSERT = "{ ? = call PKG_AFFILIATIONS.INSERT_AFFILIATION(?, ?, ?, ?) }";
    private static final String CALL_UPDATE = "{ ? = call PKG_AFFILIATIONS.UPDATE_AFFILIATION(?, ?, ?, ?, ?) }";

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
     * Guarda una afiliación nueva con PKG_AFFILIATIONS.INSERT_AFFILIATION. El ID
     * lo genera Oracle (IDENTITY), la función lo devuelve y se le asigna al
     * objeto recibido, igual que hace AffiliationFileDAO.
     */
    @Override
    public void save(Affiliation affiliation) {
        try (Connection connection = DatabaseConnection.getConnection();
                CallableStatement call = connection.prepareCall(CALL_INSERT)) {
            call.registerOutParameter(1, Types.NUMERIC); // ID nuevo
            call.setInt(2, affiliation.getSupplierId());
            call.setInt(3, affiliation.getWorkshopId());
            call.setDate(4, Date.valueOf(affiliation.getStartDate()));
            call.setString(5, affiliation.getStatus().name());
            call.execute();
            affiliation.setId(call.getInt(1));
        } catch (SQLException e) {
            throw new PersistenceException("No se pudo guardar la afiliación.", e);
        }
    }

    /**
     * Actualiza con PKG_AFFILIATIONS.UPDATE_AFFILIATION. La función devuelve
     * cuántas filas cambió; si es 0, la afiliación no existe.
     *
     * @return true si la afiliación existía y se actualizó
     */
    @Override
    public boolean update(Affiliation affiliation) {
        try (Connection connection = DatabaseConnection.getConnection();
                CallableStatement call = connection.prepareCall(CALL_UPDATE)) {
            call.registerOutParameter(1, Types.NUMERIC); // filas cambiadas
            call.setInt(2, affiliation.getId());
            call.setInt(3, affiliation.getSupplierId());
            call.setInt(4, affiliation.getWorkshopId());
            call.setDate(5, Date.valueOf(affiliation.getStartDate()));
            call.setString(6, affiliation.getStatus().name());
            call.execute();
            return call.getInt(1) > 0;
        } catch (SQLException e) {
            throw new PersistenceException("No se pudo actualizar la afiliación.", e);
        }
    }

    @Override
    public Optional<Affiliation> findById(int id) {
        try (Connection connection = DatabaseConnection.getConnection();
                CallableStatement call = connection.prepareCall(CALL_FIND_BY_ID)) {
            call.registerOutParameter(1, Types.REF_CURSOR);
            call.setInt(2, id);
            call.execute();
            try (ResultSet result = (ResultSet) call.getObject(1)) {
                return result.next() ? Optional.of(mapRow(result)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw new PersistenceException("No se pudo buscar la afiliación.", e);
        }
    }

    @Override
    public List<Affiliation> findByWorkshop(int workshopId) {
        return listBy(CALL_LIST_BY_WORKSHOP, workshopId);
    }

    @Override
    public List<Affiliation> findBySupplier(int supplierId) {
        return listBy(CALL_LIST_BY_SUPPLIER, supplierId);
    }

    @Override
    public List<Affiliation> findAll() {
        List<Affiliation> affiliations = new ArrayList<>();
        try (Connection connection = DatabaseConnection.getConnection();
                CallableStatement call = connection.prepareCall(CALL_LIST_ALL)) {
            call.registerOutParameter(1, Types.REF_CURSOR);
            call.execute();
            try (ResultSet result = (ResultSet) call.getObject(1)) {
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
     * Llama una función de consulta con un solo parámetro numérico (el ID del
     * taller o del proveedor) que devuelve un cursor de afiliaciones.
     */
    private List<Affiliation> listBy(String function, int value) {
        List<Affiliation> affiliations = new ArrayList<>();
        try (Connection connection = DatabaseConnection.getConnection();
                CallableStatement call = connection.prepareCall(function)) {
            call.registerOutParameter(1, Types.REF_CURSOR);
            call.setInt(2, value);
            call.execute();
            try (ResultSet result = (ResultSet) call.getObject(1)) {
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
