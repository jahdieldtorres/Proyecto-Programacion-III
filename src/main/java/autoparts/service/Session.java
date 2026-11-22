package autoparts.service;

import autoparts.model.Cart;
import autoparts.model.User;
import autoparts.model.Vehicle;

/**
 * Guarda el usuario que inició sesión, para que cualquier pantalla sepa
 * quién es y si es administrador. También guarda el carrito de compras de ese
 * usuario y el vehículo que eligió para buscar repuestos.
 *
 * @author Jahdiel Torres
 */
public class Session {

    private static User currentUser;

    /** Carrito del cliente. Se vacía al iniciar y al cerrar sesión. */
    private static Cart cart = new Cart();

    /** Vehículo elegido en Inicio para ver solo lo compatible. Null si no eligió. */
    private static Vehicle vehicle;

    public static void start(User user) {
        currentUser = user;
        cart = new Cart();
        vehicle = null;
    }

    public static User getUser() {
        return currentUser;
    }

    public static boolean isActive() {
        return currentUser != null;
    }

    public static void close() {
        currentUser = null;
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
