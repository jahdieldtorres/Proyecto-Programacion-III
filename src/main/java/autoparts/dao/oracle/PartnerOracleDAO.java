package autoparts.dao.oracle;

import autoparts.dao.PartnerDAO;
import autoparts.exception.PersistenceException;
import autoparts.model.Partner;
import autoparts.model.Status;
import autoparts.model.Supplier;
import autoparts.model.Workshop;
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
 * Guarda y consulta los socios (proveedores y talleres) en la base de datos Oracle.
 *
 * Implementa la misma interfaz {@link PartnerDAO} que PartnerFileDAO, por eso
 * PartnerService no cambia: solo se cambia el DAO en AppServices.
 *
 * Los socios usan "una tabla por subclase" (ver database/base-de-datos-info.md):
 * PARTNERS guarda lo común y una columna de tipo; SUPPLIERS y WORKSHOPS guardan
 * lo propio de cada tipo, con el mismo ID del socio. Por eso:
 * <ul>
 *   <li>Leer es un LEFT JOIN de PARTNERS con las dos subtablas.</li>
 *   <li>Guardar y actualizar son dos sentencias dentro de una transacción:
 *       o se guardan las dos o no se guarda ninguna (rollback).</li>
 * </ul>
 *
 * @author Héctor Guevara
 */
public class PartnerOracleDAO implements PartnerDAO {

    private static final String SELECT_PARTNERS =
            "SELECT p.ID, p.PARTNER_TYPE, p.COMPANY_NAME, p.TAX_ID, p.PHONE, p.STATUS, "
            + "p.REGISTRATION_DATE, p.ADDRESS, p.LATITUDE, p.LONGITUDE, "
            + "s.DELIVERY_DAYS, w.SPECIALTY, w.BAY_COUNT "
            + "FROM PARTNERS p "
            + "LEFT JOIN SUPPLIERS s ON s.PARTNER_ID = p.ID "
            + "LEFT JOIN WORKSHOPS w ON w.PARTNER_ID = p.ID";

    private static final String FIND_ALL = SELECT_PARTNERS + " ORDER BY p.ID";

    private static final String FIND_BY_ID = SELECT_PARTNERS + " WHERE p.ID = ?";

    private static final String FIND_BY_TAX_ID = SELECT_PARTNERS + " WHERE UPPER(p.TAX_ID) = UPPER(?)";

    private static final String INSERT_PARTNER =
            "INSERT INTO PARTNERS (PARTNER_TYPE, COMPANY_NAME, TAX_ID, PHONE, STATUS, "
            + "REGISTRATION_DATE, ADDRESS, LATITUDE, LONGITUDE) "
            + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

    private static final String INSERT_SUPPLIER =
            "INSERT INTO SUPPLIERS (PARTNER_ID, DELIVERY_DAYS) VALUES (?, ?)";

    private static final String INSERT_WORKSHOP =
            "INSERT INTO WORKSHOPS (PARTNER_ID, SPECIALTY, BAY_COUNT) VALUES (?, ?, ?)";

    private static final String UPDATE_PARTNER =
            "UPDATE PARTNERS SET COMPANY_NAME = ?, TAX_ID = ?, PHONE = ?, STATUS = ?, "
            + "REGISTRATION_DATE = ?, ADDRESS = ?, LATITUDE = ?, LONGITUDE = ? WHERE ID = ?";

    private static final String UPDATE_SUPPLIER =
            "UPDATE SUPPLIERS SET DELIVERY_DAYS = ? WHERE PARTNER_ID = ?";

    private static final String UPDATE_WORKSHOP =
            "UPDATE WORKSHOPS SET SPECIALTY = ?, BAY_COUNT = ? WHERE PARTNER_ID = ?";

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
     * Guarda un socio nuevo en PARTNERS y en su subtabla, en una transacción.
     * Después de guardar, el socio recibido queda con el ID que le asignó Oracle.
     */
    @Override
    public void save(Partner partner) {
        try (Connection connection = DatabaseConnection.getConnection()) {
            connection.setAutoCommit(false);
            try {
                int id = insertPartner(connection, partner);
                insertSubtype(connection, id, partner);
                connection.commit();
                partner.setId(id);
            } catch (SQLException | RuntimeException e) {
                connection.rollback();
                throw e;
            }
        } catch (SQLException e) {
            throw new PersistenceException("No se pudo guardar el socio.", e);
        }
    }

