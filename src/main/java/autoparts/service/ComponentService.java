package autoparts.service;

import autoparts.dao.BrandDAO;
import autoparts.dao.CategoryDAO;
import autoparts.dao.ComponentDAO;
import autoparts.exception.InvalidDataException;
import autoparts.model.Brand;
import autoparts.model.Category;
import autoparts.model.Component;
import autoparts.model.Status;
import autoparts.util.TextUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * Servicio encargado de la lógica de negocio y gestión de componentes y autopartes.
 * 
 * @author Alvaro Aramendiz
 */
public class ComponentService {

    /** Longitud máxima de la descripción (igual que COMPONENTS.DESCRIPTION en Oracle). */
    private static final int MAX_DESCRIPTION_LENGTH = 300;

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
        component.setStatus(Status.ACTIVO);
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
        validateDescription(component);
        String reference = component.getReference().trim();
        for (Component other : componentDAO.listAll()) {
            boolean sameComponent = other.getId() == component.getId();
            if (!sameComponent && reference.equalsIgnoreCase(other.getReference())) {
                throw new InvalidDataException("Ya existe un componente con esa referencia.");
            }
        }
    }

    /**
     * Valida la descripción: es opcional, pero no puede pasar de 300 caracteres.
     * Si viene vacía se guarda como null y si trae espacios sobrantes se recortan.
     *
     * @param component componente cuya descripción se revisa
     * @throws InvalidDataException si la descripción supera el máximo permitido
     */
    private void validateDescription(Component component) {
        String description = component.getDescription();
        if (isBlank(description)) {
            component.setDescription(null);
            return;
        }
        description = description.trim();
        if (description.length() > MAX_DESCRIPTION_LENGTH) {
            throw new InvalidDataException("La descripción no puede pasar de "
                    + MAX_DESCRIPTION_LENGTH + " caracteres (tiene " + description.length() + ").");
        }
        component.setDescription(description);
    }

    private boolean isBlank(String text) {
        return text == null || text.isBlank();
    }

    /**
     * Busca los componentes activos que coinciden con el texto, sin importar
     * tildes ni mayúsculas.
     *
     * @param text texto a buscar en el nombre, la referencia, la marca o la categoría; vacío o null no filtra
     * @return lista de componentes activos filtrados
     */
    public List<Component> search(String text) {
        return filter(text, null, true);
    }

    /**
     * Igual que {@link #search(String)} pero además deja solo los componentes de una categoría.
     *
     * @param text texto a buscar en el nombre, la referencia, la marca o la categoría; vacío o null no filtra
     * @param categoryId identificador de la categoría; null para no filtrar por categoría
     * @return lista de componentes activos filtrados
     */
    public List<Component> search(String text, Integer categoryId) {
        return filter(text, categoryId, true);
    }

    /**
     * Igual que {@link #search(String)} pero incluye también los componentes
     * INACTIVOS. Lo usa la pantalla del catálogo para el administrador.
     *
     * @param text texto a buscar en el nombre, la referencia, la marca o la categoría
     * @return lista de componentes (activos e inactivos) filtrados
     */
    public List<Component> searchAll(String text) {
        return filter(text, null, false);
    }

    /**
     * Igual que {@link #search(String, Integer)} pero incluye también los componentes INACTIVOS.
     *
     * @param text texto a buscar en el nombre, la referencia, la marca o la categoría
     * @param categoryId identificador de la categoría; null para no filtrar por categoría
     * @return lista de componentes (activos e inactivos) filtrados
     */
    public List<Component> searchAll(String text, Integer categoryId) {
        return filter(text, categoryId, false);
    }

    /**
     * Búsqueda común: filtra por estado, por categoría y por texto.
     */
    private List<Component> filter(String text, Integer categoryId, boolean onlyActive) {
        String query = TextUtil.normalize(text);
        List<Component> result = new ArrayList<>();

        for (Component component : componentDAO.listAll()) {
            if (onlyActive && component.getStatus() != Status.ACTIVO) {
                continue;
            }
            if (categoryId != null && component.getCategory().getId() != categoryId) {
                continue;
            }
            if (query.isEmpty() || matches(component, query)) {
                result.add(component);
            }
        }
        return result;
    }

    /**
     * Indica si el texto (ya normalizado) aparece en el nombre, la referencia,
     * la marca o la categoría del componente.
     */
    private boolean matches(Component component, String query) {
        String brand = component.getBrand() != null ? component.getBrand().getName() : "";
        String category = component.getCategory() != null ? component.getCategory().getName() : "";
        return TextUtil.normalize(component.getName()).contains(query)
                || TextUtil.normalize(component.getReference()).contains(query)
                || TextUtil.normalize(brand).contains(query)
                || TextUtil.normalize(category).contains(query);
    }
}