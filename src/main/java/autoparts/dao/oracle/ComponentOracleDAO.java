package autoparts.dao.oracle;

import autoparts.dao.ComponentDAO;
import autoparts.exception.PersistenceException;
import autoparts.model.Brand;
import autoparts.model.Category;
import autoparts.model.Component;
import autoparts.model.ComponentType;
import autoparts.model.Engine;
import autoparts.model.FuelType;
import autoparts.model.Rim;
import autoparts.model.RimMaterial;
import autoparts.model.Status;
import autoparts.model.Tire;
import autoparts.model.Transmission;
import autoparts.model.TransmissionType;
import autoparts.util.DatabaseConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Guarda y consulta los componentes (repuestos) en Oracle.
 *
 * Cada componente ocupa dos tablas: COMPONENTS (datos comunes) y, si es una
 * llanta, un rin, un motor o una transmisión, también TIRES, RIMS, ENGINES o
 * TRANSMISSIONS (datos propios) con el mismo ID. Por eso guardar y actualizar
 * se hacen en una transacción: o se guardan las dos filas o no se guarda
 * ninguna. Un componente GENERAL solo tiene fila en COMPONENTS.
 *
 * Implementa la misma interfaz {@link ComponentDAO} que ComponentFileDAO, por
 * eso ComponentService no cambia. Sigue el patrón de {@link UserOracleDAO}.
 *
 * @author Alvaro Aramendiz
 */
public class ComponentOracleDAO implements ComponentDAO {

    /**
     * Lee el componente con su marca, su categoría y los datos de su subtipo.
     * Los LEFT JOIN dejan en NULL las columnas de los subtipos que no aplican.
     */
    private static final String SELECT_COMPONENTS =
            "SELECT C.ID, C.COMPONENT_TYPE, C.NAME, C.REFERENCE, C.ORIGIN, C.STATUS, "
            + "B.ID AS BRAND_ID, B.NAME AS BRAND_NAME, "
            + "G.ID AS CATEGORY_ID, G.NAME AS CATEGORY_NAME, "
            + "T.WIDTH_MM, T.ASPECT_RATIO, T.DIAMETER_INCHES AS TIRE_DIAMETER, T.LOAD_INDEX, T.SPEED_INDEX, "
            + "R.DIAMETER_INCHES AS RIM_DIAMETER, R.WIDTH_INCHES AS RIM_WIDTH, R.PCD, R.MATERIAL, "
            + "E.DISPLACEMENT_CC, E.CYLINDERS, E.FUEL, E.HORSEPOWER, "
            + "X.TRANSMISSION_TYPE, X.NUMBER_OF_SPEEDS "
            + "FROM COMPONENTS C "
            + "JOIN BRANDS B ON B.ID = C.BRAND_ID "
            + "JOIN CATEGORIES G ON G.ID = C.CATEGORY_ID "
            + "LEFT JOIN TIRES T ON T.COMPONENT_ID = C.ID "
            + "LEFT JOIN RIMS R ON R.COMPONENT_ID = C.ID "
            + "LEFT JOIN ENGINES E ON E.COMPONENT_ID = C.ID "
            + "LEFT JOIN TRANSMISSIONS X ON X.COMPONENT_ID = C.ID";

    private static final String FIND_ALL = SELECT_COMPONENTS + " ORDER BY C.ID";

    private static final String FIND_BY_ID = SELECT_COMPONENTS + " WHERE C.ID = ?";

    private static final String INSERT_COMPONENT =
            "INSERT INTO COMPONENTS (COMPONENT_TYPE, NAME, REFERENCE, BRAND_ID, CATEGORY_ID, ORIGIN, STATUS) "
            + "VALUES (?, ?, ?, ?, ?, ?, ?)";

    private static final String UPDATE_COMPONENT =
            "UPDATE COMPONENTS SET COMPONENT_TYPE = ?, NAME = ?, REFERENCE = ?, BRAND_ID = ?, "
            + "CATEGORY_ID = ?, ORIGIN = ?, STATUS = ? WHERE ID = ?";

    /** Los componentes no se borran (un disparador lo impide): se pasan a INACTIVO. */
    private static final String DEACTIVATE = "UPDATE COMPONENTS SET STATUS = 'INACTIVO' WHERE ID = ?";

    // En los INSERT y UPDATE de los subtipos el ID va de último, así los dos usan los mismos parámetros.
    private static final String INSERT_TIRE =
            "INSERT INTO TIRES (WIDTH_MM, ASPECT_RATIO, DIAMETER_INCHES, LOAD_INDEX, SPEED_INDEX, COMPONENT_ID) "
            + "VALUES (?, ?, ?, ?, ?, ?)";

    private static final String UPDATE_TIRE =
            "UPDATE TIRES SET WIDTH_MM = ?, ASPECT_RATIO = ?, DIAMETER_INCHES = ?, LOAD_INDEX = ?, "
            + "SPEED_INDEX = ? WHERE COMPONENT_ID = ?";

