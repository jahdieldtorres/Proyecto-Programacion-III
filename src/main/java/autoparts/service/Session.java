package autoparts.service;

import autoparts.model.Cart;
import autoparts.model.User;

/**
 * Guarda el usuario que inició sesión, para que cualquier pantalla sepa
 * quién es y si es administrador. También guarda el carrito de compras de ese
 * usuario.
 *
 * @author Jahdiel Torres
 */
public class Session {

    private static User currentUser;

    /** Carrito del cliente. Se vacía al iniciar y al cerrar sesión. */
    private static Cart cart = new Cart();

    public static void start(User user) {
        currentUser = user;
        cart = new Cart();
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
    }

    /**
     * Carrito de compras de quien inició sesión. Siempre existe (puede estar vacío)
     * y es nuevo en cada inicio y cierre de sesión.
     */
    public static Cart getCart() {
        return cart;
    }

    /**
     * Indica si quien inició sesión es administrador. Si no hay sesión, devuelve false.
     */
    public static boolean isAdmin() {
        return currentUser != null && currentUser.isAdmin();
    }
}
