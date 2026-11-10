package autoparts.service;

import autoparts.dao.BrandDAO;
import autoparts.dao.CategoryDAO;
import autoparts.dao.ComponentDAO;
import autoparts.exception.InvalidDataException;
import autoparts.model.Brand;
import autoparts.model.Category;
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
    private final BrandDAO brandDAO;
    private final CategoryDAO categoryDAO;

    /**
     * Crea una nueva instancia de ComponentService con los DAO inyectados.
     * 
     * @param componentDAO el objeto de acceso a datos para componentes
     * @param brandDAO el objeto de acceso a datos para marcas
     * @param categoryDAO el objeto de acceso a datos para categorías
     */
    public ComponentService(ComponentDAO componentDAO, BrandDAO brandDAO, CategoryDAO categoryDAO) {
        this.componentDAO = componentDAO;
        this.brandDAO = brandDAO;
        this.categoryDAO = categoryDAO;
    }

    /**
     * Devuelve todos los componentes, activos e inactivos (para la vista del administrador).
     *
     * @return lista completa de componentes
     */
    public List<Component> findAll() {
        return componentDAO.listAll();
    }

    /**
     * Devuelve las marcas disponibles para llenar el ComboBox del formulario.
     *
     * @return lista de marcas
     */
    public List<Brand> findBrands() {
        return brandDAO.listAll();
    }

    /**
     * Devuelve las categorías disponibles para llenar el ComboBox del formulario.
     *
     * @return lista de categorías
     */
    public List<Category> findCategories() {
        return categoryDAO.listAll();
    }

    /**
     * Registra un componente nuevo como ACTIVO.
     *
     * @param component componente a registrar
     * @throws InvalidDataException si los datos son incompletos o la referencia ya existe
     */
    public void register(Component component) {
        validate(component);
        component.setStatus("ACTIVO");
        componentDAO.save(component);
    }

    /**
     * Actualiza un componente existente.
     *
     * @param component componente con los datos nuevos
     * @throws InvalidDataException si los datos son incompletos o la referencia ya existe
     */
    public void update(Component component) {
        if (componentDAO.findById(component.getId()) == null) {
            throw new InvalidDataException("El componente no existe.");
        }
        validate(component);
        componentDAO.update(component);
    }

    /**
     * Desactiva un componente (no borra la línea del archivo: el estado pasa a INACTIVO).
     *
     * @param id identificador del componente
     * @throws InvalidDataException si el componente no existe
     */
    public void deactivate(int id) {
        if (componentDAO.findById(id) == null) {
            throw new InvalidDataException("El componente no existe.");
        }
        componentDAO.delete(id);
    }

    private void validate(Component component) {
        if (isBlank(component.getName()) || isBlank(component.getReference())) {
            throw new InvalidDataException("El nombre y la referencia son obligatorios.");
        }
        if (component.getBrand() == null) {
            throw new InvalidDataException("Debe elegir una marca.");
        }
        if (component.getCategory() == null) {
            throw new InvalidDataException("Debe elegir una categoría.");
        }
        String origin = component.getOrigin();
        if (!"ORIGINAL".equals(origin) && !"GENERICO".equals(origin)) {
            throw new InvalidDataException("El origen debe ser ORIGINAL o GENERICO.");
        }
        String reference = component.getReference().trim();
        for (Component other : componentDAO.listAll()) {
            boolean sameComponent = other.getId() == component.getId();
            if (!sameComponent && reference.equalsIgnoreCase(other.getReference())) {
                throw new InvalidDataException("Ya existe un componente con esa referencia.");
            }
        }
    }

    private boolean isBlank(String text) {
        return text == null || text.isBlank();
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

    /**
     * Igual que {@link #search(String)} pero incluye también los componentes
     * INACTIVOS. Lo usa la pantalla del catálogo para el administrador.
     *
     * @param text texto a buscar en el nombre o la referencia del componente
     * @return lista de componentes (activos e inactivos) filtrados
     */
    public List<Component> searchAll(String text) {
        List<Component> result = new ArrayList<>();
        String query = text == null ? "" : text.trim().toLowerCase();

        for (Component component : componentDAO.listAll()) {
            String name = component.getName() != null ? component.getName().toLowerCase() : "";
            String reference = component.getReference() != null ? component.getReference().toLowerCase() : "";
            if (query.isEmpty() || name.contains(query) || reference.contains(query)) {
                result.add(component);
            }
        }
        return result;
    }
}