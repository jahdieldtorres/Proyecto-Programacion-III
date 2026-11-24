package autoparts.dao.oracle;

import autoparts.dao.CategoryDAO;
import autoparts.exception.PersistenceException;
import autoparts.model.Category;
import autoparts.util.DatabaseConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Guarda y consulta las categorías de repuestos en la tabla CATEGORIES de Oracle.
 *
 * Implementa la misma interfaz {@link CategoryDAO} que CategoryFileDAO, por eso
 * ComponentService no cambia. Sigue el patrón de {@link UserOracleDAO}.
 *
 * @author Alvaro Aramendiz
 */
public class CategoryOracleDAO implements CategoryDAO {

    private static final String SELECT_CATEGORIES = "SELECT ID, NAME FROM CATEGORIES";

    private static final String FIND_ALL = SELECT_CATEGORIES + " ORDER BY ID";

    private static final String FIND_BY_ID = SELECT_CATEGORIES + " WHERE ID = ?";

    private static final String INSERT = "INSERT INTO CATEGORIES (NAME) VALUES (?)";

    private static final String UPDATE = "UPDATE CATEGORIES SET NAME = ? WHERE ID = ?";

    private static final String DELETE = "DELETE FROM CATEGORIES WHERE ID = ?";

    @Override
    public List<Category> listAll() {
        List<Category> categories = new ArrayList<>();
        try (Connection connection = DatabaseConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(FIND_ALL);
                ResultSet result = statement.executeQuery()) {
            while (result.next()) {
                categories.add(mapRow(result));
            }
        } catch (SQLException e) {
            throw new PersistenceException("No se pudieron leer las categorías.", e);
        }
        return categories;
    }

    @Override
    public Category findById(int id) {
        try (Connection connection = DatabaseConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(FIND_BY_ID)) {
            statement.setInt(1, id);
            try (ResultSet result = statement.executeQuery()) {
                return result.next() ? mapRow(result) : null;
            }
        } catch (SQLException e) {
            throw new PersistenceException("No se pudo buscar la categoría.", e);
        }
    }

    /**
     * Guarda una categoría nueva. El ID lo genera Oracle (IDENTITY) y se le
     * asigna al objeto recibido, igual que hace CategoryFileDAO.
     */
    @Override
    public void save(Category category) {
        try (Connection connection = DatabaseConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(INSERT, new String[]{"ID"})) {
            statement.setString(1, category.getName());
            statement.executeUpdate();

            try (ResultSet keys = statement.getGeneratedKeys()) {
                keys.next();
                category.setId(keys.getInt(1));
            }
        } catch (SQLException e) {
            throw new PersistenceException("No se pudo guardar la categoría.", e);
        }
    }

    @Override
    public void update(Category category) {
        try (Connection connection = DatabaseConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(UPDATE)) {
            statement.setString(1, category.getName());
            statement.setInt(2, category.getId());
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new PersistenceException("No se pudo actualizar la categoría.", e);
        }
    }

    /**
     * Elimina la categoría. Si algún componente la usa, Oracle rechaza el borrado
     * (llave foránea) y se avisa con un mensaje claro.
     */
    @Override
    public void delete(int id) {
        try (Connection connection = DatabaseConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(DELETE)) {
            statement.setInt(1, id);
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new PersistenceException(
                    "No se pudo eliminar la categoría. Revisa que ningún componente la esté usando.", e);
        }
    }

    /**
     * Convierte la fila actual del resultado en un objeto Category.
     */
    private Category mapRow(ResultSet result) throws SQLException {
        return new Category(result.getInt("ID"), result.getString("NAME"));
    }
}