package autoparts.service;

import autoparts.dao.UserDAO;
import autoparts.exception.InvalidDataException;
import autoparts.model.User;
import autoparts.util.PasswordHasher;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Reglas de negocio de los usuarios: iniciar sesión, registrarse, cambiar los
 * datos propios y, para el administrador, gestionar roles y estados.
 *
 * Recibe el DAO por constructor y depende solo de la interfaz UserDAO.
 *
 * @author Jahdiel Torres
 */
public class UserService {

    private static final int MIN_PASSWORD_LENGTH = 8;

    private final UserDAO userDAO;

    public UserService(UserDAO userDAO) {
        this.userDAO = userDAO;
    }

    /**
     * Valida las credenciales y, si son correctas, inicia la sesión.
     *
     * @return el usuario que inició sesión
     * @throws InvalidDataException si faltan datos o las credenciales no son válidas
     */
    public User login(String email, String password) {
        if (isBlank(email) || isBlank(password)) {
            throw new InvalidDataException("Ingresa tu correo y tu contraseña.");
        }
        User user = userDAO.findByEmail(email.trim());
        if (user == null || !user.getPasswordHash().equals(PasswordHasher.hash(password))) {
            throw new InvalidDataException("Correo o contraseña incorrectos.");
        }
        if (!user.isActive()) {
            throw new InvalidDataException("Tu cuenta está inactiva. Contacta al administrador.");
        }
        Session.start(user);
        return user;
    }

    /**
     * Registra un cliente nuevo después de validar sus datos.
     *
     * @throws InvalidDataException si algún dato no es válido o el correo ya existe
     */
    public void register(String name, String email, String password, String confirmation) {
        if (isBlank(name) || isBlank(email) || isBlank(password)) {
            throw new InvalidDataException("Completa todos los campos.");
        }
        if (!email.contains("@") || !email.contains(".")) {
            throw new InvalidDataException("El correo no es válido.");
        }
        validateNewPassword(password, confirmation);
        if (userDAO.findByEmail(email.trim()) != null) {
            throw new InvalidDataException("Ya existe una cuenta con ese correo.");
        }
        User user = new User(0, name.trim(), email.trim(), PasswordHasher.hash(password),
                User.ROLE_CLIENT, User.STATUS_ACTIVE, LocalDate.now(), 0);
        userDAO.save(user);
    }

    /**
     * Cambia la contraseña del usuario después de comprobar la actual.
     *
     * @throws InvalidDataException si la actual no es correcta o la nueva no es válida
     */
    public void changePassword(User user, String current, String newPassword, String confirmation) {
        if (isBlank(current) || isBlank(newPassword) || isBlank(confirmation)) {
            throw new InvalidDataException("Completa los tres campos.");
        }
        if (!PasswordHasher.hash(current).equals(user.getPasswordHash())) {
            throw new InvalidDataException("La contraseña actual no es correcta.");
        }
        if (current.equals(newPassword)) {
            throw new InvalidDataException("La nueva contraseña debe ser distinta de la actual.");
        }
        validateNewPassword(newPassword, confirmation);
        user.setPasswordHash(PasswordHasher.hash(newPassword));
        userDAO.update(user);
    }

    /**
     * Cambia el nombre que se muestra del usuario.
     *
     * @throws InvalidDataException si el nombre está vacío
     */
    public void updateName(User user, String name) {
        if (isBlank(name)) {
            throw new InvalidDataException("El nombre no puede estar vacío.");
        }
        user.setName(name.trim());
        userDAO.update(user);
    }

    /**
     * Devuelve los usuarios cuyo nombre o correo contienen el texto (todos si
     * está vacío) y que tienen el rol indicado (todos si el rol es null).
     */
    public List<User> search(String text, String role) {
        String query = text == null ? "" : text.trim().toLowerCase();
        List<User> result = new ArrayList<>();
        for (User user : userDAO.findAll()) {
            boolean matchesText = user.getName().toLowerCase().contains(query)
                    || user.getEmail().toLowerCase().contains(query);
            boolean matchesRole = role == null || role.equals(user.getRole());
            if (matchesText && matchesRole) {
                result.add(user);
            }
        }
        return result;
    }

    /**
     * Cambia el rol de un usuario. Solo lo usa el administrador.
     *
     * @throws InvalidDataException si el usuario no existe, el rol no es válido
     *                              o el administrador intenta cambiar su propio rol
     */
    public void changeRole(int userId, String role) {
        if (!User.ROLE_CLIENT.equals(role) && !User.ROLE_PARTNER.equals(role)
                && !User.ROLE_ADMIN.equals(role)) {
            throw new InvalidDataException("Elige un rol válido.");
        }
        User user = findExisting(userId);
        checkNotCurrentUser(user, "No puedes cambiar tu propio rol.");
        user.setRole(role);
        userDAO.update(user);
    }

    /**
     * Activa o desactiva un usuario (borrado lógico). Solo lo usa el administrador.
     *
     * @throws InvalidDataException si el usuario no existe o es el mismo administrador
     */
    public void setActive(int userId, boolean active) {
        User user = findExisting(userId);
        checkNotCurrentUser(user, "No puedes desactivar tu propia cuenta.");
        user.setStatus(active ? User.STATUS_ACTIVE : User.STATUS_INACTIVE);
        userDAO.update(user);
    }

    private User findExisting(int userId) {
        User user = userDAO.findById(userId);
        if (user == null) {
            throw new InvalidDataException("El usuario no existe.");
        }
        return user;
    }

    private void checkNotCurrentUser(User user, String message) {
        User current = Session.getUser();
        if (current != null && current.getId() == user.getId()) {
            throw new InvalidDataException(message);
        }
    }

    private void validateNewPassword(String password, String confirmation) {
        if (password.length() < MIN_PASSWORD_LENGTH) {
            throw new InvalidDataException("La contraseña debe tener al menos "
                    + MIN_PASSWORD_LENGTH + " caracteres.");
        }
        if (!password.equals(confirmation)) {
            throw new InvalidDataException("Las contraseñas no coinciden.");
        }
    }

    private boolean isBlank(String text) {
        return text == null || text.isBlank();
    }
}
