package autoparts;

import autoparts.dao.local.UserLocationPreferencesDAO;
import autoparts.dao.oracle.AffiliationOracleDAO;
import autoparts.dao.oracle.AppointmentOracleDAO;
import autoparts.dao.oracle.BrandOracleDAO;
import autoparts.dao.oracle.CategoryOracleDAO;
import autoparts.dao.oracle.CompatibilityOracleDAO;
import autoparts.dao.oracle.ComponentImageOracleDAO;
import autoparts.dao.oracle.ComponentOracleDAO;
import autoparts.dao.oracle.FeeOracleDAO;
import autoparts.dao.oracle.InventoryOracleDAO;
import autoparts.dao.oracle.PartnerOracleDAO;
import autoparts.dao.oracle.UserOracleDAO;
import autoparts.dao.oracle.VehicleOracleDAO;
import autoparts.dao.oracle.WorkshopServiceOracleDAO;
import autoparts.service.AffiliationService;
import autoparts.service.AppointmentService;
import autoparts.service.CatalogService;
import autoparts.service.ComparatorService;
import autoparts.service.ComponentImageService;
import autoparts.service.ComponentService;
import autoparts.service.FeeService;
import autoparts.service.InventoryService;
import autoparts.service.PartnerService;
import autoparts.service.ServiceCatalogService;
import autoparts.service.UserLocationService;
import autoparts.service.UserService;
import autoparts.service.VehicleService;

/**
 * Único lugar donde se crean los servicios con su DAO.
 *
 * Los controladores de JavaFX los obtienen de aquí. Cada servicio recibe sus
 * DAO por el constructor (inyección de dependencias), así que para cambiar de
 * persistencia solo se cambia esta clase. Los servicios no cambian.
 *
 * Todos los módulos usan Oracle (paquete dao.oracle). Las versiones con
 * archivos (paquete dao.file) se conservan como respaldo. La ubicación de
 * cada cliente se guarda en el computador (paquete dao.local).
 *
 * @author Jahdiel Torres
 */
public class AppServices {

    private static final UserService USER_SERVICE = new UserService(new UserOracleDAO());
    private static final ComponentService COMPONENT_SERVICE = new ComponentService(
            new ComponentOracleDAO(), new BrandOracleDAO(), new CategoryOracleDAO());
    private static final PartnerService PARTNER_SERVICE = new PartnerService(new PartnerOracleDAO());
    private static final FeeService FEE_SERVICE = new FeeService(new FeeOracleDAO());
    private static final AffiliationService AFFILIATION_SERVICE =
            new AffiliationService(new AffiliationOracleDAO(), new PartnerOracleDAO());
    private static final ComparatorService COMPARATOR_SERVICE =
            new ComparatorService(new InventoryOracleDAO(), new PartnerOracleDAO());
    private static final InventoryService INVENTORY_SERVICE =
            new InventoryService(new InventoryOracleDAO(), new PartnerOracleDAO());
    private static final VehicleService VEHICLE_SERVICE = new VehicleService(
            new VehicleOracleDAO(), new CompatibilityOracleDAO(), new ComponentOracleDAO());
    private static final ComponentImageService IMAGE_SERVICE =
            new ComponentImageService(new ComponentImageOracleDAO());
    private static final CatalogService CATALOG_SERVICE = new CatalogService(
            COMPONENT_SERVICE, IMAGE_SERVICE, new InventoryOracleDAO(), new CompatibilityOracleDAO());
    private static final UserLocationService LOCATION_SERVICE =
            new UserLocationService(new UserLocationPreferencesDAO());
    private static final ServiceCatalogService SERVICE_CATALOG_SERVICE =
            new ServiceCatalogService(new WorkshopServiceOracleDAO());
    private static final AppointmentService APPOINTMENT_SERVICE =
            new AppointmentService(new AppointmentOracleDAO(), new WorkshopServiceOracleDAO());

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

    public static ComponentImageService images() {
        return IMAGE_SERVICE;
    }

    public static CatalogService catalog() {
        return CATALOG_SERVICE;
    }

    public static UserLocationService locations() {
        return LOCATION_SERVICE;
    }

    public static ServiceCatalogService serviceCatalog() {
        return SERVICE_CATALOG_SERVICE;
    }

    public static AppointmentService appointments() {
        return APPOINTMENT_SERVICE;
    }
}
