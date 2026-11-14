package autoparts.util;

import autoparts.exception.PersistenceException;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Abre las conexiones a la base de datos Oracle del proyecto.
 *
 * Los datos de conexión (URL, usuario y clave) se leen del archivo
 * db.properties, en la raíz del proyecto. Ese archivo no se sube a GitHub:
 * cada integrante lo crea a partir de db.properties.example con sus datos.
 *
 * Cada llamada a getConnection() abre una conexión nueva. Quien la pide debe
 * cerrarla, idealmente con try-with-resources:
 * <pre>
 * try (Connection connection = DatabaseConnection.getConnection()) {
 *     ...
 * }
 * </pre>
 *
 * @author Jahdiel Torres
 */
public final class DatabaseConnection {

    /** Archivo de configuración, relativo a la raíz del proyecto. */
    public static final String CONFIG_FILE = "db.properties";

    private static final String KEY_URL = "db.url";
    private static final String KEY_USER = "db.user";
    private static final String KEY_PASSWORD = "db.password";

    /** Configuración leída del archivo; se carga una sola vez. */
    private static Properties config;

    private DatabaseConnection() {
    }

    /**
     * Abre una conexión nueva con los datos de db.properties.
     *
     * @return conexión abierta; quien la usa debe cerrarla
     * @throws PersistenceException si falta la configuración o no se puede conectar
     */
    public static Connection getConnection() {
        Properties settings = loadConfig();
        try {
            return DriverManager.getConnection(settings.getProperty(KEY_URL),
                    settings.getProperty(KEY_USER), settings.getProperty(KEY_PASSWORD));
        } catch (SQLException e) {
            throw new PersistenceException("No se pudo conectar a la base de datos. "
                    + "Revisa los datos de " + CONFIG_FILE + " y tu conexión a internet.", e);
        }
    }

    /**
     * Lee db.properties la primera vez y revisa que tenga los tres datos.
     */
    private static Properties loadConfig() {
        if (config == null) {
            Properties settings = new Properties();
            try (InputStream input = new FileInputStream(CONFIG_FILE)) {
                settings.load(input);
            } catch (IOException e) {
                throw new PersistenceException("No se encontró el archivo " + CONFIG_FILE
                        + " en la carpeta del proyecto. Cópialo de db.properties.example "
                        + "y escribe tus datos.", e);
            }
            requireValue(settings, KEY_URL);
            requireValue(settings, KEY_USER);
            requireValue(settings, KEY_PASSWORD);
            config = settings;
        }
        return config;
    }

    private static void requireValue(Properties settings, String key) {
        String value = settings.getProperty(key);
        if (value == null || value.isBlank()) {
            throw new PersistenceException("Falta el valor de " + key + " en " + CONFIG_FILE + ".");
        }
    }
}
