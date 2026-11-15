package autoparts;

import autoparts.dao.file.AffiliationFileDAO;
import autoparts.dao.file.BrandFileDAO;
import autoparts.dao.file.CategoryFileDAO;
import autoparts.dao.file.CompatibilityFileDAO;
import autoparts.dao.file.ComponentFileDAO;
import autoparts.dao.file.FeeFileDAO;
import autoparts.dao.file.InventoryFileDAO;
import autoparts.dao.file.PartnerFileDAO;
import autoparts.dao.file.VehicleFileDAO;
import autoparts.dao.oracle.UserOracleDAO;
import autoparts.service.AffiliationService;
import autoparts.service.ComparatorService;
import autoparts.service.ComponentService;
import autoparts.service.FeeService;
import autoparts.service.InventoryService;
import autoparts.service.PartnerService;
import autoparts.service.UserService;
import autoparts.service.VehicleService;

/**
 * Único lugar donde se crean los servicios con su DAO.
 *
 * Los controladores de JavaFX los obtienen de aquí. Para pasar un módulo a la
 * base de datos solo se cambia, en esta clase, su FileDAO por la versión de
 * Oracle (paquete dao.oracle). Los servicios no cambian.
 *
 * Ya usan Oracle: usuarios.
 *
 * @author Jahdiel Torres
 */
public class AppServices {

    private static final UserService USER_SERVICE = new UserService(new UserOracleDAO());
    private static final ComponentService COMPONENT_SERVICE = new ComponentService(
            new ComponentFileDAO(), new BrandFileDAO(), new CategoryFileDAO());
    private static final PartnerService PARTNER_SERVICE = new PartnerService(new PartnerFileDAO());
    private static final FeeService FEE_SERVICE = new FeeService(new FeeFileDAO());
    private static final AffiliationService AFFILIATION_SERVICE =
            new AffiliationService(new AffiliationFileDAO(), new PartnerFileDAO());
    private static final ComparatorService COMPARATOR_SERVICE =
            new ComparatorService(new InventoryFileDAO(), new PartnerFileDAO());
    private static final InventoryService INVENTORY_SERVICE =
            new InventoryService(new InventoryFileDAO(), new PartnerFileDAO());
    private static final VehicleService VEHICLE_SERVICE =
            new VehicleService(new VehicleFileDAO(), new CompatibilityFileDAO(), new ComponentFileDAO());

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

    public static InventoryService inventory() {
        return INVENTORY_SERVICE;
    }

    public static VehicleService vehicles() {
        return VEHICLE_SERVICE;
    }
}