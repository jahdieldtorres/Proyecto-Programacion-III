package autoparts.service;

import autoparts.dao.WorkshopServiceDAO;
import autoparts.model.Status;
import autoparts.model.WorkshopService;

import java.util.List;
import java.util.Optional;

/**
 * Consulta los servicios que ofrecen los talleres. Para mostrar un servicio
 * con su precio se combina con {@link FeeService#findCurrentByService(int)}.
 *
 * @author Héctor Guevara
 */
public class ServiceCatalogService {

    private final WorkshopServiceDAO serviceDAO;

    /**
     * Crea el servicio con su DAO.
     *
     * @param serviceDAO acceso a datos de los servicios de talleres
     */
    public ServiceCatalogService(WorkshopServiceDAO serviceDAO) {
        this.serviceDAO = serviceDAO;
    }

    /**
     * Busca un servicio por su identificador.
     *
     * @param id identificador del servicio
     * @return el servicio, o vacío si no existe
     */
    public Optional<WorkshopService> findById(int id) {
        return serviceDAO.findById(id);
    }

    /**
     * Devuelve los servicios activos de un taller.
     *
     * @param workshopId identificador del taller
     * @return servicios con estado {@code ACTIVO}, en el orden en que se crearon
     */
    public List<WorkshopService> findActiveByWorkshop(int workshopId) {
        return serviceDAO.findByWorkshop(workshopId).stream()
                .filter(service -> service.getStatus() == Status.ACTIVO)
                .toList();
    }
}
