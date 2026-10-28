package autoparts.dao;

import autoparts.model.Vehicle;
import java.util.List;

/**
 *
 * @author ALVARO
 */
public interface VehicleDAO {
    
    /**
     * Devuelve una lista con todos los registros de vehículos.
     * @return Lista de objetos Vehicle
     */
    List<Vehicle> listAll();
    
    /**
     * Busca un vehículo por su identificador único.
     * @param id El identificador del vehículo
     * @return El objeto Vehicle encontrado, o null en caso contrario
     */
    Vehicle findById(Long id);
    
    /**
     * Guarda un nuevo registro de vehículo.
     * @param vehicle El objeto Vehicle a guardar
     */
    void save(Vehicle vehicle);
    
    /**
     * Actualiza un registro de vehículo existente.
     * @param vehicle El objeto Vehicle con los valores actualizados
     */
    void update(Vehicle vehicle);
    
    /**
     * Elimina un registro de vehículo por su identificador único.
     * @param id El identificador del vehículo a eliminar
     */
    void delete(Long id);
    
}