    private static final String INSERT_RIM =
            "INSERT INTO RIMS (DIAMETER_INCHES, WIDTH_INCHES, PCD, MATERIAL, COMPONENT_ID) VALUES (?, ?, ?, ?, ?)";

    private static final String UPDATE_RIM =
            "UPDATE RIMS SET DIAMETER_INCHES = ?, WIDTH_INCHES = ?, PCD = ?, MATERIAL = ? WHERE COMPONENT_ID = ?";

    private static final String INSERT_ENGINE =
            "INSERT INTO ENGINES (DISPLACEMENT_CC, CYLINDERS, FUEL, HORSEPOWER, COMPONENT_ID) VALUES (?, ?, ?, ?, ?)";

    private static final String UPDATE_ENGINE =
            "UPDATE ENGINES SET DISPLACEMENT_CC = ?, CYLINDERS = ?, FUEL = ?, HORSEPOWER = ? WHERE COMPONENT_ID = ?";

    private static final String INSERT_TRANSMISSION =
            "INSERT INTO TRANSMISSIONS (TRANSMISSION_TYPE, NUMBER_OF_SPEEDS, COMPONENT_ID) VALUES (?, ?, ?)";

    private static final String UPDATE_TRANSMISSION =
            "UPDATE TRANSMISSIONS SET TRANSMISSION_TYPE = ?, NUMBER_OF_SPEEDS = ? WHERE COMPONENT_ID = ?";

    @Override
    public List<Component> listAll() {
        List<Component> components = new ArrayList<>();
        try (Connection connection = DatabaseConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(FIND_ALL);
                ResultSet result = statement.executeQuery()) {
            while (result.next()) {
                components.add(mapRow(result));
            }
        } catch (SQLException e) {
            throw new PersistenceException("No se pudieron leer los componentes.", e);
        }
        return components;
    }

    @Override
    public Component findById(int id) {
        try (Connection connection = DatabaseConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(FIND_BY_ID)) {
            statement.setInt(1, id);
            try (ResultSet result = statement.executeQuery()) {
                return result.next() ? mapRow(result) : null;
            }
        } catch (SQLException e) {
            throw new PersistenceException("No se pudo buscar el componente.", e);
        }
    }

    /**
     * Guarda un componente nuevo con una transacción: inserta en COMPONENTS,
     * toma el ID que genera Oracle e inserta en la tabla de su subtipo. Si
     * algo falla, se deshace todo. Al terminar, el ID queda asignado en el
     * objeto, igual que hace ComponentFileDAO.
     */
    @Override
    public void save(Component component) {
        if (component.getStatus() == null) {
            component.setStatus(Status.ACTIVO);
        }
        try (Connection connection = DatabaseConnection.getConnection()) {
            connection.setAutoCommit(false);
            try {
                int id = insertComponent(connection, component);
                writeSubtype(connection, id, component, true);
                connection.commit();
                component.setId(id);
            } catch (SQLException | RuntimeException e) {
                rollback(connection);
                throw e;
            }
        } catch (SQLException e) {
            throw new PersistenceException("No se pudo guardar el componente.", e);
        }
    }

    /**
     * Actualiza COMPONENTS y la tabla de su subtipo en una sola transacción.
     */
    @Override
    public void update(Component component) {
        try (Connection connection = DatabaseConnection.getConnection()) {
            connection.setAutoCommit(false);
            try {
                updateComponent(connection, component);
                int rows = writeSubtype(connection, component.getId(), component, false);
                if (rows == 0) {
                    // El componente todavía no tenía fila de subtipo: se crea.
                    writeSubtype(connection, component.getId(), component, true);
                }
                connection.commit();
            } catch (SQLException | RuntimeException e) {
                rollback(connection);
                throw e;
            }
        } catch (SQLException e) {
            throw new PersistenceException("No se pudo actualizar el componente.", e);
        }
    }

    /**
     * Desactiva el componente (borrado lógico). Sus ofertas y su
     * compatibilidad se conservan.
     */
    @Override
    public void delete(int id) {
        try (Connection connection = DatabaseConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(DEACTIVATE)) {
            statement.setInt(1, id);
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new PersistenceException("No se pudo desactivar el componente.", e);
        }
    }

