package autoparts.dao.oracle;

import autoparts.dao.CategoryDAO;
import autoparts.exception.PersistenceException;
import autoparts.model.Category;
import autoparts.util.DatabaseConnection;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

/**
 * Guarda y consulta las categorías del catálogo llamando a las funciones del paquete PL/SQL
 * PKG_CATEGORIES (database/05a_paquetes_catalogo.sql).
 *
 * Todo es una función y el resultado llega por el primer ? de la llamada: un
 * cursor en las consultas, el ID nuevo en el INSERT y las filas cambiadas en el
 * UPDATE y el DELETE.
 *
 * Implementa la misma interfaz {@link CategoryDAO} que CategoryFileDAO, por eso
 * ComponentService no cambia. Sigue el patrón de {@link UserOracleDAO}.
 *
 * @author Álvaro Aramendiz
 */
public class CategoryOracleDAO implements CategoryDAO {

    private static final String CALL_LIST_ALL = "{ ? = call PKG_CATEGORIES.LIST_ALL }";
    private static final String CALL_FIND_BY_ID = "{ ? = call PKG_CATEGORIES.FIND_BY_ID(?) }";
    private static final String CALL_INSERT = "{ ? = call PKG_CATEGORIES.INSERT_CATEGORY(?) }";
    private static final String CALL_UPDATE = "{ ? = call PKG_CATEGORIES.UPDATE_CATEGORY(?, ?) }";
    private static final String CALL_DELETE = "{ ? = call PKG_CATEGORIES.DELETE_CATEGORY(?) }";

    @Override
    public List<Category> listAll() {
        List<Category> categorys = new ArrayList<>();
        try (Connection connection = DatabaseConnection.getConnection();
                CallableStatement call = connection.prepareCall(CALL_LIST_ALL)) {
            call.registerOutParameter(1, Types.REF_CURSOR);
            call.execute();
            try (ResultSet result = (ResultSet) call.getObject(1)) {
                while (result.next()) {
                    categorys.add(mapRow(result));
                }
            }
        } catch (SQLException e) {
            throw new PersistenceException("No se pudieron leer las categorías del catálogo.", e);
        }
        return categorys;
    }

    @Override
    public Category findById(int id) {
        try (Connection connection = DatabaseConnection.getConnection();
                CallableStatement call = connection.prepareCall(CALL_FIND_BY_ID)) {
            call.registerOutParameter(1, Types.REF_CURSOR);
            call.setInt(2, id);
            call.execute();
            try (ResultSet result = (ResultSet) call.getObject(1)) {
                return result.next() ? mapRow(result) : null;
            }
        } catch (SQLException e) {
            throw new PersistenceException("No se pudo buscar la categoría.", e);
        }
    }

    /**
     * Guarda una categoría nueva con PKG_CATEGORIES.INSERT_CATEGORY. El ID lo genera Oracle
     * (IDENTITY), la función lo devuelve y se le asigna al objeto recibido,
     * igual que hace CategoryFileDAO.
     */
    @Override
    public void save(Category category) {
        try (Connection connection = DatabaseConnection.getConnection();
                CallableStatement call = connection.prepareCall(CALL_INSERT)) {
            call.registerOutParameter(1, Types.NUMERIC); // ID nuevo
            call.setString(2, category.getName());
            call.execute();
            category.setId(call.getInt(1));
        } catch (SQLException e) {
            throw new PersistenceException("No se pudo guardar la categoría.", e);
        }
    }

    /**
     * Actualiza con PKG_CATEGORIES.UPDATE_CATEGORY. La función devuelve cuántas filas
     * cambió; si es 0, la categoría no existe.
     */
    @Override
    public void update(Category category) {
        try (Connection connection = DatabaseConnection.getConnection();
                CallableStatement call = connection.prepareCall(CALL_UPDATE)) {
            call.registerOutParameter(1, Types.NUMERIC); // filas cambiadas
            call.setInt(2, category.getId());
            call.setString(3, category.getName());
            call.execute();
            if (call.getInt(1) == 0) {
                throw new PersistenceException("No existe la categoría que se quiere actualizar.");
            }
        } catch (SQLException e) {
            throw new PersistenceException("No se pudo actualizar la categoría.", e);
        }
    }

    /**
     * Elimina con PKG_CATEGORIES.DELETE_CATEGORY. Si algún componente la usa, Oracle
     * rechaza el borrado (llave foránea) y se avisa con un mensaje claro.
     */
    @Override
    public void delete(int id) {
        try (Connection connection = DatabaseConnection.getConnection();
                CallableStatement call = connection.prepareCall(CALL_DELETE)) {
            call.registerOutParameter(1, Types.NUMERIC); // filas borradas
            call.setInt(2, id);
            call.execute();
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
