package autoparts.model;

import autoparts.exception.PersistenceException;
import autoparts.util.FileUtil;
import java.util.Map;

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
    private String quality;
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
     * @param quality calidad del componente
     * @param status estado cualitativo del componente
     */
    public Component(ComponentType type, int id, String name, String reference, Brand brand, Category category, String quality, String status) {
        this.type = type;
        this.id = id;
        this.name = name;
        this.reference = reference;
        this.brand = brand;
        this.category = category;
        this.quality = quality;
        this.status = status;
    }
    
    /**
     * Crea el componente a partir de una línea de components.csv. Los campos
     * comunes son:
     * tipo|id|nombre|referencia|id_marca|id_categoria|calidad|estado.
     *
     * @param line línea del archivo
     * @param brands marcas disponibles, por identificador
     * @param categories categorías disponibles, por identificador
     */
    public Component(String line, Map<Integer, Brand> brands, Map<Integer, Category> categories) {
        String[] f = FileUtil.split(line);
        this.type = ComponentType.fromFileValue(f[0]);
        this.id = Integer.parseInt(f[1]);
        this.name = f[2];
        this.reference = f[3];
        this.brand = brands.get(Integer.parseInt(f[4]));
        this.category = categories.get(Integer.parseInt(f[5]));
        this.quality = f[6];
        this.status = f[7];
        if (brand == null || category == null) {
            throw new PersistenceException("El componente " + id
                    + " apunta a una marca o categoría que no existe.");
        }
    }

    /**
     * Convierte el objeto en una línea de registro.
     * @return Devuelve la línea para guardar en components.csv.
     */
    public String toLine() {
        return FileUtil.join(type.getFileValue(), id, name, reference, brand.getId(),
                category.getId(), quality, status);
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

    public String getQuality() {
        return quality;
    }

    public void setQuality(String quality) {
        this.quality = quality;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
