package autoparts.dao;

import autoparts.model.WorkshopService;

import java.util.List;
import java.util.Optional;

/**
 * Operaciones para consultar los servicios que ofrecen los talleres.
 *
 * Los servicios de negocio dependen de esta interfaz y no de cómo se guardan
 * los datos. Hoy solo la implementa WorkshopServiceOracleDAO.
 *
 * @author Héctor Guevara
 */
public interface WorkshopServiceDAO {

    /**
     * Busca un servicio por su identificador.
     *
     * @param id identificador del servicio
     * @return el servicio, o vacío si no existe
     */
    Optional<WorkshopService> findById(int id);

    /**
     * Devuelve los servicios de un taller, activos o no.
     *
     * @param workshopId identificador del taller
     * @return lista de servicios del taller
     */
    List<WorkshopService> findByWorkshop(int workshopId);

    /**
     * Devuelve todos los servicios.
     *
     * @return lista de servicios
     */
    List<WorkshopService> findAll();
}
