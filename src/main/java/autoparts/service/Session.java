package autoparts.service;

import autoparts.model.User;

/**
 * Guarda el usuario que inició sesión, para que cualquier pantalla sepa
 * quién es y qué rol tiene.
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
     * Indica si el usuario de la sesión tiene el rol indicado.
     *
     * @param role uno de User.ROLE_CLIENT, User.ROLE_PARTNER o User.ROLE_ADMIN
     */
    public static boolean hasRole(String role) {
        return currentUser != null && currentUser.getRole().equals(role);
    }

    /**
     * Atajo para preguntar si quien inició sesión es administrador.
     */
    public static boolean isAdmin() {
        return hasRole(User.ROLE_ADMIN);
    }
}
