package autoparts.model;

import autoparts.util.FileUtil;

/**
 * Clase que contiene los atributos de componentes y autopartes para vehículos automotrices.
 * 
 * @author ALVARO
 */
public class Component {

    private ComponentType type;
    private int id;
    private String name;
    private String reference;
    private Brand brand;
    private Category category;
    private String origin;
    private String status;

    
    /**
     * Crea la clase de componentes como un componente genérico.
     */
    
    public Component() {
        this.type = ComponentType.GENERAL;
    }

    /**
     * Crea la clase de componentes con un nombre descriptivo e identificador único de postulamiento.
     *
     * @param type tipo del componente
     * @param id identificador de producto componente
     * @param name nombre descriptivo del componente
     * @param reference referencia de fabricante
     * @param brand marca del componente
     * @param category categoría del componente
     * @param origin origen del componente
     * @param status estado cualitativo del componente
     */
    public Component(ComponentType type, int id, String name, String reference, Brand brand, Category category, String origin, String status) {
        this.type = type;
        this.id = id;
        this.name = name;
        this.reference = reference;
        this.brand = brand;
        this.category = category;
        this.origin = origin;
        this.status = status;
    }

    /**
     * Convierte el objeto en una línea de registro.
     * @return Devuelve la línea para guardar en components.csv.
     */
    public String toLine() {
        return FileUtil.join(type.getFileValue(), id, name, reference, brand.getId(),
                category.getId(), origin, status);
    }

    public ComponentType getType() {
        return type;
    }

    public void setType(ComponentType type) {
        this.type = type;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getReference() {
        return reference;
    }

    public void setReference(String reference) {
        this.reference = reference;
    }

    public Brand getBrand() {
        return brand;
    }

    public void setBrand(Brand brand) {
        this.brand = brand;
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    public String getOrigin() {
        return origin;
    }

    public void setOrigin(String origin) {
        this.origin = origin;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
