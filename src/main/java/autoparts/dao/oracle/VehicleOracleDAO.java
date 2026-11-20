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
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Guarda y consulta los vehículos en Oracle.
 *
 * Cada vehículo ocupa dos tablas: VEHICLES (datos comunes) y CARS o
 * MOTORCYCLES (datos propios del tipo), con el mismo ID. Por eso guardar y
 * actualizar se hacen en una transacción: o se guardan las dos filas o no se
 * guarda ninguna.
 *
 * Implementa la misma interfaz {@link VehicleDAO} que VehicleFileDAO. Sigue el
 * patrón de {@link UserOracleDAO}.
 *
 * @author Alvaro Aramendiz
 */
public class VehicleOracleDAO implements VehicleDAO {

    /**
     * Lee el vehículo con los datos de su subtipo. La marca del vehículo es un
     * texto; si coincide con una marca de BRANDS se usa su ID, si no, 0
     * (igual que VehicleFileDAO).
     */
    private static final String SELECT_VEHICLES =
            "SELECT V.ID, V.VEHICLE_TYPE, V.BRAND_NAME, NVL(B.ID, 0) AS BRAND_ID, "
            + "V.MODEL_NAME, V.YEAR_RELEASE, V.STATUS, "
            + "C.NUM_DOORS, C.CAR_TYPE, M.DISPLACEMENT_CC, M.MOTORCYCLE_TYPE "
            + "FROM VEHICLES V "
            + "LEFT JOIN BRANDS B ON B.NAME = V.BRAND_NAME "
            + "LEFT JOIN CARS C ON C.VEHICLE_ID = V.ID "
            + "LEFT JOIN MOTORCYCLES M ON M.VEHICLE_ID = V.ID";

    private static final String FIND_ALL = SELECT_VEHICLES + " ORDER BY V.ID";

    private static final String FIND_BY_ID = SELECT_VEHICLES + " WHERE V.ID = ?";

    private static final String INSERT_VEHICLE =
            "INSERT INTO VEHICLES (VEHICLE_TYPE, BRAND_NAME, MODEL_NAME, YEAR_RELEASE, STATUS) "
            + "VALUES (?, ?, ?, ?, ?)";

    private static final String UPDATE_VEHICLE =
            "UPDATE VEHICLES SET VEHICLE_TYPE = ?, BRAND_NAME = ?, MODEL_NAME = ?, "
            + "YEAR_RELEASE = ?, STATUS = ? WHERE ID = ?";

    /** Los vehículos no se borran: se pasan a INACTIVO, como en VehicleFileDAO. */
    private static final String DEACTIVATE = "UPDATE VEHICLES SET STATUS = 'INACTIVO' WHERE ID = ?";

    // En los INSERT y UPDATE de los subtipos el ID va de último, así los dos usan los mismos parámetros.
    private static final String INSERT_CAR =
            "INSERT INTO CARS (NUM_DOORS, CAR_TYPE, VEHICLE_ID) VALUES (?, ?, ?)";

    private static final String UPDATE_CAR =
            "UPDATE CARS SET NUM_DOORS = ?, CAR_TYPE = ? WHERE VEHICLE_ID = ?";

    private static final String INSERT_MOTORCYCLE =
            "INSERT INTO MOTORCYCLES (DISPLACEMENT_CC, MOTORCYCLE_TYPE, VEHICLE_ID) VALUES (?, ?, ?)";

    private static final String UPDATE_MOTORCYCLE =
            "UPDATE MOTORCYCLES SET DISPLACEMENT_CC = ?, MOTORCYCLE_TYPE = ? WHERE VEHICLE_ID = ?";

    @Override
    public List<Vehicle> listAll() {
        List<Vehicle> vehicles = new ArrayList<>();
        try (Connection connection = DatabaseConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(FIND_ALL);
                ResultSet result = statement.executeQuery()) {
            while (result.next()) {
                vehicles.add(mapRow(result));
            }
        } catch (SQLException e) {
            throw new PersistenceException("No se pudieron leer los vehículos.", e);
        }
        return vehicles;
    }

    @Override
    public Vehicle findById(int id) {
        try (Connection connection = DatabaseConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(FIND_BY_ID)) {
            statement.setInt(1, id);
            try (ResultSet result = statement.executeQuery()) {
                return result.next() ? mapRow(result) : null;
            }
        } catch (SQLException e) {
            throw new PersistenceException("No se pudo buscar el vehículo.", e);
        }
    }

    /**
     * Guarda un vehículo nuevo con una transacción: inserta en VEHICLES, toma
     * el ID que genera Oracle e inserta en CARS o MOTORCYCLES. Si algo falla,
     * se deshace todo. Al terminar, el ID queda asignado en el objeto.
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
                writeSubtype(connection, id, vehicle, INSERT_CAR, INSERT_MOTORCYCLE);
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
                int rows = writeSubtype(connection, vehicle.getId(), vehicle, UPDATE_CAR, UPDATE_MOTORCYCLE);
                if (rows == 0) {
                    // El vehículo todavía no tenía fila de subtipo: se crea.
                    writeSubtype(connection, vehicle.getId(), vehicle, INSERT_CAR, INSERT_MOTORCYCLE);
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
                PreparedStatement statement = connection.prepareStatement(DEACTIVATE)) {
            statement.setInt(1, id);
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new PersistenceException("No se pudo desactivar el vehículo.", e);
        }
    }

    /**
     * Inserta la fila de VEHICLES y devuelve el ID que generó Oracle.
     */
    private int insertVehicle(Connection connection, Vehicle vehicle) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(INSERT_VEHICLE, new String[]{"ID"})) {
            bindVehicle(statement, vehicle);
            statement.executeUpdate();
            try (ResultSet keys = statement.getGeneratedKeys()) {
                keys.next();
                return keys.getInt(1);
            }
        }
    }

    private void updateVehicle(Connection connection, Vehicle vehicle) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(UPDATE_VEHICLE)) {
            bindVehicle(statement, vehicle);
            statement.setInt(6, vehicle.getId());
            statement.executeUpdate();
        }
    }

    /**
     * Pone en la sentencia los 5 datos comunes (parámetros 1 a 5).
     */
    private void bindVehicle(PreparedStatement statement, Vehicle vehicle) throws SQLException {
        statement.setString(1, vehicle.getType().getFileValue());
        statement.setString(2, vehicle.getBrand().getName());
        statement.setString(3, vehicle.getModelName());
        statement.setInt(4, vehicle.getYearRelease());
        statement.setString(5, vehicle.getStatus().name());
    }

    /**
     * Ejecuta el INSERT o el UPDATE de la tabla del subtipo (CARS o MOTORCYCLES).
     *
     * @return filas afectadas
     */
    private int writeSubtype(Connection connection, int id, Vehicle vehicle,
            String carSql, String motorcycleSql) throws SQLException {
        if (vehicle instanceof Car car) {
            try (PreparedStatement statement = connection.prepareStatement(carSql)) {
                statement.setInt(1, car.getNumDoors());
                statement.setString(2, car.getCarType() == null ? null : car.getCarType().getFileValue());
                statement.setInt(3, id);
                return statement.executeUpdate();
            }
        }
        if (vehicle instanceof Motorcycle motorcycle) {
            try (PreparedStatement statement = connection.prepareStatement(motorcycleSql)) {
                statement.setInt(1, motorcycle.getCylinders());
                statement.setString(2, motorcycle.getMotorcycleType() == null
                        ? null : motorcycle.getMotorcycleType().getFileValue());
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