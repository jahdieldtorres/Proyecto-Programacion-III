package autoparts.service;

import autoparts.model.Cart;
import autoparts.model.User;
import autoparts.model.Vehicle;

/**
 * Guarda el usuario que inició sesión, para que cualquier pantalla sepa
 * quién es y si es administrador. También guarda el carrito de compras de ese
 * usuario y el vehículo que eligió para buscar repuestos.
 *
 * Además puede ser una sesión de invitado: alguien que entra sin cuenta solo
 * para mirar. El invitado vive en memoria (no se crea nada en Oracle) y no
 * tiene usuario: {@link #getUser()} devuelve null.
 *
 * @author Jahdiel Torres
 * @author Héctor Guevara (modo invitado)
 */
public class Session {

    private static User currentUser;

    /** True si la sesión es de invitado (entró con "Explorar como invitado"). */
    private static boolean guest;

    /** Carrito del cliente. Se vacía al iniciar y al cerrar sesión. */
    private static Cart cart = new Cart();

    /** Vehículo elegido en Inicio para ver solo lo compatible. Null si no eligió. */
    private static Vehicle vehicle;

    public static void start(User user) {
        currentUser = user;
        guest = false;
        cart = new Cart();
        vehicle = null;
    }

    /**
     * Abre una sesión de invitado: se puede mirar el catálogo, los servicios y
     * el mapa, pero no comprar. No hay usuario ni se guarda nada en Oracle.
     */
    public static void startGuest() {
        currentUser = null;
        guest = true;
        cart = new Cart();
        vehicle = null;
    }

    /**
     * Indica si la sesión actual es de invitado.
     */
    public static boolean isGuest() {
        return guest;
    }

    /**
     * Indica si quien está usando la app tiene una cuenta con sesión iniciada.
     * La pantalla que lo necesita (agregar al carrito, agendar una cita)
     * pregunta aquí y, si devuelve false, abre la ventana "Regístrate".
     *
     * @return true si hay un usuario registrado; false si es invitado o no hay sesión
     */
    public static boolean requireAccount() {
        return currentUser != null && !guest;
    }

    public static User getUser() {
        return currentUser;
    }

    /**
     * Indica si hay una sesión abierta, ya sea de un usuario o de un invitado.
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
     * Carrito de compras de quien inició sesión. Siempre existe (puede estar vacío)
     * y es nuevo en cada inicio y cierre de sesión.
     */
    public static Cart getCart() {
        return cart;
    }

    /**
     * Vehículo que el cliente eligió para buscar repuestos, o null si no eligió.
     */
    public static Vehicle getVehicle() {
        return vehicle;
    }

    /**
     * Guarda el vehículo elegido. Con null se deja de filtrar por vehículo.
     */
    public static void setVehicle(Vehicle selected) {
        vehicle = selected;
    }

    /**
     * Indica si quien inició sesión es administrador. Si no hay sesión, devuelve false.
     */
    public static boolean isAdmin() {
        return currentUser != null && currentUser.isAdmin();
    }
}