    /**
     * Inserta la fila de COMPONENTS y devuelve el ID que generó Oracle.
     */
    private int insertComponent(Connection connection, Component component) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(INSERT_COMPONENT, new String[]{"ID"})) {
            bindComponent(statement, component);
            statement.executeUpdate();
            try (ResultSet keys = statement.getGeneratedKeys()) {
                keys.next();
                return keys.getInt(1);
            }
        }
    }

    private void updateComponent(Connection connection, Component component) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(UPDATE_COMPONENT)) {
            bindComponent(statement, component);
            statement.setInt(8, component.getId());
            statement.executeUpdate();
        }
    }

    /**
     * Pone en la sentencia los 7 datos comunes (parámetros 1 a 7).
     */
    private void bindComponent(PreparedStatement statement, Component component) throws SQLException {
        statement.setString(1, component.getType().getFileValue());
        statement.setString(2, component.getName());
        statement.setString(3, component.getReference());
        statement.setInt(4, component.getBrand().getId());
        statement.setInt(5, component.getCategory().getId());
        statement.setString(6, component.getOrigin());
        statement.setString(7, component.getStatus().name());
    }

    /**
     * Ejecuta el INSERT o el UPDATE de la tabla del subtipo del componente.
     * Un componente GENERAL no tiene tabla de subtipo y no hace nada.
     *
     * @param insert true para insertar, false para actualizar
     * @return filas afectadas
     */
    private int writeSubtype(Connection connection, int id, Component component, boolean insert)
            throws SQLException {
        if (component instanceof Tire tire) {
            try (PreparedStatement statement = connection.prepareStatement(insert ? INSERT_TIRE : UPDATE_TIRE)) {
                statement.setInt(1, tire.getWidthMm());
                statement.setInt(2, tire.getProfile());
                statement.setInt(3, tire.getDiameterInches());
                statement.setInt(4, tire.getLoadIndex());
                statement.setString(5, tire.getSpeedIndex());
                statement.setInt(6, id);
                return statement.executeUpdate();
            }
        }
        if (component instanceof Rim rim) {
            try (PreparedStatement statement = connection.prepareStatement(insert ? INSERT_RIM : UPDATE_RIM)) {
                statement.setInt(1, rim.getDiameterInches());
                statement.setDouble(2, rim.getWidthInches());
                statement.setString(3, rim.getPcd());
                statement.setString(4, rim.getMaterial().getFileValue());
                statement.setInt(5, id);
                return statement.executeUpdate();
            }
        }
        if (component instanceof Engine engine) {
            try (PreparedStatement statement = connection.prepareStatement(insert ? INSERT_ENGINE : UPDATE_ENGINE)) {
                statement.setInt(1, engine.getDisplacementCc());
                statement.setInt(2, engine.getCylinders());
                statement.setString(3, engine.getFuel().getFileValue());
                statement.setInt(4, engine.getHorsepower());
                statement.setInt(5, id);
                return statement.executeUpdate();
            }
        }
        if (component instanceof Transmission transmission) {
            try (PreparedStatement statement = connection.prepareStatement(
                    insert ? INSERT_TRANSMISSION : UPDATE_TRANSMISSION)) {
                statement.setString(1, transmission.getTransmissionType().getFileValue());
                statement.setInt(2, transmission.getNumberOfSpeeds());
                statement.setInt(3, id);
                return statement.executeUpdate();
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
     * Convierte la fila actual en el componente que corresponde a su tipo
     * (Tire, Rim, Engine, Transmission o Component general).
     */
    private Component mapRow(ResultSet result) throws SQLException {
        ComponentType type = ComponentType.fromFileValue(result.getString("COMPONENT_TYPE"));
        int id = result.getInt("ID");
        String name = result.getString("NAME");
        String reference = result.getString("REFERENCE");
        Brand brand = new Brand(result.getInt("BRAND_ID"), result.getString("BRAND_NAME"));
        Category category = new Category(result.getInt("CATEGORY_ID"), result.getString("CATEGORY_NAME"));
        String origin = result.getString("ORIGIN");
        Status status = Status.valueOf(result.getString("STATUS"));

        return switch (type) {
            case TIRE -> new Tire(id, name, reference, brand, category, origin, status,
                    result.getInt("WIDTH_MM"), result.getInt("ASPECT_RATIO"),
                    result.getInt("TIRE_DIAMETER"), result.getInt("LOAD_INDEX"),
                    result.getString("SPEED_INDEX"));
            case RIM -> new Rim(id, name, reference, brand, category, origin, status,
                    result.getInt("RIM_DIAMETER"), result.getDouble("RIM_WIDTH"),
                    result.getString("PCD"), RimMaterial.fromFileValue(result.getString("MATERIAL")));
            case ENGINE -> new Engine(id, name, reference, brand, category, origin, status,
                    result.getInt("DISPLACEMENT_CC"), result.getInt("CYLINDERS"),
                    FuelType.fromFileValue(result.getString("FUEL")), result.getInt("HORSEPOWER"));
            case TRANSMISSION -> new Transmission(id, name, reference, brand, category, origin, status,
                    TransmissionType.fromFileValue(result.getString("TRANSMISSION_TYPE")),
                    result.getInt("NUMBER_OF_SPEEDS"));
            default -> new Component(type, id, name, reference, brand, category, origin, status);
        };
    }
}