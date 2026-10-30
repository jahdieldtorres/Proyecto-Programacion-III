package autoparts.service;

import autoparts.dao.ComponentDAO;
import autoparts.model.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Servicio encargado de la lógica de negocio y gestión de componentes y autopartes.
 * 
 * @author ALVARO
 */
public class ComponentService {

    private final ComponentDAO componentDAO;

    /**
     * Crea una nueva instancia de ComponentService con el DAO inyectado.
     * 
     * @param componentDAO el objeto de acceso a datos para componentes
     */
    public ComponentService(ComponentDAO componentDAO) {
        this.componentDAO = componentDAO;
    }

    /**
     * Busca componentes que se encuentren activos y coincidan con el texto de búsqueda.
     * 
     * @param text texto a buscar en el nombre o la referencia del componente
     * @return lista de componentes activos filtrados
     */
    public List<Component> search(String text) {
        List<Component> allComponents = componentDAO.listAll();
        List<Component> filteredComponents = new ArrayList<>();
        
        String query = text == null ? "" : text.trim().toLowerCase();

        for (Component component : allComponents) {
            boolean isActive = "ACTIVO".equalsIgnoreCase(component.getStatus());
            
            if (isActive) {
                if (query.isEmpty()) {
                    filteredComponents.add(component);
                } else {
                    String name = component.getName() != null ? component.getName().toLowerCase() : "";
                    String reference = component.getReference() != null ? component.getReference().toLowerCase() : "";
                    
                    if (name.contains(query) || reference.contains(query)) {
                        filteredComponents.add(component);
                    }
                }
            }
        }
        
        return filteredComponents;
    }
}
