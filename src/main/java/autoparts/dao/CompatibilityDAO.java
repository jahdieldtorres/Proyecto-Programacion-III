package autoparts.dao;

import java.util.List;

/**
 *
 * @author ALVARO
 */
public interface CompatibilityDAO {
    
    /**
     * Devuelve una lista con los identificadores de vehículos compatibles con un componente.
     * @param componentId El identificador del componente
     * @return Lista de identificadores de vehículos
     */
    List<Long> listVehicleIds(Long componentId);
    
    /**
     * Devuelve una lista con los identificadores de componentes compatibles con un vehículo.
     * @param vehicleId El identificador del vehículo
     * @return Lista de identificadores de componentes
     */
    List<Long> listComponentIds(Long vehicleId);
    
    /**
     * Enlaza un vehículo con un componente.
     * @param vehicleId El identificador del vehículo
     * @param componentId El identificador del componente
     */
    void link(Long vehicleId, Long componentId);
    
    /**
     * Desenlaza un vehículo de un componente.
     * @param vehicleId El identificador del vehículo
     * @param componentId El identificador del componente
     */
    void unlink(Long vehicleId, Long componentId);
    
}
