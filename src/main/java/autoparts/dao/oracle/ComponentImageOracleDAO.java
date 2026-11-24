package autoparts.dao.oracle;

import autoparts.dao.ComponentImageDAO;
import autoparts.exception.PersistenceException;
import autoparts.model.ComponentImage;
import autoparts.util.DatabaseConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Guarda y consulta los registros de las fotos de los componentes en la tabla
 * COMPONENT_IMAGES de Oracle.
 *
 * Regla que cuida este DAO: un componente tiene como máximo una foto
 * principal. Al guardar o actualizar una foto como principal, las otras del
 * mismo componente se desmarcan en la misma transacción.
 *
 * Sigue el patrón de {@link UserOracleDAO}.
 *
 * @author Alvaro Aramendiz
 */
public class ComponentImageOracleDAO implements ComponentImageDAO {

    private static final String SELECT_IMAGES =
            "SELECT ID, COMPONENT_ID, FILE_NAME, IS_MAIN, SORT_ORDER FROM COMPONENT_IMAGES";

    /** La principal primero ('S' es mayor que 'N'), luego por orden. */
    private static final String FIND_BY_COMPONENT =
            SELECT_IMAGES + " WHERE COMPONENT_ID = ? ORDER BY IS_MAIN DESC, SORT_ORDER, ID";

    private static final String FIND_MAIN = FIND_BY_COMPONENT + " FETCH FIRST 1 ROWS ONLY";

    private static final String FIND_BY_ID = SELECT_IMAGES + " WHERE ID = ?";

    private static final String INSERT =
            "INSERT INTO COMPONENT_IMAGES (COMPONENT_ID, FILE_NAME, IS_MAIN, SORT_ORDER) VALUES (?, ?, ?, ?)";

    private static final String UPDATE =
            "UPDATE COMPONENT_IMAGES SET FILE_NAME = ?, IS_MAIN = ?, SORT_ORDER = ? WHERE ID = ?";

    private static final String UNMARK_OTHERS =
            "UPDATE COMPONENT_IMAGES SET IS_MAIN = 'N' WHERE COMPONENT_ID = ? AND ID <> ?";

    private static final String DELETE = "DELETE FROM COMPONENT_IMAGES WHERE ID = ?";

    @Override
    public List<ComponentImage> listByComponent(int componentId) {
        List<ComponentImage> images = new ArrayList<>();
        try (Connection connection = DatabaseConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(FIND_BY_COMPONENT)) {
            statement.setInt(1, componentId);
            try (ResultSet result = statement.executeQuery()) {
                while (result.next()) {
                    images.add(mapRow(result));
                }
            }
        } catch (SQLException e) {
            throw new PersistenceException("No se pudieron leer las fotos del componente.", e);
        }
        return images;
    }

    @Override
    public ComponentImage findMain(int componentId) {
        try (Connection connection = DatabaseConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(FIND_MAIN)) {
            statement.setInt(1, componentId);
            try (ResultSet result = statement.executeQuery()) {
                return result.next() ? mapRow(result) : null;
            }
        } catch (SQLException e) {
            throw new PersistenceException("No se pudo leer la foto principal del componente.", e);
        }
    }

    @Override
    public ComponentImage findById(int id) {
        try (Connection connection = DatabaseConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(FIND_BY_ID)) {
            statement.setInt(1, id);
            try (ResultSet result = statement.executeQuery()) {
                return result.next() ? mapRow(result) : null;
            }
        } catch (SQLException e) {
            throw new PersistenceException("No se pudo buscar la foto.", e);
        }
    }

    /**
     * Guarda una foto nueva. El ID lo genera Oracle (IDENTITY) y se le asigna
     * al objeto. Si es la principal, desmarca las otras en la misma transacción.
     */
    @Override
    public void save(ComponentImage image) {
        try (Connection connection = DatabaseConnection.getConnection()) {
            connection.setAutoCommit(false);
            try {
                int id = insert(connection, image);
                if (image.isMain()) {
                    unmarkOthers(connection, image.getComponentId(), id);
                }
                connection.commit();
                image.setId(id);
            } catch (SQLException | RuntimeException e) {
                rollback(connection);
                throw e;
            }
        } catch (SQLException e) {
            throw new PersistenceException("No se pudo guardar la foto del componente.", e);
        }
    }

    /**
     * Actualiza la foto. Si pasa a ser la principal, desmarca las otras en la
     * misma transacción.
     */
    @Override
    public void update(ComponentImage image) {
        try (Connection connection = DatabaseConnection.getConnection()) {
            connection.setAutoCommit(false);
            try {
                try (PreparedStatement statement = connection.prepareStatement(UPDATE)) {
                    statement.setString(1, image.getFileName());
                    statement.setString(2, image.isMain() ? "S" : "N");
                    statement.setInt(3, image.getSortOrder());
                    statement.setInt(4, image.getId());
                    statement.executeUpdate();
                }
                if (image.isMain()) {
                    unmarkOthers(connection, image.getComponentId(), image.getId());
                }
                connection.commit();
            } catch (SQLException | RuntimeException e) {
                rollback(connection);
                throw e;
            }
        } catch (SQLException e) {
            throw new PersistenceException("No se pudo actualizar la foto del componente.", e);
        }
    }

    @Override
    public void delete(int id) {
        try (Connection connection = DatabaseConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(DELETE)) {
            statement.setInt(1, id);
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new PersistenceException("No se pudo eliminar la foto del componente.", e);
        }
    }

    /**
     * Inserta la fila y devuelve el ID que generó Oracle.
     */
    private int insert(Connection connection, ComponentImage image) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(INSERT, new String[]{"ID"})) {
            statement.setInt(1, image.getComponentId());
            statement.setString(2, image.getFileName());
            statement.setString(3, image.isMain() ? "S" : "N");
            statement.setInt(4, image.getSortOrder());
            statement.executeUpdate();
            try (ResultSet keys = statement.getGeneratedKeys()) {
                keys.next();
                return keys.getInt(1);
            }
        }
    }

    /**
     * Deja como no principales todas las fotos del componente menos la indicada.
     */
    private void unmarkOthers(Connection connection, int componentId, int keepId) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(UNMARK_OTHERS)) {
            statement.setInt(1, componentId);
            statement.setInt(2, keepId);
            statement.executeUpdate();
        }
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
     * Convierte la fila actual del resultado en un objeto ComponentImage.
     */
    private ComponentImage mapRow(ResultSet result) throws SQLException {
        return new ComponentImage(
                result.getInt("ID"),
                result.getInt("COMPONENT_ID"),
                result.getString("FILE_NAME"),
                "S".equals(result.getString("IS_MAIN")),
                result.getInt("SORT_ORDER"));
    }
}
