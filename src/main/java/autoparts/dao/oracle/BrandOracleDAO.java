package autoparts.dao.oracle;

import autoparts.dao.BrandDAO;
import autoparts.exception.PersistenceException;
import autoparts.model.Brand;
import autoparts.util.DatabaseConnection;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

/**
 * Guarda y consulta las marcas de repuestos llamando a las funciones del paquete PL/SQL
 * PKG_BRANDS (database/05a_paquetes_catalogo.sql).
 *
 * Todo es una función y el resultado llega por el primer ? de la llamada: un
 * cursor en las consultas, el ID nuevo en el INSERT y las filas cambiadas en el
 * UPDATE y el DELETE.
 *
 * Implementa la misma interfaz {@link BrandDAO} que BrandFileDAO, por eso
 * ComponentService no cambia. Sigue el patrón de {@link UserOracleDAO}.
 *
 * @author Álvaro Aramendiz
 */
public class BrandOracleDAO implements BrandDAO {

    private static final String CALL_LIST_ALL = "{ ? = call PKG_BRANDS.LIST_ALL }";
    private static final String CALL_FIND_BY_ID = "{ ? = call PKG_BRANDS.FIND_BY_ID(?) }";
    private static final String CALL_INSERT = "{ ? = call PKG_BRANDS.INSERT_BRAND(?) }";
    private static final String CALL_UPDATE = "{ ? = call PKG_BRANDS.UPDATE_BRAND(?, ?) }";
    private static final String CALL_DELETE = "{ ? = call PKG_BRANDS.DELETE_BRAND(?) }";

    @Override
    public List<Brand> listAll() {
        List<Brand> brands = new ArrayList<>();
        try (Connection connection = DatabaseConnection.getConnection();
                CallableStatement call = connection.prepareCall(CALL_LIST_ALL)) {
            call.registerOutParameter(1, Types.REF_CURSOR);
            call.execute();
            try (ResultSet result = (ResultSet) call.getObject(1)) {
                while (result.next()) {
                    brands.add(mapRow(result));
                }
            }
        } catch (SQLException e) {
            throw new PersistenceException("No se pudieron leer las marcas de repuestos.", e);
        }
        return brands;
    }

    @Override
    public Brand findById(int id) {
        try (Connection connection = DatabaseConnection.getConnection();
                CallableStatement call = connection.prepareCall(CALL_FIND_BY_ID)) {
            call.registerOutParameter(1, Types.REF_CURSOR);
            call.setInt(2, id);
            call.execute();
            try (ResultSet result = (ResultSet) call.getObject(1)) {
                return result.next() ? mapRow(result) : null;
            }
        } catch (SQLException e) {
            throw new PersistenceException("No se pudo buscar la marca.", e);
        }
    }

    /**
     * Guarda una marca nueva con PKG_BRANDS.INSERT_BRAND. El ID lo genera Oracle
     * (IDENTITY), la función lo devuelve y se le asigna al objeto recibido,
     * igual que hace BrandFileDAO.
     */
    @Override
    public void save(Brand brand) {
        try (Connection connection = DatabaseConnection.getConnection();
                CallableStatement call = connection.prepareCall(CALL_INSERT)) {
            call.registerOutParameter(1, Types.NUMERIC); // ID nuevo
            call.setString(2, brand.getName());
            call.execute();
            brand.setId(call.getInt(1));
        } catch (SQLException e) {
            throw new PersistenceException("No se pudo guardar la marca.", e);
        }
    }

    /**
     * Actualiza con PKG_BRANDS.UPDATE_BRAND. La función devuelve cuántas filas
     * cambió; si es 0, la marca no existe.
     */
    @Override
    public void update(Brand brand) {
        try (Connection connection = DatabaseConnection.getConnection();
                CallableStatement call = connection.prepareCall(CALL_UPDATE)) {
            call.registerOutParameter(1, Types.NUMERIC); // filas cambiadas
            call.setInt(2, brand.getId());
            call.setString(3, brand.getName());
            call.execute();
            if (call.getInt(1) == 0) {
                throw new PersistenceException("No existe la marca que se quiere actualizar.");
            }
        } catch (SQLException e) {
            throw new PersistenceException("No se pudo actualizar la marca.", e);
        }
    }

    /**
     * Elimina con PKG_BRANDS.DELETE_BRAND. Si algún componente la usa, Oracle
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
