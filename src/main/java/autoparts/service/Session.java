package autoparts.service;

import autoparts.model.Cart;
import autoparts.model.User;
import autoparts.model.Vehicle;

/**
 * Guarda el usuario que inici¢ sesi¢n, para que cualquier pantalla sepa
 * qui‚n es y si es administrador. Tambi‚n guarda el carrito de compras de ese
 * usuario y el veh¡culo que eligi¢ para buscar repuestos.
 *
 * Adem s puede ser una sesi¢n de invitado: alguien que entra sin cuenta solo
 * para mirar. El invitado vive en memoria (no se crea nada en Oracle) y no
 * tiene usuario: {@link #getUser()} devuelve null.
 *
 * @author Jahdiel Torres
 * @author H‚ctor Guevara (modo invitado)
 */
public class Session {

    private static User currentUser;

    /** True si la sesi¢n es de invitado (entr¢ con "Explorar como invitado"). */
    private static boolean guest;

    /** Carrito del cliente. Se vac¡a al iniciar y al cerrar sesi¢n. */
    private static Cart cart = new Cart();

    /** Veh¡culo elegido en Inicio para ver solo lo compatible. Null si no eligi¢. */
    private static Vehicle vehicle;

    public static void start(User user) {
        currentUser = user;
        guest = false;
        cart = new Cart();
        vehicle = null;
    }

    /**
     * Abre una sesi¢n de invitado: se puede mirar el cat logo, los servicios y
     * el mapa, pero no comprar. No hay usuario ni se guarda nada en Oracle.
     */
    public static void startGuest() {
        currentUser = null;
        guest = true;
        cart = new Cart();
        vehicle = null;
    }

    /**
     * Indica si la sesi¢n actual es de invitado.
     */
    public static boolean isGuest() {
        return guest;
    }

    /**
     * Indica si quien est  usando la app tiene una cuenta con sesi¢n iniciada.
     * La pantalla que lo necesita (agregar al carrito, agendar una cita)
     * pregunta aqu¡ y, si devuelve false, abre la ventana "Reg¡strate".
     *
     * @return true si hay un usuario registrado; false si es invitado o no hay sesi¢n
     */
    public static boolean requireAccount() {
        return currentUser != null && !guest;
    }

    public static User getUser() {
        return currentUser;
    }

    /**
     * Indica si hay una sesi¢n abierta, ya sea de un usuario o de un invitado.
     */
    public static boolean isActive() {
        return currentUser != null || guest;
    }

    public static void close() {
        currentUser = null;
        guest = false;
        cart = new Cart();
        vehicle = null;
    }

    /**
     * Carrito de compras de quien inici¢ sesi¢n. Siempre existe (puede estar vac¡o)
     * y es nuevo en cada inicio y cierre de sesi¢n.
     */
    public static Cart getCart() {
        return cart;
    }

    /**
     * Veh¡culo que el cliente eligi¢ para buscar repuestos, o null si no eligi¢.
     */
    public static Vehicle getVehicle() {
        return vehicle;
    }

    /**
     * Guarda el veh¡culo elegido. Con null se deja de filtrar por veh¡culo.
     */
    public static void setVehicle(Vehicle selected) {
        vehicle = selected;
    }

    /**
     * Indica si quien inici¢ sesi¢n es administrador. Si no hay sesi¢n, devuelve false.
     */
    public static boolean isAdmin() {
        return currentUser != null && currentUser.isAdmin();
    }
}
