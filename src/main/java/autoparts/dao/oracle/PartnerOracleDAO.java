package autoparts.dao.oracle;

import autoparts.dao.PartnerDAO;
import autoparts.exception.PersistenceException;
import autoparts.model.Partner;
import autoparts.model.Status;
import autoparts.model.Supplier;
import autoparts.model.Workshop;
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
 * Guarda y consulta los socios (proveedores y talleres) llamando a las
 * funciones del paquete PL/SQL PKG_PARTNERS
 * (database/05b_paquetes_negocio.sql).
 *
 * Todo es una función y el resultado llega por el primer ? de la llamada: un
 * cursor en las consultas, el ID nuevo en INSERT_PARTNER y las filas afectadas
 * en las demás.
 *
 * Los socios usan "una tabla por subclase" (ver database/base-de-datos-info.md):
 * PARTNERS guarda lo común y una columna de tipo; SUPPLIERS y WORKSHOPS guardan
 * lo propio de cada tipo, con el mismo ID del socio. Por eso:
 * <ul>
 *   <li>Leer es una sola consulta del paquete, con un LEFT JOIN de PARTNERS
 *       con las dos subtablas.</li>
 *   <li>Cada función de escritura toca una sola tabla, así que guardar y
 *       actualizar siguen haciéndose aquí en una transacción (commit y
 *       rollback los maneja Java, el paquete no confirma): o se guardan las
 *       dos filas o no se guarda ninguna.</li>
 * </ul>
 *
 * Implementa la misma interfaz {@link PartnerDAO} que PartnerFileDAO, por eso
 * PartnerService no cambia: solo se cambia el DAO en AppServices. Sigue el
 * patrón de {@link UserOracleDAO}.
 *
 * @author Héctor Guevara
 */
public class PartnerOracleDAO implements PartnerDAO {

    // Consultas: el primer ? es el cursor que devuelve la función
    private static final String CALL_LIST_ALL = "{ ? = call PKG_PARTNERS.LIST_ALL }";
    private static final String CALL_FIND_BY_ID = "{ ? = call PKG_PARTNERS.FIND_BY_ID(?) }";
    private static final String CALL_FIND_BY_TAX_ID = "{ ? = call PKG_PARTNERS.FIND_BY_TAX_ID(?) }";

    // Socio: INSERT devuelve el ID nuevo; UPDATE, las filas cambiadas
    private static final String CALL_INSERT_PARTNER =
            "{ ? = call PKG_PARTNERS.INSERT_PARTNER(?, ?, ?, ?, ?, ?, ?, ?, ?) }";
    private static final String CALL_UPDATE_PARTNER =
            "{ ? = call PKG_PARTNERS.UPDATE_PARTNER(?, ?, ?, ?, ?, ?, ?, ?, ?) }";

    // Subtipos: devuelven las filas afectadas. El ID del socio va de último,
    // así INSERT y UPDATE de cada subtipo usan los mismos parámetros.
    private static final String CALL_INSERT_SUPPLIER = "{ ? = call PKG_PARTNERS.INSERT_SUPPLIER(?, ?) }";
    private static final String CALL_UPDATE_SUPPLIER = "{ ? = call PKG_PARTNERS.UPDATE_SUPPLIER(?, ?) }";
    private static final String CALL_INSERT_WORKSHOP = "{ ? = call PKG_PARTNERS.INSERT_WORKSHOP(?, ?, ?) }";
    private static final String CALL_UPDATE_WORKSHOP = "{ ? = call PKG_PARTNERS.UPDATE_WORKSHOP(?, ?, ?) }";

    /**
     * Con Oracle el ID lo genera la base (columna IDENTITY) al guardar, así que
     * no se puede calcular antes. Devuelve 0 como valor provisorio:
     * {@link #save(Partner)} reemplaza ese 0 por el ID real en el mismo objeto.
     *
     * @return siempre 0
     */
    @Override
    public int nextId() {
        return 0;
    }

