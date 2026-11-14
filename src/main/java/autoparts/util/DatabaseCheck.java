package autoparts.util;

import autoparts.exception.PersistenceException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Prueba rápida de la conexión a la base de datos. No es parte de la
 * aplicación: se ejecuta aparte (clic derecho → Run File) para comprobar que
 * db.properties y el wallet están bien configurados en cada PC.
 *
 * @author Jahdiel Torres
 */
public class DatabaseCheck {

    public static void main(String[] args) {
        try (Connection connection = DatabaseConnection.getConnection();
                Statement statement = connection.createStatement();
                ResultSet result = statement.executeQuery("SELECT USER FROM DUAL")) {
            result.next();
            System.out.println("Conexión correcta. Usuario conectado: " + result.getString(1));
        } catch (PersistenceException e) {
            System.out.println(e.getMessage());
            System.out.println("Detalle: " + e.getCause());
        } catch (SQLException e) {
            System.out.println("La conexión abrió, pero falló la consulta: " + e.getMessage());
        }
    }
}
