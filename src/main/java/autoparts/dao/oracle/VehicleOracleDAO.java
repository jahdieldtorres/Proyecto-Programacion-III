package autoparts.dao.oracle;

import autoparts.dao.VehicleDAO;
import autoparts.exception.PersistenceException;
import autoparts.model.Brand;
import autoparts.model.Car;
import autoparts.model.CarType;
import autoparts.model.Motorcycle;
import autoparts.model.MotorcycleType;
import autoparts.model.Status;
import autoparts.model.Vehicle;
import autoparts.model.VehicleType;
import autoparts.util.DatabaseConnection;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

/**
 * Guarda y consulta los vehículos llamando a las funciones del paquete PL/SQL
 * PKG_VEHICLES (database/05a_paquetes_catalogo.sql).
 *
 * Todo es una función y el resultado llega por el primer ? de la llamada: un
 * cursor en las consultas, el ID nuevo en INSERT_VEHICLE y las filas afectadas
 * en las demás.
 *
 * Cada vehículo ocupa dos tablas: VEHICLES (datos comunes) y CARS o
 * MOTORCYCLES (datos propios del tipo), con el mismo ID. Cada función del
 * paquete toca una sola tabla, así que guardar y actualizar siguen haciéndose
 * aquí en una transacción (commit y rollback los maneja Java, el paquete no
 * confirma): o se guardan las dos filas o no se guarda ninguna.
 *
 * Implementa la misma interfaz {@link VehicleDAO} que VehicleFileDAO. Sigue el
 * patrón de {@link UserOracleDAO}.
 *
 * @author Álvaro Aramendiz
 */
public class VehicleOracleDAO implements VehicleDAO {

    // Consultas: el primer ? es el cursor que devuelve la función
    private static final String CALL_LIST_ALL = "{ ? = call PKG_VEHICLES.LIST_ALL }";
    private static final String CALL_FIND_BY_ID = "{ ? = call PKG_VEHICLES.FIND_BY_ID(?) }";

    // Vehículo: INSERT devuelve el ID nuevo; UPDATE y DEACTIVATE, las filas cambiadas
    private static final String CALL_INSERT_VEHICLE = "{ ? = call PKG_VEHICLES.INSERT_VEHICLE(?, ?, ?, ?, ?) }";
    private static final String CALL_UPDATE_VEHICLE = "{ ? = call PKG_VEHICLES.UPDATE_VEHICLE(?, ?, ?, ?, ?, ?) }";

    /** Los vehículos no se borran: se pasan a INACTIVO, como en VehicleFileDAO. */
    private static final String CALL_DEACTIVATE = "{ ? = call PKG_VEHICLES.DEACTIVATE_VEHICLE(?) }";

    // Subtipos: devuelven las filas afectadas. El ID del vehículo va de último,
    // así INSERT y UPDATE de cada subtipo usan los mismos parámetros.
    private static final String CALL_INSERT_CAR = "{ ? = call PKG_VEHICLES.INSERT_CAR(?, ?, ?) }";
    private static final String CALL_UPDATE_CAR = "{ ? = call PKG_VEHICLES.UPDATE_CAR(?, ?, ?) }";
    private static final String CALL_INSERT_MOTORCYCLE = "{ ? = call PKG_VEHICLES.INSERT_MOTORCYCLE(?, ?, ?) }";
    private static final String CALL_UPDATE_MOTORCYCLE = "{ ? = call PKG_VEHICLES.UPDATE_MOTORCYCLE(?, ?, ?) }";

    @Override
    public List<Vehicle> listAll() {
        List<Vehicle> vehicles = new ArrayList<>();
        try (Connection connection = DatabaseConnection.getConnection();
                CallableStatement call = connection.prepareCall(CALL_LIST_ALL)) {
            call.registerOutParameter(1, Types.REF_CURSOR);
            call.execute();
            try (ResultSet result = (ResultSet) call.getObject(1)) {
                while (result.next()) {
                    vehicles.add(mapRow(result));
                }
            }
        } catch (SQLException e) {
            throw new PersistenceException("No se pudieron leer los vehículos.", e);
        }
        return vehicles;
    }

    @Override
    public Vehicle findById(int id) {
        try (Connection connection = DatabaseConnection.getConnection();
                CallableStatement call = connection.prepareCall(CALL_FIND_BY_ID)) {
            call.registerOutParameter(1, Types.REF_CURSOR);
            call.setInt(2, id);
            call.execute();
            try (ResultSet result = (ResultSet) call.getObject(1)) {
                return result.next() ? mapRow(result) : null;
            }
        } catch (SQLException e) {
            throw new PersistenceException("No se pudo buscar el vehículo.", e);
        }
    }

    /**
     * Guarda un vehículo nuevo con una transacción: llama a INSERT_VEHICLE,
     * toma el ID que devuelve la función y llama a la de CARS o MOTORCYCLES.
     * Si algo falla, se deshace todo. Al terminar, el ID queda asignado en el
     * objeto.
     */
    @Override
    public void save(Vehicle vehicle) {
        if (vehicle.getStatus() == null) {
            vehicle.setStatus(Status.ACTIVO);
        }
        try (Connection connection = DatabaseConnection.getConnection()) {
            connection.setAutoCommit(false);
            try {
                int id = insertVehicle(connection, vehicle);
                writeSubtype(connection, id, vehicle, CALL_INSERT_CAR, CALL_INSERT_MOTORCYCLE);
                connection.commit();
                vehicle.setId(id);
            } catch (SQLException | RuntimeException e) {
                rollback(connection);
                throw e;
            }
        } catch (SQLException e) {
            throw new PersistenceException("No se pudo guardar el vehículo.", e);
        }
    }

