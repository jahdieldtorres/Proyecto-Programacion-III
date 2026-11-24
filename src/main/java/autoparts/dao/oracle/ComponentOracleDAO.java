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
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

/**
 * Guarda y consulta los componentes (repuestos) llamando a las funciones del
 * paquete PL/SQL PKG_COMPONENTS (database/05a_paquetes_catalogo.sql).
 *
 * Todo es una función y el resultado llega por el primer ? de la llamada: un
 * cursor en las consultas, el ID nuevo en INSERT_COMPONENT y las filas afectadas
 * en las demás.
 *
 * Cada componente ocupa dos tablas: COMPONENTS (datos comunes) y, si es una
 * llanta, un rin, un motor o una transmisión, también TIRES, RIMS, ENGINES o
 * TRANSMISSIONS (datos propios) con el mismo ID. Cada función del paquete toca
 * una sola tabla, así que guardar y actualizar siguen haciéndose aquí en una
 * transacción (commit y rollback los maneja Java, el paquete no confirma):
 * o se guardan las dos filas o no se guarda ninguna. Un componente GENERAL
 * solo tiene fila en COMPONENTS.
 *
 * Implementa la misma interfaz {@link ComponentDAO} que ComponentFileDAO, por
 * eso ComponentService no cambia. Sigue el patrón de {@link UserOracleDAO}.
 *
 * @author Álvaro Aramendiz
 */
public class ComponentOracleDAO implements ComponentDAO {

    // Consultas: el primer ? es el cursor que devuelve la función
    private static final String CALL_LIST_ALL = "{ ? = call PKG_COMPONENTS.LIST_ALL }";
    private static final String CALL_FIND_BY_ID = "{ ? = call PKG_COMPONENTS.FIND_BY_ID(?) }";

    // Componente: INSERT devuelve el ID nuevo; UPDATE y DEACTIVATE, las filas cambiadas
    private static final String CALL_INSERT_COMPONENT =
            "{ ? = call PKG_COMPONENTS.INSERT_COMPONENT(?, ?, ?, ?, ?, ?, ?, ?) }";
    private static final String CALL_UPDATE_COMPONENT =
            "{ ? = call PKG_COMPONENTS.UPDATE_COMPONENT(?, ?, ?, ?, ?, ?, ?, ?, ?) }";

    /** Los componentes no se borran (un disparador lo impide): se pasan a INACTIVO. */
    private static final String CALL_DEACTIVATE = "{ ? = call PKG_COMPONENTS.DEACTIVATE_COMPONENT(?) }";

    // Subtipos: devuelven las filas afectadas. El ID del componente va de último,
    // así INSERT y UPDATE de cada subtipo usan los mismos parámetros.
    private static final String CALL_INSERT_TIRE = "{ ? = call PKG_COMPONENTS.INSERT_TIRE(?, ?, ?, ?, ?, ?) }";
    private static final String CALL_UPDATE_TIRE = "{ ? = call PKG_COMPONENTS.UPDATE_TIRE(?, ?, ?, ?, ?, ?) }";
    private static final String CALL_INSERT_RIM = "{ ? = call PKG_COMPONENTS.INSERT_RIM(?, ?, ?, ?, ?) }";
    private static final String CALL_UPDATE_RIM = "{ ? = call PKG_COMPONENTS.UPDATE_RIM(?, ?, ?, ?, ?) }";
    private static final String CALL_INSERT_ENGINE = "{ ? = call PKG_COMPONENTS.INSERT_ENGINE(?, ?, ?, ?, ?) }";
    private static final String CALL_UPDATE_ENGINE = "{ ? = call PKG_COMPONENTS.UPDATE_ENGINE(?, ?, ?, ?, ?) }";
    private static final String CALL_INSERT_TRANSMISSION =
            "{ ? = call PKG_COMPONENTS.INSERT_TRANSMISSION(?, ?, ?) }";
    private static final String CALL_UPDATE_TRANSMISSION =
            "{ ? = call PKG_COMPONENTS.UPDATE_TRANSMISSION(?, ?, ?) }";

    @Override
    public List<Component> listAll() {
        List<Component> components = new ArrayList<>();
        try (Connection connection = DatabaseConnection.getConnection();
                CallableStatement call = connection.prepareCall(CALL_LIST_ALL)) {
            call.registerOutParameter(1, Types.REF_CURSOR);
            call.execute();
            try (ResultSet result = (ResultSet) call.getObject(1)) {
                while (result.next()) {
                    components.add(mapRow(result));
                }
            }
        } catch (SQLException e) {
            throw new PersistenceException("No se pudieron leer los componentes.", e);
        }
        return components;
    }

    @Override
    public Component findById(int id) {
        try (Connection connection = DatabaseConnection.getConnection();
                CallableStatement call = connection.prepareCall(CALL_FIND_BY_ID)) {
            call.registerOutParameter(1, Types.REF_CURSOR);
            call.setInt(2, id);
            call.execute();
            try (ResultSet result = (ResultSet) call.getObject(1)) {
                return result.next() ? mapRow(result) : null;
            }
        } catch (SQLException e) {
            throw new PersistenceException("No se pudo buscar el componente.", e);
        }
    }