    /**
     * Guarda un socio nuevo con una transacción: llama a INSERT_PARTNER, toma el
     * ID que devuelve la función y llama a la de SUPPLIERS o WORKSHOPS. Si algo
     * falla, se deshace todo. Al terminar, el socio recibido queda con el ID que
     * le asignó Oracle.
     */
    @Override
    public void save(Partner partner) {
        try (Connection connection = DatabaseConnection.getConnection()) {
            connection.setAutoCommit(false);
            try {
                int id = insertPartner(connection, partner);
                writeSubtype(connection, id, partner, CALL_INSERT_SUPPLIER, CALL_INSERT_WORKSHOP);
                connection.commit();
                partner.setId(id);
            } catch (SQLException | RuntimeException e) {
                rollback(connection);
                throw e;
            }
        } catch (SQLException e) {
            throw new PersistenceException("No se pudo guardar el socio.", e);
        }
    }

    /**
     * Actualiza PARTNERS y la tabla de su subtipo en una sola transacción.
     *
     * @return true si el socio existía y se actualizó
     */
    @Override
    public boolean update(Partner partner) {
        try (Connection connection = DatabaseConnection.getConnection()) {
            connection.setAutoCommit(false);
            try {
                boolean updated = updatePartner(connection, partner);
                if (updated) {
                    writeSubtype(connection, partner.getId(), partner,
                            CALL_UPDATE_SUPPLIER, CALL_UPDATE_WORKSHOP);
                }
                connection.commit();
                return updated;
            } catch (SQLException | RuntimeException e) {
                rollback(connection);
                throw e;
            }
        } catch (SQLException e) {
            throw new PersistenceException("No se pudo actualizar el socio.", e);
        }
    }

