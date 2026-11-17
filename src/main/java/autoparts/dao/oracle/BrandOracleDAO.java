package autoparts.dao.oracle;

import autoparts.dao.BrandDAO;
import autoparts.exception.PersistenceException;
import autoparts.model.Brand;
import autoparts.util.DatabaseConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Guarda y consulta las marcas de repuestos en la tabla BRANDS de Oracle.
 *
 * Implementa la misma interfaz {@link BrandDAO} que BrandFileDAO, por eso
 * ComponentService no cambia. Sigue el patrón de {@link UserOracleDAO}.
 *
 * @author ALVARO
 */
public class BrandOracleDAO implements BrandDAO {

    private static final String SELECT_BRANDS = "SELECT ID, NAME FROM BRANDS";

    private static final String FIND_ALL = SELECT_BRANDS + " ORDER BY ID";

    private static final String FIND_BY_ID = SELECT_BRANDS + " WHERE ID = ?";

    private static final String INSERT = "INSERT INTO BRANDS (NAME) VALUES (?)";

    private static final String UPDATE = "UPDATE BRANDS SET NAME = ? WHERE ID = ?";

    private static final String DELETE = "DELETE FROM BRANDS WHERE ID = ?";

    @Override
    public List<Brand> listAll() {
        List<Brand> brands = new ArrayList<>();
        try (Connection connection = DatabaseConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(FIND_ALL);
                ResultSet result = statement.executeQuery()) {
            while (result.next()) {
                brands.add(mapRow(result));
            }
        } catch (SQLException e) {
            throw new PersistenceException("No se pudieron leer las marcas.", e);
        }
        return brands;
    }

    @Override
    public Brand findById(int id) {
        try (Connection connection = DatabaseConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(FIND_BY_ID)) {
            statement.setInt(1, id);
            try (ResultSet result = statement.executeQuery()) {
                return result.next() ? mapRow(result) : null;
            }
        } catch (SQLException e) {
            throw new PersistenceException("No se pudo buscar la marca.", e);
        }
    }

    /**
     * Guarda una marca nueva. El ID lo genera Oracle (IDENTITY) y se le
     * asigna al objeto recibido, igual que hace BrandFileDAO.
     */
    @Override
    public void save(Brand brand) {
        try (Connection connection = DatabaseConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(INSERT, new String[]{"ID"})) {
            statement.setString(1, brand.getName());
            statement.executeUpdate();

            try (ResultSet keys = statement.getGeneratedKeys()) {
                keys.next();
                brand.setId(keys.getInt(1));
            }
        } catch (SQLException e) {
            throw new PersistenceException("No se pudo guardar la marca.", e);
        }
    }

    @Override
    public void update(Brand brand) {
        try (Connection connection = DatabaseConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(UPDATE)) {
            statement.setString(1, brand.getName());
            statement.setInt(2, brand.getId());
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new PersistenceException("No se pudo actualizar la marca.", e);
        }
    }

    /**
     * Elimina la marca. Si algún componente la usa, Oracle rechaza el borrado
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
                    "No se pudo eliminar la marca. Revisa que ningún componente la esté usando.", e);
        }
    }

    /**
     * Convierte la fila actual del resultado en un objeto Brand.
     */
    private Brand mapRow(ResultSet result) throws SQLException {
        return new Brand(result.getInt("ID"), result.getString("NAME"));
    }
}