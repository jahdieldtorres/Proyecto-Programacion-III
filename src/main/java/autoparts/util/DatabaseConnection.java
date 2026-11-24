package autoparts.util;

import autoparts.exception.PersistenceException;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;
import oracle.ucp.jdbc.PoolDataSource;
import oracle.ucp.jdbc.PoolDataSourceFactory;

/**
 * Abre las conexiones a la base de datos Oracle del proyecto.
 *
 * Los datos de conexión (URL, usuario y clave) se leen del archivo
 * db.properties, en la raíz del proyecto. Ese archivo no se sube a GitHub:
 * cada integrante lo crea a partir de db.properties.example con sus datos.
 *
 * Usa un pool de conexiones: la primera vez abre unas pocas conexiones y
 * después las reutiliza. Cuando un DAO cierra su conexión, en realidad la
 * devuelve al pool para que la use otro. Así no se abre una conexión nueva a
 * Oracle Cloud en cada consulta, que era lo que hacía lenta la app.
 *
 * Quien pide una conexión la cierra igual que antes, con try-with-resources:
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

    /** Conexiones que el pool deja abiertas y listas desde el inicio. */
    private static final int MIN_CONNECTIONS = 2;
    /** Máximo de conexiones abiertas al mismo tiempo. */
    private static final int MAX_CONNECTIONS = 10;

    /** Configuración leída del archivo; se carga una sola vez. */
    private static Properties config;

    /** El pool se crea una sola vez, la primera vez que se pide una conexión. */
    private static PoolDataSource pool;

    private DatabaseConnection() {
    }

    /**
     * Entrega una conexión del pool, siempre con autocommit activado.
     *
     * Las conexiones se reutilizan: si un DAO apagó el autocommit para una
     * transacción (por ejemplo, guardar un pedido con sus líneas), la conexión
     * vuelve al pool así. Por eso se activa de nuevo aquí, para que el
     * siguiente DAO no deje sus cambios sin confirmar.
     *
     * @return conexión lista para usar; al cerrarla vuelve al pool
     * @throws PersistenceException si falta la configuración o no se puede conectar
     */
    public static synchronized Connection getConnection() {
        try {
            Connection connection = getPool().getConnection();
            connection.setAutoCommit(true);
            return connection;
        } catch (SQLException e) {
            throw new PersistenceException("No se pudo conectar a la base de datos. "
                    + "Revisa los datos de " + CONFIG_FILE + " y tu conexión a internet.", e);
        }
    }

    /**
     * Crea el pool la primera vez, con los datos de db.properties.
     */
    private static PoolDataSource getPool() throws SQLException {
        if (pool == null) {
            Properties settings = loadConfig();
            PoolDataSource newPool = PoolDataSourceFactory.getPoolDataSource();
            newPool.setConnectionFactoryClassName("oracle.jdbc.pool.OracleDataSource");
            newPool.setURL(settings.getProperty(KEY_URL));
            newPool.setUser(settings.getProperty(KEY_USER));
            newPool.setPassword(settings.getProperty(KEY_PASSWORD));
            newPool.setInitialPoolSize(MIN_CONNECTIONS);
            newPool.setMinPoolSize(MIN_CONNECTIONS);
            newPool.setMaxPoolSize(MAX_CONNECTIONS);
            pool = newPool;
        }
        return pool;
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
