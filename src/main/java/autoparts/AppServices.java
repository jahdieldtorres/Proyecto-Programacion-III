package autoparts;

import autoparts.dao.file.ComponentFileDAO;
import autoparts.dao.file.UserFileDAO;
import autoparts.service.ComponentService;
import autoparts.service.UserService;

/**
 * Único lugar donde se crean los servicios con su DAO.
 *
 * Los controladores de JavaFX los obtienen de aquí. Cuando se conecte la base
 * de datos, solo se cambian en esta clase los FileDAO por la versión de Oracle.
 *
 * @author Jahdiel Torres
 */
public class AppServices {

    private static final UserService USER_SERVICE = new UserService(new UserFileDAO());
    private static final ComponentService COMPONENT_SERVICE = new ComponentService(new ComponentFileDAO());

    public static UserService users() {
        return USER_SERVICE;
    }

    public static ComponentService components() {
        return COMPONENT_SERVICE;
    }
}
