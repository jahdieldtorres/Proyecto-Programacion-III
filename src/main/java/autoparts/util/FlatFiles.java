package autopartes.util;

/**
 * Rutas de todos los archivos planos de la carpeta {@code data/}.
 *
 * <p>Es el único lugar donde se escriben rutas: ningún DAO debe usar
 * cadenas con rutas directamente. Las rutas son relativas a la raíz del
 * proyecto, que es el directorio de trabajo cuando se ejecuta
 * {@code mvn clean javafx:run}.</p>
 *
 * <p>Los nombres de los archivos siguen la convención del proyecto: plural,
 * minúsculas y extensión {@code .csv}. El formato de cada uno está en
 * {@code docs/formatos-archivos.md}.</p>
 */
public final class FlatFiles {

    /** Carpeta que contiene todos los archivos de datos. */
    public static final String DATA_DIR = "data";

    // Ubicación geográfica
    public static final String DEPARTMENTS = DATA_DIR + "/departaments.csv";
    public static final String MUNICIPALITIES = DATA_DIR + "/municipalities.csv";
    public static final String NEIGHBORHOODS = DATA_DIR + "/neighborhoods.csv";

    // Usuarios
    public static final String USERS = DATA_DIR + "/users.csv";
    public static final String USER_ADDRESSES = DATA_DIR + "/addresses_user.csv";

    // Socios (proveedores y talleres)
    public static final String PARTNERS = DATA_DIR + "/partners.csv";
    public static final String SERVICES = DATA_DIR + "/services.csv";
    public static final String FEES = DATA_DIR + "/fees.csv";
    public static final String AFFILIATIONS = DATA_DIR + "/affiliations.csv";

    // Catálogo
    public static final String BRANDS = DATA_DIR + "/brands.csv";
    public static final String CATEGORIES = DATA_DIR + "/categories.csv";
    public static final String COMPONENTS = DATA_DIR + "/components.csv";
    public static final String VEHICLES = DATA_DIR + "/vehicles.csv";
    public static final String COMPATIBILITY = DATA_DIR + "/compatibility.csv";

    // Inventario y transacciones
    public static final String INVENTORY = DATA_DIR + "/inventory.csv";
    public static final String TRANSACTIONS = DATA_DIR + "/transactions.csv";
    public static final String TRANSACTION_DETAILS = DATA_DIR + "/details_transaction.csv";

    /** Clase de constantes: no se instancia. */
    private FlatFiles() {
    }
}