    @Override
    public Optional<Partner> findById(int id) {
        try (Connection connection = DatabaseConnection.getConnection();
                CallableStatement call = connection.prepareCall(CALL_FIND_BY_ID)) {
            call.registerOutParameter(1, Types.REF_CURSOR);
            call.setInt(2, id);
            call.execute();
            try (ResultSet result = (ResultSet) call.getObject(1)) {
                return result.next() ? Optional.of(mapRow(result)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw new PersistenceException("No se pudo buscar el socio.", e);
        }
    }

    @Override
    public Optional<Partner> findByTaxId(String taxId) {
        if (taxId == null) {
            return Optional.empty();
        }
        try (Connection connection = DatabaseConnection.getConnection();
                CallableStatement call = connection.prepareCall(CALL_FIND_BY_TAX_ID)) {
            call.registerOutParameter(1, Types.REF_CURSOR);
            call.setString(2, taxId.trim());
            call.execute();
            try (ResultSet result = (ResultSet) call.getObject(1)) {
                return result.next() ? Optional.of(mapRow(result)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw new PersistenceException("No se pudo buscar el socio.", e);
        }
    }

    @Override
    public List<Partner> findAll() {
        List<Partner> partners = new ArrayList<>();
        try (Connection connection = DatabaseConnection.getConnection();
                CallableStatement call = connection.prepareCall(CALL_LIST_ALL)) {
            call.registerOutParameter(1, Types.REF_CURSOR);
            call.execute();
            try (ResultSet result = (ResultSet) call.getObject(1)) {
                while (result.next()) {
                    partners.add(mapRow(result));
                }
            }
        } catch (SQLException e) {
            throw new PersistenceException("No se pudieron leer los socios.", e);
        }
        return partners;
    }

    /**
     * Llama a INSERT_PARTNER con la conexión de la transacción y devuelve el ID
     * que Oracle le asignó al socio.
     */
    private int insertPartner(Connection connection, Partner partner) throws SQLException {
        try (CallableStatement call = connection.prepareCall(CALL_INSERT_PARTNER)) {
            call.registerOutParameter(1, Types.NUMERIC); // ID nuevo
            call.setString(2, typeOf(partner));
            call.setString(3, partner.getCompanyName());
            call.setString(4, partner.getTaxId());
            call.setString(5, partner.getPhone());
            call.setString(6, partner.getStatus().name());
            call.setDate(7, Date.valueOf(partner.getRegistrationDate()));
            call.setString(8, partner.getAddress());
            call.setDouble(9, partner.getLatitude());
            call.setDouble(10, partner.getLongitude());
            call.execute();
            return call.getInt(1);
        }
    }

    /**
     * Llama a UPDATE_PARTNER. El tipo del socio no se cambia.
     *
     * @return true si cambió una fila (el socio existe)
     */
    private boolean updatePartner(Connection connection, Partner partner) throws SQLException {
        try (CallableStatement call = connection.prepareCall(CALL_UPDATE_PARTNER)) {
            call.registerOutParameter(1, Types.NUMERIC); // filas cambiadas
            call.setString(2, partner.getCompanyName());
            call.setString(3, partner.getTaxId());
            call.setString(4, partner.getPhone());
            call.setString(5, partner.getStatus().name());
            call.setDate(6, Date.valueOf(partner.getRegistrationDate()));
            call.setString(7, partner.getAddress());
            call.setDouble(8, partner.getLatitude());
            call.setDouble(9, partner.getLongitude());
            call.setInt(10, partner.getId());
            call.execute();
            return call.getInt(1) > 0;
        }
    }

    /**
     * Llama a la función INSERT o UPDATE de la tabla del subtipo (SUPPLIERS o
     * WORKSHOPS) con el ID del socio de último parámetro.
     *
     * @return filas afectadas
     */
    private int writeSubtype(Connection connection, int id, Partner partner,
            String supplierCall, String workshopCall) throws SQLException {
        if (partner instanceof Supplier supplier) {
            try (CallableStatement call = connection.prepareCall(supplierCall)) {
                call.registerOutParameter(1, Types.NUMERIC);
                call.setInt(2, supplier.getDeliveryDays());
                call.setInt(3, id);
                call.execute();
                return call.getInt(1);
            }
        }
        if (partner instanceof Workshop workshop) {
            try (CallableStatement call = connection.prepareCall(workshopCall)) {
                call.registerOutParameter(1, Types.NUMERIC);
                call.setString(2, workshop.getSpecialty());
                call.setInt(3, workshop.getBayCount());
                call.setInt(4, id);
                call.execute();
                return call.getInt(1);
            }
        }
        return 0;
    }

    /**
     * Deshace la transacción. Es importante: Oracle confirma lo pendiente al
     * cerrar la conexión, así que sin rollback quedaría guardada a medias.
     */
    private void rollback(Connection connection) {
        try {
            connection.rollback();
        } catch (SQLException ignored) {
            // Si el rollback falla, la excepción original es la que importa.
        }
    }

    /**
     * Devuelve el texto del tipo que se guarda en PARTNERS.PARTNER_TYPE.
     */
    private String typeOf(Partner partner) {
        if (partner instanceof Supplier) {
            return Partner.TYPE_SUPPLIER;
        }
        if (partner instanceof Workshop) {
            return Partner.TYPE_WORKSHOP;
        }
        throw new PersistenceException("Tipo de socio no soportado.");
    }

    /**
     * Convierte la fila actual del resultado en un Supplier o un Workshop,
     * según la columna PARTNER_TYPE.
     */
    private Partner mapRow(ResultSet result) throws SQLException {
        String type = result.getString("PARTNER_TYPE");
        int id = result.getInt("ID");
        String companyName = result.getString("COMPANY_NAME");
        String taxId = result.getString("TAX_ID");
        String phone = result.getString("PHONE");
        Status status = Status.valueOf(result.getString("STATUS"));
        var registrationDate = result.getDate("REGISTRATION_DATE").toLocalDate();
        String address = result.getString("ADDRESS");
        double latitude = result.getDouble("LATITUDE");
        double longitude = result.getDouble("LONGITUDE");

        return switch (type) {
            case Partner.TYPE_SUPPLIER -> new Supplier(id, companyName, taxId, phone, status,
                    registrationDate, address, latitude, longitude,
                    result.getInt("DELIVERY_DAYS"));
            case Partner.TYPE_WORKSHOP -> new Workshop(id, companyName, taxId, phone, status,
                    registrationDate, address, latitude, longitude,
                    result.getString("SPECIALTY"), result.getInt("BAY_COUNT"));
            default -> throw new SQLException("Tipo de socio desconocido: " + type);
        };
    }
}
