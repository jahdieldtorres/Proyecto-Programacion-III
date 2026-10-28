package autoparts;

import autoparts.dao.file.AffiliationFileDAO;
import autoparts.dao.file.ComponentFileDAO;
import autoparts.dao.file.FeeFileDAO;
import autoparts.dao.file.InventoryFileDAO;
import autoparts.dao.file.PartnerFileDAO;
import autoparts.dao.file.UserFileDAO;
import autoparts.service.AffiliationService;
import autoparts.service.ComparatorService;
import autoparts.service.ComponentService;
import autoparts.service.FeeService;
import autoparts.service.PartnerService;
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
    private static final PartnerService PARTNER_SERVICE = new PartnerService(new PartnerFileDAO());
    private static final FeeService FEE_SERVICE = new FeeService(new FeeFileDAO());
    private static final AffiliationService AFFILIATION_SERVICE =
            new AffiliationService(new AffiliationFileDAO(), new PartnerFileDAO());
    private static final ComparatorService COMPARATOR_SERVICE =
            new ComparatorService(new InventoryFileDAO(), new PartnerFileDAO());

    public static UserService users() {
        return USER_SERVICE;
    }

    public static ComponentService components() {
        return COMPONENT_SERVICE;
    }

    public static PartnerService partners() {
        return PARTNER_SERVICE;
    }

    public static FeeService fees() {
        return FEE_SERVICE;
    }

    public static AffiliationService affiliations() {
        return AFFILIATION_SERVICE;
    }

    public static ComparatorService comparator() {
        return COMPARATOR_SERVICE;
    }
}
