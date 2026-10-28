package autoparts.service;

import autoparts.dao.UserDAO;
import autoparts.exception.InvalidDataException;
import autoparts.model.User;
import autoparts.util.PasswordHasher;
import java.time.LocalDate;

/**
 * Reglas de negocio de los usuarios: iniciar sesión y registrarse.
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
        if (password.length() < MIN_PASSWORD_LENGTH) {
            throw new InvalidDataException("La contraseña debe tener al menos "
                    + MIN_PASSWORD_LENGTH + " caracteres.");
        }
        if (!password.equals(confirmation)) {
            throw new InvalidDataException("Las contraseñas no coinciden.");
        }
        if (userDAO.findByEmail(email.trim()) != null) {
            throw new InvalidDataException("Ya existe una cuenta con ese correo.");
        }
        User user = new User(0, name.trim(), email.trim(), PasswordHasher.hash(password),
                User.ROLE_CLIENT, User.STATUS_ACTIVE, LocalDate.now(), 0);
        userDAO.save(user);
    }

    private boolean isBlank(String text) {
        return text == null || text.isBlank();
    }
}
