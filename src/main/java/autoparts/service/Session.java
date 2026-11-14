package autoparts.service;

import autoparts.model.User;

/**
 * Guarda el usuario que inició sesión, para que cualquier pantalla sepa
 * quién es y si es administrador.
 *
 * @author Jahdiel Torres
 */
public class Session {

    private static User currentUser;

    public static void start(User user) {
        currentUser = user;
    }

    public static User getUser() {
        return currentUser;
    }

    public static boolean isActive() {
        return currentUser != null;
    }

    public static void close() {
        currentUser = null;
    }

    /**
     * Indica si quien inició sesión es administrador. Si no hay sesión, devuelve false.
     */
    public static boolean isAdmin() {
        return currentUser != null && currentUser.isAdmin();
    }
}