    /**
     * Actualiza el socio en PARTNERS y en su subtabla, en una transacción.
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
                    updateSubtype(connection, partner);
                }
                connection.commit();
                return updated;
            } catch (SQLException | RuntimeException e) {
                connection.rollback();
                throw e;
            }
        } catch (SQLException e) {
            throw new PersistenceException("No se pudo actualizar el socio.", e);
        }
    }

    @Override
    public Optional<Partner> findById(int id) {
        return findOne(FIND_BY_ID, id);
    }

    @Override
    public Optional<Partner> findByTaxId(String taxId) {
        if (taxId == null) {
            return Optional.empty();
        }
        return findOne(FIND_BY_TAX_ID, taxId.trim());
    }

    @Override
    public List<Partner> findAll() {
        List<Partner> partners = new ArrayList<>();
        try (Connection connection = DatabaseConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(FIND_ALL);
                ResultSet result = statement.executeQuery()) {
            while (result.next()) {
                partners.add(mapRow(result));
            }
        } catch (SQLException e) {
            throw new PersistenceException("No se pudieron leer los socios.", e);
        }
        return partners;
    }

    /**
     * Ejecuta una consulta que devuelve como máximo un socio.
     *
     * @param sql   consulta con un solo parámetro (?)
     * @param value valor del parámetro (el ID o el NIT)
     * @return el socio encontrado, o vacío si no existe
     */
    private Optional<Partner> findOne(String sql, Object value) {
        try (Connection connection = DatabaseConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setObject(1, value);
            try (ResultSet result = statement.executeQuery()) {
                return result.next() ? Optional.of(mapRow(result)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw new PersistenceException("No se pudo buscar el socio.", e);
        }
    }

    /**
     * Inserta la fila común en PARTNERS con la conexión de la transacción.
     *
     * @return el ID que Oracle le asignó al socio
     */
    private int insertPartner(Connection connection, Partner partner) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(INSERT_PARTNER, new String[]{"ID"})) {
            statement.setString(1, typeOf(partner));
            statement.setString(2, partner.getCompanyName());
            statement.setString(3, partner.getTaxId());
            statement.setString(4, partner.getPhone());
            statement.setString(5, partner.getStatus().name());
            statement.setDate(6, Date.valueOf(partner.getRegistrationDate()));
            statement.setString(7, partner.getAddress());
            statement.setDouble(8, partner.getLatitude());
            statement.setDouble(9, partner.getLongitude());
            statement.executeUpdate();

            try (ResultSet keys = statement.getGeneratedKeys()) {
                keys.next();
                return keys.getInt(1);
            }
        }
    }

    /**
     * Inserta la fila propia del tipo (SUPPLIERS o WORKSHOPS) con el ID del socio.
     */
    private void insertSubtype(Connection connection, int id, Partner partner) throws SQLException {
        if (partner instanceof Supplier supplier) {
            try (PreparedStatement statement = connection.prepareStatement(INSERT_SUPPLIER)) {
                statement.setInt(1, id);
                statement.setInt(2, supplier.getDeliveryDays());
                statement.executeUpdate();
            }
        } else if (partner instanceof Workshop workshop) {
            try (PreparedStatement statement = connection.prepareStatement(INSERT_WORKSHOP)) {
                statement.setInt(1, id);
                statement.setString(2, workshop.getSpecialty());
                statement.setInt(3, workshop.getBayCount());
                statement.executeUpdate();
            }
        }
    }

    /**
     * Actualiza la fila común en PARTNERS.
     *
     * @return true si existía una fila con ese ID
     */
    private boolean updatePartner(Connection connection, Partner partner) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(UPDATE_PARTNER)) {
            statement.setString(1, partner.getCompanyName());
            statement.setString(2, partner.getTaxId());
            statement.setString(3, partner.getPhone());
            statement.setString(4, partner.getStatus().name());
            statement.setDate(5, Date.valueOf(partner.getRegistrationDate()));
            statement.setString(6, partner.getAddress());
            statement.setDouble(7, partner.getLatitude());
            statement.setDouble(8, partner.getLongitude());
            statement.setInt(9, partner.getId());
            return statement.executeUpdate() > 0;
        }
    }

    /**
     * Actualiza la fila propia del tipo (SUPPLIERS o WORKSHOPS).
     */
    private void updateSubtype(Connection connection, Partner partner) throws SQLException {
        if (partner instanceof Supplier supplier) {
            try (PreparedStatement statement = connection.prepareStatement(UPDATE_SUPPLIER)) {
                statement.setInt(1, supplier.getDeliveryDays());
                statement.setInt(2, supplier.getId());
                statement.executeUpdate();
            }
        } else if (partner instanceof Workshop workshop) {
            try (PreparedStatement statement = connection.prepareStatement(UPDATE_WORKSHOP)) {
                statement.setString(1, workshop.getSpecialty());
                statement.setInt(2, workshop.getBayCount());
                statement.setInt(3, workshop.getId());
                statement.executeUpdate();
            }
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