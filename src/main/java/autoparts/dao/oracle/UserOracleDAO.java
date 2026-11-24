package autoparts.dao.oracle;

import autoparts.dao.UserDAO;
import autoparts.exception.PersistenceException;
import autoparts.model.Role;
import autoparts.model.Status;
import autoparts.model.User;
import autoparts.util.DatabaseConnection;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Guarda y consulta los usuarios en la tabla USERS de la base de datos Oracle.
 *
 * Implementa la misma interfaz {@link UserDAO} que UserFileDAO, por eso
 * UserService no cambia: solo se cambia el DAO en AppServices.
 *
 * Patrón que siguen todos los DAO de Oracle del proyecto:
 * <ul>
 *   <li>Las consultas SQL son constantes al inicio de la clase.</li>
 *   <li>Cada método abre su conexión con try-with-resources, así la conexión,
 *       la sentencia y el resultado se cierran solos.</li>
 *   <li>Los valores se pasan con PreparedStatement (los ?), nunca pegando
 *       texto en el SQL, para evitar la inyección SQL.</li>
 *   <li>mapRow() convierte una fila en un objeto, en un solo lugar.</li>
 *   <li>Cada SQLException se convierte en PersistenceException con un mensaje
 *       en español, igual que en los DAO de archivos.</li>
 * </ul>
 *
 * @author Jahdiel Torres
 */
public class UserOracleDAO implements UserDAO {

    private static final String SELECT_USERS =
            "SELECT ID, NAME, EMAIL, PASSWORD_HASH, ROLE, STATUS, REGISTRATION_DATE FROM USERS";

    private static final String FIND_ALL = SELECT_USERS + " ORDER BY ID";

    private static final String FIND_BY_EMAIL = SELECT_USERS + " WHERE LOWER(EMAIL) = LOWER(?)";

    private static final String FIND_BY_ID = SELECT_USERS + " WHERE ID = ?";

    private static final String INSERT =
            "INSERT INTO USERS (NAME, EMAIL, PASSWORD_HASH, ROLE, STATUS, REGISTRATION_DATE) "
            + "VALUES (?, ?, ?, ?, ?, ?)";

    private static final String UPDATE =
            "UPDATE USERS SET NAME = ?, EMAIL = ?, PASSWORD_HASH = ?, ROLE = ?, STATUS = ? "
            + "WHERE ID = ?";

    @Override
    public List<User> findAll() {
        List<User> users = new ArrayList<>();
        try (Connection connection = DatabaseConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(FIND_ALL);
                ResultSet result = statement.executeQuery()) {
            while (result.next()) {
                users.add(mapRow(result));
            }
        } catch (SQLException e) {
            throw new PersistenceException("No se pudieron leer los usuarios.", e);
        }
        return users;
    }

    @Override
    public User findByEmail(String email) {
        return findOne(FIND_BY_EMAIL, email);
    }

    @Override
    public User findById(int id) {
        return findOne(FIND_BY_ID, id);
    }

    /**
     * Guarda un usuario nuevo. El ID no se envía: lo genera Oracle
     * (columna IDENTITY) y este método lo devuelve.
     *
     * @return el ID que Oracle le asignó al usuario
     */
    @Override
    public int save(User user) {
        try (Connection connection = DatabaseConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(INSERT, new String[]{"ID"})) {
            statement.setString(1, user.getName());
            statement.setString(2, user.getEmail());
            statement.setString(3, user.getPasswordHash());
            statement.setString(4, user.getRole().name());
            statement.setString(5, user.getStatus().name());
            statement.setDate(6, Date.valueOf(user.getRegistrationDate()));
            statement.executeUpdate();

            try (ResultSet keys = statement.getGeneratedKeys()) {
                keys.next();
                return keys.getInt(1);
            }
        } catch (SQLException e) {
            throw new PersistenceException("No se pudo guardar el usuario.", e);
        }
    }

    @Override
    public void update(User user) {
        try (Connection connection = DatabaseConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(UPDATE)) {
            statement.setString(1, user.getName());
            statement.setString(2, user.getEmail());
            statement.setString(3, user.getPasswordHash());
            statement.setString(4, user.getRole().name());
            statement.setString(5, user.getStatus().name());
            statement.setInt(6, user.getId());
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new PersistenceException("No se pudo actualizar el usuario.", e);
        }
    }

    /**
     * Ejecuta una consulta que devuelve como máximo un usuario.
     *
     * @param sql   consulta con un solo parámetro (?)
     * @param value valor del parámetro (el correo o el ID)
     * @return el usuario encontrado, o null si no existe
     */
    private User findOne(String sql, Object value) {
        try (Connection connection = DatabaseConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setObject(1, value);
            try (ResultSet result = statement.executeQuery()) {
                return result.next() ? mapRow(result) : null;
            }
        } catch (SQLException e) {
            throw new PersistenceException("No se pudo buscar el usuario.", e);
        }
    }

    /**
     * Convierte la fila actual del resultado en un objeto User.
     */
    private User mapRow(ResultSet result) throws SQLException {
        return new User(
                result.getInt("ID"),
                result.getString("NAME"),
                result.getString("EMAIL"),
                result.getString("PASSWORD_HASH"),
                Role.valueOf(result.getString("ROLE")),
                Status.valueOf(result.getString("STATUS")),
                result.getDate("REGISTRATION_DATE").toLocalDate());
    }
}