    /**
     * Guarda un componente nuevo con una transacción: llama a INSERT_COMPONENT,
     * toma el ID que devuelve la función y llama a la función de su subtipo. Si
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
                CallableStatement call = connection.prepareCall(CALL_DEACTIVATE)) {
            call.registerOutParameter(1, Types.NUMERIC); // filas cambiadas
            call.setInt(2, id);
            call.execute();
        } catch (SQLException e) {
            throw new PersistenceException("No se pudo desactivar el componente.", e);
        }
    }

    /**
     * Llama a INSERT_COMPONENT y devuelve el ID que generó Oracle.
     */
    private int insertComponent(Connection connection, Component component) throws SQLException {
        try (CallableStatement call = connection.prepareCall(CALL_INSERT_COMPONENT)) {
            call.registerOutParameter(1, Types.NUMERIC); // ID nuevo
            bindComponent(call, component);
            call.execute();
            return call.getInt(1);
        }
    }

    /**
     * Llama a UPDATE_COMPONENT. Si no cambió ninguna fila, el componente no
     * existe y la transacción se deshace.
     */
    private void updateComponent(Connection connection, Component component) throws SQLException {
        try (CallableStatement call = connection.prepareCall(CALL_UPDATE_COMPONENT)) {
            call.registerOutParameter(1, Types.NUMERIC); // filas cambiadas
            bindComponent(call, component);
            call.setInt(10, component.getId());
            call.execute();
            if (call.getInt(1) == 0) {
                throw new PersistenceException("No existe el componente que se quiere actualizar.");
            }
        }
    }

    /**
     * Pone en la llamada los 8 datos comunes (parámetros 2 a 9; el 1 es lo que
     * devuelve la función). La descripción puede ir vacía: se guarda como NULL.
     */
    private void bindComponent(CallableStatement call, Component component) throws SQLException {
        call.setString(2, component.getType().getFileValue());
        call.setString(3, component.getName());
        call.setString(4, component.getReference());
        call.setInt(5, component.getBrand().getId());
        call.setInt(6, component.getCategory().getId());
        call.setString(7, component.getOrigin());
        call.setString(8, component.getDescription());
        call.setString(9, component.getStatus().name());
    }

    /**
     * Llama a la función INSERT o UPDATE de la tabla del subtipo del
     * componente. Un componente GENERAL no tiene tabla de subtipo y no hace
     * nada.
     *
     * @param insert true para insertar, false para actualizar
     * @return filas afectadas
     */
    private int writeSubtype(Connection connection, int id, Component component, boolean insert)
            throws SQLException {
        if (component instanceof Tire tire) {
            try (CallableStatement call = connection.prepareCall(insert ? CALL_INSERT_TIRE : CALL_UPDATE_TIRE)) {
                call.registerOutParameter(1, Types.NUMERIC);
                call.setInt(2, tire.getWidthMm());
                call.setInt(3, tire.getProfile());
                call.setInt(4, tire.getDiameterInches());
                call.setInt(5, tire.getLoadIndex());
                call.setString(6, tire.getSpeedIndex());
                call.setInt(7, id);
                call.execute();
                return call.getInt(1);
            }
        }
        if (component instanceof Rim rim) {
            try (CallableStatement call = connection.prepareCall(insert ? CALL_INSERT_RIM : CALL_UPDATE_RIM)) {
                call.registerOutParameter(1, Types.NUMERIC);
                call.setInt(2, rim.getDiameterInches());
                call.setDouble(3, rim.getWidthInches());
                call.setString(4, rim.getPcd());
                call.setString(5, rim.getMaterial().getFileValue());
                call.setInt(6, id);
                call.execute();
                return call.getInt(1);
            }
        }
        if (component instanceof Engine engine) {
            try (CallableStatement call = connection.prepareCall(insert ? CALL_INSERT_ENGINE : CALL_UPDATE_ENGINE)) {
                call.registerOutParameter(1, Types.NUMERIC);
                call.setInt(2, engine.getDisplacementCc());
                call.setInt(3, engine.getCylinders());
                call.setString(4, engine.getFuel().getFileValue());
                call.setInt(5, engine.getHorsepower());
                call.setInt(6, id);
                call.execute();
                return call.getInt(1);
            }
        }
        if (component instanceof Transmission transmission) {
            try (CallableStatement call = connection.prepareCall(
                    insert ? CALL_INSERT_TRANSMISSION : CALL_UPDATE_TRANSMISSION)) {
                call.registerOutParameter(1, Types.NUMERIC);
                call.setString(2, transmission.getTransmissionType().getFileValue());
                call.setInt(3, transmission.getNumberOfSpeeds());
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

        Component component = switch (type) {
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
        // La descripción es común a todos los tipos: se asigna después de crear el objeto.
        component.setDescription(result.getString("DESCRIPTION"));
        return component;
    }
}