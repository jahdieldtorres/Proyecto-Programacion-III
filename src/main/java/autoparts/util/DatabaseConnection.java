package autoparts.util;

import autoparts.exception.PersistenceException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import oracle.ucp.jdbc.PoolDataSource;
import oracle.ucp.jdbc.PoolDataSourceFactory;

/**
 * Abre las conexiones a la base de datos Oracle del proyecto.
 *
 * Los datos de conexión (URL, usuario y clave) se leen del archivo
 * db.properties. Se busca primero en la raíz del proyecto (carpeta de
 * trabajo) y, si no está ahí, dentro de la propia aplicación
 * (src/main/resources/db.properties). Ese archivo no se sube a GitHub: cada
 * integrante lo crea a partir de db.properties.example con sus datos.
 *
 * El wallet de Oracle también puede ir dentro de la aplicación, en
 * src/main/resources/wallet/. Si está, al conectarse se copia a una carpeta
 * temporal y se arma la URL con esa ruta, así que nadie tiene que copiar
 * archivos en su PC (ni en el PC donde se abre el .exe). Si no hay wallet en
 * los recursos, se usa db.url tal como está en db.properties.
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
 * @author Álvaro Aramendiz
 */
public final class DatabaseConnection {

    /** Archivo de configuración, relativo a la raíz del proyecto. */
    public static final String CONFIG_FILE = "db.properties";

    private static final String KEY_URL = "db.url";
    private static final String KEY_USER = "db.user";
    private static final String KEY_PASSWORD = "db.password";

    /** Ruta de db.properties dentro de los recursos de la aplicación. */
    private static final String CONFIG_RESOURCE = "/" + CONFIG_FILE;

    /** Carpeta de los recursos donde va el wallet de Oracle. */
    private static final String WALLET_RESOURCE_FOLDER = "/wallet/";

    /** Archivo que indica que el wallet sí viene dentro de la aplicación. */
    private static final String WALLET_MARKER_FILE = "tnsnames.ora";

    /** Alias de conexión que se usa si no se puede leer de db.url. */
    private static final String DEFAULT_ALIAS = "origen_tp";

    /** Saca el alias de una URL como jdbc:oracle:thin:@origen_tp?TNS_ADMIN=... */
    private static final Pattern ALIAS_PATTERN = Pattern.compile("@([\\w.-]+)(?:\\?|$)");

    /** Archivos del wallet que se copian a la carpeta temporal. */
    private static final String[] WALLET_FILES = {
        "cwallet.sso", "ewallet.p12", "tnsnames.ora", "sqlnet.ora",
        "ojdbc.properties", "keystore.jks", "truststore.jks"
    };

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
            newPool.setURL(resolveUrl(settings));
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
     * Decide qué URL usar: la del wallet incluido en la aplicación o, si no
     * hay wallet en los recursos, la que dice db.properties.
     */
    private static String resolveUrl(Properties settings) {
        if (!hasEmbeddedWallet()) {
            // No hay wallet en los recursos: se conecta como siempre, con db.url
            requireValue(settings, KEY_URL);
            return settings.getProperty(KEY_URL);
        }
        try {
            return walletUrl(extractAlias(settings.getProperty(KEY_URL)));
        } catch (IOException e) {
            throw new PersistenceException("No se pudo preparar el wallet incluido en la aplicación.", e);
        }
    }

    /**
     * Indica si la aplicación trae el wallet de Oracle en sus recursos.
     */
    private static boolean hasEmbeddedWallet() {
        return DatabaseConnection.class.getResource(WALLET_RESOURCE_FOLDER + WALLET_MARKER_FILE) != null;
    }

    /**
     * Saca el alias de conexión (por ejemplo origen_tp) de db.url. Si db.url
     * no existe o no tiene ese formato, usa el alias por defecto.
     */
    private static String extractAlias(String url) {
        if (url != null) {
            Matcher matcher = ALIAS_PATTERN.matcher(url);
            if (matcher.find()) {
                return matcher.group(1);
            }
        }
        return DEFAULT_ALIAS;
    }

    /**
     * Copia el wallet de los recursos a una carpeta temporal y arma la URL
     * de conexión con esa ruta.
     *
     * La carpeta y sus archivos se borran cuando la aplicación se cierra.
     *
     * @param alias alias del servicio, tal como está en tnsnames.ora
     * @return URL JDBC que apunta al wallet temporal
     * @throws IOException si no se puede crear la carpeta o copiar un archivo
     */
    private static String walletUrl(String alias) throws IOException {
        Path folder = Files.createTempDirectory("origen-wallet");
        // deleteOnExit borra en orden inverso: primero los archivos y al final la carpeta
        folder.toFile().deleteOnExit();
        for (String name : WALLET_FILES) {
            try (InputStream input = DatabaseConnection.class
                    .getResourceAsStream(WALLET_RESOURCE_FOLDER + name)) {
                if (input != null) {
                    Path target = folder.resolve(name);
                    Files.copy(input, target);
                    target.toFile().deleteOnExit();
                }
            }
        }
        // El driver espera / en la ruta, incluso en Windows
        String path = folder.toAbsolutePath().toString().replace('\\', '/');
        return "jdbc:oracle:thin:@" + alias + "?TNS_ADMIN=" + path;
    }

    /**
     * Lee db.properties la primera vez y revisa que tenga el usuario y la clave.
     *
     * Busca el archivo en la carpeta del proyecto; si no está, lo busca dentro
     * de los recursos de la aplicación (así funciona también en el .exe). La
     * URL (db.url) se revisa después, solo cuando hace falta.
     */
    private static Properties loadConfig() {
        if (config == null) {
            Properties settings = new Properties();
            Path file = Path.of(CONFIG_FILE);
            try {
                if (Files.exists(file)) {
                    try (InputStream input = Files.newInputStream(file)) {
                        settings.load(input);
                    }
                } else {
                    try (InputStream input = DatabaseConnection.class.getResourceAsStream(CONFIG_RESOURCE)) {
                        if (input == null) {
                            throw new PersistenceException("No se encontró el archivo " + CONFIG_FILE
                                    + " en la carpeta del proyecto ni dentro de la aplicación. "
                                    + "Cópialo de db.properties.example y escribe tus datos.");
                        }
                        settings.load(input);
                    }
                }
            } catch (IOException e) {
                throw new PersistenceException("No se pudo leer el archivo " + CONFIG_FILE + ".", e);
            }
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