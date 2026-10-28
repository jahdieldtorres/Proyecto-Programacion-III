package autoparts;

import autoparts.dao.file.UserFileDAO;
import autoparts.service.UserService;

/**
 * Único lugar donde se crean los servicios con su DAO.
 *
 * Los controladores de JavaFX los obtienen de aquí. Cuando se conecte la base
 * de datos, solo se cambia en esta clase UserFileDAO por la versión de Oracle.
 *
 * @author Jahdiel Torres
 */
public class AppServices {

    private static final UserService USER_SERVICE = new UserService(new UserFileDAO());

    public static UserService users() {
        return USER_SERVICE;
    }
}
