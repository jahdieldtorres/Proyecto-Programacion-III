package autoparts.service;

import autoparts.dao.CompatibilityDAO;
import autoparts.dao.ComponentDAO;
import autoparts.dao.VehicleDAO;
import autoparts.exception.InvalidDataException;
import autoparts.model.Component;
import autoparts.model.Vehicle;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Servicio de consulta de vehículos y de los repuestos compatibles con cada
 * uno. Es solo de lectura: no crea, edita ni desactiva vehículos.
 *
 * @author ALVARO
 */
public class VehicleService {

    private final VehicleDAO vehicleDAO;
    private final CompatibilityDAO compatibilityDAO;
    private final ComponentDAO componentDAO;

    /**
     * Crea el servicio con los DAO inyectados.
     *
     * @param vehicleDAO acceso a datos de vehículos
     * @param compatibilityDAO acceso a datos de la relación vehículo - componente
     * @param componentDAO acceso a datos de componentes
     */
    public VehicleService(VehicleDAO vehicleDAO, CompatibilityDAO compatibilityDAO, ComponentDAO componentDAO) {
        this.vehicleDAO = vehicleDAO;
        this.compatibilityDAO = compatibilityDAO;
        this.componentDAO = componentDAO;
    }

    /**
     * Devuelve los vehículos activos cuya marca o modelo contiene el texto.
     *
     * @param text texto a buscar; vacío o null devuelve todos los activos
     * @return lista de vehículos activos filtrados
     */
    public List<Vehicle> search(String text) {
        String query = text == null ? "" : text.trim().toLowerCase();
        List<Vehicle> result = new ArrayList<>();
        for (Vehicle vehicle : vehicleDAO.listAll()) {
            if (!"ACTIVO".equalsIgnoreCase(vehicle.getStatus())) {
                continue;
            }
            String brand = vehicle.getBrand() != null ? vehicle.getBrand().getName().toLowerCase() : "";
            String model = vehicle.getModelName() != null ? vehicle.getModelName().toLowerCase() : "";
            if (query.isEmpty() || brand.contains(query) || model.contains(query)) {
                result.add(vehicle);
            }
        }
        return result;
    }

    /**
     * Devuelve los repuestos activos compatibles con un vehículo.
     *
     * @param vehicleId identificador del vehículo
     * @return lista de componentes compatibles (puede estar vacía)
     * @throws InvalidDataException si el vehículo no existe
     */
    public List<Component> findCompatibleComponents(int vehicleId) {
        if (vehicleDAO.findById(vehicleId) == null) {
            throw new InvalidDataException("El vehículo no existe.");
        }
        Set<Integer> componentIds = new HashSet<>(compatibilityDAO.listComponentIds(vehicleId));
        List<Component> result = new ArrayList<>();
        for (Component component : componentDAO.listAll()) {
            boolean active = "ACTIVO".equalsIgnoreCase(component.getStatus());
            if (active && componentIds.contains(component.getId())) {
                result.add(component);
            }
        }
        return result;
    }
}
