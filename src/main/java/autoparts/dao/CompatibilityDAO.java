package autoparts.dao;

import java.util.List;

/**
 * Interfaz que define las operaciones de acceso a datos para la compatibilidad entre vehículos y componentes.
 * 
 * @author ALVARO
 */
public interface CompatibilityDAO {
    
    /**
     * Devuelve una lista con los identificadores de vehículos compatibles con un componente.
     * @param componentId El identificador del componente
     * @return Lista de identificadores de vehículos
     */
    List<Integer> listVehicleIds(int componentId);
    
    /**
     * Devuelve una lista con los identificadores de componentes compatibles con un vehículo.
     * @param vehicleId El identificador del vehículo
     * @return Lista de identificadores de componentes
     */
    List<Integer> listComponentIds(int vehicleId);
    
    /**
     * Enlaza un vehículo con un componente.
     * @param vehicleId El identificador del vehículo
     * @param componentId El identificador del componente
     */
    void link(int vehicleId, int componentId);
    
    /**
     * Desenlaza un vehículo de un componente.
     * @param vehicleId El identificador del vehículo
     * @param componentId El identificador del componente
     */
    void unlink(int vehicleId, int componentId);
    
}