    /**
     * Actualiza VEHICLES y la tabla de su subtipo en una sola transacción.
     */
    @Override
    public void update(Vehicle vehicle) {
        try (Connection connection = DatabaseConnection.getConnection()) {
            connection.setAutoCommit(false);
            try {
                updateVehicle(connection, vehicle);
                int rows = writeSubtype(connection, vehicle.getId(), vehicle,
                        CALL_UPDATE_CAR, CALL_UPDATE_MOTORCYCLE);
                if (rows == 0) {
                    // El vehículo todavía no tenía fila de subtipo: se crea.
                    writeSubtype(connection, vehicle.getId(), vehicle,
                            CALL_INSERT_CAR, CALL_INSERT_MOTORCYCLE);
                }
                connection.commit();
            } catch (SQLException | RuntimeException e) {
                rollback(connection);
                throw e;
            }
        } catch (SQLException e) {
            throw new PersistenceException("No se pudo actualizar el vehículo.", e);
        }
    }

    /**
     * Desactiva el vehículo (borrado lógico). La compatibilidad se conserva.
     */
    @Override
    public void delete(int id) {
        try (Connection connection = DatabaseConnection.getConnection();
                CallableStatement call = connection.prepareCall(CALL_DEACTIVATE)) {
            call.registerOutParameter(1, Types.NUMERIC); // filas cambiadas
            call.setInt(2, id);
            call.execute();
        } catch (SQLException e) {
            throw new PersistenceException("No se pudo desactivar el vehículo.", e);
        }
    }

    /**
     * Llama a INSERT_VEHICLE y devuelve el ID que generó Oracle.
     */
    private int insertVehicle(Connection connection, Vehicle vehicle) throws SQLException {
        try (CallableStatement call = connection.prepareCall(CALL_INSERT_VEHICLE)) {
            call.registerOutParameter(1, Types.NUMERIC); // ID nuevo
            bindVehicle(call, vehicle);
            call.execute();
            return call.getInt(1);
        }
    }

    /**
     * Llama a UPDATE_VEHICLE. Si no cambió ninguna fila, el vehículo no existe
     * y la transacción se deshace.
     */
    private void updateVehicle(Connection connection, Vehicle vehicle) throws SQLException {
        try (CallableStatement call = connection.prepareCall(CALL_UPDATE_VEHICLE)) {
            call.registerOutParameter(1, Types.NUMERIC); // filas cambiadas
            bindVehicle(call, vehicle);
            call.setInt(7, vehicle.getId());
            call.execute();
            if (call.getInt(1) == 0) {
                throw new PersistenceException("No existe el vehículo que se quiere actualizar.");
            }
        }
    }

    /**
     * Pone en la llamada los 5 datos comunes (parámetros 2 a 6; el 1 es lo que
     * devuelve la función).
     */
    private void bindVehicle(CallableStatement call, Vehicle vehicle) throws SQLException {
        call.setString(2, vehicle.getType().getFileValue());
        call.setString(3, vehicle.getBrand().getName());
        call.setString(4, vehicle.getModelName());
        call.setInt(5, vehicle.getYearRelease());
        call.setString(6, vehicle.getStatus().name());
    }

    /**
     * Llama a la función INSERT o UPDATE de la tabla del subtipo (CARS o
     * MOTORCYCLES).
     *
     * @return filas afectadas
     */
    private int writeSubtype(Connection connection, int id, Vehicle vehicle,
            String carCall, String motorcycleCall) throws SQLException {
        if (vehicle instanceof Car car) {
            try (CallableStatement call = connection.prepareCall(carCall)) {
                call.registerOutParameter(1, Types.NUMERIC);
                call.setInt(2, car.getNumDoors());
                call.setString(3, car.getCarType() == null ? null : car.getCarType().getFileValue());
                call.setInt(4, id);
                call.execute();
                return call.getInt(1);
            }
        }
        if (vehicle instanceof Motorcycle motorcycle) {
            try (CallableStatement call = connection.prepareCall(motorcycleCall)) {
                call.registerOutParameter(1, Types.NUMERIC);
                call.setInt(2, motorcycle.getCylinders());
                call.setString(3, motorcycle.getMotorcycleType() == null
                        ? null : motorcycle.getMotorcycleType().getFileValue());
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
     * Convierte la fila actual en un Car o un Motorcycle según su tipo.
     */
    private Vehicle mapRow(ResultSet result) throws SQLException {
        VehicleType type = VehicleType.fromFileValue(result.getString("VEHICLE_TYPE"));
        int id = result.getInt("ID");
        Brand brand = new Brand(result.getInt("BRAND_ID"), result.getString("BRAND_NAME"));
        String modelName = result.getString("MODEL_NAME");
        int year = result.getInt("YEAR_RELEASE");
        Status status = Status.valueOf(result.getString("STATUS"));

        if (type == VehicleType.MOTOCICLETA) {
            String motorcycleType = result.getString("MOTORCYCLE_TYPE");
            return new Motorcycle(id, brand, modelName, year, status,
                    result.getInt("DISPLACEMENT_CC"),
                    motorcycleType == null ? null : MotorcycleType.fromFileValue(motorcycleType));
        }
        String carType = result.getString("CAR_TYPE");
        return new Car(id, brand, modelName, year, status,
                result.getInt("NUM_DOORS"),
                carType == null ? null : CarType.fromFileValue(carType));
    }
}